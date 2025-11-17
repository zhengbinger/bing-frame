package com.bing.framework.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 角色实体类
 * 对应数据库role表，存储角色基本信息和状态数据
 * 
 * @author zhengbing
 * @date 2025-11-05
 */
@ApiModel(value = "角色信息", description = "系统角色实体类，存储角色基本信息和状态数据")
@Data
@TableName("role")
public class Role implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 角色ID
     */
    @ApiModelProperty(value = "id", notes = "角色ID，自增主键", dataType = "Long", example = "1")
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 角色名称
     */
    @ApiModelProperty(value = "name", notes = "角色名称", required = true, dataType = "String", example = "管理员")
    private String name;

    /**
     * 角色标识
     */
    @ApiModelProperty(value = "code", notes = "角色标识/代码", required = true, dataType = "String", example = "ADMIN")
    private String code;

    /**
     * 角色描述
     */
    @ApiModelProperty(value = "description", notes = "角色描述信息", dataType = "String")
    private String description;

    /**
     * 状态：0-禁用，1-启用
     */
    @ApiModelProperty(value = "status", notes = "角色状态：0-禁用，1-启用", dataType = "Integer", example = "1")
    private Integer status;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "createTime", notes = "创建时间", dataType = "Date")
    private Date createTime;

    /**
     * 更新时间
     */
    @ApiModelProperty(value = "updateTime", notes = "更新时间", dataType = "Date")
    private Date updateTime;
}