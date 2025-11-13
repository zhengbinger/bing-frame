package com.bing.framework.controller;

import com.bing.framework.config.CaptchaConfig;
import com.bing.framework.dto.CaptchaResult;
import com.bing.framework.strategy.CaptchaStrategy;
import com.bing.framework.strategy.CaptchaStrategyFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 验证码控制器测试类
 * 测试CaptchaController的各个API接口功能
 * 
 * @author zhengbing
 * @date 2025-11-13
 */
class CaptchaControllerTest {

    @Mock
    private CaptchaStrategyFactory captchaStrategyFactory;
    
    @Mock
    private CaptchaConfig captchaConfig;

    @Mock
    private CaptchaStrategy imageCaptchaStrategy;

    @Mock
    private CaptchaStrategy smsCaptchaStrategy;

    @InjectMocks
    private CaptchaController captchaController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private String testKey = "test-key-123";
    private String testPhone = "13800138000";
    private String testCode = "123456";
    private CaptchaResult captchaResult;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(captchaController).build();
        objectMapper = new ObjectMapper();

        // 准备模拟数据
        captchaResult = new CaptchaResult();
        captchaResult.setCaptchaKey(testKey);
        captchaResult.setCaptchaContent("test-content");
        captchaResult.setCaptchaType("image");
        captchaResult.setExpireTime(System.currentTimeMillis() + 300000); // 5分钟后过期

        // 模拟验证码配置
        when(captchaConfig.isEnabled()).thenReturn(true);
        when(captchaConfig.getDefaultType()).thenReturn("image");
        
        // 模拟工厂行为
        when(captchaStrategyFactory.getStrategy("image")).thenReturn(imageCaptchaStrategy);
        when(captchaStrategyFactory.getStrategy("sms")).thenReturn(smsCaptchaStrategy);
        when(captchaStrategyFactory.supports("image")).thenReturn(true);
        when(captchaStrategyFactory.supports("sms")).thenReturn(true);
        when(captchaStrategyFactory.supports("invalid")).thenReturn(false);
        doThrow(new IllegalArgumentException("不支持的验证码类型: invalid")).when(captchaStrategyFactory).getStrategy("invalid");

        // 模拟策略行为
        when(imageCaptchaStrategy.generateCaptcha(anyString())).thenReturn(captchaResult);
        when(smsCaptchaStrategy.generateCaptcha(anyString())).thenReturn(captchaResult);
        when(imageCaptchaStrategy.validateCaptcha(testKey, testCode)).thenReturn(true);
        when(imageCaptchaStrategy.validateCaptcha(testKey, "wrong")).thenReturn(false);
    }

    @Test
    void generateCaptcha_shouldReturnSuccessForImageType() throws Exception {
        // 执行测试
        mockMvc.perform(get("/api/captcha/generate/image"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data").isNotEmpty());

        // 验证调用
        verify(captchaStrategyFactory, times(1)).getStrategy("image");
        verify(imageCaptchaStrategy, times(1)).generateCaptcha(testKey);
    }

    @Test
    void generateCaptcha_shouldReturnSuccessForSmsType() throws Exception {
        // 执行测试
        mockMvc.perform(get("/api/captcha/generate/sms"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"));

        // 验证调用
        verify(captchaStrategyFactory, times(1)).getStrategy("sms");
        verify(smsCaptchaStrategy, times(1)).generateCaptcha(testPhone);
    }

    @Test
    void generateCaptcha_shouldReturnErrorWhenTypeIsInvalid() throws Exception {
        // 执行测试
        mockMvc.perform(get("/api/captcha/generate/invalid"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").isNotEmpty());

        // 验证调用
        verify(captchaStrategyFactory, times(1)).supports("invalid");
    }

    @Test
    void generateCaptcha_shouldReturnErrorWhenKeyIsNull() throws Exception {
        // 执行测试
        mockMvc.perform(get("/api/captcha/generate/image"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void refreshCaptcha_shouldReturnSuccess() throws Exception {
        // 执行测试
        mockMvc.perform(get("/captcha/refresh")
                .param("type", "image")
                .param("key", testKey))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"));

        // 验证调用
        verify(captchaStrategyFactory, times(1)).getStrategy("image");
        verify(imageCaptchaStrategy, times(1)).generateCaptcha(testKey);
    }

    @Test
    void validateCaptcha_shouldReturnSuccessWhenValid() throws Exception {
        // 准备请求数据
        Map<String, String> request = new HashMap<>();
        request.put("type", "image");
        request.put("key", testKey);
        request.put("code", testCode);

        // 执行测试
        mockMvc.perform(post("/captcha/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"));

        // 验证调用
        verify(captchaStrategyFactory, times(1)).getStrategy("image");
        verify(imageCaptchaStrategy, times(1)).validateCaptcha(testKey, testCode);
    }

    @Test
    void validateCaptcha_shouldReturnErrorWhenInvalidCode() throws Exception {
        // 准备请求数据
        Map<String, String> request = new HashMap<>();
        request.put("type", "image");
        request.put("key", testKey);
        request.put("code", "wrong");

        // 执行测试
        mockMvc.perform(post("/captcha/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").isNotEmpty());

        // 验证调用
        verify(captchaStrategyFactory, times(1)).getStrategy("image");
        verify(imageCaptchaStrategy, times(1)).validateCaptcha(testKey, "wrong");
    }

    @Test
    void validateCaptcha_shouldReturnErrorWhenMissingParams() throws Exception {
        // 准备请求数据（缺少code参数）
        Map<String, String> request = new HashMap<>();
        request.put("type", "image");
        request.put("key", testKey);

        // 执行测试
        mockMvc.perform(post("/captcha/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void generateCaptchaKey_shouldReturnValidKey() throws Exception {
        // 使用spy来模拟静态方法
        CaptchaController spyController = spy(captchaController);
        MockMvc spyMockMvc = MockMvcBuilders.standaloneSetup(spyController).build();

        // 执行测试
        spyMockMvc.perform(get("/api/captcha/generate"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data").isNotEmpty());
    }

    // 测试工具类方法在CaptchaUtilsTest中进行测试
}