package com.practice.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.practice.order.model.Order;
import org.apache.ibatis.annotations.Mapper;

/** * 订单 Mapper * @author ymx * @since 2026-01-25 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}
