package com.practice.export.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.practice.export.model.ExportOrder;
import org.apache.ibatis.annotations.Mapper;

/** * 导出订单 Mapper * @author ymx * @since 2026-03-01 */
@Mapper
public interface ExportOrderMapper extends BaseMapper<ExportOrder> {
}
