package com.practice.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Set;

/** * JWT 工具类 * @author ymx * @since 2026-01-08 */
@Component
public class JwtUtil {
    // 签名密钥，生产环境应从配置中心获取并定期轮换
    private final String secret = "PracticeSecretKey2026";
    // accessToken 有效期 30 分钟：短时效降低泄露风险，每次请求携带用于身份认证
    private final long accessExpire = 1800000;
    // refreshToken 有效期 30 天：长时效用于静默续期，仅在刷新接口传输，减少暴露面
    private final long refreshExpire = 2592000000L;

    // 生成 accessToken，携带用户 ID、用户名、权限列表，供后续请求鉴权使用
    public String generateAccessToken(Integer userId, String username, Set<String> permissions) {
        return Jwts.builder()
                .claim("userId", userId)
                .claim("username", username)
                .claim("permissions", String.join(",", permissions))
                .claim("type", "access")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessExpire))
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    // 生成 refreshToken，仅携带 userId 和类型标记，不包含敏感权限信息
    public String generateRefreshToken(Integer userId) {
        return Jwts.builder()
                .claim("userId", userId)
                .claim("type", "refresh")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpire))
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    // 解析 JWT 令牌：验证签名与有效期，返回 Claims 载荷
    public Claims parseToken(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }

    // 验证令牌合法性：通过捕获 parse 异常判断令牌是否过期或被篡改
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Integer getUserId(String token) {
        return (Integer) parseToken(token).get("userId");
    }

    public String getUsername(String token) {
        return (String) parseToken(token).get("username");
    }

    // 获取令牌类型，用于区分 accessToken 和 refreshToken
    public String getTokenType(String token) {
        return (String) parseToken(token).get("type");
    }

    // 从 accessToken 中解析权限集合（逗号分隔转 Set），权限为空时返回空集合而非 null
    public Set<String> getPermissions(String token) {
        String perms = (String) parseToken(token).get("permissions");
        if (perms == null) return java.util.Collections.emptySet();
        java.util.Set<String> result = new java.util.HashSet<>();
        java.util.Collections.addAll(result, perms.split(","));
        return result;
    }
}
