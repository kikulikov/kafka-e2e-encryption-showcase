FROM confluentinc/cp-kafka-connect:7.3.1

RUN confluent-hub install --no-prompt mongodb/kafka-connect-mongodb:1.9.1