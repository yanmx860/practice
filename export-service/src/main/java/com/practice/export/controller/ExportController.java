package com.practice.export.controller;

import com.practice.export.service.impl.OrderExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

/** * 导出控制器 * @author ymx * @since 2026-03-03 */
@Tag(name = "数据导出", description = "百万级数据导出（EasyExcel+线程池）")
@RestController
@RequestMapping("/api/export")
@Slf4j
public class ExportController {

    @Autowired
    private OrderExportService orderExportService;

    @Operation(summary = "导出订单", description = "分页+线程池并行查询，EasyExcel流式写入防OOM")
    @GetMapping("/orders")
    public void exportOrders(HttpServletResponse response,
                             @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
                             @Parameter(description = "结束日期") @RequestParam(required = false) String endDate) {
        StringBuilder sb = new StringBuilder();
        if (startDate != null) {
            sb.append(startDate);
        }
        sb.append(",");
        if (endDate != null) {
            sb.append(endDate);
        }
        String dateRange = sb.toString();
        orderExportService.exportOrders(response, dateRange);
    }
}
