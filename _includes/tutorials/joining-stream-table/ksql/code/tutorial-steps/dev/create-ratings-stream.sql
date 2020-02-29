CREATE STREAM ratings (ROWKEY INT KEY, id INT, rating DOUBLE)
    WITH (kafka_topic='ratings', key='id', partitions=1, value_format='avro');
