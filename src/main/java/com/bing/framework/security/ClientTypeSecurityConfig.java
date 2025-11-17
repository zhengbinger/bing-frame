package com.bing.framework.security;

import com.bing.framework.config.SecurityProperties;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 客户端类型安全配置管理器
 * 根据不同客户端类型的安全级别动态调整令牌参数
 * 
 * @author zhengbing
 * @date 2025-11-17
 */
@Data
@Slf4j
@Component
public class ClientTypeSecurityConfig {
    
    @Autowired(required = false)
    private SecurityProperties securityProperties;
    
    /**
     * 无参数构造器
     */
    public ClientTypeSecurityConfig() {
    }
    
    /**
     * 客户端类型安全级别映射
     */
    private static final Map<String, ClientSecurityProfile> SECURITY_PROFILES = new HashMap<>();
    
    static {
        SECURITY_PROFILES.put("WEB", ClientSecurityProfile.builder()
                .expirationHours(2)
                .maxConcurrentSessions(2)
                .requireDeviceFingerprint(true)
                .requireGeoVerification(false)
                .riskLevel(ClientRiskLevel.HIGH)
                .build());
                
        SECURITY_PROFILES.put("APP", ClientSecurityProfile.builder()
                .expirationHours(24)
                .maxConcurrentSessions(3)
                .requireDeviceFingerprint(true)
                .requireGeoVerification(true)
                .riskLevel(ClientRiskLevel.MEDIUM)
                .build());
                
        SECURITY_PROFILES.put("WECHAT", ClientSecurityProfile.builder()
                .expirationHours(168) // 7天
                .maxConcurrentSessions(5)
                .requireDeviceFingerprint(true)
                .requireGeoVerification(false)
                .riskLevel(ClientRiskLevel.MEDIUM)
                .build());
                
        SECURITY_PROFILES.put("MINIPROGRAM", ClientSecurityProfile.builder()
                .expirationHours(720) // 30天
                .maxConcurrentSessions(10)
                .requireDeviceFingerprint(false)
                .requireGeoVerification(false)
                .riskLevel(ClientRiskLevel.LOW)
                .build());
                
        SECURITY_PROFILES.put("HARMONY", ClientSecurityProfile.builder()
                .expirationHours(168) // 7天
                .maxConcurrentSessions(3)
                .requireDeviceFingerprint(true)
                .requireGeoVerification(true)
                .riskLevel(ClientRiskLevel.MEDIUM)
                .build());
                
        SECURITY_PROFILES.put("META", ClientSecurityProfile.builder()
                .expirationHours(12)
                .maxConcurrentSessions(1)
                .requireDeviceFingerprint(true)
                .requireGeoVerification(true)
                .riskLevel(ClientRiskLevel.HIGH)
                .build());
    }
    
    /**
     * 获取增强的过期时间配置
     */
    public ExpirationConfig getExpirationConfig(String clientType, String riskLevel) {
        ClientSecurityProfile profile = getSecurityProfile(clientType);
        
        // 根据风险级别调整过期时间
        long baseExpiration = profile.getExpirationHours();
        long adjustedExpiration = adjustExpirationByRisk(baseExpiration, riskLevel, clientType);
        
        return ExpirationConfig.builder()
                .clientType(clientType)
                .baseExpirationHours(baseExpiration)
                .adjustedExpirationHours(adjustedExpiration)
                .maxConcurrentSessions(profile.getMaxConcurrentSessions())
                .requireDeviceFingerprint(profile.isRequireDeviceFingerprint())
                .requireGeoVerification(profile.isRequireGeoVerification())
                .riskLevel(profile.getRiskLevel())
                .build();
    }
    
    /**
     * 根据风险级别调整过期时间
     */
    private long adjustExpirationByRisk(long baseHours, String riskLevel, String clientType) {
        switch (riskLevel != null ? riskLevel.toUpperCase() : "UNKNOWN") {
            case "HIGH":
                return Math.max(1, baseHours / 4); // 高风险缩短至1/4
            case "MEDIUM":
                return baseHours; // 中风险保持不变
            case "LOW":
                return baseHours * 2; // 低风险可延长至2倍
            case "UNKNOWN":
            default:
                log.warn("未知风险级别，对客户端类型 {} 使用保守配置", clientType);
                return Math.max(1, baseHours / 2); // 未知风险缩短至1/2
        }
    }
    
    /**
     * 验证客户端类型是否安全
     */
    public boolean isSecureClientType(String clientType) {
        if (clientType == null || clientType.trim().isEmpty()) {
            return false;
        }
        
        ClientSecurityProfile profile = SECURITY_PROFILES.get(clientType.toUpperCase());
        return profile != null && profile.getRiskLevel() != ClientRiskLevel.HIGH;
    }
    
    /**
     * 获取安全级别
     */
    public ClientRiskLevel getSecurityLevel(String clientType) {
        ClientSecurityProfile profile = getSecurityProfile(clientType);
        return profile != null ? profile.getRiskLevel() : ClientRiskLevel.HIGH;
    }
    
    /**
     * 获取安全配置档案
     */
    private ClientSecurityProfile getSecurityProfile(String clientType) {
        if (clientType == null) {
            return SECURITY_PROFILES.get("WEB"); // 默认使用WEB配置
        }
        
        return SECURITY_PROFILES.get(clientType.toUpperCase());
    }
    
    /**
     * 检查是否需要额外验证
     */
    public boolean requireAdditionalVerification(String clientType, String requestContext) {
        ClientSecurityProfile profile = getSecurityProfile(clientType);
        if (profile == null) {
            return true; // 未知客户端类型需要额外验证
        }
        
        // 基于请求上下文判断
        if ("admin".equals(requestContext) || "payment".equals(requestContext)) {
            return true; // 敏感操作需要额外验证
        }
        
        return profile.getRiskLevel() == ClientRiskLevel.HIGH;
    }
}