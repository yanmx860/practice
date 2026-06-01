package com.practice.auth.service;

import com.practice.auth.mapper.UserMapper;
import com.practice.auth.model.User;
import com.practice.common.exception.BizException;
import com.practice.common.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

/** * 认证服务 * @author ymx * @since 2026-01-21 */
@Service
public class AuthService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private JwtUtil jwtUtil;

    // 内存中的失败计数和锁定时间映射，生产环境应改用 Redis 以支持多实例
    private final Map<Integer, Integer> failCount = new HashMap<>();
    private final Map<Integer, Long> lockTime = new HashMap<>();
    private static final int MAX_FAIL = 5;          // 最大连续失败次数
    private static final long LOCK_DURATION = 1800000;  // 锁定时长 30 分钟

    /*
     * 登录流程：
     * 1. 根据用户名查询用户，不存在则直接返回
     * 2. 检查账户是否被锁定（30分钟内失败5次）
     * 3. 用 BCrypt 比对密码，匹配失败则记录失败次数
     * 4. 登录成功后清除失败记录，生成 accessToken + refreshToken 返回
     */
    public Map<String, Object> login(String username, String password) {
        User user = userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username));
        if (user == null) throw new BizException("401", "用户不存在");

        if (isLocked(user.getId())) throw new BizException("401", "账户已锁定, 请30分钟后重试");

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (!encoder.matches(password, user.getPassword())) {
            recordFail(user.getId());
            throw new BizException("401", "密码错误");
        }
        resetFail(user.getId());

        Set<String> perms = user.getPermissions() == null ? new HashSet<>() :
                new HashSet<>(Arrays.asList(user.getPermissions().split(",")));
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername(), perms);
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("accessToken", accessToken);
        result.put("refreshToken", refreshToken);
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("permissions", perms);
        return result;
    }

    /*
     * 刷新流程：校验 refreshToken 类型 → 提取用户 ID → 查库获取最新权限 → 签发新 accessToken
     * 注意：refreshToken 仅在服务端验证，不暴露到前端业务请求中
     */
    public Map<String, Object> refresh(String refreshToken) {
        if (!"refresh".equals(jwtUtil.getTokenType(refreshToken))) {
            throw new BizException("401", "无效的refreshToken");
        }
        Integer userId = jwtUtil.getUserId(refreshToken);
        User user = userMapper.selectById(userId);
        if (user == null) throw new BizException("401", "用户不存在");

        Set<String> perms = user.getPermissions() == null ? new HashSet<>() :
                new HashSet<>(Arrays.asList(user.getPermissions().split(",")));
        String accessToken = jwtUtil.generateAccessToken(userId, user.getUsername(), perms);
        Map<String, Object> result = new HashMap<>();
        result.put("accessToken", accessToken);
        return result;
    }

    // 判断用户是否在锁定期内：已超时则自动解锁并清空记录
    private boolean isLocked(Integer userId) {
        Long lock = lockTime.get(userId);
        if (lock == null) return false;
        if (System.currentTimeMillis() - lock > LOCK_DURATION) {
            lockTime.remove(userId);
            failCount.remove(userId);
            return false;
        }
        return true;
    }

    // 记录一次失败，连续失败达到 MAX_FAIL 次时触发锁定并记录锁定时间戳
    private void recordFail(Integer userId) {
        int count = failCount.getOrDefault(userId, 0) + 1;
        failCount.put(userId, count);
        if (count >= MAX_FAIL) {
            lockTime.put(userId, System.currentTimeMillis());
        }
    }

    // 登录成功后清除该用户的失败和锁定记录
    private void resetFail(Integer userId) {
        failCount.remove(userId);
        lockTime.remove(userId);
    }
}
