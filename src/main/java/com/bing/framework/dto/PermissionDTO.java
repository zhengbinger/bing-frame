package com.bing.framework.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 权限数据传输对象
 * 用于权限管理的请求和响应数据封装
 * 
 * @author zhengbing
 * @date 2025-11-05
 */
@ApiModel(value = "权限数据传输对象", description = "用于权限管理的请求和响应数据封装，支持树形结构")
@Data
public class PermissionDTO {

    /**
     * 权限ID
     */
    @ApiModelProperty(value = "权限ID", notes = "权限唯一标识", dataType = "Long", example = "1")
    private Long id;

    /**
     * 权限名称
     */
    @ApiModelProperty(value = "权限名称", notes = "权限的显示名称", required = true, dataType = "String", example = "查看用户列表")
    private String name;

    /**
     * 权限标识
     */
    @ApiModelProperty(value = "权限标识", notes = "权限的唯一编码", required = true, dataType = "String", example = "user:list")
    private String code;

    /**
     * 权限描述
     */
    @ApiModelProperty(value = "权限描述", notes = "权限的详细说明", dataType = "String")
    private String description;

    /**
     * 权限URL
     */
    @ApiModelProperty(value = "权限URL", notes = "API接口路径", dataType = "String", example = "/api/user/list")
    private String url;

    /**
     * 请求方法
     */
    @ApiModelProperty(value = "请求方法", notes = "HTTP请求方法，如GET、POST等", dataType = "String", example = "GET")
    private String method;

    /**
     * 父级权限ID
     */
    @ApiModelProperty(value = "父级权限ID", notes = "父权限的ID，顶级权限为0", dataType = "Long", example = "0")
    private Long parentId;

    /**
     * 排序
     */
    @ApiModelProperty(value = "排序", notes = "权限在列表中的显示顺序", dataType = "Integer", example = "1")
    private Integer sort;

    /**
     * 类型：0-菜单，1-按钮，2-API
     */
    @ApiModelProperty(value = "类型", notes = "权限类型：0-菜单，1-按钮，2-API", dataType = "Integer", example = "2")
    private Integer type;

    /**
     * 图标
     */
    @ApiModelProperty(value = "图标", notes = "权限菜单显示的图标", dataType = "String")
    private String icon;

    /**
     * 状态：0-禁用，1-启用
     */
    @ApiModelProperty(value = "状态", notes = "权限状态：0-禁用，1-启用", dataType = "Integer", example = "1")
    private Integer status;

    /**
     * 子权限列表（用于树形结构展示）
     */
    @ApiModelProperty(value = "children", notes = "子权限列表，用于构建树形结构", dataType = "List<PermissionDTO>")
    private List<PermissionDTO> children;
}