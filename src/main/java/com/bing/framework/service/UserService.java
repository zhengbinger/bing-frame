package com.bing.framework.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bing.framework.entity.User;
import java.util.List;

/**
 * 用户服务接口
 * 继承MyBatis-Plus的IService接口，扩展自定义业务方法，实现用户管理的核心业务逻辑
 * 定义用户查询、新增、更新、删除等基础操作的方法规范
 * 
 * @author zhengbing
 * @date 2025-11-01
 */
public interface UserService extends IService<User> {

    /**
     * 根据ID查询用户
     * @param id 用户ID
     * @return 用户对象
     */
    User getUserById(Long id);

    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户对象
     */
    User getUserByUsername(String username);

    /**
     * 查询所有用户
     * @return 用户列表
     */
    List<User> getAllUsers();

    /**
     * 新增用户
     * @param user 用户对象
     * @return 是否成功
     */
    boolean saveUser(User user);

    /**
     * 更新用户
     * @param user 用户对象
     * @return 是否成功
     */
    boolean updateUser(User user);

    /**
     * 删除用户
     * @param id 用户ID
     * @return 是否成功
     */
    boolean deleteUser(Long id);

    /**
     * 批量删除用户
     * @param ids 用户ID列表
     * @return 是否成功
     */
    boolean deleteBatch(List<Long> ids);
    
    /**
     * 重置用户密码
     * @param id 用户ID
     * @param newPassword 新密码
     * @return 是否成功
     */
    boolean resetPassword(Long id, String newPassword);
    
    /**
     * 生成随机密码并重置
     * @param id 用户ID
     * @return 生成的随机密码
     */
    String generateAndResetPassword(Long id);
}