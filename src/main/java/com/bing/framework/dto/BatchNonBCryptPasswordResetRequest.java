/**
 * 批量重置非BCrypt格式密码请求DTO
 * 用于接收批量重置所有非BCrypt格式密码的请求数据
 * 
 * @author zhengbing
 * @date 2025-11-13
 */
package com.bing.framework.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@ApiModel(value = "批量重置非BCrypt格式密码请求参数", description = "用于接收批量重置所有非BCrypt格式密码的请求数据")
public class BatchNonBCryptPasswordResetRequest {

    /**
     * 新密码
     */
    @ApiModelProperty(value = "新密码", notes = "用于重置所有非BCrypt格式密码的新密码，长度必须在6-20个字符之间", required = true, dataType = "String", example = "123456")
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    private String newPassword;
}