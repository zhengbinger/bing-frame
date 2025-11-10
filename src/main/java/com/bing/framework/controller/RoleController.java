package com.bing.framework.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bing.framework.annotation.AuditLogLevel;
import com.bing.framework.dto.RoleDTO;
import com.bing.framework.entity.Role;
import com.bing.framework.service.RoleService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色控制器
 * 提供角色管理相关的RESTful API接口
 * 
 * @author zhengbing
 * @date 2025-11-05
 */
@RestController
@RequestMapping("/api/roles")
@AuditLogLevel(module = "角色管理", value = AuditLogLevel.Level.BASIC)
public class RoleController {

    @Autowired
    private RoleService roleService;

    /**
     * 获取所有角色列表
     * 
     * @return 角色列表
     */
    @GetMapping
    public List<Role> listRoles() {
        return roleService.listAllRoles();
    }

    /**
     * 根据ID获取角色详情
     * 
     * @param id 角色ID
     * @return 角色详情
     */
    @GetMapping("/{id}")
    public RoleDTO getRole(@PathVariable Long id) {
        return roleService.getRoleById(id);
    }

    /**
     * 创建角色
     * 
     * @param roleDTO 角色数据
     * @return 创建的角色
     */
    @PostMapping
    @AuditLogLevel(value = AuditLogLevel.Level.FULL, description = "创建新角色")
    public Role createRole(@RequestBody RoleDTO roleDTO) {
        return roleService.createRole(roleDTO);
    }

    /**
     * 更新角色
     * 
     * @param id 角色ID
     * @param roleDTO 角色数据
     * @return 更新后的角色
     */
    @PutMapping("/{id}")
    @AuditLogLevel(value = AuditLogLevel.Level.FULL, description = "更新角色信息")
    public Role updateRole(@PathVariable Long id, @RequestBody RoleDTO roleDTO) {
        roleDTO.setId(id);
        return roleService.updateRole(roleDTO);
    }

    /**
     * 删除角色
     * 
     * @param id 角色ID
     */
    @DeleteMapping("/{id}")
    @AuditLogLevel(value = AuditLogLevel.Level.FULL, description = "删除角色")
    public void deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
    }

    /**
     * 根据用户ID获取用户拥有的角色列表
     * 
     * @param userId 用户ID
     * @return 角色列表
     */
    @GetMapping("/user/{userId}")
    public List<Role> getRolesByUserId(@PathVariable Long userId) {
        return roleService.getRolesByUserId(userId);
    }

    /**
     * 为用户分配角色
     * 
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     */
    @PostMapping("/assign/{userId}")
    @AuditLogLevel(value = AuditLogLevel.Level.FULL, description = "为用户分配角色")
    public void assignRolesToUser(@PathVariable Long userId, @RequestBody List<Long> roleIds) {
        roleService.assignRolesToUser(userId, roleIds);
    }
}