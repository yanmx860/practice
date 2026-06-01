package com.practice.search.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;

/** * Kafka 配置 * @author ymx * @since 2026-02-25 */
@Configuration
public class KafkaConfig {

    // 定义 Kafka Topic：商品同步主题，与消费者 @KafkaListener(topics = "product-sync-topic") 对应
    // consumer-group = "search-group" 在消费者端通过 @KafkaListener 的 groupId 指定
    @Bean
    public NewTopic productSyncTopic() {
        return new NewTopic("product-sync-topic", 1, (short) 1); // 1个分区，1副本
    }

    @Bean
    public StringJsonMessageConverter jsonMessageConverter() {
        return new StringJsonMessageConverter();
    }
}
