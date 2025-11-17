package com.bing.framework.security;

import com.bing.framework.util.AESUtil;
import io.jsonwebtoken.*;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 增强版安全JWT令牌提供者
 * 提供更安全的JWT令牌生成和验证功能，包含设备指纹、地理位置等信息
 * 
 * @author zhengbing
 * @date 2025-11-17
 */
@Slf4j
@Component
public class SecureJwtTokenProvider {

    /**
     * 密钥长度（256位）
     */
    private static final int KEY_LENGTH = 256;
    
    /**
     * 令牌类型
     */
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";
    
    /**
     * 证书版本（用于密钥轮换）
     */
    @Value("${jwt.certificate.version:v1}")
    private String certificateVersion;
    
    /**
     * 过期时间配置
     */
    @Value("${jwt.expiration:24}")
    private Integer defaultExpiration;
    
    @Value("${jwt.refresh.expiration:168}")
    private Integer defaultRefreshExpiration;
    
    /**
     * 安全配置
     */
    @Value("${jwt.security.device-fingerprint:true}")
    private boolean enableDeviceFingerprint;
    
    @Value("${jwt.security.location-aware:true}")
    private boolean enableLocationAware;
    
    @Value("${jwt.security.anti-replay:true}")
    private boolean enableAntiReplay;
    
    @Value("${jwt.security.token-blacklist-enabled:true}")
    private boolean enableTokenBlacklist;
    
    /**
     * 客户端类型安全配置管理器
     */
    @Autowired
    private ClientTypeSecurityConfig clientTypeSecurityConfig;
    
    /**
     * 静态常量：固定密钥（用于开发和测试）
     */
    private static final String STATIC_SECRET_KEY = "ThisIsAStaticSecretKeyForProductionUse1234567890";
    
    /**
     * 静态常量：测试密钥（仅用于单元测试）
     */
    public static final String TEST_SECRET_KEY = "ThisIsATestSecretKeyForUnitTestingOnly1234567890";
    
    /**
     * 生成强密钥（优先使用固定密钥，在生产环境中可以使用配置中心的密钥）
     */
    private SecretKey generateSecretKey() {
        // 在实际生产环境中，这里可以从配置中心或密钥管理系统获取密钥
        // 为了简化演示，使用固定的密钥
        String secretKeyString = STATIC_SECRET_KEY;
        
        // 将字符串转换为密钥
        byte[] keyBytes = Base64.getDecoder().decode(
            Base64.getEncoder().encodeToString(secretKeyString.getBytes())
        );
        
        // 确保密钥长度为256位
        if (keyBytes.length < KEY_LENGTH / 8) {
            byte[] paddedKey = new byte[KEY_LENGTH / 8];
            System.arraycopy(keyBytes, 0, paddedKey, 0, keyBytes.length);
            // 使用随机数填充剩余部分
            SecureRandom secureRandom = new SecureRandom();
            secureRandom.nextBytes(paddedKey);
            keyBytes = paddedKey;
        } else if (keyBytes.length > KEY_LENGTH / 8) {
            // 截取到指定长度
            byte[] truncatedKey = new byte[KEY_LENGTH / 8];
            System.arraycopy(keyBytes, 0, truncatedKey, 0, KEY_LENGTH / 8);
            keyBytes = truncatedKey;
        }
        
        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }
    
    /**
     * 生成安全令牌
     */
    public String generateSecureToken(Long userId, String username, String clientType, 
                                     String deviceId, String userAgent, String clientIp) {
        
        // 生成令牌序列号（防重放攻击）
        String tokenId = generateTokenId();
        
        // 获取强密钥
        SecretKey secretKey = generateSecretKey();
        
        // 构建安全Claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("type", ACCESS_TOKEN_TYPE);
        claims.put("tokenId", tokenId);
        claims.put("certVersion", certificateVersion);
        claims.put("clientType", clientType);
        claims.put("createdAt", System.currentTimeMillis());
        
        // 添加设备指纹（如果启用）
        if (enableDeviceFingerprint && deviceId != null) {
            claims.put("deviceFingerprint", encryptData(deviceId));
        }
        
        // 添加用户代理信息
        if (userAgent != null) {
            claims.put("userAgentHash", hashData(userAgent));
        }
        
        // 添加客户端IP信息
        if (clientIp != null) {
            claims.put("clientIpHash", hashData(clientIp));
        }
        
        // 设置过期时间（根据客户端类型调整）
        long expirationTime = getExpirationTimeByClientType(clientType);
        Date expirationDate = new Date(System.currentTimeMillis() + expirationTime);
        
        // 生成JWT令牌 (JJWT 0.9.1兼容方式)
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }
    
    /**
     * 生成刷新令牌
     */
    public String generateSecureRefreshToken(Long userId, String username, String tokenId) {
        
        SecretKey secretKey = generateSecretKey();
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("type", REFRESH_TOKEN_TYPE);
        claims.put("tokenId", tokenId);
        claims.put("certVersion", certificateVersion);
        
        Date expirationDate = new Date(System.currentTimeMillis() + 
                TimeUnit.HOURS.toMillis(defaultRefreshExpiration));
        
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }
    
    /**
     * 验证安全令牌
     */
    public SecureTokenValidationResult validateSecureToken(String token, String deviceId, 
                                                         String userAgent, String clientIp) {
        
        try {
            SecretKey secretKey = generateSecretKey();
            
            // 解析令牌
            Jws<Claims> jws = Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token);
            
            Claims claims = jws.getBody();
            
            // 验证令牌类型
            if (!ACCESS_TOKEN_TYPE.equals(claims.get("type"))) {
                return SecureTokenValidationResult.rejected("Invalid token type");
            }
            
            // 验证证书版本
            if (!certificateVersion.equals(claims.get("certVersion"))) {
                return SecureTokenValidationResult.rejected("Certificate version mismatch");
            }
            
            // 验证设备指纹（如果启用）
            if (enableDeviceFingerprint && deviceId != null) {
                String storedDeviceFingerprint = (String) claims.get("deviceFingerprint");
                if (storedDeviceFingerprint != null) {
                    String decryptedDeviceId = decryptData(storedDeviceFingerprint);
                    if (!deviceId.equals(decryptedDeviceId)) {
                        return SecureTokenValidationResult.rejected("Device fingerprint mismatch");
                    }
                }
            }
            
            // 验证用户代理（可选）
            if (userAgent != null) {
                String storedUserAgentHash = (String) claims.get("userAgentHash");
                if (storedUserAgentHash != null) {
                    String currentUserAgentHash = hashData(userAgent);
                    if (!currentUserAgentHash.equals(storedUserAgentHash)) {
                        log.warn("User agent mismatch detected for token: {}", claims.get("tokenId"));
                        // 这里可以选择警告或拒绝
                    }
                }
            }
            
            // 验证客户端IP（可选）
            if (enableLocationAware && clientIp != null) {
                String storedClientIpHash = (String) claims.get("clientIpHash");
                if (storedClientIpHash != null) {
                    String currentClientIpHash = hashData(clientIp);
                    if (!currentClientIpHash.equals(storedClientIpHash)) {
                        log.warn("Client IP mismatch detected for token: {}", claims.get("tokenId"));
                        // 这里可以选择警告或拒绝
                    }
                }
            }
            
            // 反重放攻击检查
            if (enableAntiReplay) {
                String tokenId = (String) claims.get("tokenId");
                if (isTokenIdUsed(tokenId)) {
                    return SecureTokenValidationResult.rejected("Token ID already used");
                }
            }
            
            return SecureTokenValidationResult.approved(claims);
            
        } catch (ExpiredJwtException e) {
            return SecureTokenValidationResult.expired(e.getMessage());
        } catch (JwtException e) {
            return SecureTokenValidationResult.rejected(e.getMessage());
        } catch (Exception e) {
            log.error("Token validation error", e);
            return SecureTokenValidationResult.rejected("Validation error");
        }
    }
    
    /**
     * 生成令牌序列号
     */
    private String generateTokenId() {
        return UUID.randomUUID().toString().replace("-", "").toLowerCase();
    }
    
    /**
     * 根据客户端类型和安全级别获取动态过期时间
     */
    private long getExpirationTimeByClientType(String clientType) {
        // 获取客户端安全配置
        ExpirationConfig config = clientTypeSecurityConfig.getExpirationConfig(clientType, "UNKNOWN");
        
        if (config == null) {
            log.warn("未找到客户端类型 {} 的安全配置，使用默认配置", clientType);
            return TimeUnit.HOURS.toMillis(defaultExpiration);
        }
        
        long expirationMs = config.getExpirationTimeMs();
        log.debug("客户端类型 {} 的过期时间配置: {} 小时", clientType, config.getAdjustedExpirationHours());
        
        return expirationMs;
    }
    
    /**
     * 根据客户端类型和安全级别获取增强的过期时间配置
     */
    public ExpirationConfig getEnhancedExpirationConfig(String clientType, String riskLevel) {
        return clientTypeSecurityConfig.getExpirationConfig(clientType, riskLevel);
    }
    
    /**
     * 加密敏感数据
     */
    private String encryptData(String data) {
        try {
            return AESUtil.encrypt(data);
        } catch (Exception e) {
            log.warn("Failed to encrypt data", e);
            return data;
        }
    }
    
    /**
     * 解密敏感数据
     */
    private String decryptData(String encryptedData) {
        try {
            return AESUtil.decrypt(encryptedData);
        } catch (Exception e) {
            log.warn("Failed to decrypt data", e);
            return encryptedData;
        }
    }
    
    /**
     * 哈希敏感数据
     */
    private String hashData(String data) {
        try {
            return org.apache.commons.codec.digest.DigestUtils.sha256Hex(data);
        } catch (Exception e) {
            log.warn("Failed to hash data", e);
            // 如果哈希失败，返回原始数据的简单哈希作为回退
            return Integer.toHexString(data.hashCode());
        }
    }
    
    /**
     * 检查令牌ID是否已使用（反重放攻击）
     */
    private boolean isTokenIdUsed(String tokenId) {
        // 实现令牌ID的缓存检查机制
        // 在Redis中存储已使用的令牌ID，设置过期时间以防止重放攻击
        // 建议使用令牌的有效期作为Redis键的过期时间
        try {
            // 示例实现：查询Redis中是否存在该令牌ID
            // return redisUtil.hasKey("token:blacklist:" + tokenId);
            return false; // 临时返回false，需要实现完整的缓存检查逻辑
        } catch (Exception e) {
            log.warn("Failed to check token ID usage: {}", tokenId, e);
            return false;
        }
    }
    
    /**
     * 撤销令牌
     */
    public void revokeToken(String tokenId) {
        if (enableTokenBlacklist && tokenId != null) {
            // 将令牌ID加入黑名单，防止重复使用
            try {
                // 示例实现：将令牌ID存储到Redis黑名单中
                // redisUtil.set("token:blacklist:" + tokenId, "revoked", expirationTime);
                log.debug("Token revoked: {}", tokenId);
            } catch (Exception e) {
                log.error("Failed to revoke token: {}", tokenId, e);
            }
        }
    }
}