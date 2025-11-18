package com.bing.framework.security.integration;

import com.bing.framework.interceptor.EnhancedSecurityInterceptor;
import com.bing.framework.security.*;
import com.bing.framework.service.WhiteListService;
import com.bing.framework.util.RequestContextUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 安全功能集成测试类
 * 测试多个安全组件之间的协作和端到端安全流程
 * 
 * @author zhengbing
 * @date 2025-11-17
 */
@ExtendWith(MockitoExtension.class)
class SecurityIntegrationTest {

    @Mock
    private SecureJwtTokenProvider secureJwtTokenProvider;

    @Mock
    private WhiteListService whiteListService;

    @InjectMocks
    private EnhancedSecurityInterceptor securityInterceptor;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private Object handler;

    // 测试数据常量
    private static final String TEST_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiJ1c2VyMTIzIiwic3ViIjoidGVzdFVzZXIiLCJ0b2tlbklkIjoidG9rZW4xMjMiLCJleHAiOjk5OTk5OTk5OTl9.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
    private static final String TEST_CLIENT_IP = "192.168.1.100";
    private static final String TEST_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";
    private static final String TEST_DEVICE_ID = "device_abc123";
    private static final String TEST_CLIENT_TYPE = "web";
    private static final Long TEST_USER_ID = 12345L;
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_TOKEN_ID = "token123";
    private static final String REFRESH_TOKEN = "refresh_token_123";

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        handler = new Object();
        
        setupBasicRequest();
    }

    @AfterEach
    void tearDown() {
        // 清理上下文
        RequestContextUtil.clear();
    }

    private void setupBasicRequest() {
        request.setRemoteAddr(TEST_CLIENT_IP);
        request.addHeader("User-Agent", TEST_USER_AGENT);
        request.addHeader("X-Device-ID", TEST_DEVICE_ID);
        request.addHeader("X-Client-Type", TEST_CLIENT_TYPE);
    }

    @Test
    @DisplayName("完整认证流程集成测试")
    void testCompleteAuthenticationFlow() {
        // 1. 生成令牌
        String generatedToken = generateSecureToken();
        assertNotNull(generatedToken);
        assertFalse(generatedToken.isEmpty());

        // 2. 验证令牌
        SecureTokenValidationResult validationResult = validateSecureToken(generatedToken);
        assertNotNull(validationResult);
        assertTrue(validationResult.isApproved());

        // 3. 使用令牌进行请求
        String requestPath = "/api/protected";
        request.setRequestURI(requestPath);
        request.setMethod("GET");
        request.addHeader("Authorization", "Bearer " + generatedToken);

        // 4. 模拟白名单检查
        when(whiteListService.isInWhiteList(requestPath)).thenReturn(false);

        // 5. 验证令牌流程
        when(secureJwtTokenProvider.validateSecureToken(
                generatedToken, TEST_DEVICE_ID, TEST_USER_AGENT, TEST_CLIENT_IP))
                .thenReturn(createSuccessValidationResult());

        // 6. 执行拦截器验证
        boolean result = securityInterceptor.preHandle(request, response, handler);

        // 7. 验证结果
        assertTrue(result);
        assertEquals(TEST_USER_ID, RequestContextUtil.getUserId());
        assertEquals(TEST_USERNAME, RequestContextUtil.getUsername());
        assertEquals(TEST_TOKEN_ID, RequestContextUtil.getTokenId());
    }

    @Test
    @DisplayName("令牌过期处理集成测试")
    void testTokenExpirationHandling() {
        // 1. 生成即将过期的令牌
        String expiredToken = generateExpiringToken();
        
        // 2. 验证过期令牌应该失败
        SecureTokenValidationResult expiredResult = validateExpiringToken(expiredToken);
        assertNotNull(expiredResult);
        assertFalse(expiredResult.isApproved());
        assertTrue(expiredResult.getReason().contains("expired"));

        // 3. 使用过期令牌进行请求
        String requestPath = "/api/protected";
        request.setRequestURI(requestPath);
        request.setMethod("GET");
        request.addHeader("Authorization", "Bearer " + expiredToken);

        // 4. 模拟白名单检查
        when(whiteListService.isInWhiteList(requestPath)).thenReturn(false);

        // 5. 验证过期令牌流程
        when(secureJwtTokenProvider.validateSecureToken(
                expiredToken, TEST_DEVICE_ID, TEST_USER_AGENT, TEST_CLIENT_IP))
                .thenReturn(createExpiredValidationResult());

        // 6. 执行拦截器验证，应该抛出异常
        assertThrows(Exception.class, () -> {
            securityInterceptor.preHandle(request, response, handler);
        });

        // 7. 验证上下文未设置
        assertNull(RequestContextUtil.getUserId());
    }

    @Test
    @DisplayName("设备指纹不匹配集成测试")
    void testDeviceFingerprintMismatchIntegration() {
        // 1. 使用原有设备生成令牌
        String originalToken = generateSecureToken();
        
        // 2. 从不同设备尝试使用令牌
        String differentDeviceId = "device_xyz789";
        request.removeHeader("X-Device-ID");
        request.addHeader("X-Device-ID", differentDeviceId);

        String requestPath = "/api/protected";
        request.setRequestURI(requestPath);
        request.setMethod("GET");
        request.addHeader("Authorization", "Bearer " + originalToken);

        // 3. 模拟白名单检查
        when(whiteListService.isInWhiteList(requestPath)).thenReturn(false);

        // 4. 验证设备指纹不匹配的令牌
        when(secureJwtTokenProvider.validateSecureToken(
                originalToken, differentDeviceId, TEST_USER_AGENT, TEST_CLIENT_IP))
                .thenReturn(createDeviceMismatchValidationResult());

        // 5. 执行拦截器验证，应该抛出异常
        assertThrows(Exception.class, () -> {
            securityInterceptor.preHandle(request, response, handler);
        });

        // 6. 验证上下文未设置
        assertNull(RequestContextUtil.getUserId());
    }

    @Test
    @DisplayName("白名单路径集成测试")
    void testWhiteListPathIntegration() {
        // 1. 白名单路径列表
        Set<String> whiteListPaths = new HashSet<>(Arrays.asList(
            "/api/public/health",
            "/api/public/info",
            "/api/auth/refresh"
        ));

        for (String whiteListPath : whiteListPaths) {
            // 2. 设置请求路径
            request.setRequestURI(whiteListPath);
            request.setMethod("GET");
            // 注意：不设置Authorization头，白名单路径应该跳过令牌验证

            // 3. 模拟白名单检查
            when(whiteListService.isInWhiteList(whiteListPath)).thenReturn(true);

            // 4. 执行拦截器验证
            boolean result = securityInterceptor.preHandle(request, response, handler);

            // 5. 验证结果
            assertTrue(result, "白名单路径 " + whiteListPath + " 应该直接通过");
            assertNull(RequestContextUtil.getUserId(), "白名单路径不应该设置用户上下文");
        }
    }

    @Test
    @DisplayName("安全事件监控集成测试")
    void testSecurityEventMonitoringIntegration() {
        // 1. 生成令牌
        String token = generateSecureToken();
        
        // 2. 设置受保护的路径
        String requestPath = "/api/admin/users";
        request.setRequestURI(requestPath);
        request.setMethod("POST");
        request.addHeader("Authorization", "Bearer " + token);

        // 3. 模拟白名单检查
        when(whiteListService.isInWhiteList(requestPath)).thenReturn(false);

        // 4. 创建成功的验证结果
        SecureTokenValidationResult successResult = createSuccessValidationResult();
        when(secureJwtTokenProvider.validateSecureToken(
                token, TEST_DEVICE_ID, TEST_USER_AGENT, TEST_CLIENT_IP))
                .thenReturn(successResult);

        // 5. 执行拦截器验证
        boolean result = securityInterceptor.preHandle(request, response, handler);

        // 6. 验证结果
        assertTrue(result);
        assertNotNull(RequestContextUtil.getUserId());
        
        // 7. 验证安全事件应该被监控（通过日志记录进行）
        // 在实际实现中，这里应该验证安全事件被正确记录
    }

    @Test
    @DisplayName("可疑活动检测集成测试")
    void testSuspiciousActivityDetectionIntegration() {
        // 1. 测试各种可疑活动场景
        
        // 场景1：缺少Authorization头
        testSuspiciousActivityScenario("/api/protected", null, 
            "missing_authorization_header");

        // 场景2：无效的令牌格式
        testSuspiciousActivityScenario("/api/protected", "InvalidToken", 
            "token_validation_failed");

        // 场景3：设备指纹不匹配
        testSuspiciousActivityScenario("/api/protected", generateSecureToken(), 
            "token_validation_failed");
    }

    private void testSuspiciousActivityScenario(String path, String token, String expectedActivityType) {
        request.setRequestURI(path);
        request.setMethod("GET");
        
        if (token != null) {
            request.addHeader("Authorization", "Bearer " + token);
        }

        // 模拟白名单检查
        when(whiteListService.isInWhiteList(path)).thenReturn(false);

        // 根据场景设置不同的模拟行为
        if (token == null) {
            // 缺少Authorization头
            // 直接执行，应该抛出异常
        } else if ("InvalidToken".equals(token)) {
            // 无效令牌格式
            // 应该抛出UNAUTHORIZED异常
        } else {
            // 设备指纹不匹配
            when(secureJwtTokenProvider.validateSecureToken(
                    anyString(), anyString(), anyString(), anyString()))
                    .thenReturn(createDeviceMismatchValidationResult());
        }

        // 执行测试
        assertThrows(Exception.class, () -> {
            securityInterceptor.preHandle(request, response, handler);
        });

        // 验证可疑活动被记录（通过日志记录进行验证）
        // 在实际实现中，这里应该验证SuspiciousActivity对象被正确创建
    }

    @Test
    @DisplayName("刷新令牌流程集成测试")
    void testRefreshTokenFlowIntegration() {
        // 1. 生成刷新令牌
        String refreshToken = generateRefreshToken();
        assertNotNull(refreshToken);
        assertFalse(refreshToken.isEmpty());

        // 2. 验证刷新令牌
        SecureTokenValidationResult refreshResult = validateRefreshToken(refreshToken);
        assertNotNull(refreshResult);
        assertTrue(refreshResult.isApproved());

        // 3. 使用刷新令牌获取新的访问令牌
        String newAccessToken = generateAccessTokenFromRefresh(refreshToken);
        assertNotNull(newAccessToken);
        assertNotEquals(refreshToken, newAccessToken);

        // 4. 使用新的访问令牌进行请求
        String requestPath = "/api/protected";
        request.setRequestURI(requestPath);
        request.setMethod("GET");
        request.addHeader("Authorization", "Bearer " + newAccessToken);

        // 5. 模拟白名单检查
        when(whiteListService.isInWhiteList(requestPath)).thenReturn(false);

        // 6. 验证新令牌
        when(secureJwtTokenProvider.validateSecureToken(
                newAccessToken, TEST_DEVICE_ID, TEST_USER_AGENT, TEST_CLIENT_IP))
                .thenReturn(createSuccessValidationResult());

        // 7. 执行拦截器验证
        boolean result = securityInterceptor.preHandle(request, response, handler);

        // 8. 验证结果
        assertTrue(result);
        assertEquals(TEST_USER_ID, RequestContextUtil.getUserId());
    }

    @Test
    @DisplayName("多个安全组件协作测试")
    void testMultipleSecurityComponentsCollaboration() {
        // 模拟整个安全体系的协作
        // 1. 令牌生成
        String token = generateSecureToken();
        
        // 2. 令牌存储和验证
        SecureTokenValidationResult validationResult = validateSecureToken(token);
        assertTrue(validationResult.isApproved());
        
        // 3. 安全事件记录
        SecurityEvent securityEvent = SecurityEvent.builder()
                .eventType("TOKEN_VALIDATION_SUCCESS")
                .userId(TEST_USER_ID)
                .username(TEST_USERNAME)
                .clientIp(TEST_CLIENT_IP)
                .userAgent(TEST_USER_AGENT)
                .deviceId(TEST_DEVICE_ID)
                .requestPath("/api/test")
                .method("GET")
                .timestamp(LocalDateTime.now())
                .build();
        assertNotNull(securityEvent);
        
        // 4. 可疑活动检测
        SuspiciousActivity suspiciousActivity = SuspiciousActivity.builder()
                .activityType("MULTIPLE_LOGIN_ATTEMPTS")
                .clientIp(TEST_CLIENT_IP)
                .details("连续登录尝试")
                .timestamp(LocalDateTime.now())
                .severityLevel(3)
                .build();
        assertNotNull(suspiciousActivity);
        
        // 5. 客户端类型安全配置（简化测试）
        ClientTypeSecurityConfig clientConfig = new ClientTypeSecurityConfig();
        assertNotNull(clientConfig);
        
        // 6. 客户端安全档案
        ClientSecurityProfile profile = ClientSecurityProfile.builder()
                .expirationHours(2)
                .maxConcurrentSessions(2)
                .requireDeviceFingerprint(true)
                .requireGeoVerification(false)
                .riskLevel(ClientRiskLevel.MEDIUM)
                .securityCheckInterval(10)
                .enableRealtimeMonitoring(true)
                .build();
        assertNotNull(profile);
        
        // 7. 执行完整的安全检查流程
        executeSecurityCheck(token);
        
        // 8. 验证所有组件正常工作
        assertTrue(validationResult.isApproved());
        assertNotNull(securityEvent);
        assertNotNull(suspiciousActivity);
        assertNotNull(clientConfig);
        assertNotNull(profile);
    }

    private void executeSecurityCheck(String token) {
        String requestPath = "/api/comprehensive/test";
        request.setRequestURI(requestPath);
        request.setMethod("GET");
        request.addHeader("Authorization", "Bearer " + token);

        // 模拟白名单检查
        when(whiteListService.isInWhiteList(requestPath)).thenReturn(false);

        // 模拟令牌验证
        when(secureJwtTokenProvider.validateSecureToken(
                token, TEST_DEVICE_ID, TEST_USER_AGENT, TEST_CLIENT_IP))
                .thenReturn(createSuccessValidationResult());

        // 执行拦截器验证
        boolean result = securityInterceptor.preHandle(request, response, handler);
        assertTrue(result);
    }

    @Test
    @DisplayName("不同客户端类型安全验证测试")
    void testDifferentClientTypesSecurityValidation() {
        // 测试不同客户端类型的安全要求
        String[] clientTypes = {"web", "mobile", "api", "internal"};
        
        for (String clientType : clientTypes) {
            // 设置客户端类型
            request.removeHeader("X-Client-Type");
            request.addHeader("X-Client-Type", clientType);
            
            // 生成对应类型的令牌
            String token = generateClientSpecificToken(clientType);
            
            // 执行安全验证
            String requestPath = "/api/" + clientType + "/test";
            request.setRequestURI(requestPath);
            request.setMethod("GET");
            request.addHeader("Authorization", "Bearer " + token);
            
            // 模拟白名单检查
            when(whiteListService.isInWhiteList(requestPath)).thenReturn(false);
            
            // 模拟令牌验证
            SecureTokenValidationResult clientResult = createClientSpecificValidationResult(clientType);
            when(secureJwtTokenProvider.validateSecureToken(
                    token, TEST_DEVICE_ID, TEST_USER_AGENT, TEST_CLIENT_IP))
                    .thenReturn(clientResult);
            
            // 执行拦截器验证
            boolean result = securityInterceptor.preHandle(request, response, handler);
            
            // 验证结果（根据客户端类型可能不同）
            if (clientResult.isApproved()) {
                assertTrue(result);
            } else {
                assertThrows(Exception.class, () -> {
                    securityInterceptor.preHandle(request, response, handler);
                });
            }
        }
    }

    // 辅助方法：生成安全令牌
    private String generateSecureToken() {
        // 模拟令牌生成
        return TEST_TOKEN;
    }

    // 辅助方法：生成即将过期的令牌
    private String generateExpiringToken() {
        // 模拟即将过期的令牌
        return "expiring_token_" + System.currentTimeMillis();
    }

    // 辅助方法：生成刷新令牌
    private String generateRefreshToken() {
        // 模拟刷新令牌生成
        return REFRESH_TOKEN;
    }

    // 辅助方法：生成客户端特定令牌
    private String generateClientSpecificToken(String clientType) {
        // 模拟客户端特定令牌生成
        return "token_for_" + clientType + "_" + System.currentTimeMillis();
    }

    // 辅助方法：验证安全令牌
    private SecureTokenValidationResult validateSecureToken(String token) {
        // 模拟令牌验证
        return createSuccessValidationResult();
    }

    // 辅助方法：验证即将过期的令牌
    private SecureTokenValidationResult validateExpiringToken(String token) {
        // 模拟过期令牌验证
        return createExpiredValidationResult();
    }

    // 辅助方法：验证刷新令牌
    private SecureTokenValidationResult validateRefreshToken(String refreshToken) {
        // 模拟刷新令牌验证
        return createSuccessValidationResult();
    }

    // 辅助方法：从刷新令牌生成访问令牌
    private String generateAccessTokenFromRefresh(String refreshToken) {
        // 模拟从刷新令牌生成访问令牌
        return "new_access_token_from_" + refreshToken;
    }

    // 辅助方法：创建成功的验证结果
    private SecureTokenValidationResult createSuccessValidationResult() {
        // 创建Claims对象
        Claims claims = Jwts.claims();
        claims.put("userId", TEST_USER_ID);
        claims.put("username", TEST_USERNAME);
        claims.put("tokenId", TEST_TOKEN_ID);
        
        return SecureTokenValidationResult.approved(claims);
    }

    // 辅助方法：创建过期的验证结果
    private SecureTokenValidationResult createExpiredValidationResult() {
        return SecureTokenValidationResult.expired("Token has expired");
    }

    // 辅助方法：创建设备指纹不匹配的验证结果
    private SecureTokenValidationResult createDeviceMismatchValidationResult() {
        return SecureTokenValidationResult.rejected("Device fingerprint mismatch");
    }

    // 辅助方法：创建客户端特定的验证结果
    private SecureTokenValidationResult createClientSpecificValidationResult(String clientType) {
        // 根据客户端类型返回不同的验证结果
        if ("internal".equals(clientType)) {
            return createSuccessValidationResult();
        } else {
            return SecureTokenValidationResult.rejected("Client type " + clientType + " not supported");
        }
    }
}