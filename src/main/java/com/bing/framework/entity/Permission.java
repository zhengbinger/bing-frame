package com.bing.framework.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
@Data
@TableName("permission")
public class Permission implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 权限ID
     */
    @TableId(type = IdType.AUTO)
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
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}