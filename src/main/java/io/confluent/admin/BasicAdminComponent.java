package io.confluent.admin;

import org.apache.kafka.clients.admin.NewTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@EnableAutoConfiguration
public class BasicAdminComponent {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicAdminComponent.class);

    @Value("${application.topic.default.replicas}")
    private Short defaultReplicas;

    @Value("${application.topic.default.partitions}")
    private Integer defaultPartitions;

    @Value("${application.topic.orders}")
    private String topicOrders;

    @Value("${application.topic.books}")
    private String topicBooks;

    @Value("${application.topic.customers}")
    private String topicCustomers;

    @Value("${application.topic.enriched}")
    private String topicEnriched;

    @Bean
    NewTopic topicOrders() {
        // Makes sure that the topic exists using the Kafka Admin API
        return new NewTopic(topicOrders, Optional.of(defaultPartitions), Optional.of(defaultReplicas));
    }

    @Bean
    NewTopic topicBooks() {
        // Makes sure that the topic exists using the Kafka Admin API
        return new NewTopic(topicBooks, Optional.of(defaultPartitions), Optional.of(defaultReplicas));
    }

    @Bean
    NewTopic topicCustomers() {
        // Makes sure that the topic exists using the Kafka Admin API
        return new NewTopic(topicCustomers, Optional.of(defaultPartitions), Optional.of(defaultReplicas));
    }

    @Bean
    NewTopic topicEnriched() {
        // Makes sure that the topic exists using the Kafka Admin API
        return new NewTopic(topicEnriched, Optional.of(defaultPartitions), Optional.of(defaultReplicas));
    }

    // @Autowired
    // @SuppressWarnings("unused")
    // private KafkaAdmin kafkaAdmin;
    //
    // @EventListener(ApplicationReadyEvent.class)
    // @SuppressWarnings("unused")
    // private void describeTopics() {
    //     kafkaAdmin.describeTopics(topicOrders, topicBooks, topicCustomers, topicEnriched)
    //             .forEach((m, n) -> LOGGER.info("Topic '{}' metadata {}", n.name(), n));
    // }
}

