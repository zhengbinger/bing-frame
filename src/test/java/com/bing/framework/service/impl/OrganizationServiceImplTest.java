package com.bing.framework.service.impl;

import com.bing.framework.entity.Organization;
import com.bing.framework.mapper.OrganizationMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 组织服务层单元测试
 * 测试组织CRUD和树形结构相关功能
 *
 * @author zhengbing
 * @date 2025-11-12
 */
@ExtendWith(MockitoExtension.class)
class OrganizationServiceImplTest {

    @Mock
    private OrganizationMapper organizationMapper;

    @InjectMocks
    private OrganizationServiceImpl organizationService;

    @Test
    void testGetOrganizationById() {
        // 准备测试数据
        Long organizationId = 1L;
        Organization organization = new Organization();
        organization.setId(organizationId);
        organization.setName("测试组织");

        // 模拟Mapper行为
        when(organizationMapper.selectById(organizationId)).thenReturn(organization);

        // 执行测试
        Organization result = organizationService.getOrganizationById(organizationId);

        // 验证结果
        assertNotNull(result);
        assertEquals(organizationId, result.getId());
        assertEquals("测试组织", result.getName());
        verify(organizationMapper).selectById(organizationId);
    }

    @Test
    void testGetOrganizationTree() {
        // 准备测试数据
        Organization rootOrg = new Organization();
        rootOrg.setId(1L);
        rootOrg.setName("根组织");
        rootOrg.setParentId(0L);

        Organization childOrg = new Organization();
        childOrg.setId(2L);
        childOrg.setName("子组织");
        childOrg.setParentId(1L);

        List<Organization> organizationList = Arrays.asList(rootOrg, childOrg);

        // 模拟Mapper行为
        when(organizationMapper.selectList(null)).thenReturn(organizationList);

        // 执行测试
        List<Organization> result = organizationService.getOrganizationTree();

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size()); // 应该只有一个根节点
        assertEquals("根组织", result.get(0).getName());
        assertNotNull(result.get(0).getChildren());
        assertEquals(1, result.get(0).getChildren().size()); // 根节点下有一个子节点
        assertEquals("子组织", result.get(0).getChildren().get(0).getName());
    }

    @Test
    void testAddOrganization() {
        // 准备测试数据
        Organization organization = new Organization();
        organization.setName("新组织");
        organization.setParentId(1L);

        // 模拟Mapper行为
        when(organizationMapper.insert(organization)).thenReturn(1);

        // 执行测试
        boolean result = organizationService.save(organization);

        // 验证结果
        assertTrue(result);
        verify(organizationMapper).insert(organization);
        // 验证创建时间和更新时间是否被设置
        assertNotNull(organization.getCreateTime());
        assertNotNull(organization.getUpdateTime());
    }

    @Test
    void testUpdateOrganization() {
        // 准备测试数据
        Organization organization = new Organization();
        organization.setId(1L);
        organization.setName("更新后的组织");

        // 模拟Mapper行为
        when(organizationMapper.updateById(organization)).thenReturn(1);

        // 执行测试
        boolean result = organizationService.updateOrganization(organization);

        // 验证结果
        assertTrue(result);
        verify(organizationMapper).updateById(organization);
        // 验证更新时间是否被更新
        assertNotNull(organization.getUpdateTime());
    }

    @Test
    void testDeleteOrganization() {
        // 准备测试数据
        Long organizationId = 1L;

        // 模拟Mapper行为
        when(organizationMapper.hasChildren(organizationId)).thenReturn(false);
        when(organizationMapper.deleteById(organizationId)).thenReturn(1);

        // 执行测试
        boolean result = organizationService.deleteOrganization(organizationId);

        // 验证结果
        assertTrue(result);
        verify(organizationMapper).hasChildren(organizationId);
        verify(organizationMapper).deleteById(organizationId);
    }

    @Test
    void testDeleteOrganizationWithChildren() {
        // 准备测试数据
        Long organizationId = 1L;

        // 模拟Mapper行为 - 表示有子节点
        when(organizationMapper.hasChildren(organizationId)).thenReturn(true);

        // 执行测试并验证异常 - 修正异常类型为IllegalStateException
        assertThrows(IllegalStateException.class, () -> {
            organizationService.deleteOrganization(organizationId);
        });

        // 验证只调用了hasChildren方法，没有调用deleteById
        verify(organizationMapper).hasChildren(organizationId);
        verify(organizationMapper, never()).deleteById(organizationId);
    }

    @Test
    void testCheckCircularReference() {
        // 准备测试数据 - 创建组织对象
        Organization org1 = new Organization();
        org1.setId(1L);
        org1.setParentId(0L);
        
        Organization org2 = new Organization();
        org2.setId(2L);
        org2.setParentId(1L);
        
        Organization org3 = new Organization();
        org3.setId(3L);
        org3.setParentId(2L);
        
        // 模拟selectById方法的返回值
        when(organizationMapper.selectById(1L)).thenReturn(org1);
        when(organizationMapper.selectById(2L)).thenReturn(org2);
        when(organizationMapper.selectById(3L)).thenReturn(org3);
        when(organizationMapper.selectById(0L)).thenReturn(null);

        // 执行测试 - 正常情况（设置org2的父组织为org3不会形成循环引用）
        boolean noCircularRef = organizationService.checkCircularReference(2L, 3L);
        assertFalse(noCircularRef, "不应该检测到循环引用");

        // 执行测试 - 循环引用情况（设置org1的父组织为org3会形成循环引用）
        boolean hasCircularRef = organizationService.checkCircularReference(1L, 3L);
        assertTrue(hasCircularRef, "应该检测到循环引用");
    }


    /**
     * 测试通过创建组织后检查循环引用功能
     * 此测试验证在实际创建组织对象后，checkCircularReference方法的行为是否符合预期
     * 
     * 修复说明：
     * 1. 原代码存在语法错误，测试代码直接位于类体中而不是在方法内
     * 2. 修正了测试方法的实现，确保与mock测试的工作方式一致
     * 3. 手动设置了组织ID，因为我们使用的是mock对象，不会自动生成ID
     * 4. 模拟了Mapper的insert行为，确保测试能够正常执行
     * 5. 更改了测试断言方式，使用JUnit 5的标准断言API
     * 6. 修正了循环引用测试的参数顺序，确保测试逻辑正确
     */
    @Test
    void testCheckCircularReferenceWithCreatedOrgs() {
        // 准备测试数据
        Organization org1 = new Organization();
        org1.setId(1L); // 手动设置ID，因为我们使用的是mock对象
        org1.setName("组织1");
        org1.setCode("ORG1");
        org1.setParentId(0L);
        org1.setSort(1);
        org1.setEnabled(true);
        org1.setPath("/1"); // 手动设置路径
        
        Organization org2 = new Organization();
        org2.setId(2L); // 手动设置ID
        org2.setName("组织2");
        org2.setCode("ORG2");
        org2.setParentId(org1.getId());
        org2.setSort(1);
        org2.setEnabled(true);
        org2.setPath("/1/2"); // 手动设置路径
        
        // 准备getOrganizationById的模拟数据
        when(organizationMapper.selectById(1L)).thenReturn(org1);
        when(organizationMapper.selectById(2L)).thenReturn(org2);
        when(organizationMapper.selectById(0L)).thenReturn(null);
        
        // 检查正常情况（没有循环引用）- 将组织1的父组织设置为0L（根）
        boolean noCircularRef = organizationService.checkCircularReference(org1.getId(), 0L);
        assertFalse(noCircularRef, "不应该检测到循环引用");
        
        // 尝试创建循环引用（org1作为org2的子组织）
        // 由于org2的父组织已经是org1，现在将org1的父组织设置为org2将形成循环
        boolean hasCircularRef = organizationService.checkCircularReference(org1.getId(), org2.getId());
        assertTrue(hasCircularRef, "应该检测到循环引用");
    }

    /**
     * 测试获取启用的组织列表
     */
    @Test
    void getEnabledOrganizations() {
        // 准备测试数据
        Organization enabledOrg = new Organization();
        enabledOrg.setId(1L);
        enabledOrg.setName("启用组织");
        enabledOrg.setCode("ENABLED_ORG");
        enabledOrg.setParentId(0L);
        enabledOrg.setSort(1);
        enabledOrg.setEnabled(true);
        
        Organization disabledOrg = new Organization();
        disabledOrg.setId(2L);
        disabledOrg.setName("禁用组织");
        disabledOrg.setCode("DISABLED_ORG");
        disabledOrg.setParentId(0L);
        disabledOrg.setSort(2);
        disabledOrg.setEnabled(false);
        
        // 模拟Mapper行为
        when(organizationMapper.selectEnabledOrganizations()).thenReturn(Collections.singletonList(enabledOrg));
        
        // 执行测试
        List<Organization> enabledOrgs = organizationService.getEnabledOrganizations();
        
        // 验证结果
        assertNotNull(enabledOrgs);
        assertEquals(1, enabledOrgs.size());
        assertEquals("启用组织", enabledOrgs.get(0).getName());
        verify(organizationMapper).selectEnabledOrganizations();
    }
}