package com.bing.framework.controller;

import com.bing.framework.common.ErrorCode;
import com.bing.framework.common.Result;
import com.bing.framework.config.CaptchaConfig;
import com.bing.framework.dto.CaptchaResult;
import com.bing.framework.exception.BusinessException;
import com.bing.framework.strategy.CaptchaStrategyFactory;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * 验证码控制器
 * 提供验证码生成和刷新的API接口
 * 支持多种类型的验证码，如图形验证码、短信验证码等
 * 
 * @author zhengbing
 * @date 2025-11-05
 */
@Api(tags = "验证码管理", description = "提供验证码生成和刷新的API接口")
@RestController
@RequestMapping("/api/captcha")
@Slf4j
public class CaptchaController {
    
    @Autowired
    private CaptchaStrategyFactory captchaStrategyFactory;
    
    @Autowired
    private CaptchaConfig captchaConfig;
    
    /**
     * 生成验证码
     * 
     * @param type 验证码类型，如image（图形验证码）、sms（短信验证码）等
     * @return 验证码生成结果
     */
    @ApiOperation(value = "生成验证码", notes = "根据指定类型生成验证码，返回验证码图片或发送状态")
    @ApiResponses({
        @ApiResponse(code = 200, message = "验证码生成成功"),
        @ApiResponse(code = 400, message = "参数错误或不支持的验证码类型"),
        @ApiResponse(code = 500, message = "验证码生成失败")
    })
    @GetMapping("/generate/{type}")
    public Result<CaptchaResult> generateCaptcha(
            @ApiParam(name = "type", value = "验证码类型", required = true, allowableValues = "image,sms")
            @PathVariable String type) {
        
        // 检查验证码功能是否启用
        if (!captchaConfig.isEnabled()) {
            throw new BusinessException(ErrorCode.CAPTCHA_REQUIRED, "验证码功能未启用");
        }
        
        // 检查验证码类型是否支持
        if (!captchaStrategyFactory.supports(type)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "不支持的验证码类型: " + type);
        }
        
        try {
            // 生成唯一的验证码标识
            String captchaKey = generateCaptchaKey();
            
            // 根据类型生成验证码
            CaptchaResult result = captchaStrategyFactory.getStrategy(type).generateCaptcha(captchaKey);
            
            log.info("验证码生成成功，类型: {}, key: {}", type, captchaKey);
            return Result.success(result);
        } catch (BusinessException e) {
            throw e;
        } catch (RuntimeException e) {
            log.error("验证码生成失败: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "验证码生成失败");
        }
    }
    
    /**
     * 刷新验证码
     * 
     * @param type 验证码类型，如image（图形验证码）、sms（短信验证码）等
     * @return 新的验证码生成结果
     */
    @ApiOperation(value = "刷新验证码", notes = "根据指定类型刷新验证码，返回新的验证码")
    @ApiResponses({
        @ApiResponse(code = 200, message = "验证码刷新成功"),
        @ApiResponse(code = 400, message = "参数错误或不支持的验证码类型"),
        @ApiResponse(code = 500, message = "验证码刷新失败")
    })
    @GetMapping("/refresh/{type}")
    public Result<CaptchaResult> refreshCaptcha(
            @ApiParam(name = "type", value = "验证码类型", required = true, allowableValues = "image,sms")
            @PathVariable String type) {
        
        // 刷新验证码实际上就是重新生成一个新的验证码
        return generateCaptcha(type);
    }
    
    /**
     * 生成默认类型的验证码
     * 使用系统配置的默认验证码类型
     * 
     * @return 验证码生成结果
     */
    @ApiOperation(value = "生成默认验证码", notes = "使用系统配置的默认验证码类型生成验证码")
    @ApiResponses({
        @ApiResponse(code = 200, message = "验证码生成成功"),
        @ApiResponse(code = 400, message = "默认验证码类型配置错误"),
        @ApiResponse(code = 500, message = "验证码生成失败")
    })
    @GetMapping("/generate")
    public Result<CaptchaResult> generateDefaultCaptcha() {
        String defaultType = captchaConfig.getDefaultType();
        return generateCaptcha(defaultType);
    }
    
    /**
     * 刷新默认类型的验证码
     * 
     * @return 新的验证码生成结果
     */
    @ApiOperation(value = "刷新默认验证码", notes = "使用系统配置的默认验证码类型刷新验证码")
    @ApiResponses({
        @ApiResponse(code = 200, message = "验证码刷新成功"),
        @ApiResponse(code = 400, message = "默认验证码类型配置错误"),
        @ApiResponse(code = 500, message = "验证码刷新失败")
    })
    @GetMapping("/refresh")
    public Result<CaptchaResult> refreshDefaultCaptcha() {
        return generateDefaultCaptcha();
    }
    
    /**
     * 生成验证码唯一标识
     * 使用UUID生成随机唯一的验证码标识
     * 
     * @return 验证码唯一标识
     */
    private String generateCaptchaKey() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}