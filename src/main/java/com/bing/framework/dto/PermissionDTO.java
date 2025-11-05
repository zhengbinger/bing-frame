package com.bing.framework.dto;

import lombok.Data;

import java.util.List;

/**
 * 权限数据传输对象
 * 用于权限管理的请求和响应数据封装
 * 
 * @author zhengbing
 * @date 2025-11-05
 */
@Data
public class PermissionDTO {

    /**
     * 权限ID
     */
    private Long id;

    /**
     * 权限名称
     */
    private String name;

    /**
     * 权限标识
     */
    private String code;

    /**
     * 权限描述
     */
    private String description;

    /**
     * 权限URL
     */
    private String url;

    /**
     * 请求方法
     */
    private String method;

    /**
     * 父级权限ID
     */
    private Long parentId;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 类型：0-菜单，1-按钮，2-API
     */
    private Integer type;

    /**
     * 图标
     */
    private String icon;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 子权限列表（用于树形结构展示）
     */
    private List<PermissionDTO> children;
}