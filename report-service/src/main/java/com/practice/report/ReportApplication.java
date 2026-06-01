package com.practice.report;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** * 报表服务启动入口 * @author ymx * @since 2026-03-08 */
@SpringBootApplication(scanBasePackages = {"com.practice.report", "com.practice.common"})
public class ReportApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReportApplication.class, args);
    }
}
