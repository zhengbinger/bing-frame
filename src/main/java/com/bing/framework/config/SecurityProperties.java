package com.bing.framework.config;

import java.util.Arrays;
import java.util.List;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 安全配置属性类
 * 集中管理所有安全相关的配置参数
 * 
 * @author zhengbing
 * @date 2025-11-11
 */
@Data
@Component
@ConfigurationProperties(prefix = "bing.security")
public class SecurityProperties {
    
    /**
     * 密码安全配置
     */
    private Password password = new Password();
    
    /**
     * 会话安全配置
     */
    private Session session = new Session();
    
    /**
     * 请求安全配置
     */
    private Request request = new Request();
    
    /**
     * API签名配置
     */
    private ApiSignature apiSignature = new ApiSignature();
    
    /**
     * 加密配置
     */
    private Encryption encryption = new Encryption();
    
    /**
     * 监控配置
     */
    private Monitoring monitoring = new Monitoring();
    
    @Data
    public static class Password {
        /** 最小长度 */
        private int minLength = 8;
        /** 最大长度 */
        private int maxLength = 64;
        /** 是否必须包含大小写字母 */
        private boolean requireUpperAndLower = true;
        /** 是否必须包含数字 */
        private boolean requireDigit = true;
        /** 是否必须包含特殊字符 */
        private boolean requireSpecialChar = true;
        /** 不允许的密码列表 */
        private List<String> forbiddenPasswords = Arrays.asList(
                "123456", "password", "admin", "root", "bing2025"
        );
        /** 密码历史检查数量 */
        private int historyCheckCount = 5;
    }
    
    @Data
    public static class Session {
        /** 最大并发会话数 */
        private int maxConcurrentSessions = 3;
        /** 会话超时时间(分钟) */
        private int timeoutMinutes = 30;
        /** 会话固定防护 */
        private boolean sessionFixationProtection = true;
        /** 安全会话Cookie */
        private boolean secureCookie = true;
        /** HTTPOnly Cookie */
        private boolean httpOnlyCookie = true;
        /** 同源策略 */
        private boolean sameSiteStrict = true;
    }
    
    @Data
    public static class Request {
        /** 最大请求大小(MB) */
        private int maxRequestSizeMb = 10;
        /** 慢请求阈值(毫秒) */
        private long slowRequestThresholdMs = 5000;
        /** 允许的Content-Type */
        private List<String> allowedContentTypes = Arrays.asList(
                "application/json", "application/xml", "application/x-www-form-urlencoded"
        );
        /** 禁止的头部 */
        private List<String> forbiddenHeaders = Arrays.asList(
                "X-Forwarded-Host", "X-Original-URL", "X-Rewrite-URL"
        );
    }
    
    @Data
    public static class ApiSignature {
        /** 是否启用API签名 */
        private boolean enabled = true;
        /** 签名算法 */
        private String algorithm = "HmacSHA256";
        /** 签名超时时间(分钟) */
        private int timeoutMinutes = 15;
        /** 时间窗口允许误差(秒) */
        private int timeWindowSeconds = 300;
        /** 是否强制HTTPS */
        private boolean requireHttps = true;
    }
    
    @Data
    public static class Encryption {
        /** AES密钥 */
        private String aesKey = "BingFrameworkKey2025!";
        /** 密钥轮换天数 */
        private int keyRotationDays = 90;
        /** 敏感字段加密 */
        private boolean enableFieldEncryption = true;
        /** 加密字段列表 */
        private List<String> encryptedFields = Arrays.asList(
                "password", "phone", "email", "idCard", "bankAccount"
        );
    }
    
    @Data
    public static class Monitoring {
        /** 安全事件监控 */
        private boolean enableSecurityMonitoring = true;
        /** 可疑活动阈值 */
        private int suspiciousActivityThreshold = 10;
        /** 监控保留天数 */
        private int retentionDays = 90;
        /** 告警启用 */
        private boolean enableAlerts = true;
        /** 告警邮箱 */
        private List<String> alertEmails = Arrays.asList("admin@bingframework.com");
        /** 自动封禁IP */
        private boolean enableAutoBlock = true;
        /** 封禁阈值(可疑次数) */
        private int blockThreshold = 20;
        /** 封禁时间(小时) */
        private int blockHours = 24;
    }
}