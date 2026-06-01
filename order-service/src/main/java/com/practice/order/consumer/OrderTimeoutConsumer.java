package com.practice.order.consumer;

import com.practice.order.model.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 订单超时消费者
 * <p>
 * 监听 RabbitMQ 延迟队列（通过死信交换机 + TTL 实现），当订单在指定时间内未被处理时，
 * 消息从原始队列（order.delay.queue）被转发到死信交换机（order.dlx），
 * 最终路由到 order.event.queue 由本消费者消费，执行超时处理逻辑（如取消订单、释放库存）。
 * </p>
 * 延迟机制链路:
 * <pre>
 *   producer → order.exchange → order.delay.queue (TTL=30s)
 *       ↓ 超时后
 *   order.dlx (死信交换机) → order.event.queue → handleOrderEvent()
 * </pre>
 * @author ymx
 * @since 2026-02-14
 */
@Slf4j
@Component
public class OrderTimeoutConsumer {

    // 监听 order.event.queue 队列，该队列的消息来源于死信转发，即已超时的订单
    @RabbitListener(queues = "order.event.queue")
    public void handleOrderEvent(Order order) {
        // 收到超时订单事件，执行超时处理逻辑：如将订单状态置为"已取消"、释放预占库存等
        log.info("收到订单事件, orderId={}, orderNo={}, status={}",
                order.getId(), order.getOrderNo(), order.getStatus());
    }
}
