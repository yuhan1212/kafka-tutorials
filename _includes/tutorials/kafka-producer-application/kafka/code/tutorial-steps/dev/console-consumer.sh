kafka-console-consumer --topic output-topic \
 --bootstrap-server pkc-4kgmg.us-west-2.aws.confluent.cloud:9092 \
 --from-beginning \
 --consumer.config stack-configs/java-service-account-*.config \
 --property print.key=true \
 --property key.separator=" : "