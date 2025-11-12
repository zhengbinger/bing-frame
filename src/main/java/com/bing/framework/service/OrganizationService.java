package com.bing.framework.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bing.framework.entity.Organization;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 组织服务层接口
 * 继承IService，提供基础的CRUD服务
 * 定义了组织管理的核心业务方法
 * 
 * @author zhengbing
 * @date 2025-11-12
 */
public interface OrganizationService extends IService<Organization> {

    /**
     * 根据ID获取组织
     * @param id 组织ID
     * @return Organization对象
     */
    Organization getOrganizationById(Long id);

    /**
     * 根据代码获取组织
     * @param code 组织代码
     * @return Organization对象
     */
    Organization getOrganizationByCode(String code);

    /**
     * 创建组织
     * @param organization 组织对象
     * @return 是否创建成功
     */
    boolean createOrganization(Organization organization);

    /**
     * 更新组织
     * @param organization 组织对象
     * @return 是否更新成功
     */
    boolean updateOrganization(Organization organization);

    /**
     * 删除组织
     * @param id 组织ID
     * @return 是否删除成功
     */
    boolean deleteOrganization(Long id);

    /**
     * 根据父组织ID获取子组织列表
     * @param parentId 父组织ID
     * @return 子组织列表
     */
    List<Organization> getOrganizationsByParentId(Long parentId);

    /**
     * 获取组织树形结构
     * @return 组织树列表
     */
    List<Organization> getOrganizationTree();

    /**
     * 获取启用的组织列表
     * @return 启用的组织列表
     */
    List<Organization> getEnabledOrganizations();

    /**
     * 检查是否存在循环引用
     * @param organizationId 组织ID
     * @param parentId 父组织ID
     * @return 是否存在循环引用
     */
    boolean checkCircularReference(Long organizationId, Long parentId);

    /**
     * 检查组织是否有子组织
     * @param organizationId 组织ID
     * @return 是否有子组织
     */
    boolean hasChildren(Long organizationId);

    /**
     * 根据条件分页查询组织
     * @param page 分页参数
     * @param queryWrapper 查询条件
     * @return 分页组织列表
     */
    <T> IPage<Organization> getOrganizationPage(Page<T> page, @Param("ew") Object queryWrapper);

    /**
     * 根据路径前缀查询组织
     * @param path 路径前缀
     * @return 组织列表
     */
    List<Organization> getOrganizationsByPathLike(String path);
}