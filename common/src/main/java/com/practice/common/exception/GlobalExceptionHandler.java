package com.practice.common.exception;

import com.practice.common.result.RespBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** * 全局异常处理器 * @author ymx * @since 2026-01-07 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    @ExceptionHandler(BizException.class)
    public RespBean handleBiz(BizException e) {
        log.warn("业务异常: {} - {}", e.getCode(), e.getMessage());
        return RespBean.error(400, e.getMessage());
    }
    @ExceptionHandler(Exception.class)
    public RespBean handleEx(Exception e) {
        log.error("系统异常", e);
        return RespBean.error("系统繁忙, 请稍后重试");
    }
}
