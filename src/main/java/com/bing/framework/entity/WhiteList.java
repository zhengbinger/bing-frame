package com.bing.framework.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * 白名单实体类。
 * 用于存储可配置到数据库的请求白名单信息。
 *
 * @author zhengbing
 * @date 2024-11-03
 */
@Data
@TableName("sys_white_list")
public class WhiteList implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 请求路径模式。
     * 支持Ant风格路径模式，如：/api/**, /public/*
     */
    private String pattern;

    /**
     * 白名单类型。
     * 如：URL, IP, USER_AGENT等
     */
    private String type;

    /**
     * 描述。
     */
    private String description;

    /**
     * 是否启用。
     */
    private Boolean enabled;

    /**
     * 创建时间。
     */
    private Date createTime;

    /**
     * 更新时间。
     */
    private Date updateTime;
}