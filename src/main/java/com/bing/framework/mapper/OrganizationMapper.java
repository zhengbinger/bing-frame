package com.bing.framework.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bing.framework.entity.Organization;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 组织数据访问层接口
 * 继承MyBatis-Plus的BaseMapper，提供基础的CRUD操作
 * 同时定义了组织特定的查询方法
 * 
 * @author zhengbing
 * @date 2025-11-12
 */
@Mapper
public interface OrganizationMapper extends BaseMapper<Organization> {

    /**
     * 根据组织代码查询组织
     * @param code 组织代码
     * @return Organization对象
     */
    Organization selectByCode(@Param("code") String code);

    /**
     * 根据父组织ID查询子组织列表
     * @param parentId 父组织ID
     * @return 子组织列表
     */
    List<Organization> selectByParentId(@Param("parentId") Long parentId);

    /**
     * 查询所有启用的组织
     * @return 启用的组织列表
     */
    List<Organization> selectEnabledOrganizations();

    /**
     * 检查组织是否有子组织
     * @param organizationId 组织ID
     * @return 是否有子组织
     */
    boolean hasChildren(@Param("organizationId") Long organizationId);

    /**
     * 根据条件分页查询组织
     * @param page 分页参数
     * @param queryWrapper 查询条件
     * @return 分页组织列表
     */
    <T> IPage<Organization> selectPageVo(Page<T> page, @Param("ew") Object queryWrapper);

    /**
     * 根据路径前缀查询组织
     * 用于快速查询某个组织下的所有子组织
     * @param path 路径前缀
     * @return 组织列表
     */
    List<Organization> selectByPathLike(@Param("path") String path);
}