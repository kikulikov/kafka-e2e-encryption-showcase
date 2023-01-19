package io.confluent.streams;

import io.confluent.encryption.kstreams.api.SecuredStores;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import io.confluent.model.avro.Book;
import io.confluent.model.avro.EnrichedOrder;
import io.confluent.model.avro.OnlineOrder;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;

@Configuration
@EnableKafkaStreams
public class EnrichTopology {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnrichTopology.class);

    @Value("${application.topic.books}")
    private String topicBooks;

    @Value("${application.topic.online-orders}")
    private String topicOnlineOrders;

    @Value("${application.topic.enriched-orders}")
    private String topicEnrichedOrders;

    private static final Serde<String> STRING_SERDE = Serdes.String();
    private static final Serde<Book> bookSerde = new SpecificAvroSerde<>();
    private static final Serde<OnlineOrder> onlineOrderSerde = new SpecificAvroSerde<>();
    private static final Serde<EnrichedOrder> enrichedOrderSerde = new SpecificAvroSerde<>();

    // public EnrichTopology(@NonNull SchemaRegistryConfig registryConfig, @NonNull BasicAuthConfig authConfig) {
    //     final var configMap = new HashMap<String, String>();
    //     configMap.putAll(registryConfig.asMap());
    //     configMap.putAll(authConfig.asMap());
    //
    //     bookSerde.configure(configMap, false);
    //     onlineOrderSerde.configure(configMap, false);
    //     enrichedOrderSerde.configure(configMap, false);
    // }

    @Bean
    public KStream<String, EnrichedOrder> enrich(StreamsBuilder streamsBuilder) {

        final Materialized<String, Book, KeyValueStore<Bytes, byte[]>> bookstore =
                Materialized.<String, Book>as(SecuredStores.persistentKeyValueStore("bookstore"))
                        .withKeySerde(STRING_SERDE).withValueSerde(bookSerde).withCachingEnabled();

        final GlobalKTable<String, Book> bookGlobalTable = streamsBuilder
                .globalTable(topicBooks, Consumed.with(STRING_SERDE, bookSerde), bookstore);

        final KStream<String, OnlineOrder> ordersStream = streamsBuilder
                .stream(topicOnlineOrders, Consumed.with(STRING_SERDE, onlineOrderSerde));

        final var ordersWithBooks = ordersStream
                .join(bookGlobalTable, (key, order) -> order.getBookId(), (order, book) -> {
                    final var builder = EnrichedOrder.newBuilder();
                    builder.setOrderId(order.getOrderId());
                    builder.setBookTitle(book.getBookTitle());
                    builder.setQuantity(order.getQuantity());
                    return builder.build();
                });

        ordersWithBooks.peek((m, n) -> LOGGER.info("Enriched order [" + m + ":" + n + "]"));
        ordersWithBooks.to(topicEnrichedOrders, Produced.with(STRING_SERDE, enrichedOrderSerde));

        return ordersWithBooks;
    }
}