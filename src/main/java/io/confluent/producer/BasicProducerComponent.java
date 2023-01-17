package io.confluent.producer;

import io.confluent.generator.DataSource;
import io.confluent.model.avro.Book;
import io.confluent.model.avro.Customer;
import io.confluent.model.avro.Order;
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

    @Value("${application.topic.orders}")
    private String ordersTopic;

    @Autowired
    @SuppressWarnings("unused")
    private KafkaTemplate<String, Order> ordersTemplate;

    @Scheduled(initialDelay = 500, fixedRate = 3000)
    @SuppressWarnings("unused")
    public void ordersProducer() {
        final Order order = dataSource.retrieveOrder();
        LOGGER.info("Sending='{}'", order);
        ordersTemplate.send(ordersTopic, order);
    }

    /**
     * BOOKS
     */

    @Value("${application.topic.books}")
    private String booksTopic;

    @Autowired
    @SuppressWarnings("unused")
    private KafkaTemplate<String, Book> booksTemplate;

    @Scheduled(initialDelay = 500, fixedRate = 2500)
    @SuppressWarnings("unused")
    public void produceBooks() {
        final Book book = dataSource.retrieveBook();

        LOGGER.info("Sending='{}'", book);
        booksTemplate.send(booksTopic, book);
    }

    /**
     * CUSTOMERS
     */

    @Value("${application.topic.customers}")
    private String customersTopic;

    @Autowired
    @SuppressWarnings("unused")
    private KafkaTemplate<String, Customer> customersTemplate;

    @Scheduled(initialDelay = 500, fixedRate = 2000)
    @SuppressWarnings("unused")
    public void customersProducer() {
        final Customer customer = dataSource.retrieveCustomer();

        LOGGER.info("Sending='{}'", customer);
        customersTemplate.send(customersTopic, customer);
    }
}
