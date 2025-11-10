package com.bing.framework.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;

/**
 * 用户角色关联实体类
 * 对应数据库user_role表，存储用户与角色之间的多对多关系
 * 
 * @author zhengbing
 * @date 2025-11-05
 */
@ApiModel(value = "用户角色关联信息", description = "存储用户与角色之间的多对多关系")
@Data
@TableName("user_role")
public class UserRole implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "主键ID", notes = "自增主键", dataType = "Long", example = "1")
    private Long id;

    /**
     * 用户ID
     */
    @ApiModelProperty(value = "用户ID", notes = "关联的用户ID", required = true, dataType = "Long", example = "1")
    private Long userId;

    /**
     * 角色ID
     */
    @ApiModelProperty(value = "角色ID", notes = "关联的角色ID", required = true, dataType = "Long", example = "1")
    private Long roleId;
}