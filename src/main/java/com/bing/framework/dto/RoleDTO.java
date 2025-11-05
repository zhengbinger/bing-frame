package com.bing.framework.dto;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 角色数据传输对象
 * 用于在控制器和服务层之间传递角色相关数据
 * 
 * @author zhengbing
 * @date 2025-11-05
 */
@Data
public class RoleDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 角色ID
     */
    private Long id;

    /**
     * 角色名称
     */
    private String name;

    /**
     * 角色标识
     */
    private String code;

    /**
     * 角色描述
     */
    private String description;

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

    /**
     * 关联的用户ID列表
     */
    private List<Long> userIds;
    
    /**
     * 权限ID列表（用于角色分配权限）
     */
    private List<Long> permissionIds;
}