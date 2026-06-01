package com.practice.inventory.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** * RabbitMQ 配置 * @author ymx * @since 2026-02-15 */
@Configuration
public class RabbitConfig {

    // 库存扣减队列名
    public static final String STOCK_DEDUCT_QUEUE = "stock.deduct.queue";
    // 订单 Topic 交换器名
    public static final String ORDER_EXCHANGE = "order.exchange";
    // 路由键：订单创建事件
    public static final String ROUTING_KEY = "order.created";

    // 定义队列： durable=true 表示重启后队列不丢失
    @Bean
    public Queue stockDeductQueue() {
        return new Queue(STOCK_DEDUCT_QUEUE, true);
    }

    // 定义 TopicExchange：支持 routingKey 模式匹配
    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(ORDER_EXCHANGE);
    }

    // 绑定队列到交换器：stock.deduct.queue 通过 order.created 路由键接收消息
    @Bean
    public Binding stockDeductBinding(Queue stockDeductQueue, TopicExchange orderExchange) {
        return BindingBuilder.bind(stockDeductQueue).to(orderExchange).with(ROUTING_KEY);
    }

    @Bean
    public MessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
