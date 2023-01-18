package io.confluent.streams;

import com.google.common.base.Strings;
import io.confluent.model.avro.Book;
import io.confluent.model.avro.Customer;
import io.confluent.model.avro.EnrichedOrder;
import io.confluent.model.avro.Order;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.JoinWindows;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;

import java.time.Duration;

@Configuration
@EnableKafkaStreams
public class EnrichTopology {

    public static final String EMPTY = "";

    private static final Logger LOGGER = LoggerFactory.getLogger(EnrichTopology.class);

    @Value("${application.topic.orders}")
    private String ordersTopic;

    @Value("${application.topic.books}")
    private String booksTopic;

    @Value("${application.topic.customers}")
    private String customersTopic;

    @Bean
    public KStream<String, EnrichedOrder> enrich(StreamsBuilder streamsBuilder) {

        final KStream<String, Order> ordersStream = streamsBuilder.stream(ordersTopic);

        final GlobalKTable<String, Book> booksTable = streamsBuilder
                .globalTable(booksTopic, Materialized.as("books-table"));

        final GlobalKTable<String, Customer> customersTable = streamsBuilder
                .globalTable(customersTopic, Materialized.as("customers-table"));

        final var ordersWithBooks = ordersStream
                .join(booksTable, (key, order) -> order.getBookId(), (order, book) -> {
                    final var builder = EnrichedOrder.newBuilder();
                    builder.setOrderId(order.getOrderId());
                    builder.setQuantity(order.getQuantity());
                    builder.setBookTitle(book.getBookTitle());
                    builder.setCustomerName(EMPTY);
                    return builder.build();
                });

        final var ordersWithCustomers = ordersStream
                .join(customersTable, (key, order) -> order.getCustomerId(), (order, customer) -> {
                    final var builder = EnrichedOrder.newBuilder();
                    builder.setOrderId(order.getOrderId());
                    builder.setQuantity(order.getQuantity());
                    builder.setBookTitle(EMPTY);
                    builder.setCustomerName(customer.getCustomerName());
                    return builder.build();
                });

        final var enrichedOrders = ordersWithBooks.join(ordersWithCustomers, (withBooks, withCustomers) -> {
            final var builder = EnrichedOrder.newBuilder();
            builder.setOrderId(withBooks.getOrderId());
            builder.setQuantity(withBooks.getQuantity());
            builder.setBookTitle(withBooks.getBookTitle());
            builder.setCustomerName(withCustomers.getCustomerName());
            return builder.build();
        }, JoinWindows.ofTimeDifferenceWithNoGrace(Duration.ofMinutes(1)));

        enrichedOrders.peek((m, n) -> LOGGER.info("Enriched order [" + m + ":" + n + "]"));

        return enrichedOrders;
    }
}