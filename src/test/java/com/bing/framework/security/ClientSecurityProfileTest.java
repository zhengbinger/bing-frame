package com.bing.framework.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 客户端安全配置档案单元测试
 * 
 * @author zhengbing
 * @date 2025-11-17
 */
class ClientSecurityProfileTest {

    @Test
    void testBuilder_WithAllFields() {
        // 测试构建器设置所有字段
        ClientSecurityProfile profile = ClientSecurityProfile.builder()
                .expirationHours(24)
                .maxConcurrentSessions(5)
                .requireDeviceFingerprint(true)
                .requireGeoVerification(false)
                .riskLevel(ClientRiskLevel.MEDIUM)
                .securityCheckInterval(30)
                .enableRealtimeMonitoring(true)
                .build();
        
        assertNotNull(profile);
        assertEquals(24, profile.getExpirationHours());
        assertEquals(5, profile.getMaxConcurrentSessions());
        assertTrue(profile.isRequireDeviceFingerprint());
        assertFalse(profile.isRequireGeoVerification());
        assertEquals(ClientRiskLevel.MEDIUM, profile.getRiskLevel());
        assertEquals(30, profile.getSecurityCheckInterval());
        assertTrue(profile.isEnableRealtimeMonitoring());
    }

    @Test
    void testBuilder_WithMinimalFields() {
        // 测试构建器只设置必需字段
        ClientSecurityProfile profile = ClientSecurityProfile.builder()
                .expirationHours(12)
                .maxConcurrentSessions(3)
                .requireDeviceFingerprint(false)
                .requireGeoVerification(false)
                .riskLevel(ClientRiskLevel.LOW)
                .build();
        
        assertNotNull(profile);
        assertEquals(12, profile.getExpirationHours());
        assertEquals(3, profile.getMaxConcurrentSessions());
        assertFalse(profile.isRequireDeviceFingerprint());
        assertFalse(profile.isRequireGeoVerification());
        assertEquals(ClientRiskLevel.LOW, profile.getRiskLevel());
        // 未设置的字段应该有默认值
        assertEquals(0, profile.getSecurityCheckInterval()); // int默认值
        assertFalse(profile.isEnableRealtimeMonitoring()); // boolean默认值
    }

    @Test
    void testBuilder_OnlyRequiredFields() {
        // 测试构建器只设置基本字段
        ClientSecurityProfile profile = ClientSecurityProfile.builder()
                .riskLevel(ClientRiskLevel.HIGH)
                .build();
        
        assertNotNull(profile);
        assertEquals(ClientRiskLevel.HIGH, profile.getRiskLevel());
        // 其他字段应该有默认值
        assertEquals(0, profile.getExpirationHours());
        assertEquals(0, profile.getMaxConcurrentSessions());
        assertFalse(profile.isRequireDeviceFingerprint());
        assertFalse(profile.isRequireGeoVerification());
        assertEquals(0, profile.getSecurityCheckInterval());
        assertFalse(profile.isEnableRealtimeMonitoring());
    }

    @Test
    void testBuilder_AllRiskLevels() {
        // 测试所有风险级别的设置
        ClientSecurityProfile highRisk = ClientSecurityProfile.builder()
                .riskLevel(ClientRiskLevel.HIGH)
                .build();
        assertEquals(ClientRiskLevel.HIGH, highRisk.getRiskLevel());
        
        ClientSecurityProfile mediumRisk = ClientSecurityProfile.builder()
                .riskLevel(ClientRiskLevel.MEDIUM)
                .build();
        assertEquals(ClientRiskLevel.MEDIUM, mediumRisk.getRiskLevel());
        
        ClientSecurityProfile lowRisk = ClientSecurityProfile.builder()
                .riskLevel(ClientRiskLevel.LOW)
                .build();
        assertEquals(ClientRiskLevel.LOW, lowRisk.getRiskLevel());
        
        ClientSecurityProfile criticalRisk = ClientSecurityProfile.builder()
                .riskLevel(ClientRiskLevel.CRITICAL)
                .build();
        assertEquals(ClientRiskLevel.CRITICAL, criticalRisk.getRiskLevel());
    }

    @Test
    void testGetterAndSetterMethods() {
        // 测试通过setter方法设置值
        ClientSecurityProfile profile = new ClientSecurityProfile();
        
        profile.setExpirationHours(48);
        profile.setMaxConcurrentSessions(10);
        profile.setRequireDeviceFingerprint(true);
        profile.setRequireGeoVerification(true);
        profile.setRiskLevel(ClientRiskLevel.MEDIUM);
        profile.setSecurityCheckInterval(60);
        profile.setEnableRealtimeMonitoring(true);
        
        assertEquals(48, profile.getExpirationHours());
        assertEquals(10, profile.getMaxConcurrentSessions());
        assertTrue(profile.isRequireDeviceFingerprint());
        assertTrue(profile.isRequireGeoVerification());
        assertEquals(ClientRiskLevel.MEDIUM, profile.getRiskLevel());
        assertEquals(60, profile.getSecurityCheckInterval());
        assertTrue(profile.isEnableRealtimeMonitoring());
    }

    @Test
    void testBooleanFields_True() {
        // 测试布尔字段设置为true
        ClientSecurityProfile profile = ClientSecurityProfile.builder()
                .requireDeviceFingerprint(true)
                .requireGeoVerification(true)
                .enableRealtimeMonitoring(true)
                .build();
        
        assertTrue(profile.isRequireDeviceFingerprint());
        assertTrue(profile.isRequireGeoVerification());
        assertTrue(profile.isEnableRealtimeMonitoring());
    }

    @Test
    void testBooleanFields_False() {
        // 测试布尔字段设置为false
        ClientSecurityProfile profile = ClientSecurityProfile.builder()
                .requireDeviceFingerprint(false)
                .requireGeoVerification(false)
                .enableRealtimeMonitoring(false)
                .build();
        
        assertFalse(profile.isRequireDeviceFingerprint());
        assertFalse(profile.isRequireGeoVerification());
        assertFalse(profile.isEnableRealtimeMonitoring());
    }

    @Test
    void testToString() {
        // 测试toString方法
        ClientSecurityProfile profile = ClientSecurityProfile.builder()
                .expirationHours(12)
                .maxConcurrentSessions(2)
                .riskLevel(ClientRiskLevel.HIGH)
                .build();
        
        String str = profile.toString();
        assertNotNull(str);
        assertTrue(str.contains("expirationHours=12"));
        assertTrue(str.contains("maxConcurrentSessions=2"));
        assertTrue(str.contains("riskLevel=HIGH"));
    }

    @Test
    void testEqualsAndHashCode() {
        // 测试equals和hashCode方法
        ClientSecurityProfile profile1 = ClientSecurityProfile.builder()
                .expirationHours(24)
                .maxConcurrentSessions(5)
                .riskLevel(ClientRiskLevel.MEDIUM)
                .build();
        
        ClientSecurityProfile profile2 = ClientSecurityProfile.builder()
                .expirationHours(24)
                .maxConcurrentSessions(5)
                .riskLevel(ClientRiskLevel.MEDIUM)
                .build();
        
        ClientSecurityProfile profile3 = ClientSecurityProfile.builder()
                .expirationHours(48)
                .maxConcurrentSessions(5)
                .riskLevel(ClientRiskLevel.MEDIUM)
                .build();
        
        // 相同内容的对象应该相等
        assertEquals(profile1, profile2);
        assertEquals(profile1.hashCode(), profile2.hashCode());
        
        // 不同内容的对象不应该相等
        assertNotEquals(profile1, profile3);
        assertNotEquals(profile1.hashCode(), profile3.hashCode());
    }

    @Test
    void testExtremeValues() {
        // 测试极值
        ClientSecurityProfile profile = ClientSecurityProfile.builder()
                .expirationHours(Long.MAX_VALUE)
                .maxConcurrentSessions(Integer.MAX_VALUE)
                .securityCheckInterval(Integer.MAX_VALUE)
                .build();
        
        assertEquals(Long.MAX_VALUE, profile.getExpirationHours());
        assertEquals(Integer.MAX_VALUE, profile.getMaxConcurrentSessions());
        assertEquals(Integer.MAX_VALUE, profile.getSecurityCheckInterval());
    }

    @Test
    void testZeroValues() {
        // 测试零值
        ClientSecurityProfile profile = ClientSecurityProfile.builder()
                .expirationHours(0)
                .maxConcurrentSessions(0)
                .securityCheckInterval(0)
                .build();
        
        assertEquals(0, profile.getExpirationHours());
        assertEquals(0, profile.getMaxConcurrentSessions());
        assertEquals(0, profile.getSecurityCheckInterval());
    }

    @Test
    void testNegativeValues() {
        // 测试负值（虽然不常见，但应该能正确处理）
        ClientSecurityProfile profile = ClientSecurityProfile.builder()
                .expirationHours(-1)
                .maxConcurrentSessions(-5)
                .securityCheckInterval(-10)
                .build();
        
        assertEquals(-1, profile.getExpirationHours());
        assertEquals(-5, profile.getMaxConcurrentSessions());
        assertEquals(-10, profile.getSecurityCheckInterval());
    }

    @Test
    void testBuilderImmutability() {
        // 测试构建器的不可变性（构建后修改不影响其他实例）
        ClientSecurityProfile profile1 = ClientSecurityProfile.builder()
                .expirationHours(24)
                .build();
        
        ClientSecurityProfile profile2 = ClientSecurityProfile.builder()
                .expirationHours(48)
                .build();
        
        // 两个实例应该有各自独立的值
        assertEquals(24, profile1.getExpirationHours());
        assertEquals(48, profile2.getExpirationHours());
        
        // 修改一个不应该影响另一个
        profile1.setExpirationHours(72);
        assertEquals(72, profile1.getExpirationHours());
        assertEquals(48, profile2.getExpirationHours());
    }
}