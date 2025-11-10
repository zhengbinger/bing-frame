package com.bing.framework.controller;

import com.bing.framework.dto.PermissionDTO;
import com.bing.framework.entity.Permission;
import com.bing.framework.service.PermissionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 权限控制器
 * 提供权限管理相关的API接口
 * 
 * @author zhengbing
 * @date 2025-11-05
 */
@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    /**
     * 创建权限
     * 
     * @param permissionDTO 权限数据传输对象
     * @return 创建的权限
     */
    @PostMapping
    public ResponseEntity<Permission> createPermission(@RequestBody PermissionDTO permissionDTO) {
        Permission permission = permissionService.createPermission(permissionDTO);
        return ResponseEntity.ok(permission);
    }

    /**
     * 更新权限信息
     * 
     * @param id 权限ID
     * @param permissionDTO 权限数据传输对象
     * @return 更新后的权限
     */
    @PutMapping("/{id}")
    public ResponseEntity<Permission> updatePermission(@PathVariable Long id, @RequestBody PermissionDTO permissionDTO) {
        permissionDTO.setId(id);
        Permission permission = permissionService.updatePermission(permissionDTO);
        return ResponseEntity.ok(permission);
    }

    /**
     * 删除权限
     * 
     * @param id 权限ID
     * @return 响应状态
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePermission(@PathVariable Long id) {
        permissionService.deletePermission(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 根据ID获取权限
     * 
     * @param id 权限ID
     * @return 权限信息
     */
    @GetMapping("/{id}")
    public ResponseEntity<PermissionDTO> getPermissionById(@PathVariable Long id) {
        PermissionDTO permissionDTO = permissionService.getPermissionById(id);
        return ResponseEntity.ok(permissionDTO);
    }

    /**
     * 获取所有权限列表
     * 
     * @return 权限列表
     */
    @GetMapping
    public ResponseEntity<List<Permission>> listAllPermissions() {
        List<Permission> permissions = permissionService.listAllPermissions();
        return ResponseEntity.ok(permissions);
    }

    /**
     * 获取权限树（树形结构）
     * 
     * @return 权限树列表
     */
    @GetMapping("/tree")
    public ResponseEntity<List<PermissionDTO>> getPermissionTree() {
        List<PermissionDTO> permissionTree = permissionService.getPermissionTree();
        return ResponseEntity.ok(permissionTree);
    }

    /**
     * 根据角色ID获取权限列表
     * 
     * @param roleId 角色ID
     * @return 权限列表
     */
    @GetMapping("/by-role/{roleId}")
    public ResponseEntity<List<Permission>> getPermissionsByRoleId(@PathVariable Long roleId) {
        List<Permission> permissions = permissionService.getPermissionsByRoleId(roleId);
        return ResponseEntity.ok(permissions);
    }

    /**
     * 为角色分配权限
     * 
     * @param roleId 角色ID
     * @param permissionIds 权限ID列表
     * @return 响应状态
     */
    @PostMapping("/assign-to-role/{roleId}")
    public ResponseEntity<Void> assignPermissionsToRole(@PathVariable Long roleId, @RequestBody List<Long> permissionIds) {
        permissionService.assignPermissionsToRole(roleId, permissionIds);
        return ResponseEntity.ok().build();
    }

    /**
     * 检查权限编码是否已存在
     * 
     * @param code 权限编码
     * @param id 排除的权限ID（更新时使用）
     * @return 是否已存在
     */
    @GetMapping("/check-code")
    public ResponseEntity<Boolean> checkCodeExists(@RequestParam String code, @RequestParam(required = false) Long id) {
        boolean exists = permissionService.isCodeExists(code, id);
        return ResponseEntity.ok(exists);
    }
}