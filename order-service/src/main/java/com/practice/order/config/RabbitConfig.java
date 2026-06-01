package com.practice.order.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置
 * <p>
 * 定义消息交换器（TopicExchange）、队列（Queue）及其绑定关系。
 * 死信队列（DLQ）机制：为订单延迟队列设置 x-dead-letter-exchange 和 x-dead-letter-routing-key，
 * 当消息在队列中超过 TTL 未被消费时，自动转发到死信交换机，再由死信交换机路由到事件队列，
 * 由 {@link com.practice.order.consumer.OrderTimeoutConsumer} 消费处理超时逻辑。
 * </p>
 * @author ymx
 * @since 2026-02-12
 */
@Configuration
public class RabbitConfig {

    // 业务事件队列：接收正常的订单事件消息，由消费者监听处理（durable=true 保证持久化）
    @Bean
    public Queue orderEventQueue() {
        return new Queue("order.event.queue", true);
    }

    // 订单主题交换机：支持 routing key 模式匹配，用于将订单事件路由到不同队列
    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange("order.exchange");
    }

    // 绑定关系：将 order.event.queue 绑定到 order.exchange，routing key 为 "order.created"
    @Bean
    public Binding orderBinding(Queue orderEventQueue, TopicExchange orderExchange) {
        return BindingBuilder.bind(orderEventQueue)
                .to(orderExchange)
                .with("order.created");
    }

    // 消息转换器：使用 Jackson 将 Java 对象序列化为 JSON，保证生产者和消费者消息体格式一致
    @Bean
    public MessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
