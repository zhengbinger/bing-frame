package com.bing.framework.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户与组织关联表实体类
 * 用于存储用户与组织的多对多关系，包含主组织标识
 * 
 * @author zhengbing
 * @date 2025-11-12
 */
@Data
@TableName("user_organization")
@ApiModel(value = "UserOrganization对象", description = "用户与组织关联")
public class UserOrganization {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键ID")
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    @ApiModelProperty(value = "用户ID", required = true)
    private Long userId;

    /**
     * 组织ID
     */
    @TableField("organization_id")
    @ApiModelProperty(value = "组织ID", required = true)
    private Long organizationId;

    /**
     * 是否为主组织
     */
    @TableField("is_main")
    @ApiModelProperty(value = "是否为主组织")
    private Boolean isMain;

    /**
     * 创建时间
     */
    @TableField("create_time")
    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    @ApiModelProperty(value = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    // 关联对象，不参与数据库映射
    @TableField(exist = false)
    @ApiModelProperty(value = "用户信息")
    private User user;

    @TableField(exist = false)
    @ApiModelProperty(value = "组织信息")
    private Organization organization;
}