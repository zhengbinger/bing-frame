package com.bing.framework.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bing.framework.entity.Organization;
import com.bing.framework.entity.UserOrganization;
import com.bing.framework.mapper.OrganizationMapper;
import com.bing.framework.mapper.UserOrganizationMapper;
import com.bing.framework.service.UserOrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户组织关联服务层实现类
 * 实现UserOrganizationService接口，提供用户组织关联的具体业务逻辑
 * 
 * @author zhengbing
 * @date 2025-11-12
 */
@Service
public class UserOrganizationServiceImpl extends ServiceImpl<UserOrganizationMapper, UserOrganization> implements UserOrganizationService {

    @Autowired
    private UserOrganizationMapper userOrganizationMapper;

    @Autowired
    private OrganizationMapper organizationMapper;

    @Override
    public List<Organization> getUserOrganizationsByUserId(Long userId) {
        // 根据用户ID查询关联的组织
        List<UserOrganization> userOrganizations = userOrganizationMapper.selectByUserId(userId);
        
        // 提取组织信息
        return userOrganizations.stream()
                .filter(uo -> uo.getOrganization() != null)
                .map(UserOrganization::getOrganization)
                .collect(Collectors.toList());
    }

    @Override
    public Organization getUserMainOrganization(Long userId) {
        // 查询用户的主组织
        UserOrganization userOrganization = userOrganizationMapper.selectMainOrganizationByUserId(userId);
        if (userOrganization != null && userOrganization.getOrganization() != null) {
            return userOrganization.getOrganization();
        }
        return null;
    }

    @Override
    public List<UserOrganization> getOrganizationUsersByOrganizationId(Long organizationId) {
        return userOrganizationMapper.selectByOrganizationId(organizationId);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean bindUserOrganization(Long userId, Long organizationId, Boolean isMain) {
        // 检查是否已存在关联
        if (isUserInOrganization(userId, organizationId)) {
            throw new IllegalStateException("用户已关联到该组织");
        }
        
        // 检查组织是否存在
        Organization organization = organizationMapper.selectById(organizationId);
        if (organization == null) {
            throw new IllegalArgumentException("组织不存在");
        }
        
        // 如果设置为主组织，先清除该用户其他组织的主组织标识
        if (Boolean.TRUE.equals(isMain)) {
            userOrganizationMapper.clearMainOrganizationByUserId(userId);
        }
        
        // 创建关联
        UserOrganization userOrganization = new UserOrganization();
        userOrganization.setUserId(userId);
        userOrganization.setOrganizationId(organizationId);
        userOrganization.setIsMain(isMain);
        userOrganization.setCreateTime(LocalDateTime.now());
        userOrganization.setUpdateTime(LocalDateTime.now());
        
        return save(userOrganization);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean unbindUserOrganization(Long userId, Long organizationId) {
        // 检查关联是否存在
        if (!isUserInOrganization(userId, organizationId)) {
            throw new IllegalStateException("用户与组织不存在关联");
        }
        
        // 删除关联
        QueryWrapper<UserOrganization> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                   .eq("organization_id", organizationId);
        
        return remove(queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean batchBindUserOrganizations(Long userId, List<Long> organizationIds) {
        if (organizationIds == null || organizationIds.isEmpty()) {
            return true;
        }
        
        // 检查是否有重复的组织ID
        if (organizationIds.size() != organizationIds.stream().distinct().count()) {
            throw new IllegalArgumentException("组织ID列表中存在重复项");
        }
        
        // 批量创建关联
        LocalDateTime now = LocalDateTime.now();
        List<UserOrganization> userOrganizations = organizationIds.stream()
                .filter(organizationId -> !isUserInOrganization(userId, organizationId))
                .map(organizationId -> {
                    UserOrganization userOrganization = new UserOrganization();
                    userOrganization.setUserId(userId);
                    userOrganization.setOrganizationId(organizationId);
                    userOrganization.setIsMain(false); // 批量绑定默认不是主组织
                    userOrganization.setCreateTime(now);
                    userOrganization.setUpdateTime(now);
                    return userOrganization;
                })
                .collect(Collectors.toList());
        
        if (userOrganizations.isEmpty()) {
            return true;
        }
        
        return saveBatch(userOrganizations);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean batchUnbindUserOrganizations(Long userId, List<Long> organizationIds) {
        if (organizationIds == null || organizationIds.isEmpty()) {
            return true;
        }
        
        // 批量删除关联
        QueryWrapper<UserOrganization> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                   .in("organization_id", organizationIds);
        
        return remove(queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean setMainOrganization(Long userId, Long organizationId) {
        // 检查用户是否已关联该组织
        if (!isUserInOrganization(userId, organizationId)) {
            throw new IllegalStateException("用户未关联到该组织");
        }
        
        // 清除其他组织的主组织标识
        userOrganizationMapper.clearMainOrganizationByUserId(userId);
        
        // 设置新的主组织
        QueryWrapper<UserOrganization> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                   .eq("organization_id", organizationId);
        
        UserOrganization userOrganization = new UserOrganization();
        userOrganization.setIsMain(true);
        userOrganization.setUpdateTime(LocalDateTime.now());
        
        return update(userOrganization, queryWrapper);
    }

    @Override
    public boolean isUserInOrganization(Long userId, Long organizationId) {
        return userOrganizationMapper.existsByUserIdAndOrganizationId(userId, organizationId);
    }
}