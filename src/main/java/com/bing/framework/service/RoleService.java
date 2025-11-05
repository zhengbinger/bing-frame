package com.bing.framework.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bing.framework.dto.RoleDTO;
import com.bing.framework.entity.Role;

import java.util.List;

/**
 * 角色服务接口
 * 提供角色管理相关的业务逻辑方法
 * 
 * @author zhengbing
 * @date 2025-11-05
 */
public interface RoleService extends IService<Role> {

    /**
     * 创建角色
     * 
     * @param roleDTO 角色数据传输对象
     * @return 创建的角色
     */
    Role createRole(RoleDTO roleDTO);

    /**
     * 更新角色信息
     * 
     * @param roleDTO 角色数据传输对象
     * @return 更新后的角色
     */
    Role updateRole(RoleDTO roleDTO);

    /**
     * 删除角色
     * 
     * @param id 角色ID
     */
    void deleteRole(Long id);

    /**
     * 根据ID获取角色
     * 
     * @param id 角色ID
     * @return 角色信息
     */
    RoleDTO getRoleById(Long id);

    /**
     * 获取所有角色列表
     * 
     * @return 角色列表
     */
    List<Role> listAllRoles();

    /**
     * 根据用户ID获取用户拥有的角色列表
     * 
     * @param userId 用户ID
     * @return 角色列表
     */
    List<Role> getRolesByUserId(Long userId);

    /**
     * 为用户分配角色
     * 
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     */
    void assignRolesToUser(Long userId, List<Long> roleIds);

    /**
     * 检查角色编码是否已存在
     * 
     * @param code 角色编码
     * @param id 排除的角色ID（更新时使用）
     * @return 是否已存在
     */
    boolean isCodeExists(String code, Long id);
}