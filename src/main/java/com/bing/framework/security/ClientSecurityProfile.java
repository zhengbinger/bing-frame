package com.bing.framework.security;

import lombok.Builder;
import lombok.Data;

/**
 * 客户端安全配置档案
 * 定义不同客户端的安全参数和行为
 * 
 * @author zhengbing
 * @date 2025-11-17
 */
@Data
@Builder
public class ClientSecurityProfile {
    
    /**
     * 基础过期时间(小时)
     */
    private long expirationHours;
    
    /**
     * 最大并发会话数
     */
    private int maxConcurrentSessions;
    
    /**
     * 是否需要设备指纹验证
     */
    private boolean requireDeviceFingerprint;
    
    /**
     * 是否需要地理位置验证
     */
    private boolean requireGeoVerification;
    
    /**
     * 风险级别
     */
    private ClientRiskLevel riskLevel;
    
    /**
     * 安全检查频率(分钟)
     */
    private int securityCheckInterval;
    
    /**
     * 是否启用实时监控
     */
    private boolean enableRealtimeMonitoring;
    
    /**
     * 无参数构造器
     */
    public ClientSecurityProfile() {
    }
    
    /**
     * 完整参数构造器
     */
    public ClientSecurityProfile(long expirationHours, int maxConcurrentSessions, 
                               boolean requireDeviceFingerprint, boolean requireGeoVerification,
                               ClientRiskLevel riskLevel, int securityCheckInterval,
                               boolean enableRealtimeMonitoring) {
        this.expirationHours = expirationHours;
        this.maxConcurrentSessions = maxConcurrentSessions;
        this.requireDeviceFingerprint = requireDeviceFingerprint;
        this.requireGeoVerification = requireGeoVerification;
        this.riskLevel = riskLevel;
        this.securityCheckInterval = securityCheckInterval;
        this.enableRealtimeMonitoring = enableRealtimeMonitoring;
    }
}