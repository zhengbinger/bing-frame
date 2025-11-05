package com.bing.framework.service.impl;

import com.bing.framework.dto.PermissionDTO;
import com.bing.framework.entity.Permission;
import com.bing.framework.service.PermissionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 权限服务实现类测试
 * 测试权限管理相关的核心功能
 * 
 * @author zhengbing
 * @date 2025-11-05
 */
@SpringBootTest
public class PermissionServiceImplTest {

    @Autowired
    private PermissionService permissionService;

    @Test
    @Transactional
    @Rollback
    public void testBasicPermissionOperations() {
        // 测试创建权限
        PermissionDTO permissionDTO = new PermissionDTO();
        permissionDTO.setName("测试权限");
        permissionDTO.setCode("TEST_PERMISSION_" + System.currentTimeMillis()); // 确保编码唯一
        permissionDTO.setDescription("测试描述");
        permissionDTO.setUrl("/api/test");
        permissionDTO.setMethod("GET");
        permissionDTO.setType(2); // API类型
        permissionDTO.setStatus(1);
        permissionDTO.setSort(1);
        
        Permission createdPermission = permissionService.createPermission(permissionDTO);
        assertNotNull(createdPermission, "权限创建失败");
        assertNotNull(createdPermission.getId(), "权限ID未生成");
        assertEquals(permissionDTO.getName(), createdPermission.getName(), "权限名称不匹配");
        assertEquals(permissionDTO.getCode(), createdPermission.getCode(), "权限编码不匹配");
        
        // 测试根据ID获取权限
        PermissionDTO foundDTO = permissionService.getPermissionById(createdPermission.getId());
        assertNotNull(foundDTO, "权限查询失败");
        assertEquals(permissionDTO.getName(), foundDTO.getName(), "查询的权限名称不匹配");
        
        // 测试更新权限
        foundDTO.setName("更新后的权限");
        foundDTO.setDescription("更新后的描述");
        foundDTO.setStatus(0);
        
        Permission updatedPermission = permissionService.updatePermission(foundDTO);
        assertNotNull(updatedPermission, "权限更新失败");
        assertEquals("更新后的权限", updatedPermission.getName(), "更新后的权限名称不匹配");
        assertEquals(0, updatedPermission.getStatus(), "更新后的权限状态不匹配");
        
        // 测试获取所有权限
        List<Permission> allPermissions = permissionService.listAllPermissions();
        assertNotNull(allPermissions, "权限列表获取失败");
        assertFalse(allPermissions.isEmpty(), "权限列表为空");
        
        // 测试删除权限
        permissionService.deletePermission(createdPermission.getId());
        PermissionDTO deletedDTO = permissionService.getPermissionById(createdPermission.getId());
        assertNull(deletedDTO, "已删除的权限仍然可查询到");
    }
    
    @Test
    @Transactional
    @Rollback
    public void testPermissionTree() {
        // 创建父权限
        PermissionDTO parentDTO = new PermissionDTO();
        parentDTO.setName("父级菜单");
        parentDTO.setCode("PARENT_MENU_" + System.currentTimeMillis());
        parentDTO.setDescription("父级菜单描述");
        parentDTO.setType(0); // 菜单类型
        parentDTO.setStatus(1);
        parentDTO.setSort(1);
        
        Permission parentPermission = permissionService.createPermission(parentDTO);
        assertNotNull(parentPermission, "父权限创建失败");
        
        // 创建子权限
        PermissionDTO childDTO = new PermissionDTO();
        childDTO.setName("子级按钮");
        childDTO.setCode("CHILD_BUTTON_" + System.currentTimeMillis());
        childDTO.setDescription("子级按钮描述");
        childDTO.setType(1); // 按钮类型
        childDTO.setParentId(parentPermission.getId());
        childDTO.setStatus(1);
        childDTO.setSort(1);
        
        Permission childPermission = permissionService.createPermission(childDTO);
        assertNotNull(childPermission, "子权限创建失败");
        
        // 测试获取权限树
        List<PermissionDTO> permissionTree = permissionService.getPermissionTree();
        assertNotNull(permissionTree, "权限树获取失败");
        assertFalse(permissionTree.isEmpty(), "权限树为空");
        
        // 查找父权限在树中的位置，并验证子权限
        boolean foundParent = false;
        for (PermissionDTO dto : permissionTree) {
            if (dto.getId().equals(parentPermission.getId())) {
                foundParent = true;
                assertNotNull(dto.getChildren(), "子权限列表为null");
                assertFalse(dto.getChildren().isEmpty(), "子权限列表为空");
                boolean foundChild = false;
                for (PermissionDTO child : dto.getChildren()) {
                    if (child.getId().equals(childPermission.getId())) {
                        foundChild = true;
                        assertEquals(childDTO.getName(), child.getName(), "子权限名称不匹配");
                        break;
                    }
                }
                assertTrue(foundChild, "在权限树中未找到子权限");
                break;
            }
        }
        assertTrue(foundParent, "在权限树中未找到父权限");
    }
    
    @Test
    @Transactional
    @Rollback
    public void testPermissionValidation() {
        // 测试创建权限
        PermissionDTO permissionDTO = new PermissionDTO();
        permissionDTO.setName("验证权限");
        String uniqueCode = "VALIDATION_PERMISSION_" + System.currentTimeMillis();
        permissionDTO.setCode(uniqueCode);
        permissionDTO.setType(2);
        permissionDTO.setStatus(1);
        
        permissionService.createPermission(permissionDTO);
        
        // 测试权限编码重复检查
        boolean codeExists = permissionService.isCodeExists(uniqueCode, null);
        assertTrue(codeExists, "权限编码存在检查失败");
        
        // 测试更新时的权限编码重复检查（排除当前ID）
        PermissionDTO updateDTO = new PermissionDTO();
        updateDTO.setId(permissionDTO.getId());
        updateDTO.setName(permissionDTO.getName());
        updateDTO.setCode(uniqueCode); // 使用相同的编码
        updateDTO.setType(permissionDTO.getType());
        updateDTO.setStatus(permissionDTO.getStatus());
        
        boolean updateCodeExists = permissionService.isCodeExists(uniqueCode, permissionDTO.getId());
        assertFalse(updateCodeExists, "更新时权限编码检查失败");
        
        // 测试创建重复编码的权限应该抛出异常
        PermissionDTO duplicateDTO = new PermissionDTO();
        duplicateDTO.setName("重复权限");
        duplicateDTO.setCode(uniqueCode);
        duplicateDTO.setType(2);
        duplicateDTO.setStatus(1);
        
        assertThrows(RuntimeException.class, () -> {
            permissionService.createPermission(duplicateDTO);
        }, "创建重复编码的权限未抛出异常");
    }
    
    @Test
    @Transactional
    @Rollback
    public void testAssignPermissionsToRole() {
        // 创建测试权限
        List<Long> permissionIds = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            PermissionDTO permissionDTO = new PermissionDTO();
            permissionDTO.setName("角色权限" + i);
            permissionDTO.setCode("ROLE_PERMISSION_" + i + "_" + System.currentTimeMillis());
            permissionDTO.setType(2);
            permissionDTO.setStatus(1);
            
            Permission permission = permissionService.createPermission(permissionDTO);
            permissionIds.add(permission.getId());
        }
        
        // 测试为角色分配权限（使用测试角色ID）
        Long testRoleId = 1L; // 假设角色ID为1
        permissionService.assignPermissionsToRole(testRoleId, permissionIds);
        
        // 测试获取角色的权限列表
        List<Permission> rolePermissions = permissionService.getPermissionsByRoleId(testRoleId);
        assertNotNull(rolePermissions, "角色权限获取失败");
        assertEquals(permissionIds.size(), rolePermissions.size(), "分配的权限数量不匹配");
        
        // 验证每个权限ID是否都在返回的列表中
        for (Long permissionId : permissionIds) {
            boolean found = false;
            for (Permission p : rolePermissions) {
                if (p.getId().equals(permissionId)) {
                    found = true;
                    break;
                }
            }
            assertTrue(found, "分配的权限ID " + permissionId + " 不在返回的权限列表中");
        }
        
        // 测试重新分配权限（覆盖原有权限）
        List<Long> newPermissionIds = new ArrayList<>();
        newPermissionIds.add(permissionIds.get(0)); // 只保留第一个权限
        
        permissionService.assignPermissionsToRole(testRoleId, newPermissionIds);
        
        List<Permission> updatedRolePermissions = permissionService.getPermissionsByRoleId(testRoleId);
        assertEquals(1, updatedRolePermissions.size(), "更新后的权限数量不匹配");
        assertEquals(permissionIds.get(0), updatedRolePermissions.get(0).getId(), "更新后的权限ID不匹配");
    }
}