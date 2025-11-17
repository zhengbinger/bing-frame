package com.bing.framework.dto;

import javax.validation.constraints.NotBlank;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import lombok.Data;

/**
 * 登录请求DTO
 * 用于接收用户登录时提交的数据，包含验证码相关字段
 * 
 * @author zhengbing
 * @date 2025-11-05
 */
@ApiModel(value = "登录请求参数", description = "用户登录时提交的请求数据")
@Data
public class LoginRequest {

    /**
     * 用户名
     */
    @ApiModelProperty(value = "用户名", notes = "登录账号", required = true, dataType = "String", example = "admin")
    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * 密码
     */
    @ApiModelProperty(value = "密码", notes = "用户登录密码", required = true, dataType = "String", example = "123456")
    @NotBlank(message = "密码不能为空")
    private String password;
    
    /**
     * 验证码唯一标识
     */
    @ApiModelProperty(value = "验证码唯一标识", notes = "验证码的唯一标识，用于验证时获取对应的验证码", required = true, dataType = "String")
    @NotBlank(message = "验证码标识不能为空")
    private String captchaKey;
    
    /**
     * 用户输入的验证码
     */
    @ApiModelProperty(value = "验证码", notes = "用户输入的验证码内容", required = true, dataType = "String", example = "ABCD")
    @NotBlank(message = "验证码不能为空")
    private String captcha;
    
    /**
     * 验证码类型
     */
    @ApiModelProperty(value = "验证码类型", notes = "验证码的类型，如image（图形验证码）、sms（短信验证码）等，可空，默认使用系统配置的类型", dataType = "String", example = "image")
    private String captchaType;
}