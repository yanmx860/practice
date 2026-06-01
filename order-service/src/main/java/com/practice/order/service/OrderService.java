package com.practice.order.service;

import com.practice.common.pattern.strategy.StrategyRegistry;
import com.practice.common.result.RespBean;
import com.practice.order.feign.InventoryFeignClient;
import com.practice.order.feign.SearchFeignClient;
import com.practice.order.mapper.OrderMapper;
import com.practice.order.model.Order;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 订单服务
 * <p>
 * 核心流程：参数校验 → 扣库存（Feign） → 处理支付（策略模式）→ 发MQ事件 → 异步Feign同步搜索
 * 整体使用了 Seata AT 模式（@GlobalTransactional）保证跨服务分布式事务的最终一致性。
 * </p>
 * @author ymx
 * @since 2026-01-26
 */
@Slf4j
@Service
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;

    // 声明式 Feign 客户端，远程调用库存服务扣减库存，由 Seata 代理拦截实现分支事务
    @Autowired
    private InventoryFeignClient inventoryFeignClient;

    // 声明式 Feign 客户端，远程调用搜索服务同步商品索引（仅通知，不属于核心事务）
    @Autowired
    private SearchFeignClient searchFeignClient;

    // 策略注册器：根据 channel 动态获取对应的 PaymentStrategy 实现，避免 if-else 分支
    @Autowired
    private StrategyRegistry strategyRegistry;

    // 订单超时队列服务：订单创建时加入 Redis ZSet，XXL-Job 定时扫描取消超时订单
    @Autowired
    private OrderTimeoutService orderTimeoutService;

    // RabbitTemplate 用于发送订单创建事件，解耦后续的非核心处理逻辑
    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 处理订单主流程
     * <p>
     * @GlobalTransactional 开启 Seata 全局事务，当前服务作为 TM（事务管理器）。
     * 库存扣减（inventory-service）和订单状态更新（order-service）分别在各自
     * 的本地事务中执行，Seata 通过 UNDO_LOG 表实现 AT 模式的回滚。
     * 支付渠道调用和 MQ 发送不在全局事务范围内（MQ 是最终一致性，支付是第三方）。
     * </p>
     * 流程: ①查订单 → ②更新状态为处理中 → ③扣库存(Feign) → ④支付(策略) → ⑤写DB完成 → ⑥发MQ → ⑦异步同步搜索
     */
    @GlobalTransactional
    public void processOrder(Long orderId, String channel) {
        // ① 参数校验：查询订单是否存在
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            log.error("订单不存在, orderId={}", orderId);
            return;
        }

        // 订单加入 Redis 超时队列，score = 当前时间 + 30 分钟（毫秒）
        // XXL-Job 定时扫描 ZSet，取消 score <= 当前时间且 status=0 的订单
        orderTimeoutService.addToTimeoutQueue(orderId, System.currentTimeMillis() + 30 * 60 * 1000);

        // ② 更新订单状态为"处理中"（status=1），标记业务开始
        order.setStatus(1);
        orderMapper.updateById(order);
        log.info("订单状态更新为处理中, orderId={}", orderId);

        // ③ 通过 Feign 远程调用库存服务扣减库存；若失败则将订单置为"失败"（status=3）
        RespBean resp = inventoryFeignClient.deduct(orderId, null, null);
        if (resp == null || resp.getStatus() != 200) {
            log.error("库存扣减失败, orderId={}", orderId);
            order.setStatus(3);
            orderMapper.updateById(order);
            return;
        }
        log.info("库存扣减成功, orderId={}", orderId);

        // ④ 通过策略模式获取对应渠道（alipay/wechat/card）的支付实现，调用支付并获取支付流水号
        Integer userId = order.getUserId();
        BigDecimal amount = order.getTotalAmount();
        Integer paymentId = strategyRegistry.getStrategy(channel).pay(orderId, userId.longValue(), amount);
        if (paymentId == null) {
            // 支付失败同样置为失败状态；注意此处库存已在第③步扣减，实际生产需补充库存回滚或补偿机制
            log.error("支付失败, orderId={}, channel={}", orderId, channel);
            order.setStatus(3);
            orderMapper.updateById(order);
            return;
        }
        log.info("支付成功, orderId={}, paymentId={}", orderId, paymentId);

        // ⑤ 订单处理成功，更新状态为"已完成"（status=2），记录支付流水号
        order.setStatus(2);
        order.setPaymentId(paymentId);
        orderMapper.updateById(order);
        // 处理成功，从 Redis 超时队列移除
        orderTimeoutService.removeFromTimeoutQueue(orderId);
        log.info("订单已完成, orderId={}", orderId);

        // ⑥ 通过 RabbitMQ 发送订单创建事件，供下游消费者（如物流、通知）异步处理
        rabbitTemplate.convertAndSend("order.exchange", "order.created", order);
        log.info("订单事件已发送, orderId={}", orderId);

        // ⑦ 异步调用搜索服务同步商品索引，不阻塞主流程，使用 @Async 线程池
        asyncSyncProduct(order.getOrderNo());
    }

    /**
     * 异步同步商品信息到搜索引擎
     * <p>
     * 使用 @Async("commonExecutor") 由独立线程池执行，避免 Feign 调用阻塞主流程。
     * 此操作为非核心逻辑，失败不影响订单主流程。
     * </p>
     */
    @Async("commonExecutor")
    public void asyncSyncProduct(String orderNo) {
        log.info("异步同步商品信息, orderNo={}", orderNo);
        searchFeignClient.syncProduct(null);
    }
}
