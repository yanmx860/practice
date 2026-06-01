package com.practice.common.pattern.template;

import com.practice.common.thread.UserContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** * 订单处理模版方法抽象类 * @author ymx * @since 2026-01-10 */
public abstract class AbstractOrderProcessor {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    /*
     * 模版方法：定义订单处理的标准流程骨架，子类不能重写此方法。
     * 步骤顺序：参数校验 → 预处理 → 扣库存 → 支付 → 后处理。
     * 其中 validate() 提供默认实现（通用参数检查），其余步骤由子类按业务定制。
     */
    public final void processOrder(Long orderId, Integer userId, String channel) {
        validate(orderId, userId);
        preProcess(orderId);
        Long result = deductStock(orderId);
        // 库存不足时执行失败回调并提前返回，不再继续支付流程
        if (result == null || result <= 0) {
            log.warn("库存不足, orderId={}", orderId);
            onStockFail(orderId);
            return;
        }
        Integer paymentId = processPayment(orderId, channel);
        // 支付失败时需回滚库存，保证数据一致性
        if (paymentId == null) {
            rollbackStock(orderId);
            onPaymentFail(orderId);
            return;
        }
        afterProcess(orderId, paymentId);
        log.info("订单处理完成, orderId={}, userId={}, channel={}", orderId, userId, channel);
    }

    // 通用参数校验，所有子类共享此默认逻辑
    protected void validate(Long orderId, Integer userId) {
        if (orderId == null || userId == null) {
            throw new IllegalArgumentException("参数异常");
        }
    }

    // 子类必须实现：订单预处理，如校验商品状态、冻结金额等
    protected abstract void preProcess(Long orderId);
    // 子类必须实现：扣减库存，返回剩余库存，用于判断是否充足
    protected abstract Long deductStock(Long orderId);
    // 子类必须实现：发起支付并返回支付流水号，返回 null 表示支付失败
    protected abstract Integer processPayment(Long orderId, String channel);
    // 子类必须实现：支付失败时回滚已扣减的库存
    protected abstract void rollbackStock(Long orderId);
    // 子类必须实现：库存不足时的后续处理（如通知用户、记录告警）
    protected abstract void onStockFail(Long orderId);
    // 子类必须实现：支付失败后的处理（如记录失败原因、触发重试）
    protected abstract void onPaymentFail(Long orderId);
    // 子类必须实现：订单处理成功后的收尾工作（如更新订单状态、发送通知）
    protected abstract void afterProcess(Long orderId, Integer paymentId);
}
