package com.practice.common.exception;
/**
 * 业务异常
 * @author ymx
 * @since 2026-01-06
 */

public class BizException extends RuntimeException {
    private String code;
    public BizException(String message) { super(message); }
    public BizException(String code, String message) { super(message); this.code = code; }
    public String getCode() { return code; }
}
