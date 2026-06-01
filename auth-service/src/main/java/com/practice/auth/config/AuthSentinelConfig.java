package com.practice.auth.config;

import com.practice.common.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

/** * Sentinel 限流配置 * @author ymx * @since 2026-03-16 */
@Configuration
public class AuthSentinelConfig {

    @Autowired
    private JwtUtil jwtUtil;
}
