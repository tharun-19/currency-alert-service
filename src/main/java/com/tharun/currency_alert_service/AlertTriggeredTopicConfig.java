package com.tharun.currency_alert_service;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class AlertTriggeredTopicConfig {

    @Bean
    public NewTopic alertTriggeredTopic() {
        return TopicBuilder.name("alert-triggered")
                .partitions(1)
                .replicas(1)
                .build();
    }
}
