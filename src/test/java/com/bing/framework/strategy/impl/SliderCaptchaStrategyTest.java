package com.bing.framework.strategy.impl;

import com.bing.framework.config.CaptchaConfig;
import com.bing.framework.dto.CaptchaResult;
import com.bing.framework.dto.SliderCaptchaData;
import com.bing.framework.dto.SliderCaptchaResult;
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
 * 滑动条验证码策略实现测试类
 * 测试SliderCaptchaStrategy的各项功能，包括验证码生成、验证和清理
 * 
 * 包含验证码生成、验证逻辑、Redis存储、随机位置生成等核心功能测试
 * 通过Mockito模拟Redis操作和配置，确保测试的独立性和可靠性
 * 
 * @author zhengbing
 * @date 2025-11-20
 */
class SliderCaptchaStrategyTest {

    @Mock
    private RedisUtil redisUtil;
    
    @Mock
    private CaptchaConfig captchaConfig;

    @InjectMocks
    private SliderCaptchaStrategy sliderCaptchaStrategy;

    private String testKey = "test-slider-key-123";
    private String testCode = "150"; // 滑动位置代码
    private SliderCaptchaData testCaptchaData;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // 模拟配置结构
        when(captchaConfig.getExpireMinutes()).thenReturn(5);
        
        // 创建测试验证码数据
        testCaptchaData = new SliderCaptchaData();
        testCaptchaData.setTargetPosition(150);
        testCaptchaData.setCurrentPosition(0);
        testCaptchaData.setTolerance(5);
        testCaptchaData.setTimestamp(System.currentTimeMillis());
    }

    @Test
    void generateCaptcha_shouldReturnValidSliderCaptchaResult() {
        // 执行测试
        CaptchaResult result = sliderCaptchaStrategy.generateCaptcha(testKey);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(testKey, result.getCaptchaKey());
        assertEquals("slider", result.getCaptchaType());
        assertTrue(result.getExpireTime() > System.currentTimeMillis());
        
        // 验证返回类型
        assertTrue(result instanceof SliderCaptchaResult);
        SliderCaptchaResult sliderResult = (SliderCaptchaResult) result;
        assertNotNull(sliderResult.getCaptchaData());
        assertTrue(sliderResult.getCaptchaData().getTargetPosition() >= 90);
        assertTrue(sliderResult.getCaptchaData().getTargetPosition() <= 210);
        assertEquals(5, sliderResult.getCaptchaData().getTolerance());
        
        // 验证Redis调用
        verify(redisUtil, times(1)).set(
            eq("captcha:slider:" + testKey),
            any(SliderCaptchaData.class),
            eq(300L),
            eq(TimeUnit.SECONDS)
        );
    }

    @Test
    void validateCaptcha_shouldReturnTrueWhenPositionMatches() {
        // 配置模拟行为 - 存储的验证码数据
        when(redisUtil.get("captcha:slider:" + testKey)).thenReturn(testCaptchaData);
        
        // 执行测试 - 使用正确的位置（150 ± 5容差范围内）
        boolean isValid = sliderCaptchaStrategy.validateCaptcha(testKey, testCode);
        
        // 验证结果
        assertTrue(isValid);
        
        // 验证清理调用
        verify(redisUtil, times(1)).delete("captcha:slider:" + testKey);
    }

    @Test
    void validateCaptcha_shouldReturnTrueWhenPositionWithinTolerance() {
        // 配置模拟行为
        when(redisUtil.get("captcha:slider:" + testKey)).thenReturn(testCaptchaData);
        
        // 执行测试 - 使用容差范围内的位置
        boolean isValid = sliderCaptchaStrategy.validateCaptcha(testKey, "148"); // 150-5=145范围内
        
        // 验证结果
        assertTrue(isValid);
        
        // 验证清理调用
        verify(redisUtil, times(1)).delete("captcha:slider:" + testKey);
    }

    @Test
    void validateCaptcha_shouldReturnFalseWhenPositionOutsideTolerance() {
        // 配置模拟行为
        when(redisUtil.get("captcha:slider:" + testKey)).thenReturn(testCaptchaData);
        
        // 执行测试 - 使用超出容差范围的位置
        boolean isValid = sliderCaptchaStrategy.validateCaptcha(testKey, "140"); // 150-5=145范围外
        
        // 验证结果
        assertFalse(isValid);
        
        // 验证没有清理调用
        verify(redisUtil, never()).delete("captcha:slider:" + testKey);
    }

    @Test
    void validateCaptcha_shouldReturnFalseWhenCodeExpired() {
        // 配置模拟行为
        when(redisUtil.get("captcha:slider:" + testKey)).thenReturn(null);
        
        // 执行测试
        boolean isValid = sliderCaptchaStrategy.validateCaptcha(testKey, testCode);
        
        // 验证结果
        assertFalse(isValid);
    }

    @Test
    void validateCaptcha_shouldReturnFalseWhenKeyIsNull() {
        // 执行测试
        boolean isValid = sliderCaptchaStrategy.validateCaptcha(null, testCode);
        
        // 验证结果
        assertFalse(isValid);
    }

    @Test
    void validateCaptcha_shouldReturnFalseWhenCodeIsNull() {
        // 执行测试
        boolean isValid = sliderCaptchaStrategy.validateCaptcha(testKey, null);
        
        // 验证结果
        assertFalse(isValid);
    }

    @Test
    void validateCaptcha_shouldReturnFalseWhenCodeIsInvalid() {
        // 配置模拟行为
        when(redisUtil.get("captcha:slider:" + testKey)).thenReturn(testCaptchaData);
        
        // 执行测试 - 使用无效的代码
        boolean isValid = sliderCaptchaStrategy.validateCaptcha(testKey, "invalid");
        
        // 验证结果
        assertFalse(isValid);
    }

    @Test
    void validateCaptcha_shouldParseJsonPositionCorrectly() {
        // 配置模拟行为
        when(redisUtil.get("captcha:slider:" + testKey)).thenReturn(testCaptchaData);
        
        // 执行测试 - 使用JSON格式的位置数据
        String jsonCode = "{\"position\":150,\"timestamp\":1640995200000}";
        boolean isValid = sliderCaptchaStrategy.validateCaptcha(testKey, jsonCode);
        
        // 验证结果
        assertTrue(isValid);
    }

    @Test
    void cleanCaptcha_shouldDeleteCaptchaFromRedis() {
        // 执行测试
        sliderCaptchaStrategy.cleanCaptcha(testKey);
        
        // 验证Redis调用
        verify(redisUtil, times(1)).delete("captcha:slider:" + testKey);
    }

    @Test
    void getType_shouldReturnSlider() {
        // 执行测试
        String type = sliderCaptchaStrategy.getType();
        
        // 验证结果
        assertEquals("slider", type);
    }

    @Test
    void generateRandomTargetPosition_shouldReturnPositionInValidRange() {
        // 多次执行以验证随机性
        for (int i = 0; i < 100; i++) {
            // 由于是私有方法，我们需要通过生成验证码来测试
            CaptchaResult result = sliderCaptchaStrategy.generateCaptcha("test-" + i);
            SliderCaptchaResult sliderResult = (SliderCaptchaResult) result;
            int targetPosition = sliderResult.getCaptchaData().getTargetPosition();
            
            // 验证目标位置在合理范围内 (90-210)
            assertTrue(targetPosition >= 90 && targetPosition <= 210, 
                "目标位置应在90-210范围内，实际值: " + targetPosition);
        }
    }
}