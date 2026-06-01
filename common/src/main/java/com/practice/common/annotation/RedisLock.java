package com.practice.common.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RedisLock {

    String key();

    long expireMs() default 30000;

    long waitMs() default 5000;
}
