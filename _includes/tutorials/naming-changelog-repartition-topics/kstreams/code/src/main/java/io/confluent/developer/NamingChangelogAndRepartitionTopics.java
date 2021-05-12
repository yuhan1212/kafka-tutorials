package io.confluent.developer;


import io.confluent.common.utils.TestUtils;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.JoinWindows;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.StreamJoined;

public class NamingChangelogAndRepartitionTopics {
  
  public Topology buildTopology(Properties allProps) {
    final StreamsBuilder builder = new StreamsBuilder();
    final String inputTopic = allProps.getProperty("input.topic.name");
    final String outputTopic = allProps.getProperty("output.topic.name");
    final String joinTopic = allProps.getProperty("join.topic.name");

    final Serde<Long> longSerde = Serdes.Long();
    final Serde<String> stringSerde = Serdes.String();

    final boolean addFilter = Boolean.parseBoolean(allProps.getProperty("add.filter"));
    final boolean addNames = Boolean.parseBoolean(allProps.getProperty("add.names"));

    KStream<Long, String> inputStream = builder.stream(inputTopic, Consumed.with(longSerde, stringSerde))
                                                  .selectKey((k, v) -> Long.parseLong(v.substring(0, 1)));
    if (addFilter) {
      inputStream = inputStream.filter((k, v) -> k != 100L);
    }

    final KStream<Long, String> joinedStream;
    final KStream<Long, Long> countStream;

    if (!addNames) {
         countStream = inputStream.groupByKey(Grouped.with(longSerde, stringSerde))
                                    .count()
                                    .toStream();

        joinedStream = inputStream.join(countStream, (v1, v2) -> v1 + v2.toString(),
                                                              JoinWindows.of(Duration.ofMillis(100)),
                                                              StreamJoined.with(longSerde, stringSerde, longSerde));
    } else {
        countStream = inputStream.groupByKey(Grouped.with("count", longSerde, stringSerde))
                                   .count(Materialized.as("the-counting-store"))
                                   .toStream();

        joinedStream = inputStream.join(countStream, (v1, v2) -> v1 + v2.toString(),
                                                              JoinWindows.of(Duration.ofMillis(100)),
                                                              StreamJoined.with(longSerde, stringSerde, longSerde)
                                                                          .withName("join").withStoreName("the-join-store"));
    }

    joinedStream.to(joinTopic, Produced.with(longSerde, stringSerde));
    countStream.map((k,v) -> KeyValue.pair(k.toString(), v.toString())).to(outputTopic, Produced.with(stringSerde, stringSerde));

    return builder.build();
  }

  public void createTopics(final Properties allProps) {
    try (final AdminClient client = AdminClient.create(allProps)) {

      final List<NewTopic> topics = new ArrayList<>();

      topics.add(new NewTopic(
          allProps.getProperty("input.topic.name"),
          Integer.parseInt(allProps.getProperty("input.topic.partitions")),
          Short.parseShort(allProps.getProperty("input.topic.replication.factor"))));

      topics.add(new NewTopic(
          allProps.getProperty("output.topic.name"),
          Integer.parseInt(allProps.getProperty("output.topic.partitions")),
          Short.parseShort(allProps.getProperty("output.topic.replication.factor"))));

      topics.add(new NewTopic(
          allProps.getProperty("join.topic.name"),
          Integer.parseInt(allProps.getProperty("join.topic.partitions")),
          Short.parseShort(allProps.getProperty("join.topic.replication.factor"))));

      client.createTopics(topics);
    }
  }

  public Properties loadEnvProperties(String fileName) throws IOException {
    final Properties allProps = new Properties();
    final FileInputStream input = new FileInputStream(fileName);
    allProps.load(input);
    input.close();

    return allProps;
  }

  public static void main(String[] args) throws Exception {

    if (args.length < 1) {
      throw new IllegalArgumentException(
          "This program takes one argument: the path to an environment configuration file.");
    }

    final NamingChangelogAndRepartitionTopics instance = new NamingChangelogAndRepartitionTopics();
    final Properties allProps = instance.loadEnvProperties(args[0]);
    allProps.put(StreamsConfig.STATE_DIR_CONFIG, TestUtils.tempDirectory().getPath());
    allProps.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
    if (args.length > 1 ) {
      final String namesAndFilter = args[1];

      if (namesAndFilter.contains("filter")) {
        allProps.put("add.filter", "true");
      }

      if (namesAndFilter.contains("names")) {
        allProps.put("add.names", "true");
      }
    }

    final CountDownLatch latch = new CountDownLatch(1);
    final Topology topology = instance.buildTopology(allProps);

    instance.createTopics(allProps);

    final KafkaStreams streams = new KafkaStreams(topology, allProps);

    // Attach shutdown handler to catch Control-C.
    Runtime.getRuntime().addShutdownHook(new Thread("streams-shutdown-hook") {
      @Override
      public void run() {
        streams.close(Duration.ofSeconds(5));
        latch.countDown();
      }
    });

    try {
      streams.start();
      latch.await();
    } catch (Throwable e) {
      System.exit(1);
    }
    System.exit(0);
  }

}
