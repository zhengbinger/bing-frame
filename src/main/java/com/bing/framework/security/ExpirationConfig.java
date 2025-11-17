package com.bing.framework.security;

import lombok.Builder;
import lombok.Data;

/**
 * 过期时间配置类
 * 包含基于客户端类型和风险级别的动态过期时间配置
 * 
 * @author zhengbing
 * @date 2025-11-17
 */
@Data
@Builder
public class ExpirationConfig {
    
    /**
     * 客户端类型
     */
    private String clientType;
    
    /**
     * 基础过期时间(小时)
     */
    private long baseExpirationHours;
    
    /**
     * 调整后的过期时间(小时)
     */
    private long adjustedExpirationHours;
    
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
     * 计算过期时间(毫秒)
     */
    public long getExpirationTimeMs() {
        return adjustedExpirationHours * 60 * 60 * 1000;
    }
    
    /**
     * 检查是否为高风险客户端
     */
    public boolean isHighRiskClient() {
        return riskLevel == ClientRiskLevel.HIGH;
    }
    
    /**
     * 检查是否为低风险客户端
     */
    public boolean isLowRiskClient() {
        return riskLevel == ClientRiskLevel.LOW;
    }
}