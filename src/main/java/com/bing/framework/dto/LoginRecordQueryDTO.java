package com.bing.framework.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.Date;

/**
 * 登录记录查询DTO
 * 用于封装登录记录的查询条件，支持分页和多条件筛选
 * 通过Lombok简化Getter/Setter等方法，集成Swagger文档支持
 * 
 * @author zhengbing
 * @date 2025-11-11
 */
@Data
@ApiModel(value = "LoginRecordQueryDTO", description = "登录记录查询条件")
public class LoginRecordQueryDTO {

    /**
     * 用户名
     */
    @ApiModelProperty(value = "用户名")
    private String username;

    /**
     * 登录状态：0-失败，1-成功
     */
    @ApiModelProperty(value = "登录状态：0-失败，1-成功")
    private Integer status;

    /**
     * 开始时间
     */
    @ApiModelProperty(value = "开始时间")
    private Date startTime;

    /**
     * 结束时间
     */
    @ApiModelProperty(value = "结束时间")
    private Date endTime;

    /**
     * 登录IP
     */
    @ApiModelProperty(value = "登录IP")
    private String ipAddress;

    /**
     * 页码，从1开始
     */
    @ApiModelProperty(value = "页码，从1开始")
    private Integer page;

    /**
     * 每页数量
     */
    @ApiModelProperty(value = "每页数量")
    private Integer size;
}