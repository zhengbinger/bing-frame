package com.bing.framework.context;

import com.bing.framework.entity.User;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 用户上下文管理类
 * 使用ThreadLocal存储当前线程的用户信息，提供线程安全的用户数据访问
 * 确保在多线程环境下每个请求线程都能访问到正确的用户数据
 *
 * @author zhengbing
 * @date 2025-11-11
 */
@Slf4j
public class UserContext {

    /**
     * 存储当前线程的用户信息
     */
    private static final ThreadLocal<User> USER_HOLDER = new ThreadLocal<>();

    /**
     * 存储当前线程的用户ID
     */
    private static final ThreadLocal<Long> USER_ID_HOLDER = new ThreadLocal<>();

    /**
     * 存储当前线程的用户名
     */
    private static final ThreadLocal<String> USERNAME_HOLDER = new ThreadLocal<>();

    /**
     * 存储当前线程的用户角色列表
     */
    private static final ThreadLocal<List<String>> ROLES_HOLDER = new ThreadLocal<>();

    /**
     * 设置当前线程的用户信息
     *
     * @param user 用户对象
     */
    public static void setUser(User user) {
        if (user != null) {
            USER_HOLDER.set(user);
            USER_ID_HOLDER.set(user.getId());
            USERNAME_HOLDER.set(user.getUsername());
            log.debug("设置用户上下文成功，用户ID: {}, 用户名: {}", user.getId(), user.getUsername());
        }
    }

    /**
     * 设置当前线程的用户基本信息
     *
     * @param userId   用户ID
     * @param username 用户名
     */
    public static void setUserInfo(Long userId, String username) {
        USER_ID_HOLDER.set(userId);
        USERNAME_HOLDER.set(username);
        log.debug("设置用户基本信息成功，用户ID: {}, 用户名: {}", userId, username);
    }

    /**
     * 设置当前线程的用户角色列表
     *
     * @param roles 角色列表
     */
    public static void setRoles(List<String> roles) {
        ROLES_HOLDER.set(roles);
        log.debug("设置用户角色列表成功，角色数量: {}", roles != null ? roles.size() : 0);
    }

    /**
     * 获取当前线程的用户对象
     *
     * @return 用户对象，如果没有设置则返回null
     */
    public static User getUser() {
        return USER_HOLDER.get();
    }

    /**
     * 获取当前线程的用户ID
     *
     * @return 用户ID，如果没有设置则返回null
     */
    public static Long getUserId() {
        return USER_ID_HOLDER.get();
    }

    /**
     * 获取当前线程的用户名
     *
     * @return 用户名，如果没有设置则返回null
     */
    public static String getUsername() {
        return USERNAME_HOLDER.get();
    }

    /**
     * 获取当前线程的用户角色列表
     *
     * @return 角色列表，如果没有设置则返回null
     */
    public static List<String> getRoles() {
        return ROLES_HOLDER.get();
    }

    /**
     * 判断当前线程是否已设置用户信息
     *
     * @return 是否已设置用户信息
     */
    public static boolean hasUser() {
        return USER_ID_HOLDER.get() != null;
    }

    /**
     * 清理当前线程的用户上下文信息
     * 防止ThreadLocal内存泄漏，在线程结束时必须调用此方法
     */
    public static void clear() {
        USER_HOLDER.remove();
        USER_ID_HOLDER.remove();
        USERNAME_HOLDER.remove();
        ROLES_HOLDER.remove();
        log.debug("清理用户上下文成功");
    }

    /**
     * 快速设置用户ID（适用于只需要用户ID的场景）
     *
     * @param userId 用户ID
     */
    public static void setUserId(Long userId) {
        USER_ID_HOLDER.set(userId);
        log.debug("快速设置用户ID成功: {}", userId);
    }

    /**
     * 检查当前用户是否拥有指定角色
     *
     * @param role 角色代码
     * @return 是否拥有该角色
     */
    public static boolean hasRole(String role) {
        List<String> roles = getRoles();
        return roles != null && roles.contains(role);
    }
}