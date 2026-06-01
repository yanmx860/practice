package com.practice.common.annotation;

import com.practice.common.exception.BizException;
import com.practice.common.thread.UserContextHolder;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Set;

/** * 权限校验AOP切面 * @author ymx * @since 2026-01-16 */
@Aspect
@Component
public class PermissionAspect {

    /*
     * Around 通知：切入点匹配所有标注 @RequirePermission 的方法。
     * 在方法执行前从 UserContextHolder 获取当前用户的权限集合，
     * 与注解要求的权限进行比对，不匹配则抛出异常阻止方法执行。
     */
    @Around("@annotation(com.practice.common.annotation.RequirePermission)")
    public Object checkPermission(ProceedingJoinPoint pjp) throws Throwable {
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        RequirePermission annotation = method.getAnnotation(RequirePermission.class);
        // 理论上不会执行到此，但保留防御性判断，确保空注解时直接放行
        if (annotation == null) return pjp.proceed();

        String required = annotation.value();
        // 从 ThreadLocal 获取已通过 JWT 过滤器解析并存入的权限集合
        Set<String> permissions = UserContextHolder.getCurrentPermissions();
        if (permissions == null || !permissions.contains(required)) {
            throw new BizException("403", "无权限访问: " + required);
        }
        return pjp.proceed();
    }
}
