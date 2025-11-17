package com.bing.framework.factory;

import com.bing.framework.strategy.CaptchaStrategy;
import com.bing.framework.strategy.CaptchaStrategyFactory;
import com.bing.framework.strategy.impl.ImageCaptchaStrategy;
import com.bing.framework.strategy.impl.SmsCaptchaStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 验证码策略工厂测试类
 * 测试CaptchaStrategyFactory的策略选择和获取功能
 * 
 * @author zhengbing
 * @date 2025-11-13
 */
class CaptchaStrategyFactoryTest {

    @Mock
    private ImageCaptchaStrategy imageCaptchaStrategy;

    @Mock
    private SmsCaptchaStrategy smsCaptchaStrategy;

    private CaptchaStrategyFactory captchaStrategyFactory;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // 手动初始化策略映射，因为@InjectMocks不会自动注入Map
        Map<String, CaptchaStrategy> strategyMap = new HashMap<>();
        strategyMap.put("image", imageCaptchaStrategy);
        strategyMap.put("sms", smsCaptchaStrategy);
        
        // 使用构造函数初始化
        captchaStrategyFactory = new CaptchaStrategyFactory(strategyMap);
    }

    @Test
    void getStrategy_shouldReturnImageStrategyWhenTypeIsImage() {
        // 执行测试
        CaptchaStrategy strategy = captchaStrategyFactory.getStrategy("image");
        
        // 验证结果
        assertNotNull(strategy);
        assertEquals(imageCaptchaStrategy, strategy);
    }

    @Test
    void getStrategy_shouldReturnSmsStrategyWhenTypeIsSms() {
        // 执行测试
        CaptchaStrategy strategy = captchaStrategyFactory.getStrategy("sms");
        
        // 验证结果
        assertNotNull(strategy);
        assertEquals(smsCaptchaStrategy, strategy);
    }

    @Test
    void getStrategy_shouldThrowExceptionWhenTypeIsInvalid() {
        // 执行测试并验证异常
        assertThrows(IllegalArgumentException.class, () -> {
            captchaStrategyFactory.getStrategy("invalid-type");
        });
    }

    @Test
    void getStrategy_shouldThrowExceptionWhenTypeIsNull() {
        // 执行测试并验证异常
        assertThrows(IllegalArgumentException.class, () -> {
            captchaStrategyFactory.getStrategy(null);
        });
    }

    @Test
    void getStrategy_shouldThrowExceptionWhenTypeIsEmpty() {
        // 执行测试并验证异常
        assertThrows(IllegalArgumentException.class, () -> {
            captchaStrategyFactory.getStrategy("");
        });
    }

    @Test
    void getStrategy_shouldThrowExceptionWhenTypeIsBlank() {
        // 执行测试并验证异常
        assertThrows(IllegalArgumentException.class, () -> {
            captchaStrategyFactory.getStrategy("   ");
        });
    }

    @Test
    void testStrategyMapContainsAllStrategies() {
        // 验证策略映射包含所有需要的策略
        assertNotNull(captchaStrategyFactory.getStrategy("image"));
        assertNotNull(captchaStrategyFactory.getStrategy("sms"));
    }
    
    @Test
    void registerStrategy_shouldAddNewStrategy() {
        // 准备新策略
        CaptchaStrategy mockStrategy = mock(CaptchaStrategy.class);
        
        // 注册新策略
        captchaStrategyFactory.registerStrategy("test", mockStrategy);
        
        // 验证注册成功
        assertEquals(mockStrategy, captchaStrategyFactory.getStrategy("test"));
    }
    
    @Test
    void supports_shouldReturnTrueForRegisteredType() {
        assertTrue(captchaStrategyFactory.supports("image"));
        assertTrue(captchaStrategyFactory.supports("sms"));
    }
    
    @Test
    void supports_shouldReturnFalseForUnregisteredType() {
        assertFalse(captchaStrategyFactory.supports("invalid"));
        assertFalse(captchaStrategyFactory.supports(null));
    }
}