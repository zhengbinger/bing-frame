package com.bing.framework.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Bing 框架配置属性类
 * 集中管理 Bing 相关的配置参数
 * 
 * @author zhengbing
 * @date 2025-11-12
 */
@Data
@Component
@ConfigurationProperties(prefix = "bing")
public class BingProperties {
    
    /**
     * 客户端类型特定配置
     */
    private ClientTypes clientTypes = new ClientTypes();
    
    @Data
    public static class ClientTypes {
        
        /**
         * Web 客户端配置
         */
        private ClientTypeConfig web;
        
        /**
         * App 客户端配置
         */
        private ClientTypeConfig app;
        
        /**
         * 微信客户端配置
         */
        private ClientTypeConfig wechat;
        
        /**
         * 小程序客户端配置
         */
        private ClientTypeConfig miniprogram;
        
        /**
         * HarmonyOS 客户端配置
         */
        private ClientTypeConfig harmony;
        
        /**
         * Meta 客户端配置
         */
        private ClientTypeConfig meta;
        
        /**
         * 获取指定客户端类型的配置
         * 
         * @param clientType 客户端类型
         * @return 客户端配置，如果不存在则返回默认配置
         */
        public ClientTypeConfig getClientType(String clientType) {
            switch (clientType.toLowerCase()) {
                case "web": return web;
                case "app": return app;
                case "wechat": return wechat;
                case "miniprogram": return miniprogram;
                case "harmony": return harmony;
                case "meta": return meta;
                default:
                    // 返回默认配置（低风险等级，长过期时间）
                    return new ClientTypeConfig("LOW", 24, 5, false, false);
            }
        }
        
        /**
         * 获取所有客户端类型配置映射
         * 
         * @return 配置映射
         */
        public Map<String, ClientTypeConfig> getAllClientTypes() {
            java.util.HashMap<String, ClientTypeConfig> map = new java.util.HashMap<>();
            map.put("web", web);
            map.put("app", app);
            map.put("wechat", wechat);
            map.put("miniprogram", miniprogram);
            map.put("harmony", harmony);
            map.put("meta", meta);
            return map;
        }
    }
    
    /**
     * 客户端类型配置
     */
    @Data
    public static class ClientTypeConfig {
        
        /** 风险等级 */
        private String riskLevel;
        
        /** 令牌过期时间(小时) */
        private int expirationHours;
        
        /** 最大并发会话数 */
        private int maxConcurrentSessions;
        
        /** 是否需要设备指纹 */
        private boolean requireDeviceFingerprint;
        
        /** 是否需要地理位置验证 */
        private boolean requireGeoVerification;
        
        /**
         * 构造函数
         * 
         * @param riskLevel 风险等级
         * @param expirationHours 过期时间(小时)
         * @param maxConcurrentSessions 最大并发会话数
         * @param requireDeviceFingerprint 是否需要设备指纹
         * @param requireGeoVerification 是否需要地理位置验证
         */
        public ClientTypeConfig(String riskLevel, int expirationHours, int maxConcurrentSessions,
                               boolean requireDeviceFingerprint, boolean requireGeoVerification) {
            this.riskLevel = riskLevel;
            this.expirationHours = expirationHours;
            this.maxConcurrentSessions = maxConcurrentSessions;
            this.requireDeviceFingerprint = requireDeviceFingerprint;
            this.requireGeoVerification = requireGeoVerification;
        }
        
        /**
         * 默认构造函数
         */
        public ClientTypeConfig() {}
        
        /**
         * 判断是否为高风险等级
         * 
         * @return true 如果是 HIGH 风险等级
         */
        public boolean isHighRisk() {
            return "HIGH".equalsIgnoreCase(riskLevel);
        }
        
        /**
         * 判断是否为中等风险等级
         * 
         * @return true 如果是 MEDIUM 风险等级
         */
        public boolean isMediumRisk() {
            return "MEDIUM".equalsIgnoreCase(riskLevel);
        }
        
        /**
         * 判断是否为低风险等级
         * 
         * @return true 如果是 LOW 风险等级
         */
        public boolean isLowRisk() {
            return "LOW".equalsIgnoreCase(riskLevel);
        }
        
        /**
         * 获取风险等级枚举值
         * 
         * @return 风险等级枚举
         */
        public RiskLevel getRiskLevelEnum() {
            try {
                return RiskLevel.valueOf(riskLevel.toUpperCase());
            } catch (IllegalArgumentException e) {
                return RiskLevel.MEDIUM; // 默认返回中等风险
            }
        }
        
        /**
         * 转换为安全级别的分钟数
         * 根据风险等级调整安全超时时间
         * 
         * @return 调整后的超时时间(分钟)
         */
        public int getAdjustedTimeoutMinutes() {
            int baseTimeout = expirationHours * 60;
            if (isHighRisk()) {
                return Math.min(baseTimeout, 60); // 高风险最多1小时
            } else if (isMediumRisk()) {
                return Math.min(baseTimeout, 480); // 中等风险最多8小时
            } else {
                return baseTimeout; // 低风险使用完整时间
            }
        }
    }
    
    /**
     * 风险等级枚举
     */
    public enum RiskLevel {
        /**
         * 低风险
         */
        LOW,
        
        /**
         * 中等风险
         */
        MEDIUM,
        
        /**
         * 高风险
         */
        HIGH
    }
}