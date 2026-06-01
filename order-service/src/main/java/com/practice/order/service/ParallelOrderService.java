package com.practice.order.service;

import com.practice.common.thread.UserContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

/**
 * 并行订单处理服务
 * <p>
 * 批量订单场景下，串行处理 N 个订单的耗时为 ΣT(i)，若每个订单含远程 Feign 调用（库存、支付），
 * 整体延迟会线性增长。通过 {@link CompletableFuture} + 自定义线程池将订单分配到多个线程并行处理，
 * 使总耗时趋近于最慢订单的耗时（max(T(i))），大幅提升吞吐量。
 * </p>
 * @author ymx
 * @since 2026-01-29
 */
@Slf4j
@Service
public class ParallelOrderService {

    @Autowired
    private OrderService orderService;

    @Autowired
    @Qualifier("commonExecutor")
    private Executor executor;

    /**
     * 并行处理多个订单
     * <p>
     * 使用 CountDownLatch 实现主线程等待所有子任务完成后再返回，
     * 确保调用方（如 Controller）能拿到完整的处理结果。
     * CompletableFuture.runAsync 将每个订单提交到 shared 线程池执行，
     * 每个子线程通过 UserContextHolder.set() 初始化用户上下文（模拟登录态），
     * 并在 finally 中 clear() 防止 ThreadLocal 在线程池复用时的脏读问题。
     * </p>
     */
    public void processMultipleOrders(List<Long> orderIds, String channel) {
        // CountDownLatch 初始化为订单数量，每个子任务完成后 countDown 一次
        CountDownLatch latch = new CountDownLatch(orderIds.size());

        for (Long orderId : orderIds) {
            // 为每个订单提交一个异步任务到 commonExecutor 线程池
            CompletableFuture.runAsync(() -> {
                try {
                    // 设置用户上下文（ThreadLocal），使 orderService 内部能获取到当前用户信息
                    UserContextHolder.set(1, "system", null);
                    // 实际调用单订单处理逻辑，复用 OrderService 中的 @GlobalTransactional 流程
                    orderService.processOrder(orderId, channel);
                } catch (Exception e) {
                    // 捕获异常避免单个订单失败影响其他订单的处理
                    log.error("并行处理订单异常, orderId={}", orderId, e);
                } finally {
                    // 清理 ThreadLocal，防止线程池复用线程时上下文污染
                    UserContextHolder.clear();
                    // 计数器减一，通知主线程该子任务已完成
                    latch.countDown();
                }
            }, executor);
        }

        try {
            // 主线程阻塞等待所有订单处理完毕，确保 processMultipleOrders 返回时所有任务已完成
            latch.await();
            log.info("所有订单处理完成, 共{}个", orderIds.size());
        } catch (InterruptedException e) {
            // 中断恢复：保留中断状态以便上层感知，避免静默吞掉中断信号
            Thread.currentThread().interrupt();
            log.error("并行处理被中断", e);
        }
    }
}
