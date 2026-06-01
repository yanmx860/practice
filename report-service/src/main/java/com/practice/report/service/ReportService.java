package com.practice.report.service;

import com.alibaba.excel.EasyExcel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** * 报表服务 * @author ymx * @since 2026-03-09 */
@Slf4j
@Service
public class ReportService {

    private final Map<String, Object> data = new HashMap<>();

    // generateDailyReport：使用 EasyExcel 生成日报表 Excel 文件
    // EasyExcel.write(filePath, Map.class)：指定输出路径和数据类型
    // .sheet("Daily Report")：创建名为 "Daily Report" 的 sheet
    // .doWrite(rows)：一次性写入数据（适合数据量小的场景；大批量应用 write + finish 流式写入）
    // 报表内容：日期、总订单数、总金额、有效订单数（此处为模拟数据）
    public String generateDailyReport() {
        LocalDate today = LocalDate.now();
        String fileName = "daily-report-" + today.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";
        String filePath = System.getProperty("java.io.tmpdir") + File.separator + fileName;

        List<Map<String, Object>> rows = new ArrayList<>();
        Map<String, Object> row = new HashMap<>();
        row.put("date", today.toString());
        row.put("totalOrders", 100 + (int) (Math.random() * 100));
        row.put("totalAmount", 5000.0 + Math.random() * 10000);
        row.put("validOrders", 80 + (int) (Math.random() * 20));
        rows.add(row);

        EasyExcel.write(filePath, Map.class).sheet("Daily Report").doWrite(rows);

        data.put("dailyReportPath", filePath);
        log.info("Daily report generated: {}", filePath);
        return filePath;
    }

    // generateOrderStatistics：使用 EasyExcel 生成订单统计报表
    // 按各状态（PENDING/PAID/SHIPPED/COMPLETED/CANCELLED）分别统计数量
    // EasyExcel 自动根据 Map 的 key 作为列头写出
    public String generateOrderStatistics() {
        LocalDate today = LocalDate.now();
        String fileName = "order-statistics-" + today.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";
        String filePath = System.getProperty("java.io.tmpdir") + File.separator + fileName;

        List<Map<String, Object>> rows = new ArrayList<>();
        for (String status : new String[]{"PENDING", "PAID", "SHIPPED", "COMPLETED", "CANCELLED"}) {
            Map<String, Object> row = new HashMap<>();
            row.put("status", status);
            row.put("count", 10 + (int) (Math.random() * 200));
            rows.add(row);
        }

        EasyExcel.write(filePath, Map.class).sheet("Order Statistics").doWrite(rows);

        data.put("orderStatisticsPath", filePath);
        log.info("Order statistics generated: {}", filePath);
        return filePath;
    }

    public List<String> getReportList() {
        return data.values().stream()
                .filter(v -> v instanceof String)
                .map(v -> (String) v)
                .collect(Collectors.toList());
    }
}
