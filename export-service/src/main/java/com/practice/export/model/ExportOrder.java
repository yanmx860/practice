package com.practice.export.model;

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/** * 导出订单实体 * @author ymx * @since 2026-03-01 */
@Schema(description = "导出订单")
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_order")
public class ExportOrder {
    @Schema(description = "订单ID")
    private Long id;
    @Schema(description = "订单编号")
    private String orderNo;
    @Schema(description = "用户ID")
    private Integer userId;
    @Schema(description = "订单金额")
    private BigDecimal totalAmount;
    @Schema(description = "状态")
    private Integer status;
    @Schema(description = "支付渠道")
    private String channel;
    @Schema(description = "创建时间")
    private Date createTime;
}
