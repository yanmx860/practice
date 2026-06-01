package com.practice.common.thread;

import java.util.Set;

/** * 用户上下文Holder（ThreadLocal） * @author ymx * @since 2026-01-15 */
public class UserContextHolder {

    // 当前请求的用户ID，用于后续业务操作中识别操作人
    private static final ThreadLocal<Integer> userIdHolder = new ThreadLocal<>();
    // 当前请求的用户名，用于日志记录和权限展示
    private static final ThreadLocal<String> usernameHolder = new ThreadLocal<>();
    // 当前请求的权限集合，用于方法级别的权限校验
    private static final ThreadLocal<Set<String>> permissionsHolder = new ThreadLocal<>();

    public static void set(Integer userId, String username, Set<String> permissions) {
        userIdHolder.set(userId);
        usernameHolder.set(username);
        permissionsHolder.set(permissions);
    }

    public static Integer getCurrentUserId() {
        return userIdHolder.get();
    }

    public static String getCurrentUsername() {
        return usernameHolder.get();
    }

    public static Set<String> getCurrentPermissions() {
        return permissionsHolder.get();
    }

    /**
     * 必须在 finally 块中调用 clear()，防止 ThreadLocal 值在线程复用（如 Tomcat 线程池）时
     * 被下一个请求读到脏数据，造成权限越界或用户信息错乱。
     */
    public static void clear() {
        userIdHolder.remove();
        usernameHolder.remove();
        permissionsHolder.remove();
    }
}
