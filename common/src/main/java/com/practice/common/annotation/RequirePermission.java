package com.practice.common.annotation;

import java.lang.annotation.*;

/** * 自定义权限注解 * @author ymx * @since 2026-01-16 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {
    String value();
    String description() default "";
}
