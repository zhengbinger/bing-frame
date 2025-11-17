package com.bing.framework.util;

import lombok.extern.slf4j.Slf4j;

/**
 * 请求上下文工具类
 * 基于ThreadLocal存储和获取请求相关信息，确保线程安全
 * 
 * @author zhengbing
 * @date 2025-11-17
 */
@Slf4j
public class RequestContextUtil {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> USERNAME = new ThreadLocal<>();
    private static final ThreadLocal<String> TOKEN_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> CLIENT_IP = new ThreadLocal<>();
    private static final ThreadLocal<String> USER_AGENT = new ThreadLocal<>();
    private static final ThreadLocal<String> DEVICE_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> CLIENT_TYPE = new ThreadLocal<>();
    private static final ThreadLocal<String> REQUEST_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> SESSION_ID = new ThreadLocal<>();

    /**
     * 设置用户ID
     */
    public static void setUserId(Long userId) {
        USER_ID.set(userId);
    }

    /**
     * 获取用户ID
     */
    public static Long getUserId() {
        return USER_ID.get();
    }

    /**
     * 设置用户名
     */
    public static void setUsername(String username) {
        USERNAME.set(username);
    }

    /**
     * 获取用户名
     */
    public static String getUsername() {
        return USERNAME.get();
    }

    /**
     * 设置令牌ID
     */
    public static void setTokenId(String tokenId) {
        TOKEN_ID.set(tokenId);
    }

    /**
     * 获取令牌ID
     */
    public static String getTokenId() {
        return TOKEN_ID.get();
    }

    /**
     * 设置客户端IP地址
     */
    public static void setClientIp(String clientIp) {
        CLIENT_IP.set(clientIp);
    }

    /**
     * 获取客户端IP地址
     */
    public static String getClientIp() {
        return CLIENT_IP.get();
    }

    /**
     * 设置用户代理
     */
    public static void setUserAgent(String userAgent) {
        USER_AGENT.set(userAgent);
    }

    /**
     * 获取用户代理
     */
    public static String getUserAgent() {
        return USER_AGENT.get();
    }

    /**
     * 设置设备ID
     */
    public static void setDeviceId(String deviceId) {
        DEVICE_ID.set(deviceId);
    }

    /**
     * 获取设备ID
     */
    public static String getDeviceId() {
        return DEVICE_ID.get();
    }

    /**
     * 设置客户端类型
     */
    public static void setClientType(String clientType) {
        CLIENT_TYPE.set(clientType);
    }

    /**
     * 获取客户端类型
     */
    public static String getClientType() {
        return CLIENT_TYPE.get();
    }

    /**
     * 设置请求ID
     */
    public static void setRequestId(String requestId) {
        REQUEST_ID.set(requestId);
    }

    /**
     * 获取请求ID
     */
    public static String getRequestId() {
        return REQUEST_ID.get();
    }

    /**
     * 设置会话ID
     */
    public static void setSessionId(String sessionId) {
        SESSION_ID.set(sessionId);
    }

    /**
     * 获取会话ID
     */
    public static String getSessionId() {
        return SESSION_ID.get();
    }

    /**
     * 清除所有上下文信息
     */
    public static void clear() {
        USER_ID.remove();
        USERNAME.remove();
        TOKEN_ID.remove();
        CLIENT_IP.remove();
        USER_AGENT.remove();
        DEVICE_ID.remove();
        CLIENT_TYPE.remove();
        REQUEST_ID.remove();
        SESSION_ID.remove();
    }

    /**
     * 检查是否已经设置用户上下文
     */
    public static boolean hasUserContext() {
        return USER_ID.get() != null;
    }

    /**
     * 批量设置上下文信息
     */
    public static void setContext(Long userId, String username, String clientIp, 
                                String userAgent, String deviceId, String clientType) {
        setUserId(userId);
        setUsername(username);
        setClientIp(clientIp);
        setUserAgent(userAgent);
        setDeviceId(deviceId);
        setClientType(clientType);
    }

    /**
     * 获取当前用户信息的完整描述
     */
    public static String getCurrentUserInfo() {
        Long userId = getUserId();
        String username = getUsername();
        String clientIp = getClientIp();
        String clientType = getClientType();
        String deviceId = getDeviceId();
        
        if (userId != null) {
            return String.format("用户ID=%s, 用户名=%s, IP=%s, 客户端=%s, 设备=%s",
                    userId, username, clientIp, clientType, deviceId);
        }
        return "未知用户";
    }

    /**
     * 记录当前上下文信息（用于日志）
     */
    public static void logCurrentContext(String operation) {
        if (log.isDebugEnabled()) {
            log.debug("{} - {}", operation, getCurrentUserInfo());
        }
    }
}