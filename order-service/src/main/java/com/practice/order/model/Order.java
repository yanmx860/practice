package com.practice.order.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.practice.common.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** * 订单实体 * @author ymx * @since 2026-01-25 */
@Schema(description = "订单")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("t_order")
public class Order extends BaseEntity {
    @Schema(description = "订单编号")
    private String orderNo;
    @Schema(description = "用户ID")
    private Integer userId;
    @Schema(description = "订单金额")
    private BigDecimal totalAmount;
    @Schema(description = "状态: 0-待处理 1-处理中 2-已完成 3-失败 4-已取消")
    private Integer status;
    @Schema(description = "支付渠道: alipay/wechat/card")
    private String channel;
    @Schema(description = "支付记录ID")
    private Integer paymentId;
}
