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
 * 用户实体类
 * 使用MyBatis-Plus注解定义表映射关系，通过Lombok简化Getter/Setter等方法
 * 对应数据库user表，存储用户基本信息和状态数据
 * 
 * @author zhengbing
 * @date 2025-11-01
 */
@ApiModel(value = "用户信息", description = "系统用户实体类，存储用户基本信息和状态数据")
@Data
@TableName("user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @ApiModelProperty(value = "用户ID", notes = "自增主键", dataType = "Long", example = "1")
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户名
     */
    @ApiModelProperty(value = "用户名", notes = "登录账号", required = true, dataType = "String", example = "admin")
    private String username;

    /**
     * 密码
     */
    @ApiModelProperty(value = "密码", notes = "加密存储的密码")
    private String password;

    /**
     * 邮箱
     */
    @ApiModelProperty(value = "邮箱", dataType = "String", example = "admin@example.com")
    private String email;

    /**
     * 手机号
     */
    @ApiModelProperty(value = "手机号", dataType = "String", example = "13800138000")
    private String phone;

    /**
     * 昵称
     */
    @ApiModelProperty(value = "昵称", dataType = "String", example = "系统管理员")
    private String nickname;

    /**
     * 状态：0-禁用，1-启用
     */
    @ApiModelProperty(value = "状态", notes = "0-禁用，1-启用", dataType = "Integer", example = "1")
    private Integer status;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间", dataType = "Date")
    private Date createTime;

    /**
     * 更新时间
     */
    @ApiModelProperty(value = "更新时间", dataType = "Date")
    private Date updateTime;
}