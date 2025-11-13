/**
 * 密码重置请求DTO
 * 用于接收密码重置操作的请求数据
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
@ApiModel(value = "密码重置请求参数", description = "用于接收密码重置操作的请求数据")
public class PasswordResetRequest {

    /**
     * 新密码
     */
    @ApiModelProperty(value = "新密码", notes = "用户的新密码，长度必须在6-20个字符之间", required = true, dataType = "String", example = "123456")
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    private String newPassword;
}