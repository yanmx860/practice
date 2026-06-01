package com.practice.auth.controller;

import com.practice.auth.service.AuthService;
import com.practice.common.exception.BizException;
import com.practice.common.result.RespBean;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** * 认证控制器 * @author ymx * @since 2026-01-22 */
@Tag(name = "认证管理", description = "用户登录、令牌刷新")

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /*
     * POST /api/auth/login
     * 用户登录接口：接收用户名和密码，认证成功后返回 accessToken（用于后续请求鉴权）
     * 和 refreshToken（用于令牌过期后静默续期）
     */
    @Operation(summary = "用户登录", description = "用户名密码登录，返回accessToken和refreshToken")
    @PostMapping("/login")
    public RespBean login(@Parameter(description = "登录信息") @RequestBody Map<String, String> req) {
        try {
            Map<String, Object> result = authService.login(req.get("username"), req.get("password"));
            return RespBean.ok(result);
        } catch (BizException e) {
            return RespBean.error(401, e.getMessage());
        }
    }

    /*
     * POST /api/auth/refresh
     * 令牌刷新接口：使用 refreshToken 换取新的 accessToken，
     * 客户端可在 accessToken 过期前调用此接口实现无感续期
     */
    @Operation(summary = "刷新令牌", description = "使用refreshToken换取新的accessToken")
    @PostMapping("/refresh")
    public RespBean refresh(@Parameter(description = "包含refreshToken的请求体") @RequestBody Map<String, String> req) {
        try {
            Map<String, Object> result = authService.refresh(req.get("refreshToken"));
            return RespBean.ok(result);
        } catch (BizException e) {
            return RespBean.error(401, e.getMessage());
        }
    }
}
