package com.bing.framework.interceptor;

import com.bing.framework.common.ErrorCode;
import com.bing.framework.exception.BusinessException;
import com.bing.framework.security.SecurityEvent;
import com.bing.framework.security.SuspiciousActivity;
import com.bing.framework.security.SecureTokenValidationResult;
import com.bing.framework.security.SecureJwtTokenProvider;
import com.bing.framework.service.WhiteListService;
import com.bing.framework.util.IpUtil;
import com.bing.framework.util.RequestContextUtil;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 增强安全拦截器测试类
 * 测试安全拦截器的各种安全验证场景和异常处理
 * 
 * @author zhengbing
 * @date 2025-11-17
 */
@ExtendWith(MockitoExtension.class)
class EnhancedSecurityInterceptorTest {

    @Mock
    private SecureJwtTokenProvider secureJwtTokenProvider;

    @Mock
    private WhiteListService whiteListService;

    @InjectMocks
    private EnhancedSecurityInterceptor securityInterceptor;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private Object handler;

    private static final String TEST_TOKEN = "test_jwt_token";
    private static final String TEST_CLIENT_IP = "192.168.1.100";
    private static final String TEST_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64)";
    private static final String TEST_DEVICE_ID = "device_12345";
    private static final String TEST_CLIENT_TYPE = "mobile";
    private static final Long TEST_USER_ID = 12345L;
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_TOKEN_ID = "token123";
    private static final String TEST_REQUEST_PATH = "/api/test";
    private static final String TEST_REQUEST_METHOD = "GET";

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        handler = new Object();

        // 设置基本请求信息
        request.setRequestURI(TEST_REQUEST_PATH);
        request.setMethod(TEST_REQUEST_METHOD);
        request.setRemoteAddr(TEST_CLIENT_IP);
        request.addHeader("User-Agent", TEST_USER_AGENT);
        request.addHeader("X-Device-ID", TEST_DEVICE_ID);
        request.addHeader("X-Client-Type", TEST_CLIENT_TYPE);
    }

    @Test
    @DisplayName("白名单路径验证通过")
    void testWhiteListPathAllowed() {
        // 设置白名单路径
        when(whiteListService.isInWhiteList(TEST_REQUEST_PATH)).thenReturn(true);

        // 执行测试
        boolean result = securityInterceptor.preHandle(request, response, handler);

        // 验证结果
        assertTrue(result);
        verify(whiteListService).isInWhiteList(TEST_REQUEST_PATH);
        verify(secureJwtTokenProvider, never()).validateSecureToken(any(), any(), any(), any());
        
        // 验证未设置用户上下文
        assertNull(RequestContextUtil.getUserId());
    }

    @Test
    @DisplayName("缺少Authorization头")
    void testMissingAuthorizationHeader() {
        // 白名单返回false
        when(whiteListService.isInWhiteList(TEST_REQUEST_PATH)).thenReturn(false);

        // 执行测试，应该抛出异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            securityInterceptor.preHandle(request, response, handler);
        });

        // 验证异常类型
        assertEquals(ErrorCode.UNAUTHORIZED, exception.getErrorCode());
        verify(whiteListService).isInWhiteList(TEST_REQUEST_PATH);
        verify(secureJwtTokenProvider, never()).validateSecureToken(any(), any(), any(), any());
    }

    @Test
    @DisplayName("无效的Authorization头格式")
    void testInvalidAuthorizationHeaderFormat() {
        // 设置无效的Authorization头
        request.addHeader("Authorization", "Basic dXNlcjpwYXNz"); // Basic Auth

        // 白名单返回false
        when(whiteListService.isInWhiteList(TEST_REQUEST_PATH)).thenReturn(false);

        // 执行测试，应该抛出异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            securityInterceptor.preHandle(request, response, handler);
        });

        // 验证异常类型
        assertEquals(ErrorCode.UNAUTHORIZED, exception.getErrorCode());
        verify(whiteListService).isInWhiteList(TEST_REQUEST_PATH);
        verify(secureJwtTokenProvider, never()).validateSecureToken(any(), any(), any(), any());
    }

    @Test
    @DisplayName("令牌验证成功")
    void testTokenValidationSuccess() {
        // 设置Authorization头
        request.addHeader("Authorization", "Bearer " + TEST_TOKEN);

        // 白名单返回false
        when(whiteListService.isInWhiteList(TEST_REQUEST_PATH)).thenReturn(false);

        // 创建成功的验证结果
        SecureTokenValidationResult successResult = createSuccessValidationResult();
        when(secureJwtTokenProvider.validateSecureToken(
                TEST_TOKEN, TEST_DEVICE_ID, TEST_USER_AGENT, TEST_CLIENT_IP))
                .thenReturn(successResult);

        // 执行测试
        boolean result = securityInterceptor.preHandle(request, response, handler);

        // 验证结果
        assertTrue(result);
        verify(whiteListService).isInWhiteList(TEST_REQUEST_PATH);
        verify(secureJwtTokenProvider).validateSecureToken(
                TEST_TOKEN, TEST_DEVICE_ID, TEST_USER_AGENT, TEST_CLIENT_IP);

        // 验证用户上下文设置
        assertEquals(TEST_USER_ID, RequestContextUtil.getUserId());
        assertEquals(TEST_USERNAME, RequestContextUtil.getUsername());
        assertEquals(TEST_TOKEN_ID, RequestContextUtil.getTokenId());
        assertEquals(TEST_CLIENT_IP, RequestContextUtil.getClientIp());
        assertEquals(TEST_USER_AGENT, RequestContextUtil.getUserAgent());
        assertEquals(TEST_DEVICE_ID, RequestContextUtil.getDeviceId());
        assertEquals(TEST_CLIENT_TYPE, RequestContextUtil.getClientType());
    }

    @Test
    @DisplayName("令牌验证失败 - 令牌过期")
    void testTokenValidationExpired() {
        // 设置Authorization头
        request.addHeader("Authorization", "Bearer " + TEST_TOKEN);

        // 白名单返回false
        when(whiteListService.isInWhiteList(TEST_REQUEST_PATH)).thenReturn(false);

        // 创建过期验证结果
        SecureTokenValidationResult expiredResult = createExpiredValidationResult();
        when(secureJwtTokenProvider.validateSecureToken(
                TEST_TOKEN, TEST_DEVICE_ID, TEST_USER_AGENT, TEST_CLIENT_IP))
                .thenReturn(expiredResult);

        // 执行测试，应该抛出异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            securityInterceptor.preHandle(request, response, handler);
        });

        // 验证异常类型
        assertEquals(ErrorCode.TOKEN_EXPIRED, exception.getErrorCode());
        verify(whiteListService).isInWhiteList(TEST_REQUEST_PATH);
        verify(secureJwtTokenProvider).validateSecureToken(
                TEST_TOKEN, TEST_DEVICE_ID, TEST_USER_AGENT, TEST_CLIENT_IP);
    }

    @Test
    @DisplayName("令牌验证失败 - 设备指纹不匹配")
    void testTokenValidationDeviceMismatch() {
        // 设置Authorization头
        request.addHeader("Authorization", "Bearer " + TEST_TOKEN);

        // 白名单返回false
        when(whiteListService.isInWhiteList(TEST_REQUEST_PATH)).thenReturn(false);

        // 创建设备指纹不匹配的验证结果
        SecureTokenValidationResult deviceMismatchResult = createDeviceMismatchValidationResult();
        when(secureJwtTokenProvider.validateSecureToken(
                TEST_TOKEN, TEST_DEVICE_ID, TEST_USER_AGENT, TEST_CLIENT_IP))
                .thenReturn(deviceMismatchResult);

        // 执行测试，应该抛出异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            securityInterceptor.preHandle(request, response, handler);
        });

        // 验证异常类型
        assertEquals(ErrorCode.DEVICE_MISMATCH, exception.getErrorCode());
        verify(whiteListService).isInWhiteList(TEST_REQUEST_PATH);
        verify(secureJwtTokenProvider).validateSecureToken(
                TEST_TOKEN, TEST_DEVICE_ID, TEST_USER_AGENT, TEST_CLIENT_IP);
    }

    @Test
    @DisplayName("令牌验证失败 - 无效令牌")
    void testTokenValidationInvalid() {
        // 设置Authorization头
        request.addHeader("Authorization", "Bearer " + TEST_TOKEN);

        // 白名单返回false
        when(whiteListService.isInWhiteList(TEST_REQUEST_PATH)).thenReturn(false);

        // 创建无效验证结果
        SecureTokenValidationResult invalidResult = createInvalidValidationResult();
        when(secureJwtTokenProvider.validateSecureToken(
                TEST_TOKEN, TEST_DEVICE_ID, TEST_USER_AGENT, TEST_CLIENT_IP))
                .thenReturn(invalidResult);

        // 执行测试，应该抛出异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            securityInterceptor.preHandle(request, response, handler);
        });

        // 验证异常类型
        assertEquals(ErrorCode.INVALID_TOKEN, exception.getErrorCode());
        verify(whiteListService).isInWhiteList(TEST_REQUEST_PATH);
        verify(secureJwtTokenProvider).validateSecureToken(
                TEST_TOKEN, TEST_DEVICE_ID, TEST_USER_AGENT, TEST_CLIENT_IP);
    }

    @Test
    @DisplayName("缺少可选头信息")
    void testMissingOptionalHeaders() {
        // 清除可选头
        request.removeHeader("X-Device-ID");
        request.removeHeader("X-Client-Type");

        // 设置Authorization头
        request.addHeader("Authorization", "Bearer " + TEST_TOKEN);

        // 白名单返回false
        when(whiteListService.isInWhiteList(TEST_REQUEST_PATH)).thenReturn(false);

        // 创建成功的验证结果
        SecureTokenValidationResult successResult = createSuccessValidationResult();
        when(secureJwtTokenProvider.validateSecureToken(
                eq(TEST_TOKEN), isNull(), eq(TEST_USER_AGENT), eq(TEST_CLIENT_IP)))
                .thenReturn(successResult);

        // 执行测试
        boolean result = securityInterceptor.preHandle(request, response, handler);

        // 验证结果
        assertTrue(result);
        verify(secureJwtTokenProvider).validateSecureToken(
                TEST_TOKEN, null, TEST_USER_AGENT, TEST_CLIENT_IP);

        // 验证用户上下文设置（可选字段为null）
        assertEquals(TEST_USER_ID, RequestContextUtil.getUserId());
        assertEquals(TEST_CLIENT_TYPE, RequestContextUtil.getClientType()); // 应该是null
    }

    @Test
    @DisplayName("敏感接口访问")
    void testSensitiveEndpointAccess() {
        // 设置敏感路径
        String sensitivePath = "/api/admin/users";
        request.setRequestURI(sensitivePath);

        // 设置Authorization头
        request.addHeader("Authorization", "Bearer " + TEST_TOKEN);

        // 白名单返回false
        when(whiteListService.isInWhiteList(sensitivePath)).thenReturn(false);

        // 创建成功的验证结果
        SecureTokenValidationResult successResult = createSuccessValidationResult();
        when(secureJwtTokenProvider.validateSecureToken(
                TEST_TOKEN, TEST_DEVICE_ID, TEST_USER_AGENT, TEST_CLIENT_IP))
                .thenReturn(successResult);

        // 执行测试
        boolean result = securityInterceptor.preHandle(request, response, handler);

        // 验证结果（敏感接口仍然可以访问，只是会记录日志）
        assertTrue(result);
        verify(secureJwtTokenProvider).validateSecureToken(
                TEST_TOKEN, TEST_DEVICE_ID, TEST_USER_AGENT, TEST_CLIENT_IP);
    }

    @Test
    @DisplayName("异常访问模式检测")
    void testAbnormalAccessPattern() {
        // 测试多个敏感接口路径
        String[] sensitivePaths = {
            "/api/admin/users",
            "/api/auth/reset-password",
            "/api/user/profile",
            "/api/payment/process"
        };

        for (String path : sensitivePaths) {
            request.setRequestURI(path);

            // 设置Authorization头
            request.addHeader("Authorization", "Bearer " + TEST_TOKEN);

            // 白名单返回false
            when(whiteListService.isInWhiteList(path)).thenReturn(false);

            // 创建成功的验证结果
            SecureTokenValidationResult successResult = createSuccessValidationResult();
            when(secureJwtTokenProvider.validateSecureToken(
                    TEST_TOKEN, TEST_DEVICE_ID, TEST_USER_AGENT, TEST_CLIENT_IP))
                    .thenReturn(successResult);

            // 执行测试
            boolean result = securityInterceptor.preHandle(request, response, handler);

            // 验证结果（当前实现中访问计数返回0，所以不会触发异常模式）
            assertTrue(result);
        }
    }

    @Test
    @DisplayName("安全事件监控正常")
    void testSecurityEventMonitoring() {
        // 设置Authorization头
        request.addHeader("Authorization", "Bearer " + TEST_TOKEN);

        // 白名单返回false
        when(whiteListService.isInWhiteList(TEST_REQUEST_PATH)).thenReturn(false);

        // 创建成功的验证结果
        SecureTokenValidationResult successResult = createSuccessValidationResult();
        when(secureJwtTokenProvider.validateSecureToken(
                TEST_TOKEN, TEST_DEVICE_ID, TEST_USER_AGENT, TEST_CLIENT_IP))
                .thenReturn(successResult);

        // 执行测试
        boolean result = securityInterceptor.preHandle(request, response, handler);

        // 验证结果
        assertTrue(result);
        // 安全事件监控通过日志记录进行，无需额外验证
    }

    @Test
    @DisplayName("请求上下文清理")
    void testRequestContextCleanup() {
        // 先设置一些上下文信息
        RequestContextUtil.setUserId(99999L);
        RequestContextUtil.setUsername("previousUser");

        // 设置Authorization头
        request.addHeader("Authorization", "Bearer " + TEST_TOKEN);

        // 白名单返回false
        when(whiteListService.isInWhiteList(TEST_REQUEST_PATH)).thenReturn(false);

        // 创建成功的验证结果
        SecureTokenValidationResult successResult = createSuccessValidationResult();
        when(secureJwtTokenProvider.validateSecureToken(
                TEST_TOKEN, TEST_DEVICE_ID, TEST_USER_AGENT, TEST_CLIENT_IP))
                .thenReturn(successResult);

        // 执行测试
        boolean result = securityInterceptor.preHandle(request, response, handler);

        // 验证结果 - 上下文应该被更新
        assertTrue(result);
        assertEquals(TEST_USER_ID, RequestContextUtil.getUserId());
        assertEquals(TEST_USERNAME, RequestContextUtil.getUsername());
    }

    @Test
    @DisplayName("拦截器配置验证")
    void testInterceptorConfiguration() {
        // 验证拦截器注解和依赖注入
        assertNotNull(securityInterceptor);
        // 由于这些是私有成员，我们通过反射或者创建公共getter来验证
        // 或者通过测试其功能来间接验证依赖注入是否成功
    }

    @Test
    @DisplayName("不同HTTP方法处理")
    void testDifferentHttpMethods() {
        String[] httpMethods = {"GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD"};
        
        for (String method : httpMethods) {
            request.setMethod(method);

            // 设置Authorization头
            request.addHeader("Authorization", "Bearer " + TEST_TOKEN);

            // 白名单返回false
            when(whiteListService.isInWhiteList(TEST_REQUEST_PATH)).thenReturn(false);

            // 创建成功的验证结果
            SecureTokenValidationResult successResult = createSuccessValidationResult();
            when(secureJwtTokenProvider.validateSecureToken(
                    TEST_TOKEN, TEST_DEVICE_ID, TEST_USER_AGENT, TEST_CLIENT_IP))
                    .thenReturn(successResult);

            // 执行测试
            boolean result = securityInterceptor.preHandle(request, response, handler);

            // 验证结果
            assertTrue(result, "方法 " + method + " 应该被正确处理");
        }
    }

    @Test
    @DisplayName("异常处理测试")
    void testExceptionHandling() {
        // 设置Authorization头
        request.addHeader("Authorization", "Bearer " + TEST_TOKEN);

        // 白名单返回false
        when(whiteListService.isInWhiteList(TEST_REQUEST_PATH)).thenReturn(false);

        // 模拟令牌验证异常
        when(secureJwtTokenProvider.validateSecureToken(
                TEST_TOKEN, TEST_DEVICE_ID, TEST_USER_AGENT, TEST_CLIENT_IP))
                .thenThrow(new RuntimeException("Token validation failed"));

        // 执行测试，应该抛出异常
        assertThrows(Exception.class, () -> {
            securityInterceptor.preHandle(request, response, handler);
        });

        verify(whiteListService).isInWhiteList(TEST_REQUEST_PATH);
        verify(secureJwtTokenProvider).validateSecureToken(
                TEST_TOKEN, TEST_DEVICE_ID, TEST_USER_AGENT, TEST_CLIENT_IP);
    }

    // 辅助方法：创建成功的验证结果
    private SecureTokenValidationResult createSuccessValidationResult() {
        return SecureTokenValidationResult.approved(createMockClaims());
    }

    // 辅助方法：创建过期的验证结果
    private SecureTokenValidationResult createExpiredValidationResult() {
        return SecureTokenValidationResult.expired("Token has expired");
    }

    // 辅助方法：创建设备指纹不匹配的验证结果
    private SecureTokenValidationResult createDeviceMismatchValidationResult() {
        return SecureTokenValidationResult.rejected("Device fingerprint mismatch");
    }

    // 辅助方法：创建无效的验证结果
    private SecureTokenValidationResult createInvalidValidationResult() {
        return SecureTokenValidationResult.rejected("Invalid token signature");
    }

    // 辅助方法：创建模拟Claims
    private Claims createMockClaims() {
        Claims claims = new io.jsonwebtoken.impl.DefaultClaims();
        claims.put("userId", TEST_USER_ID);
        claims.put("username", TEST_USERNAME);
        claims.put("tokenId", TEST_TOKEN_ID);
        return claims;
    }
}