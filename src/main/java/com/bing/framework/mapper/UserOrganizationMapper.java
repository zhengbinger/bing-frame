package com.bing.framework.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bing.framework.entity.UserOrganization;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 用户组织关联Mapper接口
 * 继承MyBatis-Plus的BaseMapper接口，提供用户组织关联表的数据访问功能
 * 支持根据用户ID、组织ID等条件查询关联关系
 * 
 * @author zhengbing
 * @date 2025-11-12
 */
@Mapper
public interface UserOrganizationMapper extends BaseMapper<UserOrganization> {

    /**
     * 根据用户ID查询关联的组织列表
     * @param userId 用户ID
     * @return 用户组织关联列表
     */
    List<UserOrganization> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据组织ID查询关联的用户列表
     * @param organizationId 组织ID
     * @return 用户组织关联列表
     */
    List<UserOrganization> selectByOrganizationId(@Param("organizationId") Long organizationId);

    /**
     * 查询用户的主组织
     * @param userId 用户ID
     * @return 用户组织关联对象
     */
    UserOrganization selectMainOrganizationByUserId(@Param("userId") Long userId);

    /**
     * 检查用户是否已关联到指定组织
     * @param userId 用户ID
     * @param organizationId 组织ID
     * @return 是否存在关联
     */
    boolean existsByUserIdAndOrganizationId(@Param("userId") Long userId, @Param("organizationId") Long organizationId);

    /**
     * 取消用户的主组织标记
     * @param userId 用户ID
     * @return 影响行数
     */
    int clearMainOrganizationByUserId(@Param("userId") Long userId);
}