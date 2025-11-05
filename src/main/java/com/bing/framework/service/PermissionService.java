package com.bing.framework.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bing.framework.dto.PermissionDTO;
import com.bing.framework.entity.Permission;

import java.util.List;

/**
 * 权限服务接口
 * 提供权限管理相关的业务逻辑方法
 * 
 * @author zhengbing
 * @date 2025-11-05
 */
public interface PermissionService extends IService<Permission> {

    /**
     * 创建权限
     * 
     * @param permissionDTO 权限数据传输对象
     * @return 创建的权限
     */
    Permission createPermission(PermissionDTO permissionDTO);

    /**
     * 更新权限信息
     * 
     * @param permissionDTO 权限数据传输对象
     * @return 更新后的权限
     */
    Permission updatePermission(PermissionDTO permissionDTO);

    /**
     * 删除权限
     * 
     * @param id 权限ID
     */
    void deletePermission(Long id);

    /**
     * 根据ID获取权限
     * 
     * @param id 权限ID
     * @return 权限信息
     */
    PermissionDTO getPermissionById(Long id);

    /**
     * 获取所有权限列表
     * 
     * @return 权限列表
     */
    List<Permission> listAllPermissions();

    /**
     * 获取权限树（树形结构）
     * 
     * @return 权限树列表
     */
    List<PermissionDTO> getPermissionTree();

    /**
     * 根据角色ID获取权限列表
     * 
     * @param roleId 角色ID
     * @return 权限列表
     */
    List<Permission> getPermissionsByRoleId(Long roleId);

    /**
     * 根据用户ID获取权限列表
     * 
     * @param userId 用户ID
     * @return 权限列表
     */
    List<Permission> getPermissionsByUserId(Long userId);

    /**
     * 为角色分配权限
     * 
     * @param roleId 角色ID
     * @param permissionIds 权限ID列表
     */
    void assignPermissionsToRole(Long roleId, List<Long> permissionIds);

    /**
     * 检查权限编码是否已存在
     * 
     * @param code 权限编码
     * @param id 排除的权限ID（更新时使用）
     * @return 是否已存在
     */
    boolean isCodeExists(String code, Long id);

    /**
     * 检查用户是否拥有指定权限
     * 
     * @param userId 用户ID
     * @param permissionCode 权限编码
     * @return 是否拥有权限
     */
    boolean hasPermission(Long userId, String permissionCode);
}