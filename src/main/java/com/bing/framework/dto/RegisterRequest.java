package com.bing.framework.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 注册请求DTO
 * 用于接收用户注册时提交的数据
 * 
 * @author zhengbing
 * @date 2025-11-05
 */
@ApiModel(value = "用户注册请求参数", description = "用户注册时提交的请求数据")
@Data
public class RegisterRequest {

    /**
     * 用户名
     */
    @ApiModelProperty(value = "用户名", notes = "登录账号，3-20个字符，仅包含字母、数字和下划线", required = true, dataType = "String", example = "user123")
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在3-20个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;

    /**
     * 密码
     */
    @ApiModelProperty(value = "密码", notes = "用户密码，6-20个字符", required = true, dataType = "String", example = "123456")
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    private String password;

    /**
     * 邮箱
     */
    @ApiModelProperty(value = "email", notes = "邮箱地址，用于找回密码和接收通知", required = true, dataType = "String", example = "user@example.com")
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 手机号
     */
    @ApiModelProperty(value = "phone", notes = "手机号码，11位数字，非必填", dataType = "String", example = "13812345678")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 昵称
     */
    @ApiModelProperty(value = "nickname", notes = "用户昵称，最多50个字符", dataType = "String", example = "用户123")
    @Size(max = 50, message = "昵称长度不能超过50个字符")
    private String nickname;
}