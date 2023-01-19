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

    @Value("${application.topic.books}")
    private String topicBooks;

    @Value("${application.topic.online-orders}")
    private String topicOnlineOrders;

    @Value("${application.topic.enriched-orders}")
    private String topicEnrichedOrders;

    @Bean
    NewTopic topicBooks() {
        // Makes sure that the topic exists using the Kafka Admin API
        return new NewTopic(topicBooks, Optional.of(defaultPartitions), Optional.of(defaultReplicas));
    }

    @Bean
    NewTopic topicOnlineOrders() {
        // Makes sure that the topic exists using the Kafka Admin API
        return new NewTopic(topicOnlineOrders, Optional.of(defaultPartitions), Optional.of(defaultReplicas));
    }


    @Bean
    NewTopic topicEnrichedOrders() {
        // Makes sure that the topic exists using the Kafka Admin API
        return new NewTopic(topicEnrichedOrders, Optional.of(defaultPartitions), Optional.of(defaultReplicas));
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

