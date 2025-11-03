package com.bing.framework.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bing.framework.common.ErrorCode;
import com.bing.framework.entity.User;
import com.bing.framework.exception.BusinessException;
import com.bing.framework.mapper.UserMapper;
import com.bing.framework.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;
import java.util.List;

/**
 * 用户服务实现类
 * 继承ServiceImpl并实现UserService接口，提供用户管理的具体业务逻辑实现
 * 集成事务管理，包含数据校验、异常处理和业务规则验证
 * 
 * @author zhengbing
 * @date 2025-11-01
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    @Cacheable(value = "user", key = "#id")
    public User getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

    @Override
    @Cacheable(value = "user", key = "#username")
    public User getUserByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    @Override
    @Cacheable(value = "userList")
    public List<User> getAllUsers() {
        return userMapper.selectList(null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"userList", "user"}, allEntries = true)
    public boolean saveUser(User user) {
        // 检查用户名是否已存在
        User existingUser = userMapper.selectByUsername(user.getUsername());
        if (existingUser != null) {
            throw new BusinessException(ErrorCode.USER_EXIST);
        }
        
        // 检查邮箱是否已存在
        if (user.getEmail() != null) {
            existingUser = userMapper.selectByEmail(user.getEmail());
            if (existingUser != null) {
                throw new BusinessException(ErrorCode.USER_EXIST, "邮箱已存在");
            }
        }
        
        // 检查手机号是否已存在
        if (user.getPhone() != null) {
            existingUser = userMapper.selectByPhone(user.getPhone());
            if (existingUser != null) {
                throw new BusinessException(ErrorCode.USER_EXIST, "手机号已存在");
            }
        }
        
        // 设置默认值
        user.setStatus(1);
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        
        return userMapper.insert(user) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"userList", "user"}, allEntries = true)
    public boolean updateUser(User user) {
        // 检查用户是否存在
        User existingUser = userMapper.selectById(user.getId());
        if (existingUser == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        
        // 更新时间
        user.setUpdateTime(new Date());
        
        return userMapper.updateById(user) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"userList", "user"}, allEntries = true)
    public boolean deleteUser(Long id) {
        // 检查用户是否存在
        User existingUser = userMapper.selectById(id);
        if (existingUser == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        
        return userMapper.deleteById(id) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"userList", "user"}, allEntries = true)
    public boolean deleteBatch(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "请选择要删除的用户");
        }
        
        return userMapper.deleteBatchIds(ids) > 0;
    }
}