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
 * 登录记录实体类
 * 使用MyBatis-Plus注解定义表映射关系，通过Lombok简化Getter/Setter等方法
 * 对应数据库login_record表，存储用户登录记录信息
 * 
 * @author zhengbing
 * @date 2025-11-11
 */
@Data
@TableName("login_record")
@ApiModel(value = "LoginRecord", description = "登录记录")
public class LoginRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "主键ID")
    private Long id;

    /**
     * 用户ID
     */
    @ApiModelProperty(value = "用户ID")
    private Long userId;

    /**
     * 用户名
     */
    @ApiModelProperty(value = "用户名")
    private String username;

    /**
     * 登录IP地址
     */
    @ApiModelProperty(value = "登录IP地址")
    private String ipAddress;

    /**
     * 用户代理
     */
    @ApiModelProperty(value = "用户代理")
    private String userAgent;

    /**
     * 登录状态：0-失败，1-成功
     */
    @ApiModelProperty(value = "登录状态：0-失败，1-成功")
    private Integer status;

    /**
     * 登录时间
     */
    @ApiModelProperty(value = "登录时间")
    private Date loginTime;

    /**
     * 登录结果描述
     */
    @ApiModelProperty(value = "登录结果描述")
    private String message;

    /**
     * 登录地点（预留字段）
     */
    @ApiModelProperty(value = "登录地点")
    private String location;
}