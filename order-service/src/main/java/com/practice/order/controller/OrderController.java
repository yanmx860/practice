package com.practice.order.controller;

import com.practice.common.result.RespBean;
import com.practice.order.model.Order;
import com.practice.order.service.OrderService;
import com.practice.order.service.ParallelOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * 订单控制器
 * <p>
 * 提供单个订单处理和批量并行处理两个 REST 端点。
 * 单订单处理走 {@link OrderService#processOrder}（含 @GlobalTransactional 分布式事务）；
 * 批量处理走 {@link ParallelOrderService#processMultipleOrders}（CompletableFuture 多线程并发）。
 * </p>
 * @author ymx
 * @since 2026-01-27
 */
@Tag(name = "订单管理", description = "订单创建、处理、查询")
@Slf4j
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ParallelOrderService parallelOrderService;

    /**
     * 处理单个订单
     * @param orderId 订单ID（路径变量）
     * @param channel 支付渠道，支持 alipay/wechat/card，默认 alipay
     */
    @Operation(summary = "处理订单", description = "根据订单ID和支付渠道处理订单（模板方法+策略模式）")
    @PostMapping("/process/{orderId}")
    public RespBean processOrder(
            @Parameter(description = "订单ID") @PathVariable Long orderId,
            @Parameter(description = "支付渠道: alipay/wechat/card") @RequestParam(defaultValue = "alipay") String channel) {
        orderService.processOrder(orderId, channel);
        return RespBean.ok("订单处理完成");
    }

    /**
     * 批量处理订单
     * <p>
     * 接收逗号分隔的订单ID列表，解析后交由 {@link ParallelOrderService} 并行处理。
     * 使用 ThreadLocal（UserContextHolder）传递用户上下文 + 独立线程池隔离任务。
     * </p>
     * @param ids     逗号分隔的订单ID字符串，如 "1,2,3"
     * @param channel 支付渠道，默认 alipay
     */
    @Operation(summary = "批量处理订单", description = "多线程并行处理多个订单（ThreadLocal+线程池）")
    @PostMapping("/processBatch")
    public RespBean processBatch(
            @Parameter(description = "订单ID列表，逗号分隔") @RequestParam String ids,
            @RequestParam(defaultValue = "alipay") String channel) {
        List<String> idList = Arrays.asList(ids.split(","));
        List<Long> orderIds = new java.util.ArrayList<>();
        for (String id : idList) {
            orderIds.add(Long.parseLong(id.trim()));
        }
        parallelOrderService.processMultipleOrders(orderIds, channel);
        return RespBean.ok("批量处理完成，共" + orderIds.size() + "个订单");
    }
}
