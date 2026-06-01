package com.practice.common.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Component
public class RedisDistributedLock {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String LOCK_PREFIX = "lock:";
    private static final String UNLOCK_LUA =
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

    public boolean tryLock(String key, String value, long expireMs) {
        return stringRedisTemplate.opsForValue()
                .setIfAbsent(LOCK_PREFIX + key, value, expireMs, TimeUnit.MILLISECONDS);
    }

    public boolean lock(String key, String value, long expireMs, long waitMs, long retryIntervalMs) {
        long deadline = System.currentTimeMillis() + waitMs;
        while (System.currentTimeMillis() < deadline) {
            if (tryLock(key, value, expireMs)) {
                return true;
            }
            try {
                Thread.sleep(Math.min(retryIntervalMs, deadline - System.currentTimeMillis()));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }

    public void unlock(String key, String value) {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>(UNLOCK_LUA, Long.class);
        stringRedisTemplate.execute(script, Collections.singletonList(LOCK_PREFIX + key), value);
    }
}
