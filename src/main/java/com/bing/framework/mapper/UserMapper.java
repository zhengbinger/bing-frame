package com.bing.framework.mapper;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bing.framework.entity.User;

/**
 * 用户Mapper接口
 * 继承MyBatis-Plus的BaseMapper接口，通过@Mapper注解注册到Spring容器
 * 提供用户表的基本CRUD操作和自定义查询方法
 * 
 * @author zhengbing
 * @date 2025-11-01
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户对象
     */
    User selectByUsername(String username);

    /**
     * 根据邮箱查询用户
     * @param email 邮箱
     * @return 用户对象
     */
    User selectByEmail(String email);

    /**
     * 根据手机号查询用户
     * @param phone 手机号
     * @return 用户对象
     */
    User selectByPhone(String phone);
    
    /**
     * 批量更新用户密码
     * @param userIds 用户ID列表
     * @param password 加密后的密码
     * @param updateTime 更新时间
     * @return 更新成功的数量
     */
    int batchUpdatePassword(List<Long> userIds, String password, Date updateTime);
    
    /**
     * 批量更新所有非BCrypt格式的密码
     * @param password 加密后的密码
     * @param updateTime 更新时间
     * @return 更新成功的数量
     */
    int batchUpdateNonBCryptPassword(String password, Date updateTime);
}