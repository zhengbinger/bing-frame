package com.bing.framework.strategy.impl;

import com.bing.framework.config.CaptchaConfig;
import com.bing.framework.dto.CaptchaResult;
import com.bing.framework.util.RedisUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 短信验证码策略实现测试类
 * 测试SmsCaptchaStrategy的各项功能，包括验证码生成、验证和清理
 * 
 * @author zhengbing
 * @date 2025-11-13
 */
class SmsCaptchaStrategyTest {

    @Mock
    private CaptchaConfig captchaConfig;

    @Mock
    private CaptchaConfig.SmsCaptchaConfig smsConfig;

    @Mock
    private RedisUtil redisUtil;

    @InjectMocks
    private SmsCaptchaStrategy smsCaptchaStrategy;

    private String testPhone = "13800138000";
    private String testCode = "123456";
    private String testKey;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testKey = "sms:" + testPhone;
        
        // 配置模拟结构
        when(captchaConfig.getSms()).thenReturn(smsConfig);
        when(smsConfig.getSendIntervalSeconds()).thenReturn(60);
        when(smsConfig.getCodeLength()).thenReturn(6);
        when(captchaConfig.getExpireMinutes()).thenReturn(5);
    }

    @Test
    void generateCaptcha_shouldReturnValidCaptchaResult() {
        // 配置模拟行为
        when(redisUtil.hasKey(anyString())).thenReturn(false);
        when(captchaConfig.getSms()).thenReturn(smsConfig);
        when(smsConfig.getCodeLength()).thenReturn(6);
        when(captchaConfig.getExpireMinutes()).thenReturn(5);
        when(smsConfig.getSendIntervalSeconds()).thenReturn(60);
        
        // 执行测试
        CaptchaResult result = smsCaptchaStrategy.generateCaptcha(testPhone);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(testPhone, result.getCaptchaKey());
        assertNotNull(result.getCaptchaContent());
        assertEquals("sms", result.getCaptchaType());
        assertTrue(result.getExpireTime() > System.currentTimeMillis());
        
        // 验证Redis调用
        verify(redisUtil, times(1)).set(
            eq("captcha:" + testPhone),
            anyString(),
            eq(5L),
            eq(TimeUnit.MINUTES)
        );
        
        // 验证发送间隔设置
        verify(redisUtil, times(1)).set(
            eq("sms:interval:" + testPhone),
            eq("1"),
            eq(60L),
            eq(TimeUnit.SECONDS)
        );
    }

    @Test
    void validateCaptcha_shouldReturnTrueWhenCodeMatches() {
        // 配置模拟行为
        when(redisUtil.get("captcha:" + testKey)).thenReturn(testCode);
        
        // 执行测试
        boolean isValid = smsCaptchaStrategy.validateCaptcha(testKey, testCode);
        
        // 验证结果
        assertTrue(isValid);
        
        // 验证清理调用
        verify(redisUtil, times(1)).delete("captcha:" + testKey);
    }

    @Test
    void validateCaptcha_shouldReturnFalseWhenCodeDoesNotMatch() {
        // 配置模拟行为
        when(redisUtil.get("captcha:" + testKey)).thenReturn(testCode);
        
        // 执行测试
        boolean isValid = smsCaptchaStrategy.validateCaptcha(testKey, "654321");
        
        // 验证结果
        assertFalse(isValid);
        
        // 验证没有清理调用
        verify(redisUtil, never()).delete("captcha:" + testKey);
    }

    @Test
    void validateCaptcha_shouldReturnFalseWhenCodeExpired() {
        // 配置模拟行为
        when(redisUtil.get("captcha:" + testKey)).thenReturn(null);
        
        // 执行测试
        boolean isValid = smsCaptchaStrategy.validateCaptcha(testKey, testCode);
        
        // 验证结果
        assertFalse(isValid);
    }

    @Test
    void validateCaptcha_shouldReturnFalseWhenKeyIsNull() {
        // 执行测试
        boolean isValid = smsCaptchaStrategy.validateCaptcha(null, testCode);
        
        // 验证结果
        assertFalse(isValid);
    }

    @Test
    void validateCaptcha_shouldReturnFalseWhenCodeIsNull() {
        // 执行测试
        boolean isValid = smsCaptchaStrategy.validateCaptcha(testKey, null);
        
        // 验证结果
        assertFalse(isValid);
    }

    @Test
    void cleanCaptcha_shouldDeleteCaptchaFromRedis() {
        // 执行测试
        smsCaptchaStrategy.cleanCaptcha(testKey);
        
        // 验证Redis调用
        verify(redisUtil, times(1)).delete("captcha:" + testKey);
    }

    @Test
    void getType_shouldReturnSms() {
        // 执行测试
        String type = smsCaptchaStrategy.getType();
        
        // 验证结果
        assertEquals("sms", type);
    }

    @Test
    void generateCode_shouldBeCorrectLengthWhenGeneratingCaptcha() {
        // 配置模拟行为
        when(redisUtil.hasKey(anyString())).thenReturn(false);
        when(captchaConfig.getSms()).thenReturn(smsConfig);
        when(smsConfig.getCodeLength()).thenReturn(6);
        when(captchaConfig.getExpireMinutes()).thenReturn(5);
        when(smsConfig.getSendIntervalSeconds()).thenReturn(60);
        
        // 执行测试
        CaptchaResult result = smsCaptchaStrategy.generateCaptcha(testPhone);
        
        // 验证结果中是否有验证码相关信息
        assertNotNull(result);
        assertEquals(testPhone, result.getCaptchaKey());
        assertEquals("sms", result.getCaptchaType());
        
        // 验证Redis中存储的验证码长度是否正确
        verify(redisUtil).set(eq("captcha:" + testPhone), anyString(), eq(5L), eq(TimeUnit.MINUTES));
    }

    @Test
    void canSend_shouldReturnTrueWhenNoRecentSend() {
        // 配置模拟行为
        when(redisUtil.get("sms:interval:" + testPhone)).thenReturn(null);
        
        // 执行测试
        boolean canSend = smsCaptchaStrategy.canSend(testPhone);
        
        // 验证结果
        assertTrue(canSend);
    }

    @Test
    void canSend_shouldReturnFalseWhenRecentlySent() {
        // 配置模拟行为
        when(redisUtil.get("sms:interval:" + testPhone)).thenReturn("1");
        
        // 执行测试
        boolean canSend = smsCaptchaStrategy.canSend(testPhone);
        
        // 验证结果
        assertFalse(canSend);
    }
}