package com.bing.framework.util;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import lombok.extern.slf4j.Slf4j;

import com.bing.framework.entity.AuditLog;
import com.bing.framework.service.AuditLogService;
import com.bing.framework.util.AuditLogUserCache.CachedUserInfo;
import java.util.Map;

/**
 * 审计日志工具类
 * 提供手动记录审计日志的便捷方法，支持异步记录到数据库和自定义Appender
 * 适用于需要手动记录重要操作的审计场景，如关键业务操作、系统配置变更等
 * 
 * @author zhengbing
 * @date 2025-11-05
 */
@Component
@Slf4j
public class AuditLogUtil {

    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT_LOG");
    private static AuditLogUtil instance;
    
    private final AuditLogService auditLogService;
    private final AuditLogUserCache userCache;
    
    @Autowired
    public AuditLogUtil(AuditLogService auditLogService, AuditLogUserCache userCache) {
        this.auditLogService = auditLogService;
        this.userCache = userCache;
        // 初始化静态实例，支持静态方法调用
        AuditLogUtil.instance = this;
    }
    
    /**
     * 记录审计日志
     * 
     * @param module 模块名
     * @param operationType 操作类型
     * @param description 操作描述
     * @param requestParams 请求参数
     * @param result 操作结果
     */
    public static void log(String module, String operationType, String description, String requestParams, String result) {
        if (instance == null) {
            throw new IllegalStateException("AuditLogUtil not initialized. Check if Spring context is running.");
        }
        
        AuditLog auditLog = instance.createAuditLog(module, operationType, description, requestParams, result, null);
        instance.recordLog(auditLog, result, null);
    }
    
    /**
     * 记录成功操作的审计日志
     * 
     * @param module 模块名
     * @param operationType 操作类型
     * @param description 操作描述
     * @param requestParams 请求参数
     */
    public static void logSuccess(String module, String operationType, String description, String requestParams) {
        log(module, operationType, description, requestParams, "成功");
    }
    
    /**
     * 记录失败操作的审计日志
     * 
     * @param module 模块名
     * @param operationType 操作类型
     * @param description 操作描述
     * @param requestParams 请求参数
     * @param errorMessage 错误信息
     */
    public static void logFailure(String module, String operationType, String description, String requestParams, String errorMessage) {
        if (instance == null) {
            throw new IllegalStateException("AuditLogUtil not initialized. Check if Spring context is running.");
        }
        
        AuditLog auditLog = instance.createAuditLog(module, operationType, description, requestParams, "失败", errorMessage);
        instance.recordLog(auditLog, "失败", errorMessage);
    }
    
    /**
     * 创建审计日志对象
     * 
     * @param module 模块名
     * @param operationType 操作类型
     * @param description 操作描述
     * @param requestParams 请求参数
     * @param result 操作结果
     * @param errorMessage 错误信息
     * @return 审计日志对象
     */
    protected AuditLog createAuditLog(String module, String operationType, String description, 
                                     String requestParams, String result, String errorMessage) {
        AuditLog auditLog = new AuditLog();
        auditLog.setOperationTime(new Date());
        auditLog.setIpAddress(getCurrentIp());
        auditLog.setModule(module);
        auditLog.setOperationType(operationType);
        auditLog.setDescription(description);
        auditLog.setRequestParams(requestParams);
        auditLog.setResult(result);
        
        if (errorMessage != null) {
            auditLog.setErrorMessage(errorMessage);
        }
        
        // 获取用户信息（优化：使用缓存机制）
        UserInfo currentUserInfo = getCurrentUserInfo();
        auditLog.setUserId(currentUserInfo.getUserId());
        auditLog.setUsername(currentUserInfo.getUsername());
        
        return auditLog;
    }
    
    /**
     * 记录日志到数据库和日志系统
     * 
     * @param auditLog 审计日志对象
     * @param result 操作结果
     * @param errorMessage 错误信息
     */
    protected void recordLog(AuditLog auditLog, String result, String errorMessage) {
        // 异步记录审计日志
        auditLogService.recordAuditLogAsync(auditLog);
        
        // 同时记录到自定义Appender
        StringBuilder sb = new StringBuilder();
        sb.append("userId:").append(auditLog.getUserId() == null ? "" : auditLog.getUserId()).append(",");
        sb.append("username:").append(auditLog.getUsername()).append(",");
        sb.append("ipAddress:").append(auditLog.getIpAddress()).append(",");
        sb.append("module:").append(auditLog.getModule()).append(",");
        sb.append("operationType:").append(auditLog.getOperationType()).append(",");
        sb.append("description:").append(auditLog.getDescription()).append(",");
        sb.append("requestParams:").append(auditLog.getRequestParams()).append(",");
        sb.append("result:").append(result);
        
        if (errorMessage != null) {
            // 对于错误日志，将错误信息直接添加到日志消息中，而不是作为异常对象
            sb.append(",errorMessage:").append(errorMessage);
            auditLogger.info(sb.toString());
        } else {
            auditLogger.info(sb.toString());
        }
    }
    
    /**
     * 获取当前请求的IP地址
     * 
     * @return IP地址
     */
    private static String getCurrentIp() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String ip = request.getHeader("x-forwarded-for");
                if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getHeader("Proxy-Client-IP");
                }
                if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getHeader("WL-Proxy-Client-IP");
                }
                if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getRemoteAddr();
                }
                if (ip != null && ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        } catch (Exception e) {
            // 忽略异常
        }
        return "unknown";
    }
    
    /**
     * 获取当前用户信息（使用缓存优化）
     * 
     * @return 用户信息对象
     */
    private UserInfo getCurrentUserInfo() {
        try {
            // 尝试从请求头获取用户ID
            Long userId = getUserIdFromRequest();
            if (userId != null) {
                // 使用缓存获取用户信息
                CachedUserInfo cachedUserInfo = userCache.getUserInfo(userId);
                if (cachedUserInfo != null) {
                    return new UserInfo(cachedUserInfo.getUserId(), cachedUserInfo.getUsername(), 
                                      cachedUserInfo.getDisplayName(), cachedUserInfo.getEmail());
                }
            }
            
            // 缓存未命中或无用户ID，返回默认用户信息
            return new UserInfo(null, "anonymous", "匿名用户", "anonymous@example.com");
            
        } catch (Exception e) {
            log.warn("获取用户信息失败，返回默认用户", e);
            return new UserInfo(null, "anonymous", "匿名用户", "anonymous@example.com");
        }
    }
    
    /**
     * 从请求中获取用户ID
     * 
     * @return 用户ID
     */
    private Long getUserIdFromRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                
                // 尝试从各种可能的头信息获取用户ID
                String userIdStr = request.getHeader("X-User-Id");
                if (userIdStr == null || userIdStr.isEmpty()) {
                    userIdStr = request.getHeader("User-Id");
                }
                if (userIdStr == null || userIdStr.isEmpty()) {
                    userIdStr = request.getHeader("userId");
                }
                
                if (userIdStr != null && !userIdStr.isEmpty()) {
                    try {
                        return Long.valueOf(userIdStr);
                    } catch (NumberFormatException e) {
                        log.warn("无效的用户ID格式: {}", userIdStr);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("解析用户ID失败", e);
        }
        return null;
    }
    
    /**
     * 用户信息数据类
     */
    private static class UserInfo {
        private final Long userId;
        private final String username;
        private final String displayName;
        private final String email;
        
        public UserInfo(Long userId, String username, String displayName, String email) {
            this.userId = userId;
            this.username = username;
            this.displayName = displayName;
            this.email = email;
        }
        
        public Long getUserId() { return userId; }
        public String getUsername() { return username; }
        public String getDisplayName() { return displayName; }
        public String getEmail() { return email; }
    }
    
    /**
     * 获取当前用户名（兼容性方法）
     * 
     * @return 用户名
     */
    private static String getCurrentUsername() {
        if (instance != null) {
            return instance.getCurrentUserInfo().getUsername();
        }
        return "anonymous";
    }
    
    /**
     * 设置实例（用于测试）
     * 
     * @param instance 审计日志工具实例
     */
    static void setInstance(AuditLogUtil instance) {
        AuditLogUtil.instance = instance;
    }
    
    /**
     * 重置实例（用于测试）
     */
    static void resetInstance() {
        AuditLogUtil.instance = null;
    }
}