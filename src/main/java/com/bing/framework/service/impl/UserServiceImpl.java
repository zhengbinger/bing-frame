package com.bing.framework.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bing.framework.common.ErrorCode;
import com.bing.framework.entity.Role;
import com.bing.framework.entity.User;
import com.bing.framework.exception.BusinessException;
import com.bing.framework.mapper.UserMapper;
import com.bing.framework.service.RoleService;
import com.bing.framework.service.UserService;
import com.bing.framework.util.PasswordValidator;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    
    @Autowired
    private RoleService roleService;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    @Autowired
    private PasswordValidator passwordValidator;
    
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

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
        
        // 密码加密
        if (user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
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
        
        // 如果密码有更新，需要加密
        if (user.getPassword() != null && !user.getPassword().equals(existingUser.getPassword())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"userList", "user"}, allEntries = true)
    public boolean resetPassword(Long id, String newPassword) {
        // 检查用户是否存在
        User existingUser = userMapper.selectById(id);
        if (existingUser == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        
        // 加密新密码
        String encryptedPassword = passwordEncoder.encode(newPassword);
        
        // 更新密码
        User user = new User();
        user.setId(id);
        user.setPassword(encryptedPassword);
        user.setUpdateTime(new Date());
        
        return userMapper.updateById(user) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"userList", "user"}, allEntries = true)
    public String generateAndResetPassword(Long id) {
        // 生成8位随机密码
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()";
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            int index = (int) (Math.random() * chars.length());
            password.append(chars.charAt(index));
        }
        String randomPassword = password.toString();
        
        // 重置密码
        resetPassword(id, randomPassword);
        
        // 返回明文密码（注意：实际项目中应通过安全渠道发送给用户）
        return randomPassword;
    }
    
    @Override
    public List<Role> getUserRoles(Long userId) {
        // 检查用户是否存在
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        
        return roleService.getRolesByUserId(userId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"userList", "user"}, allEntries = true)
    public void assignRolesToUser(Long userId, List<Long> roleIds) {
        // 检查用户是否存在
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        
        roleService.assignRolesToUser(userId, roleIds);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"userList", "user"}, allEntries = true)
    public int batchResetPassword(List<Long> userIds, String newPassword) {
        if (userIds == null || userIds.isEmpty()) {
            return 0;
        }
        
        // 验证密码强度
        String validationResult = passwordValidator.validatePassword(newPassword);
        if (validationResult != null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, validationResult);
        }
        
        // 加密新密码
        String encryptedPassword = passwordEncoder.encode(newPassword);
        Date now = new Date();
        
        // 批量更新密码
        int count = userMapper.batchUpdatePassword(userIds, encryptedPassword, now);
        log.info("批量重置密码成功，共更新{}个用户的密码", count);
        return count;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"userList", "user"}, allEntries = true)
    public int batchResetNonBCryptPassword(String newPassword) {
        // 验证密码强度
        String validationResult = passwordValidator.validatePassword(newPassword);
        if (validationResult != null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, validationResult);
        }
        
        // 加密新密码
        String encryptedPassword = passwordEncoder.encode(newPassword);
        Date now = new Date();
        
        // 更新所有非BCrypt格式的密码
        int count = userMapper.batchUpdateNonBCryptPassword(encryptedPassword, now);
        log.info("批量重置非BCrypt格式密码成功，共更新{}个用户的密码", count);
        return count;
    }
}