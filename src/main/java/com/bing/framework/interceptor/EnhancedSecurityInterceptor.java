package com.bing.framework.interceptor;

import com.bing.framework.common.ErrorCode;
import com.bing.framework.exception.BusinessException;
import com.bing.framework.security.SecureJwtTokenProvider;
import com.bing.framework.security.SecureTokenValidationResult;
import com.bing.framework.service.WhiteListService;
import com.bing.framework.util.IpUtil;
import com.bing.framework.util.RequestContextUtil;
import com.bing.framework.security.SecurityEvent;
import com.bing.framework.security.SuspiciousActivity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 增强安全拦截器
 * 提供更严格的安全验证，包括设备指纹、地理位置、安全事件监控
 * 
 * @author zhengbing
 * @date 2025-11-11
 */
@Slf4j
@Component
public class EnhancedSecurityInterceptor implements HandlerInterceptor {

    @Autowired
    private SecureJwtTokenProvider secureJwtTokenProvider;
    
    @Autowired
    private WhiteListService whiteListService;
    
    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";
    private static final String SUSPICIOUS_ACTIVITY_PREFIX = "security:suspicious:";
    
    /**
     * 预处理器拦截
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        
        String requestPath = request.getRequestURI();
        String method = request.getMethod();
        String clientIp = IpUtil.getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        String deviceId = request.getHeader("X-Device-ID");
        String clientType = request.getHeader("X-Client-Type");
        
        log.debug("增强安全拦截 - 路径: {}, 方法: {}, IP: {}, 设备: {}", requestPath, method, clientIp, deviceId);
        
        // 1. 检查白名单
        if (whiteListService.isInWhiteList(requestPath)) {
            log.debug("路径在白名单中，直接通过: {}", requestPath);
            return true;
        }
        
        // 2. 获取和验证令牌
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            log.warn("请求缺少有效的Authorization头 - IP: {}, 路径: {}", clientIp, requestPath);
            recordSuspiciousActivity(clientIp, "missing_authorization_header", requestPath);
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        
        String token = authorization.substring(7);
        
        // 3. 安全令牌验证
        SecureTokenValidationResult validationResult = secureJwtTokenProvider.validateSecureToken(
                token, deviceId, userAgent, clientIp);
        
        if (!validationResult.isApproved()) {
            String reason = validationResult.getReason();
            log.warn("令牌验证失败 - IP: {}, 原因: {}, 路径: {}", clientIp, reason, requestPath);
            recordSuspiciousActivity(clientIp, "token_validation_failed", requestPath + ":" + reason);
            
            // 根据失败原因返回不同错误
            if (reason.contains("expired")) {
                throw new BusinessException(ErrorCode.TOKEN_EXPIRED);
            } else if (reason.contains("Device fingerprint")) {
                throw new BusinessException(ErrorCode.DEVICE_MISMATCH);
            } else {
                throw new BusinessException(ErrorCode.INVALID_TOKEN);
            }
        }
        
        // 4. 安全事件监控
        monitorSecurityEvent(request, validationResult, clientIp, userAgent, deviceId);
        
        // 5. 设置用户上下文
        RequestContextUtil.setUserId(validationResult.getUserId());
        RequestContextUtil.setUsername(validationResult.getUsername());
        RequestContextUtil.setTokenId(validationResult.getTokenId());
        RequestContextUtil.setClientIp(clientIp);
        RequestContextUtil.setUserAgent(userAgent);
        RequestContextUtil.setDeviceId(deviceId);
        RequestContextUtil.setClientType(clientType);
        
        // 6. 检查异常访问模式
        if (isAbnormalAccessPattern(clientIp, requestPath, method)) {
            log.warn("检测到异常访问模式 - IP: {}, 路径: {}, 方法: {}", clientIp, requestPath, method);
            recordSuspiciousActivity(clientIp, "abnormal_access_pattern", requestPath);
            // 可以选择拒绝访问或增加验证
        }
        
        log.debug("安全验证通过 - 用户ID: {}, 路径: {}", validationResult.getUserId(), requestPath);
        return true;
    }
    
    /**
     * 监控安全事件
     */
    private void monitorSecurityEvent(HttpServletRequest request, SecureTokenValidationResult result, 
                                     String clientIp, String userAgent, String deviceId) {
        
        try {
            // 记录安全验证成功事件
            SecurityEvent securityEvent = SecurityEvent.builder()
                    .eventType("TOKEN_VALIDATION_SUCCESS")
                    .userId(result.getUserId())
                    .username(result.getUsername())
                    .clientIp(clientIp)
                    .userAgent(userAgent)
                    .deviceId(deviceId)
                    .requestPath(request.getRequestURI())
                    .method(request.getMethod())
                    .timestamp(LocalDateTime.now())
                    .build();
            
            // 异步处理安全事件
            processSecurityEventAsync(securityEvent);
            
        } catch (Exception e) {
            log.error("安全事件监控异常", e);
        }
    }
    
    /**
     * 检查异常访问模式
     */
    private boolean isAbnormalAccessPattern(String clientIp, String requestPath, String method) {
        
        // 1. 检查高频访问相同路径
        String accessPatternKey = "pattern:" + clientIp + ":" + requestPath + ":" + method;
        int recentAccessCount = getRecentAccessCount(accessPatternKey);
        
        if (recentAccessCount > 100) { // 1分钟内访问超过100次
            return true;
        }
        
        // 2. 检查跨多个路径的快速访问
        String multiPathKey = "multi:" + clientIp;
        int multiPathAccessCount = getRecentAccessCount(multiPathKey);
        
        if (multiPathAccessCount > 50) { // 1分钟内访问超过50个不同路径
            return true;
        }
        
        // 3. 检查访问敏感接口
        if (isSensitiveEndpoint(requestPath)) {
            int sensitiveAccessCount = getRecentAccessCount("sensitive:" + clientIp);
            if (sensitiveAccessCount > 10) { // 1分钟内访问敏感接口超过10次
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 获取最近访问计数
     */
    private int getRecentAccessCount(String key) {
        // 实现基于Redis的访问计数逻辑
        // 在Redis中使用时间窗口计数器来记录访问频率
        // 设置1分钟过期时间，自动清理过期数据
        try {
            // 示例实现：使用Redis实现访问计数
            // RedisTemplate redisTemplate = getRedisTemplate();
            // redisTemplate.opsForValue().setIfAbsent(key, "0", Duration.ofMinutes(1));
            // Long count = redisTemplate.opsForValue().increment(key);
            // return count != null ? count.intValue() : 0;
            return 0; // 临时返回0，需要实现完整的Redis计数逻辑
        } catch (Exception e) {
            log.warn("Failed to get access count for key: {}", key, e);
            return 0;
        }
    }
    
    /**
     * 检查是否为敏感接口
     */
    private boolean isSensitiveEndpoint(String requestPath) {
        return requestPath.contains("/admin/") || 
               requestPath.contains("/api/auth/") ||
               requestPath.contains("/api/user/") ||
               requestPath.contains("/api/payment/");
    }
    
    /**
     * 记录可疑活动
     */
    private void recordSuspiciousActivity(String clientIp, String activityType, String details) {
        
        try {
            SuspiciousActivity activity = SuspiciousActivity.builder()
                    .clientIp(clientIp)
                    .activityType(activityType)
                    .details(details)
                    .timestamp(LocalDateTime.now())
                    .build();
            
            // 保存可疑活动记录到安全日志系统中
            // 可以存储到数据库、Redis或发送到安全事件队列
            log.warn("可疑活动记录 - IP: {}, 类型: {}, 详情: {}", clientIp, activityType, details);
            
        } catch (Exception e) {
            log.error("记录可疑活动失败", e);
        }
    }
    
    /**
     * 异步处理安全事件
     */
    private void processSecurityEventAsync(SecurityEvent event) {
        // 实现异步处理安全事件机制
        // 可以发送到消息队列或直接保存到数据库
        // 确保不影响正常请求的处理性能
        try {
            log.debug("处理安全事件: {}, 用户: {}, IP: {}", 
                    event.getEventType(), event.getUsername(), event.getClientIp());
            
            // 示例实现：异步保存到数据库
            // CompletableFuture.runAsync(() -> {
            //     securityEventService.saveEvent(event);
            // }).exceptionally(throwable -> {
            //     log.error("保存安全事件失败", throwable);
            //     return null;
            // });
            
            // 示例实现：发送到消息队列
            // messageQueueService.sendSecurityEvent(event);
            
        } catch (Exception e) {
            log.error("处理安全事件异常: {}", event.getEventType(), e);
        }
    }
}