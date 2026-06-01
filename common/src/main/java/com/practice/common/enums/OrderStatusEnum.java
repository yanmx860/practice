package com.practice.common.enums;
/**
 * 订单状态枚举
 * @author ymx
 * @since 2026-01-07
 */

public enum OrderStatusEnum {
    PENDING(0, "待处理"),
    PROCESSING(1, "处理中"),
    SUCCESS(2, "已完成"),
    FAILED(3, "失败"),
    CANCELLED(4, "已取消");

    private final int code;
    private final String desc;
    OrderStatusEnum(int code, String desc) { this.code = code; this.desc = desc; }
    public int getCode() { return code; }
    public String getDesc() { return desc; }
}
