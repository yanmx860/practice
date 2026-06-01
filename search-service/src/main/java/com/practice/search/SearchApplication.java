package com.practice.search;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
/** * 搜索服务启动入口 * @author ymx * @since 2026-02-22 */
@SpringBootApplication(scanBasePackages = {"com.practice.search", "com.practice.common"})
public class SearchApplication {
    public static void main(String[] args) { SpringApplication.run(SearchApplication.class, args); }
}
