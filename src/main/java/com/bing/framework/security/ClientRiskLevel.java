package com.bing.framework.security;

/**
 * 客户端风险级别枚举
 * 定义不同客户端的安全风险等级
 * 
 * @author zhengbing
 * @date 2025-11-17
 */
public enum ClientRiskLevel {
    /**
     * 严重风险级别
     * 需要最紧急的安全措施
     */
    CRITICAL("严重风险", 0),
    
    /**
     * 高风险级别
     * 需要最严格的安全措施
     */
    HIGH("高风险", 1),
    
    /**
     * 中等风险级别
     * 需要标准安全措施
     */
    MEDIUM("中等风险", 2),
    
    /**
     * 低风险级别
     * 可以放宽某些安全限制
     */
    LOW("低风险", 3);
    
    private final String description;
    private final int level;
    
    ClientRiskLevel(String description, int level) {
        this.description = description;
        this.level = level;
    }
    
    public String getDescription() {
        return description;
    }
    
    public int getLevel() {
        return level;
    }
    
    /**
     * 根据等级数字获取风险级别
     */
    public static ClientRiskLevel fromLevel(int level) {
        for (ClientRiskLevel riskLevel : values()) {
            if (riskLevel.getLevel() == level) {
                return riskLevel;
            }
        }
        return HIGH; // 默认为高风险
    }
}