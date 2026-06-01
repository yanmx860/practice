package com.practice.common.pattern.strategy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** * 支付策略注册器 * @author ymx * @since 2026-01-11 */
@Component
public class StrategyRegistry {

    // Spring 会自动收集所有 PaymentStrategy 实现类注入到此 List 中
    @Autowired
    private List<PaymentStrategy> strategies;

    // 渠道 → 策略的映射表，用于运行时快速查找
    private final Map<String, PaymentStrategy> registry = new HashMap<>();

    /*
     * @PostConstruct 在 Bean 初始化完成后自动调用。
     * 遍历所有 PaymentStrategy 实现，按渠道标识注册到 map 中，
     * 避免在调用时每次遍历 List 查找，将 O(n) 降为 O(1)。
     */
    @PostConstruct
    public void init() {
        for (PaymentStrategy strategy : strategies) {
            registry.put(strategy.getChannel(), strategy);
        }
    }

    // 根据渠道编码查找对应策略，找不到时抛出明确异常提示调用方
    public PaymentStrategy getStrategy(String channel) {
        PaymentStrategy strategy = registry.get(channel);
        if (strategy == null) {
            throw new IllegalArgumentException("不支持的支付渠道: " + channel);
        }
        return strategy;
    }
}
