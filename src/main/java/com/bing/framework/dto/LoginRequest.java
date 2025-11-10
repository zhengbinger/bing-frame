package com.bing.framework.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 登录请求DTO
 * 用于接收用户登录时提交的数据
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
}