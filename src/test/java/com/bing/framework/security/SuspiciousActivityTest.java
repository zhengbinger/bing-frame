package com.bing.framework.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 可疑活动记录类测试类
 * 测试可疑活动的创建、属性访问、对象比较等功能
 * 
 * @author zhengbing
 * @date 2025-11-17
 */
class SuspiciousActivityTest {

    private static final Long TEST_ID = 12345L;
    private static final String TEST_CLIENT_IP = "192.168.1.100";
    private static final String TEST_ACTIVITY_TYPE = "MULTIPLE_LOGIN_ATTEMPTS";
    private static final String TEST_DETAILS = "用户在5分钟内尝试登录10次失败";
    private static final Integer TEST_SEVERITY_LEVEL = 4;
    private static final Boolean TEST_IS_PROCESSED = false;
    private static final String TEST_PROCESSING_NOTE = "等待进一步调查";

    private LocalDateTime testTimestamp;
    private LocalDateTime testProcessedTime;

    @BeforeEach
    void setUp() {
        testTimestamp = LocalDateTime.now();
        testProcessedTime = LocalDateTime.now().plusMinutes(30);
    }

    @Test
    @DisplayName("使用Builder创建完整属性的可疑活动")
    void testBuilderWithAllFields() {
        // 执行测试
        SuspiciousActivity activity = SuspiciousActivity.builder()
                .id(TEST_ID)
                .clientIp(TEST_CLIENT_IP)
                .activityType(TEST_ACTIVITY_TYPE)
                .details(TEST_DETAILS)
                .timestamp(testTimestamp)
                .severityLevel(TEST_SEVERITY_LEVEL)
                .isProcessed(TEST_IS_PROCESSED)
                .processedTime(testProcessedTime)
                .processingNote(TEST_PROCESSING_NOTE)
                .build();

        // 验证结果
        assertNotNull(activity);
        assertEquals(TEST_ID, activity.getId());
        assertEquals(TEST_CLIENT_IP, activity.getClientIp());
        assertEquals(TEST_ACTIVITY_TYPE, activity.getActivityType());
        assertEquals(TEST_DETAILS, activity.getDetails());
        assertEquals(testTimestamp, activity.getTimestamp());
        assertEquals(TEST_SEVERITY_LEVEL, activity.getSeverityLevel());
        assertEquals(TEST_IS_PROCESSED, activity.getIsProcessed());
        assertEquals(testProcessedTime, activity.getProcessedTime());
        assertEquals(TEST_PROCESSING_NOTE, activity.getProcessingNote());
    }

    @Test
    @DisplayName("使用Builder创建最小字段的可疑活动")
    void testBuilderWithMinimumFields() {
        // 执行测试 - 只设置必需字段
        SuspiciousActivity activity = SuspiciousActivity.builder()
                .activityType(TEST_ACTIVITY_TYPE)
                .timestamp(testTimestamp)
                .build();

        // 验证结果
        assertNotNull(activity);
        assertEquals(TEST_ACTIVITY_TYPE, activity.getActivityType());
        assertEquals(testTimestamp, activity.getTimestamp());
        assertNull(activity.getId());
        assertNull(activity.getClientIp());
        assertNull(activity.getDetails());
        assertNull(activity.getSeverityLevel());
        assertNull(activity.getIsProcessed());
        assertNull(activity.getProcessedTime());
        assertNull(activity.getProcessingNote());
    }

    @Test
    @DisplayName("可疑活动字段setter和getter")
    void testSetterAndGetter() {
        // 创建活动对象
        SuspiciousActivity activity = SuspiciousActivity.builder().build();

        // 测试setter和getter
        activity.setId(TEST_ID);
        assertEquals(TEST_ID, activity.getId());

        activity.setClientIp(TEST_CLIENT_IP);
        assertEquals(TEST_CLIENT_IP, activity.getClientIp());

        activity.setActivityType(TEST_ACTIVITY_TYPE);
        assertEquals(TEST_ACTIVITY_TYPE, activity.getActivityType());

        activity.setDetails(TEST_DETAILS);
        assertEquals(TEST_DETAILS, activity.getDetails());

        activity.setTimestamp(testTimestamp);
        assertEquals(testTimestamp, activity.getTimestamp());

        activity.setSeverityLevel(TEST_SEVERITY_LEVEL);
        assertEquals(TEST_SEVERITY_LEVEL, activity.getSeverityLevel());

        activity.setIsProcessed(TEST_IS_PROCESSED);
        assertEquals(TEST_IS_PROCESSED, activity.getIsProcessed());

        activity.setProcessedTime(testProcessedTime);
        assertEquals(testProcessedTime, activity.getProcessedTime());

        activity.setProcessingNote(TEST_PROCESSING_NOTE);
        assertEquals(TEST_PROCESSING_NOTE, activity.getProcessingNote());
    }

    @Test
    @DisplayName("可疑活动toString方法")
    void testToString() {
        // 创建活动对象
        SuspiciousActivity activity = SuspiciousActivity.builder()
                .id(TEST_ID)
                .clientIp(TEST_CLIENT_IP)
                .activityType(TEST_ACTIVITY_TYPE)
                .details(TEST_DETAILS)
                .timestamp(testTimestamp)
                .severityLevel(TEST_SEVERITY_LEVEL)
                .isProcessed(TEST_IS_PROCESSED)
                .processedTime(testProcessedTime)
                .processingNote(TEST_PROCESSING_NOTE)
                .build();

        // 执行测试
        String toStringResult = activity.toString();

        // 验证结果
        assertNotNull(toStringResult);
        assertFalse(toStringResult.isEmpty());
        assertTrue(toStringResult.contains("SuspiciousActivity"));
        assertTrue(toStringResult.contains(TEST_ACTIVITY_TYPE));
        assertTrue(toStringResult.contains(TEST_CLIENT_IP));
        assertTrue(toStringResult.contains(String.valueOf(TEST_SEVERITY_LEVEL)));
    }

    @Test
    @DisplayName("可疑活动equals和hashCode方法")
    void testEqualsAndHashCode() {
        // 创建两个相同的活动对象
        SuspiciousActivity activity1 = SuspiciousActivity.builder()
                .id(TEST_ID)
                .clientIp(TEST_CLIENT_IP)
                .activityType(TEST_ACTIVITY_TYPE)
                .details(TEST_DETAILS)
                .timestamp(testTimestamp)
                .severityLevel(TEST_SEVERITY_LEVEL)
                .isProcessed(TEST_IS_PROCESSED)
                .processedTime(testProcessedTime)
                .processingNote(TEST_PROCESSING_NOTE)
                .build();

        SuspiciousActivity activity2 = SuspiciousActivity.builder()
                .id(TEST_ID)
                .clientIp(TEST_CLIENT_IP)
                .activityType(TEST_ACTIVITY_TYPE)
                .details(TEST_DETAILS)
                .timestamp(testTimestamp)
                .severityLevel(TEST_SEVERITY_LEVEL)
                .isProcessed(TEST_IS_PROCESSED)
                .processedTime(testProcessedTime)
                .processingNote(TEST_PROCESSING_NOTE)
                .build();

        // 创建不同的活动对象
        SuspiciousActivity activity3 = SuspiciousActivity.builder()
                .id(99999L)
                .activityType("DIFFERENT_TYPE")
                .build();

        // 验证equals
        assertEquals(activity1, activity2);
        assertNotEquals(activity1, activity3);
        assertNotEquals(activity1, null);
        assertNotEquals(activity1, new Object());

        // 验证hashCode
        assertEquals(activity1.hashCode(), activity2.hashCode());
        assertNotEquals(activity1.hashCode(), activity3.hashCode());
    }

    @Test
    @DisplayName("可疑活动不同严重级别测试")
    void testDifferentSeverityLevels() {
        for (int level = 1; level <= 5; level++) {
            // 执行测试
            SuspiciousActivity activity = SuspiciousActivity.builder()
                    .activityType(TEST_ACTIVITY_TYPE)
                    .severityLevel(level)
                    .timestamp(testTimestamp)
                    .build();

            // 验证结果
            assertNotNull(activity);
            assertEquals(level, activity.getSeverityLevel());
            assertTrue(level >= 1 && level <= 5);
        }
    }

    @Test
    @DisplayName("可疑活动严重级别边界测试")
    void testSeverityLevelBoundaryValues() {
        // 测试最小值
        SuspiciousActivity minActivity = SuspiciousActivity.builder()
                .activityType(TEST_ACTIVITY_TYPE)
                .severityLevel(1)
                .timestamp(testTimestamp)
                .build();
        assertEquals(1, minActivity.getSeverityLevel());

        // 测试最大值
        SuspiciousActivity maxActivity = SuspiciousActivity.builder()
                .activityType(TEST_ACTIVITY_TYPE)
                .severityLevel(5)
                .timestamp(testTimestamp)
                .build();
        assertEquals(5, maxActivity.getSeverityLevel());

        // 测试无效值（应该仍然可以设置，但逻辑验证应该在业务层处理）
        SuspiciousActivity invalidActivity = SuspiciousActivity.builder()
                .activityType(TEST_ACTIVITY_TYPE)
                .severityLevel(0)
                .timestamp(testTimestamp)
                .build();
        assertEquals(0, invalidActivity.getSeverityLevel());
    }

    @Test
    @DisplayName("可疑活动不同类型测试")
    void testDifferentActivityTypes() {
        String[] activityTypes = {
            "MULTIPLE_LOGIN_ATTEMPTS",
            "UNUSUAL_LOGIN_LOCATION",
            "BRUTE_FORCE_ATTACK",
            "SQL_INJECTION_ATTEMPT",
            "XSS_ATTEMPT",
            "FILE_UPLOAD_ABUSE",
            "PRIVILEGE_ESCALATION",
            "DATA_EXFILTRATION",
            "SUSPICIOUS_API_ACCESS",
            "UNUSUAL_USER_BEHAVIOR"
        };

        for (String activityType : activityTypes) {
            // 执行测试
            SuspiciousActivity activity = SuspiciousActivity.builder()
                    .activityType(activityType)
                    .timestamp(testTimestamp)
                    .build();

            // 验证结果
            assertNotNull(activity);
            assertEquals(activityType, activity.getActivityType());
        }
    }

    @Test
    @DisplayName("可疑活动处理状态测试")
    void testProcessedStatus() {
        // 测试未处理状态
        SuspiciousActivity unprocessedActivity = SuspiciousActivity.builder()
                .activityType(TEST_ACTIVITY_TYPE)
                .isProcessed(false)
                .timestamp(testTimestamp)
                .build();
        assertFalse(unprocessedActivity.getIsProcessed());
        assertNull(unprocessedActivity.getProcessedTime());

        // 测试已处理状态
        SuspiciousActivity processedActivity = SuspiciousActivity.builder()
                .activityType(TEST_ACTIVITY_TYPE)
                .isProcessed(true)
                .processedTime(testProcessedTime)
                .timestamp(testTimestamp)
                .build();
        assertTrue(processedActivity.getIsProcessed());
        assertEquals(testProcessedTime, processedActivity.getProcessedTime());

        // 测试null状态
        SuspiciousActivity nullActivity = SuspiciousActivity.builder()
                .activityType(TEST_ACTIVITY_TYPE)
                .isProcessed(null)
                .timestamp(testTimestamp)
                .build();
        assertNull(nullActivity.getIsProcessed());
    }

    @Test
    @DisplayName("可疑活动字段null值处理")
    void testNullFieldHandling() {
        // 创建包含null字段的活动
        SuspiciousActivity activity = SuspiciousActivity.builder()
                .id(null)
                .clientIp(null)
                .activityType(null)
                .details(null)
                .timestamp(null)
                .severityLevel(null)
                .isProcessed(null)
                .processedTime(null)
                .processingNote(null)
                .build();

        // 验证结果
        assertNotNull(activity);
        assertNull(activity.getId());
        assertNull(activity.getClientIp());
        assertNull(activity.getActivityType());
        assertNull(activity.getDetails());
        assertNull(activity.getTimestamp());
        assertNull(activity.getSeverityLevel());
        assertNull(activity.getIsProcessed());
        assertNull(activity.getProcessedTime());
        assertNull(activity.getProcessingNote());
    }

    @Test
    @DisplayName("可疑活动equals方法 - 字段差异测试")
    void testEqualsFieldDifferences() {
        // 创建基础活动
        SuspiciousActivity baseActivity = SuspiciousActivity.builder()
                .id(TEST_ID)
                .clientIp(TEST_CLIENT_IP)
                .activityType(TEST_ACTIVITY_TYPE)
                .severityLevel(TEST_SEVERITY_LEVEL)
                .build();

        // 测试不同字段的差异
        SuspiciousActivity differentId = SuspiciousActivity.builder()
                .id(99999L)
                .clientIp(TEST_CLIENT_IP)
                .activityType(TEST_ACTIVITY_TYPE)
                .severityLevel(TEST_SEVERITY_LEVEL)
                .build();

        SuspiciousActivity differentClientIp = SuspiciousActivity.builder()
                .id(TEST_ID)
                .clientIp("10.0.0.1")
                .activityType(TEST_ACTIVITY_TYPE)
                .severityLevel(TEST_SEVERITY_LEVEL)
                .build();

        SuspiciousActivity differentActivityType = SuspiciousActivity.builder()
                .id(TEST_ID)
                .clientIp(TEST_CLIENT_IP)
                .activityType("DIFFERENT_TYPE")
                .severityLevel(TEST_SEVERITY_LEVEL)
                .build();

        SuspiciousActivity differentSeverityLevel = SuspiciousActivity.builder()
                .id(TEST_ID)
                .clientIp(TEST_CLIENT_IP)
                .activityType(TEST_ACTIVITY_TYPE)
                .severityLevel(2)
                .build();

        // 验证不相等
        assertNotEquals(baseActivity, differentId);
        assertNotEquals(baseActivity, differentClientIp);
        assertNotEquals(baseActivity, differentActivityType);
        assertNotEquals(baseActivity, differentSeverityLevel);
    }

    @Test
    @DisplayName("可疑活动不同IP地址格式测试")
    void testDifferentIpFormats() {
        String[] ipAddresses = {
            "192.168.1.1",
            "10.0.0.1",
            "172.16.0.1",
            "127.0.0.1",
            "203.0.113.1",
            "::1",
            "2001:db8::1",
            "unknown"
        };

        for (String ip : ipAddresses) {
            // 执行测试
            SuspiciousActivity activity = SuspiciousActivity.builder()
                    .activityType(TEST_ACTIVITY_TYPE)
                    .clientIp(ip)
                    .timestamp(testTimestamp)
                    .build();

            // 验证结果
            assertNotNull(activity);
            assertEquals(ip, activity.getClientIp());
        }
    }

    @Test
    @DisplayName("可疑活动长详情测试")
    void testLongDetails() {
        // 创建很长的详情
        StringBuilder longDetails = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            longDetails.append("检测到可疑活动 - 详细描述第").append(i).append("条。");
        }
        String longDetailString = longDetails.toString();

        // 执行测试
        SuspiciousActivity activity = SuspiciousActivity.builder()
                .activityType(TEST_ACTIVITY_TYPE)
                .details(longDetailString)
                .timestamp(testTimestamp)
                .build();

        // 验证结果
        assertNotNull(activity);
        assertEquals(longDetailString, activity.getDetails());
        assertTrue(activity.getDetails().length() > 1000);
    }

    @Test
    @DisplayName("可疑活动处理备注测试")
    void testProcessingNotes() {
        String[] processingNotes = {
            "等待进一步调查",
            "已阻止IP地址",
            "通知安全团队",
            "false positive - 已关闭",
            "正在调查",
            "已升级到高级威胁",
            ""
        };

        for (String note : processingNotes) {
            // 执行测试
            SuspiciousActivity activity = SuspiciousActivity.builder()
                    .activityType(TEST_ACTIVITY_TYPE)
                    .processingNote(note)
                    .timestamp(testTimestamp)
                    .build();

            // 验证结果
            assertNotNull(activity);
            assertEquals(note, activity.getProcessingNote());
        }
    }

    @Test
    @DisplayName("可疑活动时间戳边界测试")
    void testTimestampBoundaryValues() {
        // 测试最小时间戳
        LocalDateTime minTime = LocalDateTime.of(1970, 1, 1, 0, 0, 0);
        SuspiciousActivity activity1 = SuspiciousActivity.builder()
                .activityType(TEST_ACTIVITY_TYPE)
                .timestamp(minTime)
                .build();
        assertEquals(minTime, activity1.getTimestamp());

        // 测试当前时间
        LocalDateTime now = LocalDateTime.now();
        SuspiciousActivity activity2 = SuspiciousActivity.builder()
                .activityType(TEST_ACTIVITY_TYPE)
                .timestamp(now)
                .build();
        assertEquals(now, activity2.getTimestamp());

        // 测试未来时间
        LocalDateTime futureTime = LocalDateTime.of(2099, 12, 31, 23, 59, 59);
        SuspiciousActivity activity3 = SuspiciousActivity.builder()
                .activityType(TEST_ACTIVITY_TYPE)
                .timestamp(futureTime)
                .build();
        assertEquals(futureTime, activity3.getTimestamp());
    }

    @Test
    @DisplayName("可疑活动ID唯一性测试")
    void testIdUniqueness() {
        // 创建多个活动，应该有不同的ID（如果使用不同的构建器调用）
        for (long i = 1; i <= 10; i++) {
            SuspiciousActivity activity = SuspiciousActivity.builder()
                    .id(i)
                    .activityType(TEST_ACTIVITY_TYPE)
                    .timestamp(testTimestamp)
                    .build();

            assertNotNull(activity);
            assertEquals(i, activity.getId());
        }
    }

    @Test
    @DisplayName("可疑活动equals方法 - 混合null和非null字段")
    void testEqualsWithMixedNullFields() {
        // 创建两个相同的活动（包含null字段）
        SuspiciousActivity activity1 = SuspiciousActivity.builder()
                .activityType(TEST_ACTIVITY_TYPE)
                .severityLevel(TEST_SEVERITY_LEVEL)
                .clientIp(null)
                .details(null)
                .build();

        SuspiciousActivity activity2 = SuspiciousActivity.builder()
                .activityType(TEST_ACTIVITY_TYPE)
                .severityLevel(TEST_SEVERITY_LEVEL)
                .clientIp(null)
                .details(null)
                .build();

        SuspiciousActivity activity3 = SuspiciousActivity.builder()
                .activityType(TEST_ACTIVITY_TYPE)
                .severityLevel(TEST_SEVERITY_LEVEL)
                .clientIp(TEST_CLIENT_IP) // 不同值
                .details(null)
                .build();

        // 验证equals
        assertEquals(activity1, activity2);
        assertNotEquals(activity1, activity3);
    }
}