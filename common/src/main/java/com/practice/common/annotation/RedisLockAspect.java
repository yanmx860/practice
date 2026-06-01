package com.practice.common.annotation;

import com.practice.common.exception.BizException;
import com.practice.common.redis.RedisDistributedLock;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Aspect
@Component
public class RedisLockAspect {

    @Autowired
    private RedisDistributedLock redisDistributedLock;

    private final ExpressionParser parser = new SpelExpressionParser();

    @Around("@annotation(redisLock)")
    public Object around(ProceedingJoinPoint joinPoint, RedisLock redisLock) throws Throwable {
        String lockKey = parseKey(redisLock.key(), joinPoint);
        String lockValue = UUID.randomUUID().toString();
        boolean locked = redisDistributedLock.lock(lockKey, lockValue, redisLock.expireMs(), redisLock.waitMs(), 200);
        if (!locked) {
            throw new BizException("Failed to acquire lock: " + lockKey);
        }
        try {
            return joinPoint.proceed();
        } finally {
            redisDistributedLock.unlock(lockKey, lockValue);
        }
    }

    private String parseKey(String key, ProceedingJoinPoint joinPoint) {
        if (!key.contains("#")) {
            return key;
        }
        StandardEvaluationContext context = new StandardEvaluationContext();
        Object[] args = joinPoint.getArgs();
        String[] paramNames = ((MethodSignature) joinPoint.getSignature()).getParameterNames();
        if (paramNames != null) {
            for (int i = 0; i < paramNames.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
        }
        return parser.parseExpression(key).getValue(context, String.class);
    }
}
