package com.practice.order.feign;

import com.practice.common.result.RespBean;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 搜索服务 Feign 客户端
 * <p>
 * 声明式 HTTP 客户端，通过 Spring Cloud OpenFeign 调用 search-service，
 * 用于在订单完成后异步同步商品索引到搜索引擎（如 Elasticsearch），
 * 保证用户搜索到的商品信息是最新的。
 * 此调用为非核心链路，通常使用 @Async 异步执行，失败不影响主订单流程。
 * </p>
 * @author ymx
 * @since 2026-02-10
 */
@FeignClient(name = "search-service", path = "/api/search")
public interface SearchFeignClient {

    /**
     * 同步商品信息到搜索引擎
     * @param productId 商品ID，用于索引更新
     * @return RespBean
     */
    @PostMapping("/syncProduct")
    RespBean syncProduct(@RequestParam("productId") Long productId);
}
