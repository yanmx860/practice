package com.practice.order.model;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/** * 订单日志实体 * @author ymx * @since 2026-01-25 */
@Schema(description = "订单操作日志")
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_order_log")
public class OrderLog {
    @Schema(description = "日志ID")
    private Long id;
    @Schema(description = "订单ID")
    private Long orderId;
    @Schema(description = "操作动作")
    private String action;
    @Schema(description = "操作详情")
    private String detail;
    @Schema(description = "创建时间")
    private Date createTime;
}
