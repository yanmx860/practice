package com.practice.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

/** * 订单服务启动入口 * @author ymx * @since 2026-01-25 */
@SpringBootApplication(scanBasePackages = {"com.practice.order", "com.practice.common"})
@EnableFeignClients
@EnableAsync
@MapperScan("com.practice.order.mapper")
public class OrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }
}
