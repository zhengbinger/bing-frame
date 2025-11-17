package com.bing.framework.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * JWT工具类
 * 提供JWT令牌的生成、解析、验证和刷新功能
 * 
 * @author zhengbing
 * @date 2025-11-05
 */
@Component
public class JwtUtil {

    /**
     * 密钥
     */
    @Value("${jwt.secret:default-secret-key}")
    private String secret;

    /**
     * 过期时间（小时）
     */
    @Value("${jwt.expiration:24}")
    private Integer expiration;
    
    /**
     * 刷新令牌过期时间（小时）
     */
    @Value("${jwt.refresh.expiration:72}")
    private Integer refreshExpiration;

    /**
     * 生成访问令牌
     * 
     * @param userId 用户ID
     * @param username 用户名
     * @return 访问令牌
     */
    public String generateToken(Long userId, String username) {
        // 设置过期时间
        Date expirationDate = new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(expiration));
        
        // 设置Claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("type", "access");
        
        // 生成JWT令牌
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }
    
    /**
     * 生成刷新令牌
     * 
     * @param userId 用户ID
     * @param username 用户名
     * @return 刷新令牌
     */
    public String generateRefreshToken(Long userId, String username) {
        // 设置过期时间
        Date expirationDate = new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(refreshExpiration));
        
        // 设置Claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("type", "refresh");
        
        // 生成JWT刷新令牌
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    /**
     * 解析JWT令牌
     * 
     * @param token JWT令牌
     * @return Claims对象
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 从JWT令牌中获取用户ID
     * 
     * @param token JWT令牌
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return Long.valueOf(claims.get("userId").toString());
    }

    /**
     * 从JWT令牌中获取用户名
     * 
     * @param token JWT令牌
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("username").toString();
    }

    /**
     * 验证访问令牌是否有效
     * 
     * @param token JWT令牌
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = parseToken(token);
            // 检查是否过期
            if (claims.getExpiration().before(new Date())) {
                return false;
            }
            // 检查令牌类型
            return "access".equals(claims.get("type"));
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 验证刷新令牌是否有效
     * 
     * @param refreshToken 刷新令牌
     * @return 是否有效
     */
    public boolean validateRefreshToken(String refreshToken) {
        try {
            Claims claims = parseToken(refreshToken);
            // 检查是否过期
            if (claims.getExpiration().before(new Date())) {
                return false;
            }
            // 检查令牌类型
            return "refresh".equals(claims.get("type"));
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 获取刷新令牌过期时间
     * 
     * @return 刷新令牌过期时间（小时）
     */
    public Integer getRefreshExpiration() {
        return refreshExpiration;
    }
}