package com.practice.common.pattern.strategy;

import java.math.BigDecimal;

/** * 支付策略接口 * @author ymx * @since 2026-01-10 */
public interface PaymentStrategy {
    String getChannel();
    Integer pay(Long orderId, Long userId, BigDecimal amount);
    Boolean refund(Integer paymentId, BigDecimal amount);
}
