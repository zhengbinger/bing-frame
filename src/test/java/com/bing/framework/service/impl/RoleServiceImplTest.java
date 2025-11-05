package com.bing.framework.service.impl;

import com.bing.framework.dto.RoleDTO;
import com.bing.framework.entity.Role;
import com.bing.framework.service.RoleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.annotation.Rollback;


import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 角色服务实现类测试
 * 由于测试环境可能使用内存数据库，这里只进行基本功能测试
 * 
 * @author zhengbing
 * @date 2025-11-05
 */
@SpringBootTest
public class RoleServiceImplTest {

    @Autowired
    private RoleService roleService;

    @Test
    @Transactional
    @Rollback
    public void testBasicRoleOperations() {
        // 测试保存角色
        Role role = new Role();
        role.setName("测试角色");
        role.setCode("TEST_ROLE_" + System.currentTimeMillis()); // 确保编码唯一
        role.setDescription("测试描述");
        role.setStatus(1);
        
        boolean saved = roleService.save(role);
        assertTrue(saved, "角色保存失败");
        assertNotNull(role.getId(), "角色ID未生成");
        
        // 测试查询角色
        Role found = roleService.getById(role.getId());
        assertNotNull(found, "角色查询失败");
        assertEquals("测试角色", found.getName(), "角色名称不匹配");
        
        // 测试更新角色
        found.setName("更新后的角色");
        found.setStatus(0);
        boolean updated = roleService.updateById(found);
        assertTrue(updated, "角色更新失败");
        
        Role updatedRole = roleService.getById(role.getId());
        assertEquals("更新后的角色", updatedRole.getName(), "更新后的角色名称不匹配");
        assertEquals(0, updatedRole.getStatus(), "更新后的角色状态不匹配");
        
        // 测试删除角色
        boolean deleted = roleService.removeById(role.getId());
        assertTrue(deleted, "角色删除失败");
        
        Role deletedRole = roleService.getById(role.getId());
        assertNull(deletedRole, "已删除的角色仍然可查询到");
    }
    
    @Test
    public void testRoleDTOValidation() {
        // 测试DTO创建和属性设置
        RoleDTO dto = new RoleDTO();
        dto.setName("DTO测试角色");
        dto.setCode("DTO_TEST");
        dto.setDescription("DTO测试描述");
        
        assertEquals("DTO测试角色", dto.getName(), "DTO名称设置错误");
        assertEquals("DTO_TEST", dto.getCode(), "DTO编码设置错误");
        assertEquals("DTO测试描述", dto.getDescription(), "DTO描述设置错误");
    }
}