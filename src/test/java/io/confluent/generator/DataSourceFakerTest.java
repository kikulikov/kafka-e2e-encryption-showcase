package io.confluent.generator;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class DataSourceFakerTest {

    public static final Logger LOGGER = LoggerFactory.getLogger(DataSourceFakerTest.class);
    public static final DataSource eventSource = new DataSourceFaker();

    @Test
    void generateBook() {
        final var book = eventSource.retrieveBook();
        LOGGER.info(book.toString());
        assertNotNull(book.getBookId());
        assertNotNull(book.getBookTitle());
    }

    @Test
    void generateCustomer() {
        final var customer = eventSource.retrieveCustomer();
        LOGGER.info(customer.toString());
        assertNotNull(customer.getCustomerId());
        assertNotNull(customer.getCustomerName());
    }

    @Test
    void generateOrder() {
        final var order = eventSource.retrieveOrder();
        LOGGER.info(order.toString());
        assertNotNull(order.getOrderId());
        assertNotNull(order.getBookId());
        assertNotNull(order.getCustomerId());
        assertNotNull(order.getQuantity());
    }


    @Test
    void retrieveOrder() {
    }

    @Test
    void retrieveBook() {
    }

    @Test
    void retrieveCustomer() {
    }
}