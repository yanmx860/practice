package com.practice.order.service;

import com.practice.order.mapper.OrderMapper;
import com.practice.order.model.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderTimeoutService {

    private static final String TIMEOUT_ZSET = "order:timeout";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private final OrderMapper orderMapper;

    public void addToTimeoutQueue(Long orderId, long expireTimestamp) {
        stringRedisTemplate.opsForZSet().add(TIMEOUT_ZSET, String.valueOf(orderId), expireTimestamp);
        log.info("order {} added to timeout queue, expire at {}", orderId, expireTimestamp);
    }

    public void removeFromTimeoutQueue(Long orderId) {
        stringRedisTemplate.opsForZSet().remove(TIMEOUT_ZSET, String.valueOf(orderId));
    }

    public int cancelExpiredOrders() {
        long now = System.currentTimeMillis();
        Set<String> expiredIds = stringRedisTemplate.opsForZSet().rangeByScore(TIMEOUT_ZSET, 0, now);
        if (expiredIds == null || expiredIds.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (String idStr : expiredIds) {
            Long orderId = Long.valueOf(idStr);
            Order order = orderMapper.selectById(orderId);
            if (order == null || order.getStatus() != 0) {
                stringRedisTemplate.opsForZSet().remove(TIMEOUT_ZSET, idStr);
                continue;
            }
            order.setStatus(4);
            orderMapper.updateById(order);
            stringRedisTemplate.opsForZSet().remove(TIMEOUT_ZSET, idStr);
            count++;
            log.info("canceled expired order {}", orderId);
        }
        return count;
    }
}
