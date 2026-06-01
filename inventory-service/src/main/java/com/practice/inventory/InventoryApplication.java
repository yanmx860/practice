package com.practice.inventory;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** * 库存服务启动入口 * @author ymx * @since 2026-02-02 */
@SpringBootApplication(scanBasePackages = {"com.practice.inventory", "com.practice.common"})
@MapperScan("com.practice.inventory.mapper")
public class InventoryApplication {
    public static void main(String[] args) {
        SpringApplication.run(InventoryApplication.class, args);
    }
}
