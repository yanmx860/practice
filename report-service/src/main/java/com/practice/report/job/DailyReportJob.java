package com.practice.report.job;

import com.practice.report.service.ReportService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** * 日报表 XXL-Job * @author ymx * @since 2026-03-10 */
@Slf4j
@Component
public class DailyReportJob {

    @Autowired
    private ReportService reportService;

    // @XxlJob("dailyReportJob")：XXL-Job 任务调度注解，任务名称对应调度中心配置的 JobHandler
    // 在 XXL-Job 调度中心新建任务时，"运行模式"选择"BEAN"，"JobHandler"填写 "dailyReportJob"
    // 流程：调度中心按 cron 触发 → 执行器调用此方法 → 生成 Excel 日报 → handleSuccess 汇报成功
    @XxlJob("dailyReportJob")
    public void dailyReportJob() {
        XxlJobHelper.log("Starting daily report generation...");
        try {
            String filePath = reportService.generateDailyReport();
            XxlJobHelper.log("Daily report generated successfully: {}", filePath);
            XxlJobHelper.handleSuccess(); // 通知调度中心任务执行成功
        } catch (Exception e) {
            XxlJobHelper.log("Failed to generate daily report: {}", e.getMessage());
            XxlJobHelper.handleFail("Daily report generation failed: " + e.getMessage());
        }
    }
}
