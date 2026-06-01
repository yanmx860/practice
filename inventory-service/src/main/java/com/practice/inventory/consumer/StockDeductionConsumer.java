package com.practice.inventory.consumer;

import com.practice.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/** * 库存扣减消息消费者 * @author ymx * @since 2026-02-13 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StockDeductionConsumer {

    private final InventoryService inventoryService;

    // @RabbitListener：监听 stock.deduct.queue 队列，当订单创建事件到达时自动消费
    // @RabbitHandler（用于区分同一队列中不同的消息类型，此处只有一个处理方法所以直接用 @RabbitListener）
    // 收到 OrderEvent 后调用 InventoryService.deductStock 执行库存扣减
    @RabbitListener(queues = "stock.deduct.queue")
    public void handleStockDeduction(OrderEvent event) {
        log.info("received stock deduction event: {}", event);
        inventoryService.deductStock(event.getOrderId(), event.getProductId(), event.getQuantity());
    }
}
