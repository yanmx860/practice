package com.practice.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.practice.inventory.model.ProductStock;
import org.apache.ibatis.annotations.Mapper;

/** * 商品库存 Mapper * @author ymx * @since 2026-02-02 */
@Mapper
public interface ProductStockMapper extends BaseMapper<ProductStock> {
}
