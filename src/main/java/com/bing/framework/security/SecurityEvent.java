package com.bing.framework.security;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 安全事件记录类
 * 用于记录系统中的安全相关事件
 * 
 * @author zhengbing
 * @date 2025-11-17
 */
@Data
@Builder
public class SecurityEvent {
    
    /**
     * 事件类型
     */
    private String eventType;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 客户端IP
     */
    private String clientIp;
    
    /**
     * 用户代理
     */
    private String userAgent;
    
    /**
     * 设备ID
     */
    private String deviceId;
    
    /**
     * 请求路径
     */
    private String requestPath;
    
    /**
     * HTTP方法
     */
    private String method;
    
    /**
     * 事件发生时间
     */
    private LocalDateTime timestamp;
    
    /**
     * 额外信息
     */
    private String additionalInfo;
}