package com.bing.framework.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * 用户DTO类
 * 使用Lombok的@Data注解简化开发，实现Serializable接口支持序列化
 * 包含数据校验注解，用于API接口的数据验证和传输
 * 
 * @author zhengbing
 * @date 2025-11-01
 */
@ApiModel(value = "用户数据传输对象", description = "用于用户信息的传输和验证")
@Data
public class UserDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @ApiModelProperty(value = "用户ID", notes = "用户唯一标识", dataType = "Long", example = "1")
    private Long id;

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    @ApiModelProperty(value = "用户名", notes = "用户登录账号，必须唯一", required = true, dataType = "String", example = "admin")
    private String username;

    /**
     * 密码
     */
    @ApiModelProperty(value = "密码", notes = "用户登录密码，更新时可以为空", dataType = "String", example = "123456")
    private String password;

    /**
     * 邮箱
     */
    @Pattern(regexp = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$", message = "邮箱格式不正确")
    @ApiModelProperty(value = "邮箱", notes = "用户邮箱地址，需要符合邮箱格式", dataType = "String", example = "admin@example.com")
    private String email;

    /**
     * 手机号
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @ApiModelProperty(value = "手机号", notes = "用户手机号码，需要符合手机号格式", dataType = "String", example = "13800138000")
    private String phone;

    /**
     * 昵称
     */
    @ApiModelProperty(value = "昵称", notes = "用户显示名称", dataType = "String", example = "管理员")
    private String nickname;

    /**
     * 状态：0-禁用，1-启用
     */
    @ApiModelProperty(value = "状态", notes = "用户状态：0-禁用，1-启用", dataType = "Integer", example = "1")
    private Integer status;
}