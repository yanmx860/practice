package com.practice.order.feign;

import com.practice.common.result.RespBean;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 库存服务 Feign 客户端
 * <p>
 * 声明式 HTTP 客户端，通过 Spring Cloud OpenFeign 实现对 inventory-service 的远程调用。
 * name = "inventory-service" 对应注册中心（Nacos/Eureka）中的服务名，支持客户端负载均衡。
 * 降级策略：通过 fallback 或 fallbackFactory 指定断路器降级逻辑，
 * 当服务不可用或响应超时时返回兜底数据，防止级联故障。
 * </p>
 * @author ymx
 * @since 2026-02-10
 */
@FeignClient(name = "inventory-service", path = "/api/inventory")
public interface InventoryFeignClient {

    /**
     * 扣减库存
     * @param orderId   订单ID
     * @param productId 商品ID
     * @param quantity  扣减数量
     * @return RespBean，status=200 表示成功
     */
    @PostMapping("/deduct")
    RespBean deduct(@RequestParam("orderId") Long orderId,
                    @RequestParam("productId") Long productId,
                    @RequestParam("quantity") Integer quantity);
}
