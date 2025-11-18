package com.bing.framework.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bing.framework.entity.Organization;
import com.bing.framework.mapper.OrganizationMapper;
import com.bing.framework.service.OrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 组织服务层实现类
 * 实现OrganizationService接口，提供组织管理的具体业务逻辑
 * 
 * @author zhengbing
 * @date 2025-11-12
 */
@Service
public class OrganizationServiceImpl extends ServiceImpl<OrganizationMapper, Organization> implements OrganizationService {

    @Autowired
    private OrganizationMapper organizationMapper;

    @Override
    public Organization getOrganizationById(Long id) {
        return organizationMapper.selectById(id);
    }

    @Override
    public Organization getOrganizationByCode(String code) {
        return organizationMapper.selectByCode(code);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean createOrganization(Organization organization) {
        // 设置创建时间和更新时间
        Date now = new Date();
        organization.setCreateTime(now);
        organization.setUpdateTime(now);
        
        // 设置默认值
        if (organization.getSort() == null) {
            organization.setSort(0);
        }
        if (organization.getEnabled() == null) {
            organization.setEnabled(true);
        }
        
        // 先保存组织以生成ID
        boolean result = save(organization);
        
        // 组织保存成功后处理路径
        if (result) {
            String path;
            if (organization.getParentId() != null && organization.getParentId() > 0) {
                Organization parent = getOrganizationById(organization.getParentId());
                if (parent != null) {
                    // 设置路径
                    path = parent.getPath() + "/" + organization.getId();
                } else {
                    // 如果父组织不存在，设置为根路径
                    path = "/" + organization.getId();
                }
            } else {
                // 根组织
                path = "/" + organization.getId();
            }
            
            // 更新路径
            organization.setPath(path);
            updateById(organization);
        }
        
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean updateOrganization(Organization organization) {
        // 检查是否有循环引用
        if (organization.getParentId() != null && 
            organization.getId().equals(organization.getParentId())) {
            throw new IllegalArgumentException("组织不能设置自己为父组织");
        }
        
        if (organization.getParentId() != null && 
            checkCircularReference(organization.getId(), organization.getParentId())) {
            throw new IllegalArgumentException("设置父组织会导致循环引用");
        }
        
        // 更新时间
        organization.setUpdateTime(new Date());
        
        boolean result = updateById(organization);
        
        // 如果父组织发生变化，需要更新子组织的路径
        if (result && organization.getParentId() != null) {
            updateChildOrganizationPaths(organization.getId());
        }
        
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteOrganization(Long id) {
        // 检查是否有子组织
        if (hasChildren(id)) {
            throw new IllegalStateException("该组织下存在子组织，无法删除");
        }
        
        // 检查是否被用户关联
        // 这里可以添加与UserOrganization相关的检查
        
        return removeById(id);
    }

    @Override
    public List<Organization> getOrganizationsByParentId(Long parentId) {
        return organizationMapper.selectByParentId(parentId);
    }

    @Override
    public List<Organization> getOrganizationTree() {
        // 获取所有组织
        List<Organization> organizations = organizationMapper.selectList(null);
        
        // 构建树形结构
        return buildOrganizationTree(organizations);
    }

    @Override
    public List<Organization> getEnabledOrganizations() {
        return organizationMapper.selectEnabledOrganizations();
    }

    @Override
    public boolean checkCircularReference(Long organizationId, Long parentId) {
        if (organizationId == null || parentId == null) {
            return false;
        }
        
        // 向上递归检查是否存在循环引用
        Organization current = getOrganizationById(parentId);
        while (current != null && current.getParentId() != null) {
            if (current.getParentId().equals(organizationId)) {
                return true;
            }
            current = getOrganizationById(current.getParentId());
        }
        
        return false;
    }

    @Override
    public boolean hasChildren(Long organizationId) {
        return organizationMapper.hasChildren(organizationId);
    }

    @Override
    public <T> IPage<Organization> getOrganizationPage(Page<T> page, Object queryWrapper) {
        return organizationMapper.selectPageVo(page, queryWrapper);
    }

    @Override
    public List<Organization> getOrganizationsByPathLike(String path) {
        return organizationMapper.selectByPathLike(path);
    }

    /**
     * 构建组织树形结构
     * @param organizations 组织列表
     * @return 树形结构的组织列表
     */
    private List<Organization> buildOrganizationTree(List<Organization> organizations) {
        // 将组织按父ID分组
        Map<Long, List<Organization>> groupByParentId = organizations.stream()
                .collect(Collectors.groupingBy(Organization::getParentId));
        
        // 设置每个组织的子组织
        for (Organization organization : organizations) {
            List<Organization> children = groupByParentId.get(organization.getId());
            if (children != null && !children.isEmpty()) {
                organization.setChildren(children);
            }
        }
        
        // 返回根组织列表
        return organizations.stream()
                .filter(org -> org.getParentId() == null || org.getParentId() == 0)
                .sorted(Comparator.comparingInt(Organization::getSort))
                .collect(Collectors.toList());
    }

    /**
     * 更新子组织路径
     * @param parentId 父组织ID
     */
    private void updateChildOrganizationPaths(Long parentId) {
        Organization parent = getOrganizationById(parentId);
        if (parent == null) {
            return;
        }
        
        List<Organization> children = getOrganizationsByParentId(parentId);
        for (Organization child : children) {
            // 更新子组织路径
            child.setPath(parent.getPath() + "/" + child.getId());
            updateById(child);
            
            // 递归更新子组织的子组织
            updateChildOrganizationPaths(child.getId());
        }
    }
}