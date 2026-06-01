package com.practice.report.job;

import com.practice.report.service.ReportService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** * 订单统计 XXL-Job * @author ymx * @since 2026-03-10 */
@Slf4j
@Component
public class OrderStatisticsJob {

    @Autowired
    private ReportService reportService;

    // @XxlJob("orderStatisticsJob")：XXL-Job 定时统计任务
    // 统计逻辑：按订单状态（PENDING / PAID / SHIPPED / COMPLETED / CANCELLED）分组计数
    // 生成格式为 xlsx 的订单分布统计报表，输出到临时目录
    @XxlJob("orderStatisticsJob")
    public void orderStatisticsJob() {
        XxlJobHelper.log("Starting order statistics generation...");
        try {
            String filePath = reportService.generateOrderStatistics();
            XxlJobHelper.log("Order statistics generated successfully: {}", filePath);
            XxlJobHelper.handleSuccess();
        } catch (Exception e) {
            XxlJobHelper.log("Failed to generate order statistics: {}", e.getMessage());
            XxlJobHelper.handleFail("Order statistics generation failed: " + e.getMessage());
        }
    }
}
