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
 * 图形验证码策略实现测试类
 * 测试ImageCaptchaStrategy的各项功能，包括验证码生成、验证和清理
 * 
 * @author zhengbing
 * @date 2025-11-13
 */
class ImageCaptchaStrategyTest {

    @Mock
    private RedisUtil redisUtil;
    
    @Mock
    private CaptchaConfig captchaConfig;
    
    @Mock
    private CaptchaConfig.ImageCaptchaConfig imageConfig;

    @InjectMocks
    private ImageCaptchaStrategy imageCaptchaStrategy;

    private String testKey = "test-key-123";
    private String testCode = "ABCD";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // 模拟配置结构
        when(captchaConfig.getImage()).thenReturn(imageConfig);
        when(imageConfig.getWidth()).thenReturn(120);
        when(imageConfig.getHeight()).thenReturn(40);
        when(imageConfig.getCodeCount()).thenReturn(4);
        when(imageConfig.getLineCount()).thenReturn(5);
        when(captchaConfig.getExpireMinutes()).thenReturn(5);
    }

    @Test
    void generateCaptcha_shouldReturnValidCaptchaResult() {
        // 执行测试
        CaptchaResult result = imageCaptchaStrategy.generateCaptcha(testKey);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(testKey, result.getCaptchaKey());
        assertNotNull(result.getCaptchaContent());
        assertTrue(result.getCaptchaContent().startsWith("data:image/png;base64,"));
        assertEquals("image", result.getCaptchaType());
        assertTrue(result.getExpireTime() > System.currentTimeMillis());
        
        // 验证Redis调用
        verify(redisUtil, times(1)).set(
            eq("captcha:" + testKey),
            anyString(),
            eq(5L),
            eq(TimeUnit.MINUTES)
        );
    }

    @Test
    void validateCaptcha_shouldReturnTrueWhenCodeMatches() {
        // 配置模拟行为
        when(redisUtil.get("captcha:" + testKey)).thenReturn(testCode);
        
        // 执行测试 - 不区分大小写
        boolean isValid = imageCaptchaStrategy.validateCaptcha(testKey, testCode.toLowerCase());
        
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
        boolean isValid = imageCaptchaStrategy.validateCaptcha(testKey, "wrong");
        
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
        boolean isValid = imageCaptchaStrategy.validateCaptcha(testKey, testCode);
        
        // 验证结果
        assertFalse(isValid);
    }

    @Test
    void validateCaptcha_shouldReturnFalseWhenKeyIsNull() {
        // 执行测试
        boolean isValid = imageCaptchaStrategy.validateCaptcha(null, testCode);
        
        // 验证结果
        assertFalse(isValid);
    }

    @Test
    void validateCaptcha_shouldReturnFalseWhenCodeIsNull() {
        // 执行测试
        boolean isValid = imageCaptchaStrategy.validateCaptcha(testKey, null);
        
        // 验证结果
        assertFalse(isValid);
    }

    @Test
    void cleanCaptcha_shouldDeleteCaptchaFromRedis() {
        // 执行测试
        imageCaptchaStrategy.cleanCaptcha(testKey);
        
        // 验证Redis调用
        verify(redisUtil, times(1)).delete("captcha:" + testKey);
    }

    @Test
    void getType_shouldReturnImage() {
        // 执行测试
        String type = imageCaptchaStrategy.getType();
        
        // 验证结果
        assertEquals("image", type);
    }
}