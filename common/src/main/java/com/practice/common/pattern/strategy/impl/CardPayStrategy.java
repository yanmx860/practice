package com.practice.common.pattern.strategy.impl;

import com.practice.common.pattern.strategy.PaymentStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** * 银行卡支付策略 * @author ymx * @since 2026-01-12 */
@Component
public class CardPayStrategy implements PaymentStrategy {

    private static final Logger log = LoggerFactory.getLogger(CardPayStrategy.class);

    @Override
    public String getChannel() { return "card"; }

    @Override
    public Integer pay(Long orderId, Long userId, BigDecimal amount) {
        log.info("银行卡支付: orderId={}, userId={}, amount={}", orderId, userId, amount);
        return (int)(System.currentTimeMillis() % 1000000);
    }

    @Override
    public Boolean refund(Integer paymentId, BigDecimal amount) {
        log.info("银行卡退款: paymentId={}, amount={}", paymentId, amount);
        return true;
    }
}
