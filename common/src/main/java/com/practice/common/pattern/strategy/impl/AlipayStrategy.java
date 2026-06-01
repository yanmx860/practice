package com.practice.common.pattern.strategy.impl;

import com.practice.common.pattern.strategy.PaymentStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** * 支付宝支付策略 * @author ymx * @since 2026-01-11 */
@Component
public class AlipayStrategy implements PaymentStrategy {

    private static final Logger log = LoggerFactory.getLogger(AlipayStrategy.class);

    @Override
    public String getChannel() { return "alipay"; }

    @Override
    public Integer pay(Long orderId, Long userId, BigDecimal amount) {
        log.info("支付宝支付: orderId={}, userId={}, amount={}", orderId, userId, amount);
        return (int)(System.currentTimeMillis() % 1000000);
    }

    @Override
    public Boolean refund(Integer paymentId, BigDecimal amount) {
        log.info("支付宝退款: paymentId={}, amount={}", paymentId, amount);
        return true;
    }
}
