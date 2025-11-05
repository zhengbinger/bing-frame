package com.bing.framework.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bing.framework.entity.Role;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色Mapper接口
 * 提供角色相关的数据库操作方法
 * 
 * @author zhengbing
 * @date 2025-11-05
 */
public interface RoleMapper extends BaseMapper<Role> {

    /**
     * 根据用户ID查询用户拥有的角色列表
     * 
     * @param userId 用户ID
     * @return 角色列表
     */
    List<Role> findRolesByUserId(@Param("userId") Long userId);

    /**
     * 检查角色编码是否已存在
     * 
     * @param code 角色编码
     * @param id 排除的角色ID（更新时使用）
     * @return 存在数量
     */
    int checkCodeExist(@Param("code") String code, @Param("id") Long id);
}