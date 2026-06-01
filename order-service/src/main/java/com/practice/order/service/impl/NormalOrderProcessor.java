package com.practice.order.service.impl;

import com.practice.common.pattern.template.AbstractOrderProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 普通订单处理器（模板方法实现）
 * <p>
 * 继承 {@link AbstractOrderProcessor}，实现其定义的 7 个抽象方法，
 * 构成完整的订单处理流程：preProcess → deductStock → processPayment
 * → rollbackStock(失败时) / onStockFail / onPaymentFail / afterProcess。
 * </p>
 * @author ymx
 * @since 2026-01-28
 */
@Slf4j
@Service
public class NormalOrderProcessor extends AbstractOrderProcessor {

    // 预处理：订单进入正式流程前的准备工作，如校验商品状态、冻结金额等
    @Override
    protected void preProcess(Long orderId) {
        log.info("预处理订单, orderId={}", orderId);
    }

    // 扣减库存：远程调用库存服务扣减实际库存，返回剩余库存量；<=0 表示库存不足
    @Override
    protected Long deductStock(Long orderId) {
        log.info("调用库存服务扣减库存, orderId={}", orderId);
        return 1L;
    }

    // 处理支付：根据渠道（alipay/wechat/card）调用第三方支付，返回支付流水号
    @Override
    protected Integer processPayment(Long orderId, String channel) {
        log.info("调用支付渠道 [{}] 处理支付, orderId={}", channel, orderId);
        return 1001;
    }

    // 回滚库存：支付失败时补偿操作，将 deductStock 扣减的库存归还
    @Override
    protected void rollbackStock(Long orderId) {
        log.warn("回滚库存, orderId={}", orderId);
    }

    // 库存不足回调：记录失败原因、通知用户或触发补货告警
    @Override
    protected void onStockFail(Long orderId) {
        log.error("库存扣减失败, orderId={}", orderId);
    }

    // 支付失败回调：记录支付失败原因，可在此触发重试机制或人工介入
    @Override
    protected void onPaymentFail(Long orderId) {
        log.error("支付处理失败, orderId={}", orderId);
    }

    // 后处理：订单处理成功后的收尾工作，如更新订单状态、发送通知等
    @Override
    protected void afterProcess(Long orderId, Integer paymentId) {
        log.info("订单处理完成, orderId={}, paymentId={}", orderId, paymentId);
    }
}
