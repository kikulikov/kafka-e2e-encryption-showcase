package io.confluent.consumer;

import io.confluent.model.avro.Customer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class BasicConsumerComponent {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicConsumerComponent.class);

    @KafkaListener(topics = "${application.topic.customers}")
    @SuppressWarnings("unused")
    public void receive(@Payload Customer customer) {
        // process the received record accordingly
        LOGGER.info("Received='{}'", customer.toString());
    }
}

