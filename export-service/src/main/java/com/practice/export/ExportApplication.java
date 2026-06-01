package com.practice.export;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** * 导出服务启动入口 * @author ymx * @since 2026-03-01 */
@SpringBootApplication(scanBasePackages = {"com.practice.export", "com.practice.common"})
@MapperScan("com.practice.export.mapper")
public class ExportApplication {
    public static void main(String[] args) {
        SpringApplication.run(ExportApplication.class, args);
    }
}
