package com.practice.order.model;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** * 订单项实体 * @author ymx * @since 2026-01-25 */
@Schema(description = "订单项")
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_order_item")
public class OrderItem {
    @Schema(description = "项ID")
    private Long id;
    @Schema(description = "订单ID")
    private Long orderId;
    @Schema(description = "商品ID")
    private Long productId;
    @Schema(description = "商品名称")
    private String productName;
    @Schema(description = "数量")
    private Integer quantity;
    @Schema(description = "单价")
    private BigDecimal price;
}
