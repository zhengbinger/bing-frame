package com.bing.framework.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bing.framework.entity.Permission;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 权限Mapper接口
 * 提供权限相关的数据库操作方法
 * 
 * @author zhengbing
 * @date 2025-11-05
 */
public interface PermissionMapper extends BaseMapper<Permission> {

    /**
     * 根据角色ID查询权限列表
     * 
     * @param roleId 角色ID
     * @return 权限列表
     */
    List<Permission> findPermissionsByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据用户ID查询权限列表
     * 
     * @param userId 用户ID
     * @return 权限列表
     */
    List<Permission> findPermissionsByUserId(@Param("userId") Long userId);

    /**
     * 检查权限编码是否已存在
     * 
     * @param code 权限编码
     * @param id 排除的权限ID（更新时使用）
     * @return 存在数量
     */
    int checkCodeExist(@Param("code") String code, @Param("id") Long id);

    /**
     * 根据父ID查询子权限列表
     * 
     * @param parentId 父权限ID
     * @return 子权限列表
     */
    List<Permission> findByParentId(@Param("parentId") Long parentId);
}