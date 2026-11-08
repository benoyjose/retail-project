package com.retail.customer.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class KafkaConfig {

    @Value("${app.kafka.topics.customer-registered:customer-registered-topic}")
    private String customerRegisteredTopic;

    @Bean
    public NewTopic customerRegisteredTopic() {
        return TopicBuilder.name(customerRegisteredTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
}

