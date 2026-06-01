package com.practice.auth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** * 认证服务启动入口 * @author ymx * @since 2026-01-20 */
@SpringBootApplication(scanBasePackages = {"com.practice.auth", "com.practice.common"})
@MapperScan("com.practice.auth.mapper")
public class AuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }
}
