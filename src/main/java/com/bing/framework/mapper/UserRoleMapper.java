package com.bing.framework.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bing.framework.entity.UserRole;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户角色关联Mapper接口
 * 提供用户角色关联关系的数据库操作方法
 * 
 * @author zhengbing
 * @date 2025-11-05
 */
public interface UserRoleMapper extends BaseMapper<UserRole> {

    /**
     * 根据用户ID删除用户角色关联关系
     * 
     * @param userId 用户ID
     * @return 删除数量
     */
    int deleteByUserId(@Param("userId") Long userId);

    /**
     * 根据角色ID删除用户角色关联关系
     * 
     * @param roleId 角色ID
     * @return 删除数量
     */
    int deleteByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据用户ID查询用户角色关联列表
     * 
     * @param userId 用户ID
     * @return 用户角色关联列表
     */
    List<UserRole> findByUserId(@Param("userId") Long userId);

    /**
     * 根据角色ID查询用户角色关联列表
     * 
     * @param roleId 角色ID
     * @return 用户角色关联列表
     */
    List<UserRole> findByRoleId(@Param("roleId") Long roleId);
    
    /**
     * 批量插入用户角色关联关系
     * 
     * @param userRoles 用户角色关联列表
     * @return 插入数量
     */
    int insertBatch(List<UserRole> userRoles);
}