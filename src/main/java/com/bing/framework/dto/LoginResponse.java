package com.bing.framework.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 登录响应DTO
 * 用于返回用户登录成功后的信息
 * 
 * @author zhengbing
 * @date 2025-11-05
 */
@Data
public class LoginResponse {

    /**
     * JWT令牌
     */
    private String token;

    /**
     * 过期时间
     */
    private Date expiration;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 用户角色列表
     */
    private List<String> roles;
}