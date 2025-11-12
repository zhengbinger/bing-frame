package com.bing.framework.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bing.framework.entity.Organization;
import com.bing.framework.entity.UserOrganization;
import java.util.List;

/**
 * 用户组织关联服务层接口
 * 继承IService，提供基础的CRUD服务
 * 定义了用户组织关联的核心业务方法
 * 
 * @author zhengbing
 * @date 2025-11-12
 */
public interface UserOrganizationService extends IService<UserOrganization> {

    /**
     * 根据用户ID获取用户关联的组织列表
     * @param userId 用户ID
     * @return 组织列表
     */
    List<Organization> getUserOrganizationsByUserId(Long userId);

    /**
     * 获取用户的主组织
     * @param userId 用户ID
     * @return 主组织对象
     */
    Organization getUserMainOrganization(Long userId);

    /**
     * 根据组织ID获取关联的用户列表
     * @param organizationId 组织ID
     * @return 用户组织关联列表
     */
    List<UserOrganization> getOrganizationUsersByOrganizationId(Long organizationId);

    /**
     * 关联用户与组织
     * @param userId 用户ID
     * @param organizationId 组织ID
     * @param isMain 是否为主组织
     * @return 是否关联成功
     */
    boolean bindUserOrganization(Long userId, Long organizationId, Boolean isMain);

    /**
     * 解除用户与组织的关联
     * @param userId 用户ID
     * @param organizationId 组织ID
     * @return 是否解除成功
     */
    boolean unbindUserOrganization(Long userId, Long organizationId);

    /**
     * 批量关联用户与组织
     * @param userId 用户ID
     * @param organizationIds 组织ID列表
     * @return 是否关联成功
     */
    boolean batchBindUserOrganizations(Long userId, List<Long> organizationIds);

    /**
     * 批量解除用户与组织的关联
     * @param userId 用户ID
     * @param organizationIds 组织ID列表
     * @return 是否解除成功
     */
    boolean batchUnbindUserOrganizations(Long userId, List<Long> organizationIds);

    /**
     * 设置用户的主组织
     * @param userId 用户ID
     * @param organizationId 组织ID
     * @return 是否设置成功
     */
    boolean setMainOrganization(Long userId, Long organizationId);

    /**
     * 检查用户是否已关联指定组织
     * @param userId 用户ID
     * @param organizationId 组织ID
     * @return 是否已关联
     */
    boolean isUserInOrganization(Long userId, Long organizationId);
}