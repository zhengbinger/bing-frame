package com.bing.framework.controller;

import com.bing.framework.common.ErrorCode;
import com.bing.framework.common.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ApiResponse;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 国际化测试控制器
 * 用于演示和测试错误码的多语言支持功能
 *
 * @author zhengbing
 */
@Api(tags = "国际化测试", description = "提供国际化功能测试接口，演示多语言错误消息的处理")
@RestController
@RequestMapping("/api/i18n")
public class I18nTestController {

    /**
     * 获取当前语言环境的错误码信息。
     * 通过Accept-Language请求头自动识别语言环境
     * 
     * @return 包含语言环境和各错误码消息的响应数据
     */
    @ApiOperation(value = "获取当前语言环境错误信息", notes = "根据请求头中的Accept-Language自动返回对应语言的错误码信息")
    @ApiResponses({
        @ApiResponse(code = 200, message = "查询成功"),
        @ApiResponse(code = 500, message = "查询失败")
    })
    @GetMapping("/error-info")
    public Result<?> getErrorInfo() {
        Locale currentLocale = LocaleContextHolder.getLocale();
        
        // 创建响应数据Map
        Map<String, String> data = new HashMap<>();
        data.put("locale", currentLocale.toString());
        data.put("successMessage", ErrorCode.SUCCESS.getMessage());
        data.put("systemErrorMessage", ErrorCode.SYSTEM_ERROR.getMessage());
        data.put("userNotFoundMessage", ErrorCode.USER_NOT_FOUND.getMessage());
        data.put("businessErrorMessage", ErrorCode.BUSINESS_ERROR.getMessage());
        
        // 测试不同错误码的多语言消息
        return Result.success(data);
    }

    /**
     * 通过参数指定语言环境获取错误码信息。
     * 
     * @param language 语言代码，如zh-CN, en
     * @return 包含请求语言环境和各错误码消息的响应数据
     */
    @ApiOperation(value = "通过参数指定语言获取错误信息", notes = "根据请求参数指定的语言代码返回对应语言的错误码信息")
    @ApiResponses({
        @ApiResponse(code = 200, message = "查询成功"),
        @ApiResponse(code = 500, message = "查询失败")
    })
    @GetMapping("/error-info-by-lang")
    public Result<?> getErrorInfoByLanguage(@ApiParam(name = "language", value = "语言代码，如zh-CN、en，不指定则使用系统默认语言", required = false) @RequestParam(required = false) final String language) {
        Locale locale = Locale.getDefault();
        
        if (language != null && !language.isEmpty()) {
            // 解析语言参数
            String[] langParts = language.split("-");
            if (langParts.length == 1) {
                locale = new Locale(langParts[0]);
            } else if (langParts.length >= 2) {
                locale = new Locale(langParts[0], langParts[1]);
            }
        }
        
        // 创建响应数据Map
        Map<String, String> data = new HashMap<>();
        data.put("requestedLocale", locale.toString());
        data.put("successMessage", ErrorCode.SUCCESS.getMessage(locale));
        data.put("systemErrorMessage", ErrorCode.SYSTEM_ERROR.getMessage(locale));
        data.put("userNotFoundMessage", ErrorCode.USER_NOT_FOUND.getMessage(locale));
        data.put("businessErrorMessage", ErrorCode.BUSINESS_ERROR.getMessage(locale));
        
        // 使用指定语言环境获取错误消息
        return Result.success(data);
    }

    /**
     * 测试业务异常的多语言错误消息。
     * 
     * @return 全局异常处理器处理后的统一响应格式
     */
    @ApiOperation(value = "测试异常多语言处理", notes = "模拟业务异常，测试全局异常处理器的多语言错误消息转换功能")
    @ApiResponses({
        @ApiResponse(code = 500, message = "测试异常，将通过全局异常处理器转换为多语言错误消息")
    })
    @GetMapping("/test-exception")
    public Result<?> testException() {
        // 模拟业务异常，将触发全局异常处理器返回多语言错误消息
        throw new RuntimeException("测试异常，将通过全局异常处理器转换为多语言错误消息");
    }
}