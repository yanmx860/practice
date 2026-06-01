package com.practice.search.document;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.util.Date;

/** * 商品 ES 文档 * @author ymx * @since 2026-02-22 */
@Schema(description = "商品ES文档")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "products")
public class ProductDocument {
    @Schema(description = "商品ID")
    @Id
    private Long id;
    // @Field(type = FieldType.Text, analyzer = "ik_max_word")：ik 中文分词器，最大粒度切词
    // name/description 使用 Text 类型 + ik_max_word，支持中文全文搜索
    @Schema(description = "商品名称")
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String name;
    @Schema(description = "商品描述")
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String description;
    // price 使用 Double 类型，支持范围排序和聚合统计
    @Schema(description = "价格")
    @Field(type = FieldType.Double)
    private BigDecimal price;
    // stock 使用 Integer 类型，用于库存精确匹配和过滤
    @Schema(description = "库存")
    @Field(type = FieldType.Integer)
    private Integer stock;
    @Schema(description = "创建时间")
    private Date createTime;  // 未指定 @Field 则自动映射到 ES date 类型
}
