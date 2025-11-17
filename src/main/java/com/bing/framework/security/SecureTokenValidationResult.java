package com.bing.framework.security;

import io.jsonwebtoken.Claims;

/**
 * 安全令牌验证结果
 * 
 * @author zhengbing
 * @date 2025-11-17
 */
public class SecureTokenValidationResult {
    
    private final boolean approved;
    private final String reason;
    private final Claims claims;
    
    private SecureTokenValidationResult(boolean approved, String reason, Claims claims) {
        this.approved = approved;
        this.reason = reason;
        this.claims = claims;
    }
    
    /**
     * 令牌验证通过
     */
    public static SecureTokenValidationResult approved(Claims claims) {
        return new SecureTokenValidationResult(true, null, claims);
    }
    
    /**
     * 令牌验证被拒绝
     */
    public static SecureTokenValidationResult rejected(String reason) {
        return new SecureTokenValidationResult(false, reason, null);
    }
    
    /**
     * 令牌已过期
     */
    public static SecureTokenValidationResult expired(String reason) {
        return new SecureTokenValidationResult(false, reason, null);
    }
    
    /**
     * 令牌是否被批准
     */
    public boolean isApproved() {
        return approved;
    }
    
    /**
     * 拒绝原因
     */
    public String getReason() {
        return reason;
    }
    
    /**
     * 获取Claims
     */
    public Claims getClaims() {
        return claims;
    }
    
    /**
     * 获取用户ID
     */
    public Long getUserId() {
        return claims != null ? Long.valueOf(claims.get("userId").toString()) : null;
    }
    
    /**
     * 获取用户名
     */
    public String getUsername() {
        return claims != null ? claims.get("username").toString() : null;
    }
    
    /**
     * 获取令牌ID
     */
    public String getTokenId() {
        return claims != null ? claims.get("tokenId").toString() : null;
    }
}