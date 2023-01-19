package io.confluent.producer;

import io.confluent.generator.DataSource;
import io.confluent.model.avro.Book;
import io.confluent.model.avro.OnlineOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BasicProducerComponent {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicProducerComponent.class);

    @Autowired
    @SuppressWarnings("unused")
    private DataSource dataSource;

    /**
     * ORDERS
     */

    @Value("${application.topic.online-orders}")
    private String topicOnlineOrders;

    @Autowired
    @SuppressWarnings("unused")
    private KafkaTemplate<String, OnlineOrder> onlineOrderTemplate;

    @Scheduled(initialDelay = 500, fixedRate = 4000)
    @SuppressWarnings("unused")
    public void produceOnlineOrders() {
        final OnlineOrder order = dataSource.retrieveOnlineOrder();
        LOGGER.info("Sending='{}'", order);
        onlineOrderTemplate.send(topicOnlineOrders, order.getOrderId(), order);
    }

    /**
     * BOOKS
     */

    @Value("${application.topic.books}")
    private String topicBooks;

    @Autowired
    @SuppressWarnings("unused")
    private KafkaTemplate<String, Book> booksTemplate;

    @Scheduled(initialDelay = 500, fixedRate = 7000)
    @SuppressWarnings("unused")
    public void produceBooks() {
        final Book book = dataSource.retrieveBook();

        LOGGER.info("Sending='{}'", book);
        booksTemplate.send(topicBooks, book.getBookId(), book);
    }
}
