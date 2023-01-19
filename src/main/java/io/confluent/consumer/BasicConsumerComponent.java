package io.confluent.consumer;

import io.confluent.model.avro.Book;
import io.confluent.model.avro.EnrichedOrder;
import io.confluent.model.avro.OnlineOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class BasicConsumerComponent {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicConsumerComponent.class);

    @KafkaListener(topics = "${application.topic.books}")
    @SuppressWarnings("unused")
    public void receive(@Payload Book entry) {
        // process the received record accordingly
        LOGGER.info("Received the Book '{}'", entry.toString());
    }

    @KafkaListener(topics = "${application.topic.online-orders}")
    @SuppressWarnings("unused")
    public void receive(@Payload OnlineOrder entry) {
        // process the received record accordingly
        LOGGER.info("Received the Online Order '{}'", entry.toString());
    }

    @KafkaListener(topics = "${application.topic.enriched-orders}")
    @SuppressWarnings("unused")
    public void receive(@Payload EnrichedOrder entry) {
        // process the received record accordingly
        LOGGER.info("Received the Enriched Order '{}'", entry.toString());
    }
}

