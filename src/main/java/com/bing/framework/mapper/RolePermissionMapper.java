package com.bing.framework.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bing.framework.entity.RolePermission;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色权限关联Mapper接口
 * 提供角色权限关联关系的数据库操作方法
 * 
 * @author zhengbing
 * @date 2025-11-05
 */
public interface RolePermissionMapper extends BaseMapper<RolePermission> {

    /**
     * 根据角色ID删除角色权限关联关系
     * 
     * @param roleId 角色ID
     * @return 删除数量
     */
    int deleteByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据权限ID删除角色权限关联关系
     * 
     * @param permissionId 权限ID
     * @return 删除数量
     */
    int deleteByPermissionId(@Param("permissionId") Long permissionId);

    /**
     * 根据角色ID查询角色权限关联列表
     * 
     * @param roleId 角色ID
     * @return 角色权限关联列表
     */
    List<RolePermission> findByRoleId(@Param("roleId") Long roleId);

    /**
     * 批量插入角色权限关联关系
     * 
     * @param rolePermissions 角色权限关联列表
     * @return 插入数量
     */
    int insertBatch(List<RolePermission> rolePermissions);
}