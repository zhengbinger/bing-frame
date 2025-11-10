package com.bing.framework.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * 权限实体类
 * 对应数据库permission表，存储权限基本信息
 * 
 * @author zhengbing
 * @date 2025-11-05
 */
@ApiModel(value = "权限信息", description = "系统权限实体类，存储权限基本信息")
@Data
@TableName("permission")
public class Permission implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 权限ID
     */
    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "id", notes = "权限ID，自增主键", dataType = "Long", example = "1")
    private Long id;

    /**
     * 权限名称
     */
    @ApiModelProperty(value = "name", notes = "权限名称", required = true, dataType = "String", example = "查看用户列表")
    private String name;

    /**
     * 权限标识
     */
    @ApiModelProperty(value = "code", notes = "权限标识", required = true, dataType = "String", example = "user:list")
    private String code;

    /**
     * 权限描述
     */
    @ApiModelProperty(value = "description", notes = "权限描述")
    private String description;

    /**
     * 权限URL
     */
    @ApiModelProperty(value = "url", notes = "权限URL", dataType = "String", example = "/api/user/list")
    private String url;

    /**
     * 请求方法
     */
    @ApiModelProperty(value = "method", notes = "请求方法", dataType = "String", example = "GET")
    private String method;

    /**
     * 父级权限ID
     */
    @ApiModelProperty(value = "parentId", notes = "父级权限ID", dataType = "Long", example = "0")
    private Long parentId;

    /**
     * 排序
     */
    @ApiModelProperty(value = "sort", notes = "排序值", dataType = "Integer", example = "1")
    private Integer sort;

    /**
     * 类型：0-菜单，1-按钮，2-API
     */
    @ApiModelProperty(value = "type", notes = "权限类型：0-菜单，1-按钮，2-API", dataType = "Integer", example = "2")
    private Integer type;

    /**
     * 图标
     */
    @ApiModelProperty(value = "icon", notes = "图标", dataType = "String")
    private String icon;

    /**
     * 状态：0-禁用，1-启用
     */
    @ApiModelProperty(value = "status", notes = "状态：0-禁用，1-启用", dataType = "Integer", example = "1")
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