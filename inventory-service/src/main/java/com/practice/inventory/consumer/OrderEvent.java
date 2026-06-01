package com.practice.inventory.consumer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** * 订单事件 DTO * @author ymx * @since 2026-02-11 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {
    private Long orderId;      // 订单ID，唯一标识一笔订单
    private Long productId;    // 商品ID，标识哪个商品需要扣减库存
    private Integer quantity;  // 扣减数量，下单购买的商品件数
    private String channel;    // 渠道来源，如 "app" / "web" / "miniProgram"
}
