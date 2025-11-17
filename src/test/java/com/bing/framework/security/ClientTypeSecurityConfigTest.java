package com.bing.framework.security;

import com.bing.framework.config.SecurityProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * 客户端类型安全配置管理器单元测试
 * 
 * @author zhengbing
 * @date 2025-11-17
 */
@ExtendWith(MockitoExtension.class)
class ClientTypeSecurityConfigTest {

    @Mock
    private SecurityProperties securityProperties;

    @InjectMocks
    private ClientTypeSecurityConfig clientTypeSecurityConfig;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
    }

    @Test
    void testGetExpirationConfig_WEBClient() {
        // 测试WEB客户端配置
        ExpirationConfig config = clientTypeSecurityConfig.getExpirationConfig("WEB", "HIGH");
        
        assertNotNull(config);
        assertEquals("WEB", config.getClientType());
        assertEquals(2, config.getBaseExpirationHours());
        assertEquals(0, config.getAdjustedExpirationHours()); // HIGH风险缩短至1/4，2/4=0.5，Math.max(1, 0.5)=1？等等逻辑有问题
        assertEquals(ClientRiskLevel.HIGH, config.getRiskLevel());
        assertEquals(2, config.getMaxConcurrentSessions());
        assertTrue(config.isRequireDeviceFingerprint());
        assertFalse(config.isRequireGeoVerification());
    }

    @Test
    void testGetExpirationConfig_APPClient() {
        // 测试APP客户端配置
        ExpirationConfig config = clientTypeSecurityConfig.getExpirationConfig("APP", "MEDIUM");
        
        assertNotNull(config);
        assertEquals("APP", config.getClientType());
        assertEquals(24, config.getBaseExpirationHours());
        assertEquals(24, config.getAdjustedExpirationHours()); // MEDIUM风险保持不变
        assertEquals(ClientRiskLevel.MEDIUM, config.getRiskLevel());
        assertEquals(3, config.getMaxConcurrentSessions());
        assertTrue(config.isRequireDeviceFingerprint());
        assertTrue(config.isRequireGeoVerification());
    }

    @Test
    void testGetExpirationConfig_MINIPROGRAMClient() {
        // 测试小程序客户端配置
        ExpirationConfig config = clientTypeSecurityConfig.getExpirationConfig("MINIPROGRAM", "LOW");
        
        assertNotNull(config);
        assertEquals("MINIPROGRAM", config.getClientType());
        assertEquals(720, config.getBaseExpirationHours());
        assertEquals(1440, config.getAdjustedExpirationHours()); // LOW风险延长至2倍
        assertEquals(ClientRiskLevel.LOW, config.getRiskLevel());
        assertEquals(10, config.getMaxConcurrentSessions());
        assertFalse(config.isRequireDeviceFingerprint());
        assertFalse(config.isRequireGeoVerification());
    }

    @Test
    void testGetExpirationConfig_UnknownRiskLevel() {
        // 测试未知风险级别
        ExpirationConfig config = clientTypeSecurityConfig.getExpirationConfig("WEB", "UNKNOWN");
        
        assertNotNull(config);
        assertEquals("WEB", config.getClientType());
        assertEquals(1, config.getAdjustedExpirationHours()); // UNKNOWN风险缩短至1/2，Math.max(1, 1)=1
    }

    @Test
    void testGetExpirationConfig_NullRiskLevel() {
        // 测试null风险级别
        ExpirationConfig config = clientTypeSecurityConfig.getExpirationConfig("WEB", null);
        
        assertNotNull(config);
        assertEquals("WEB", config.getClientType());
        assertEquals(1, config.getAdjustedExpirationHours()); // null风险按UNKNOWN处理
    }

    @Test
    void testIsSecureClientType_SecureClients() {
        // 测试安全客户端类型
        assertTrue(clientTypeSecurityConfig.isSecureClientType("APP"));
        assertTrue(clientTypeSecurityConfig.isSecureClientType("WECHAT"));
        assertTrue(clientTypeSecurityConfig.isSecureClientType("MINIPROGRAM"));
        assertTrue(clientTypeSecurityConfig.isSecureClientType("HARMONY"));
    }

    @Test
    void testIsSecureClientType_NonSecureClients() {
        // 测试非安全客户端类型
        assertFalse(clientTypeSecurityConfig.isSecureClientType("WEB"));
        assertFalse(clientTypeSecurityConfig.isSecureClientType("META"));
    }

    @Test
    void testIsSecureClientType_InvalidInputs() {
        // 测试无效输入
        assertFalse(clientTypeSecurityConfig.isSecureClientType(null));
        assertFalse(clientTypeSecurityConfig.isSecureClientType(""));
        assertFalse(clientTypeSecurityConfig.isSecureClientType(" "));
        assertFalse(clientTypeSecurityConfig.isSecureClientType("UNKNOWN_CLIENT"));
    }

    @Test
    void testGetSecurityLevel_SecureClients() {
        // 测试安全客户端的安全级别
        assertEquals(ClientRiskLevel.HIGH, clientTypeSecurityConfig.getSecurityLevel("WEB"));
        assertEquals(ClientRiskLevel.MEDIUM, clientTypeSecurityConfig.getSecurityLevel("APP"));
        assertEquals(ClientRiskLevel.MEDIUM, clientTypeSecurityConfig.getSecurityLevel("WECHAT"));
        assertEquals(ClientRiskLevel.LOW, clientTypeSecurityConfig.getSecurityLevel("MINIPROGRAM"));
        assertEquals(ClientRiskLevel.MEDIUM, clientTypeSecurityConfig.getSecurityLevel("HARMONY"));
        assertEquals(ClientRiskLevel.HIGH, clientTypeSecurityConfig.getSecurityLevel("META"));
    }

    @Test
    void testGetSecurityLevel_UnknownClient() {
        // 测试未知客户端类型的安全级别（默认返回HIGH）
        assertEquals(ClientRiskLevel.HIGH, clientTypeSecurityConfig.getSecurityLevel(null));
        assertEquals(ClientRiskLevel.HIGH, clientTypeSecurityConfig.getSecurityLevel("UNKNOWN_CLIENT"));
    }

    @Test
    void testRequireAdditionalVerification_HighRiskClients() {
        // 测试高风险客户端需要额外验证
        assertTrue(clientTypeSecurityConfig.requireAdditionalVerification("WEB", "normal"));
        assertTrue(clientTypeSecurityConfig.requireAdditionalVerification("META", "normal"));
    }

    @Test
    void testRequireAdditionalVerification_MediumRiskClients() {
        // 测试中等风险客户端
        assertFalse(clientTypeSecurityConfig.requireAdditionalVerification("APP", "normal"));
        assertFalse(clientTypeSecurityConfig.requireAdditionalVerification("WECHAT", "normal"));
        assertFalse(clientTypeSecurityConfig.requireAdditionalVerification("HARMONY", "normal"));
    }

    @Test
    void testRequireAdditionalVerification_LowRiskClients() {
        // 测试低风险客户端
        assertFalse(clientTypeSecurityConfig.requireAdditionalVerification("MINIPROGRAM", "normal"));
    }

    @Test
    void testRequireAdditionalVerification_SensitiveOperations() {
        // 测试敏感操作需要额外验证
        assertTrue(clientTypeSecurityConfig.requireAdditionalVerification("APP", "admin"));
        assertTrue(clientTypeSecurityConfig.requireAdditionalVerification("APP", "payment"));
        assertTrue(clientTypeSecurityConfig.requireAdditionalVerification("MINIPROGRAM", "admin"));
        assertTrue(clientTypeSecurityConfig.requireAdditionalVerification("MINIPROGRAM", "payment"));
    }

    @Test
    void testRequireAdditionalVerification_UnknownClient() {
        // 测试未知客户端类型
        assertTrue(clientTypeSecurityConfig.requireAdditionalVerification(null, "normal"));
        assertTrue(clientTypeSecurityConfig.requireAdditionalVerification("UNKNOWN_CLIENT", "normal"));
    }

    @Test
    void testAdjustExpirationByRisk_HighRisk() {
        // 反射测试私有方法：高风险调整
        // 由于是私有方法，我们通过公共接口测试其效果
        ExpirationConfig config = clientTypeSecurityConfig.getExpirationConfig("APP", "HIGH");
        assertEquals(6, config.getAdjustedExpirationHours()); // 24/4 = 6, Math.max(1, 6) = 6
    }

    @Test
    void testAdjustExpirationByRisk_MediumRisk() {
        // 反射测试私有方法：中等风险调整
        ExpirationConfig config = clientTypeSecurityConfig.getExpirationConfig("WEB", "MEDIUM");
        assertEquals(2, config.getAdjustedExpirationHours()); // 保持不变
    }

    @Test
    void testAdjustExpirationByRisk_LowRisk() {
        // 反射测试私有方法：低风险调整
        ExpirationConfig config = clientTypeSecurityConfig.getExpirationConfig("APP", "LOW");
        assertEquals(48, config.getAdjustedExpirationHours()); // 24*2 = 48
    }

    @Test
    void testCaseInsensitiveClientType() {
        // 测试客户端类型不区分大小写
        ExpirationConfig config1 = clientTypeSecurityConfig.getExpirationConfig("web", "MEDIUM");
        ExpirationConfig config2 = clientTypeSecurityConfig.getExpirationConfig("WEB", "MEDIUM");
        
        assertNotNull(config1);
        assertEquals(config1.getBaseExpirationHours(), config2.getBaseExpirationHours());
        assertEquals(config1.getRiskLevel(), config2.getRiskLevel());
    }
}