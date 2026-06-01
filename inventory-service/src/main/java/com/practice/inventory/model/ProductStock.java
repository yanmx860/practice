package com.practice.inventory.model;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** * 商品库存实体 * @author ymx * @since 2026-02-02 */
@Schema(description = "商品库存")
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_product_stock")
public class ProductStock {
    @Schema(description = "ID")
    private Long id;
    @Schema(description = "商品ID")
    private Long productId;
    @Schema(description = "商品名称")
    private String productName;
    @Schema(description = "总库存")
    private Integer totalStock;
    @Schema(description = "冻结库存")
    private Integer frozenStock;
    @Schema(description = "可用库存")
    private Integer availableStock;
    @Schema(description = "乐观锁版本号")
    private Integer version;
}
