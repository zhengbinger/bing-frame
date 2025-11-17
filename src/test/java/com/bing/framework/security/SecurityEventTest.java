package com.bing.framework.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 安全事件记录类测试类
 * 测试安全事件的创建、属性访问、对象比较等功能
 * 
 * @author zhengbing
 * @date 2025-11-17
 */
class SecurityEventTest {

    private static final String TEST_EVENT_TYPE = "LOGIN_ATTEMPT";
    private static final Long TEST_USER_ID = 12345L;
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_CLIENT_IP = "192.168.1.100";
    private static final String TEST_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64)";
    private static final String TEST_DEVICE_ID = "device123";
    private static final String TEST_REQUEST_PATH = "/api/login";
    private static final String TEST_METHOD = "POST";
    private static final String TEST_ADDITIONAL_INFO = "Login attempt from new device";

    private LocalDateTime testTimestamp;

    @BeforeEach
    void setUp() {
        testTimestamp = LocalDateTime.now();
    }

    @Test
    @DisplayName("使用Builder创建完整的安全事件")
    void testBuilderWithAllFields() {
        // 执行测试
        SecurityEvent event = SecurityEvent.builder()
                .eventType(TEST_EVENT_TYPE)
                .userId(TEST_USER_ID)
                .username(TEST_USERNAME)
                .clientIp(TEST_CLIENT_IP)
                .userAgent(TEST_USER_AGENT)
                .deviceId(TEST_DEVICE_ID)
                .requestPath(TEST_REQUEST_PATH)
                .method(TEST_METHOD)
                .timestamp(testTimestamp)
                .additionalInfo(TEST_ADDITIONAL_INFO)
                .build();

        // 验证结果
        assertNotNull(event);
        assertEquals(TEST_EVENT_TYPE, event.getEventType());
        assertEquals(TEST_USER_ID, event.getUserId());
        assertEquals(TEST_USERNAME, event.getUsername());
        assertEquals(TEST_CLIENT_IP, event.getClientIp());
        assertEquals(TEST_USER_AGENT, event.getUserAgent());
        assertEquals(TEST_DEVICE_ID, event.getDeviceId());
        assertEquals(TEST_REQUEST_PATH, event.getRequestPath());
        assertEquals(TEST_METHOD, event.getMethod());
        assertEquals(testTimestamp, event.getTimestamp());
        assertEquals(TEST_ADDITIONAL_INFO, event.getAdditionalInfo());
    }

    @Test
    @DisplayName("使用Builder创建最小字段的安全事件")
    void testBuilderWithMinimumFields() {
        // 执行测试 - 只设置必需字段
        SecurityEvent event = SecurityEvent.builder()
                .eventType(TEST_EVENT_TYPE)
                .timestamp(testTimestamp)
                .build();

        // 验证结果
        assertNotNull(event);
        assertEquals(TEST_EVENT_TYPE, event.getEventType());
        assertEquals(testTimestamp, event.getTimestamp());
        assertNull(event.getUserId());
        assertNull(event.getUsername());
        assertNull(event.getClientIp());
        assertNull(event.getUserAgent());
        assertNull(event.getDeviceId());
        assertNull(event.getRequestPath());
        assertNull(event.getMethod());
        assertNull(event.getAdditionalInfo());
    }

    @Test
    @DisplayName("安全事件字段setter和getter")
    void testSetterAndGetter() {
        // 创建事件对象
        SecurityEvent event = SecurityEvent.builder().build();

        // 测试setter和getter
        event.setEventType(TEST_EVENT_TYPE);
        assertEquals(TEST_EVENT_TYPE, event.getEventType());

        event.setUserId(TEST_USER_ID);
        assertEquals(TEST_USER_ID, event.getUserId());

        event.setUsername(TEST_USERNAME);
        assertEquals(TEST_USERNAME, event.getUsername());

        event.setClientIp(TEST_CLIENT_IP);
        assertEquals(TEST_CLIENT_IP, event.getClientIp());

        event.setUserAgent(TEST_USER_AGENT);
        assertEquals(TEST_USER_AGENT, event.getUserAgent());

        event.setDeviceId(TEST_DEVICE_ID);
        assertEquals(TEST_DEVICE_ID, event.getDeviceId());

        event.setRequestPath(TEST_REQUEST_PATH);
        assertEquals(TEST_REQUEST_PATH, event.getRequestPath());

        event.setMethod(TEST_METHOD);
        assertEquals(TEST_METHOD, event.getMethod());

        event.setTimestamp(testTimestamp);
        assertEquals(testTimestamp, event.getTimestamp());

        event.setAdditionalInfo(TEST_ADDITIONAL_INFO);
        assertEquals(TEST_ADDITIONAL_INFO, event.getAdditionalInfo());
    }

    @Test
    @DisplayName("安全事件toString方法")
    void testToString() {
        // 创建事件对象
        SecurityEvent event = SecurityEvent.builder()
                .eventType(TEST_EVENT_TYPE)
                .userId(TEST_USER_ID)
                .username(TEST_USERNAME)
                .clientIp(TEST_CLIENT_IP)
                .userAgent(TEST_USER_AGENT)
                .deviceId(TEST_DEVICE_ID)
                .requestPath(TEST_REQUEST_PATH)
                .method(TEST_METHOD)
                .timestamp(testTimestamp)
                .additionalInfo(TEST_ADDITIONAL_INFO)
                .build();

        // 执行测试
        String toStringResult = event.toString();

        // 验证结果
        assertNotNull(toStringResult);
        assertFalse(toStringResult.isEmpty());
        assertTrue(toStringResult.contains("SecurityEvent"));
        assertTrue(toStringResult.contains(TEST_EVENT_TYPE));
        assertTrue(toStringResult.contains(TEST_USERNAME));
        assertTrue(toStringResult.contains(TEST_CLIENT_IP));
    }

    @Test
    @DisplayName("安全事件equals和hashCode方法")
    void testEqualsAndHashCode() {
        // 创建两个相同的事件对象
        SecurityEvent event1 = SecurityEvent.builder()
                .eventType(TEST_EVENT_TYPE)
                .userId(TEST_USER_ID)
                .username(TEST_USERNAME)
                .clientIp(TEST_CLIENT_IP)
                .userAgent(TEST_USER_AGENT)
                .deviceId(TEST_DEVICE_ID)
                .requestPath(TEST_REQUEST_PATH)
                .method(TEST_METHOD)
                .timestamp(testTimestamp)
                .additionalInfo(TEST_ADDITIONAL_INFO)
                .build();

        SecurityEvent event2 = SecurityEvent.builder()
                .eventType(TEST_EVENT_TYPE)
                .userId(TEST_USER_ID)
                .username(TEST_USERNAME)
                .clientIp(TEST_CLIENT_IP)
                .userAgent(TEST_USER_AGENT)
                .deviceId(TEST_DEVICE_ID)
                .requestPath(TEST_REQUEST_PATH)
                .method(TEST_METHOD)
                .timestamp(testTimestamp)
                .additionalInfo(TEST_ADDITIONAL_INFO)
                .build();

        // 创建不同的事件对象
        SecurityEvent event3 = SecurityEvent.builder()
                .eventType("DIFFERENT_EVENT")
                .userId(99999L)
                .build();

        // 验证equals
        assertEquals(event1, event2);
        assertNotEquals(event1, event3);
        assertNotEquals(event1, null);
        assertNotEquals(event1, new Object());

        // 验证hashCode
        assertEquals(event1.hashCode(), event2.hashCode());
        assertNotEquals(event1.hashCode(), event3.hashCode());
    }

    @Test
    @DisplayName("安全事件equals方法 - 字段差异测试")
    void testEqualsFieldDifferences() {
        // 创建基础事件
        SecurityEvent baseEvent = SecurityEvent.builder()
                .eventType(TEST_EVENT_TYPE)
                .userId(TEST_USER_ID)
                .username(TEST_USERNAME)
                .build();

        // 测试不同字段的差异
        SecurityEvent differentEventType = SecurityEvent.builder()
                .eventType("DIFFERENT_TYPE")
                .userId(TEST_USER_ID)
                .username(TEST_USERNAME)
                .build();

        SecurityEvent differentUserId = SecurityEvent.builder()
                .eventType(TEST_EVENT_TYPE)
                .userId(99999L)
                .username(TEST_USERNAME)
                .build();

        SecurityEvent differentUsername = SecurityEvent.builder()
                .eventType(TEST_EVENT_TYPE)
                .userId(TEST_USER_ID)
                .username("differentuser")
                .build();

        // 验证不相等
        assertNotEquals(baseEvent, differentEventType);
        assertNotEquals(baseEvent, differentUserId);
        assertNotEquals(baseEvent, differentUsername);
    }

    @Test
    @DisplayName("安全事件字段null值处理")
    void testNullFieldHandling() {
        // 创建包含null字段的事件
        SecurityEvent event = SecurityEvent.builder()
                .eventType(null)
                .userId(null)
                .username(null)
                .clientIp(null)
                .userAgent(null)
                .deviceId(null)
                .requestPath(null)
                .method(null)
                .timestamp(null)
                .additionalInfo(null)
                .build();

        // 验证结果
        assertNotNull(event);
        assertNull(event.getEventType());
        assertNull(event.getUserId());
        assertNull(event.getUsername());
        assertNull(event.getClientIp());
        assertNull(event.getUserAgent());
        assertNull(event.getDeviceId());
        assertNull(event.getRequestPath());
        assertNull(event.getMethod());
        assertNull(event.getTimestamp());
        assertNull(event.getAdditionalInfo());
    }

    @Test
    @DisplayName("安全事件equals方法 - 混合null和非null字段")
    void testEqualsWithMixedNullFields() {
        // 创建两个相同的事件（包含null字段）
        SecurityEvent event1 = SecurityEvent.builder()
                .eventType(TEST_EVENT_TYPE)
                .userId(TEST_USER_ID)
                .username(null)
                .clientIp(null)
                .build();

        SecurityEvent event2 = SecurityEvent.builder()
                .eventType(TEST_EVENT_TYPE)
                .userId(TEST_USER_ID)
                .username(null)
                .clientIp(null)
                .build();

        SecurityEvent event3 = SecurityEvent.builder()
                .eventType(TEST_EVENT_TYPE)
                .userId(TEST_USER_ID)
                .username("testuser") // 不同值
                .clientIp(null)
                .build();

        // 验证equals
        assertEquals(event1, event2);
        assertNotEquals(event1, event3);
    }

    @Test
    @DisplayName("不同类型安全事件测试")
    void testDifferentEventTypes() {
        String[] eventTypes = {
            "LOGIN_ATTEMPT",
            "LOGIN_SUCCESS",
            "LOGIN_FAILURE",
            "LOGOUT",
            "PASSWORD_CHANGE",
            "ACCOUNT_LOCKED",
            "SUSPICIOUS_ACTIVITY",
            "PERMISSION_DENIED",
            "TOKEN_EXPIRED",
            "DEVICE_REGISTERED"
        };

        for (String eventType : eventTypes) {
            // 执行测试
            SecurityEvent event = SecurityEvent.builder()
                    .eventType(eventType)
                    .timestamp(testTimestamp)
                    .build();

            // 验证结果
            assertNotNull(event);
            assertEquals(eventType, event.getEventType());
            assertEquals(testTimestamp, event.getTimestamp());
        }
    }

    @Test
    @DisplayName("安全事件不同HTTP方法测试")
    void testDifferentHttpMethods() {
        String[] methods = {"GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS"};

        for (String method : methods) {
            // 执行测试
            SecurityEvent event = SecurityEvent.builder()
                    .eventType(TEST_EVENT_TYPE)
                    .method(method)
                    .timestamp(testTimestamp)
                    .build();

            // 验证结果
            assertNotNull(event);
            assertEquals(method, event.getMethod());
        }
    }

    @Test
    @DisplayName("安全事件不同路径测试")
    void testDifferentRequestPaths() {
        String[] paths = {
            "/api/login",
            "/api/logout",
            "/api/user/profile",
            "/api/admin/users",
            "/api/auth/refresh",
            "/web/dashboard",
            "/web/settings",
            "/api/data/export"
        };

        for (String path : paths) {
            // 执行测试
            SecurityEvent event = SecurityEvent.builder()
                    .eventType(TEST_EVENT_TYPE)
                    .requestPath(path)
                    .timestamp(testTimestamp)
                    .build();

            // 验证结果
            assertNotNull(event);
            assertEquals(path, event.getRequestPath());
        }
    }

    @Test
    @DisplayName("安全事件不同客户端IP格式测试")
    void testDifferentClientIpFormats() {
        String[] ipAddresses = {
            "192.168.1.1",
            "10.0.0.1",
            "172.16.0.1",
            "127.0.0.1",
            "203.0.113.1",
            "::1",
            "2001:db8::1"
        };

        for (String ip : ipAddresses) {
            // 执行测试
            SecurityEvent event = SecurityEvent.builder()
                    .eventType(TEST_EVENT_TYPE)
                    .clientIp(ip)
                    .timestamp(testTimestamp)
                    .build();

            // 验证结果
            assertNotNull(event);
            assertEquals(ip, event.getClientIp());
        }
    }

    @Test
    @DisplayName("安全事件不同用户代理测试")
    void testDifferentUserAgents() {
        String[] userAgents = {
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36",
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36",
            "curl/7.68.0",
            "PostmanRuntime/7.26.8",
            "bing-spider/1.0",
            "BingPreview/1.0"
        };

        for (String userAgent : userAgents) {
            // 执行测试
            SecurityEvent event = SecurityEvent.builder()
                    .eventType(TEST_EVENT_TYPE)
                    .userAgent(userAgent)
                    .timestamp(testTimestamp)
                    .build();

            // 验证结果
            assertNotNull(event);
            assertEquals(userAgent, event.getUserAgent());
        }
    }

    @Test
    @DisplayName("安全事件长额外信息测试")
    void testLongAdditionalInfo() {
        // 创建很长的额外信息
        StringBuilder longInfo = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            longInfo.append("这是一条很长的额外信息，用于测试系统在处理大量文本时的表现。");
        }
        String longAdditionalInfo = longInfo.toString();

        // 执行测试
        SecurityEvent event = SecurityEvent.builder()
                .eventType(TEST_EVENT_TYPE)
                .additionalInfo(longAdditionalInfo)
                .timestamp(testTimestamp)
                .build();

        // 验证结果
        assertNotNull(event);
        assertEquals(longAdditionalInfo, event.getAdditionalInfo());
        assertTrue(event.getAdditionalInfo().length() > 1000);
    }

    @Test
    @DisplayName("安全事件时间戳边界测试")
    void testTimestampBoundaryValues() {
        // 测试最小时间戳
        LocalDateTime minTime = LocalDateTime.of(1970, 1, 1, 0, 0, 0);
        SecurityEvent event1 = SecurityEvent.builder()
                .eventType(TEST_EVENT_TYPE)
                .timestamp(minTime)
                .build();
        assertEquals(minTime, event1.getTimestamp());

        // 测试当前时间
        LocalDateTime now = LocalDateTime.now();
        SecurityEvent event2 = SecurityEvent.builder()
                .eventType(TEST_EVENT_TYPE)
                .timestamp(now)
                .build();
        assertEquals(now, event2.getTimestamp());

        // 测试未来时间
        LocalDateTime futureTime = LocalDateTime.of(2099, 12, 31, 23, 59, 59);
        SecurityEvent event3 = SecurityEvent.builder()
                .eventType(TEST_EVENT_TYPE)
                .timestamp(futureTime)
                .build();
        assertEquals(futureTime, event3.getTimestamp());
    }
}