package com.bing.framework.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 角色数据传输对象
 * 用于在控制器和服务层之间传递角色相关数据
 * 
 * @author zhengbing
 * @date 2025-11-05
 */
@ApiModel(value = "角色数据传输对象", description = "用于在控制器和服务层之间传递角色相关数据")
@Data
public class RoleDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 角色ID
     */
    @ApiModelProperty(value = "角色ID", notes = "角色唯一标识", dataType = "Long", example = "1")
    private Long id;

    /**
     * 角色名称
     */
    @ApiModelProperty(value = "角色名称", notes = "角色的显示名称", required = true, dataType = "String", example = "管理员")
    private String name;

    /**
     * 角色标识
     */
    @ApiModelProperty(value = "角色标识", notes = "角色的唯一编码", required = true, dataType = "String", example = "ADMIN")
    private String code;

    /**
     * 角色描述
     */
    @ApiModelProperty(value = "角色描述", notes = "角色的详细说明", dataType = "String")
    private String description;

    /**
     * 状态：0-禁用，1-启用
     */
    @ApiModelProperty(value = "状态", notes = "角色状态：0-禁用，1-启用", dataType = "Integer", example = "1")
    private Integer status;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间", notes = "角色创建时间", dataType = "Date")
    private Date createTime;

    /**
     * 更新时间
     */
    @ApiModelProperty(value = "更新时间", notes = "角色最后更新时间", dataType = "Date")
    private Date updateTime;

    /**
     * 关联的用户ID列表
     */
    @ApiModelProperty(value = "userIds", notes = "角色关联的用户ID列表", dataType = "List<Long>")
    private List<Long> userIds;
    
    /**
     * 权限ID列表（用于角色分配权限）
     */
    @ApiModelProperty(value = "permissionIds", notes = "角色关联的权限ID列表", dataType = "List<Long>")
    private List<Long> permissionIds;
}