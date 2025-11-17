package com.bing.framework.dto;

import java.util.Date;
import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import lombok.Data;

/**
 * 登录响应DTO
 * 用于返回用户登录成功后的信息
 * 
 * @author zhengbing
 * @date 2025-11-05
 */
@ApiModel(value = "登录响应信息", description = "用户登录成功后返回的数据")
@Data
public class LoginResponse {

    /**
     * JWT访问令牌
     */
    @ApiModelProperty(value = "token", notes = "JWT访问令牌，后续请求需携带此令牌", dataType = "String")
    private String token;
    
    /**
     * 刷新令牌
     */
    @ApiModelProperty(value = "refreshToken", notes = "用于刷新访问令牌的刷新令牌", dataType = "String")
    private String refreshToken;

    /**
     * 访问令牌过期时间
     */
    @ApiModelProperty(value = "expiration", notes = "访问令牌过期时间", dataType = "Date")
    private Date expiration;
    
    /**
     * 刷新令牌过期时间
     */
    @ApiModelProperty(value = "refreshExpiration", notes = "刷新令牌过期时间", dataType = "Date")
    private Date refreshExpiration;

    /**
     * 用户ID
     */
    @ApiModelProperty(value = "userId", notes = "用户唯一标识", dataType = "Long", example = "1")
    private Long userId;

    /**
     * 用户名
     */
    @ApiModelProperty(value = "username", notes = "登录用户名", dataType = "String", example = "admin")
    private String username;

    /**
     * 昵称
     */
    @ApiModelProperty(value = "nickname", notes = "用户昵称", dataType = "String", example = "系统管理员")
    private String nickname;

    /**
     * 用户角色列表
     */
    @ApiModelProperty(value = "roles", notes = "用户拥有的角色名称列表", dataType = "List<String>")
    private List<String> roles;
}