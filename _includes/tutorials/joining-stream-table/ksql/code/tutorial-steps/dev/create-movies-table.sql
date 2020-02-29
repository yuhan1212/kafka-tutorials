CREATE TABLE movies (ROWKEY INT KEY, id INT, title VARCHAR, release_year INT)
    WITH (kafka_topic='movies', key='id', partitions=1, value_format='avro');
