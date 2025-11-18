package com.bing.framework.service.impl;

import com.bing.framework.common.ErrorCode;
import com.bing.framework.entity.User;
import com.bing.framework.exception.BusinessException;
import com.bing.framework.mapper.UserMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.mockito.Mockito.*;

/**
 * UserService实现类的单元测试
 * 使用Mockito框架模拟依赖，Junit5进行测试
 * 
 * @author zhengbing
 */
@SpringBootTest
public class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;
    
    @Mock
    private BCryptPasswordEncoder passwordEncoder;
    
    @InjectMocks
    private UserServiceImpl userService;
    
    private User testUser;
    
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("password123");
        testUser.setEmail("test@example.com");
    }
    
    @Test
    public void testCreateUser_Success() {
        // 模拟用户不存在（防止重复）
        when(userMapper.selectByUsername(testUser.getUsername())).thenReturn(null);
        when(userMapper.selectByEmail(testUser.getEmail())).thenReturn(null);
        when(userMapper.selectByPhone(null)).thenReturn(null);
        // 模拟Mapper行为
        when(userMapper.insert(testUser)).thenReturn(1);
        
        // 执行测试
        boolean result = userService.saveUser(testUser);
        
        // 验证结果
        Assertions.assertTrue(result);
        verify(userMapper).insert(testUser);
    }
    
    @Test
    public void testCreateUser_Failure() {
        // 模拟用户已存在
        when(userMapper.selectByUsername(testUser.getUsername())).thenReturn(testUser);
        
        // 执行测试并验证异常
        BusinessException exception = Assertions.assertThrows(BusinessException.class, () -> {
            userService.saveUser(testUser);
        });
        
        // 验证异常信息
        Assertions.assertEquals(ErrorCode.USER_EXIST.getCode(), exception.getCode());
        // 不应该调用insert方法
        verify(userMapper, never()).insert(testUser);
    }
    
    @Test
    public void testGetUserById_Success() {
        // 模拟Mapper行为
        when(userMapper.selectById(1L)).thenReturn(testUser);
        
        // 执行测试
        User result = userService.getUserById(1L);
        
        // 验证结果
        Assertions.assertNotNull(result);
        Assertions.assertEquals("testuser", result.getUsername());
        verify(userMapper, times(1)).selectById(1L);
    }
    
    @Test
    public void testGetUserById_NotFound() {
        // 模拟Mapper返回null
        when(userMapper.selectById(999L)).thenReturn(null);
        
        // 执行测试并验证异常
        BusinessException exception = Assertions.assertThrows(BusinessException.class, () -> {
            userService.getUserById(999L);
        });
        
        // 验证异常信息
        Assertions.assertEquals(ErrorCode.USER_NOT_FOUND.getCode(), exception.getCode());
        verify(userMapper, times(1)).selectById(999L);
    }
    
    @Test
    public void testGetAllUsers() {
        // 准备测试数据
        List<User> userList = new ArrayList<>();
        userList.add(testUser);
        
        // 模拟Mapper行为
        when(userMapper.selectList(null)).thenReturn(userList);
        
        // 执行测试
        List<User> result = userService.getAllUsers();
        
        // 验证结果
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        verify(userMapper, times(1)).selectList(null);
    }
    
    @Test
    public void testSaveUser_WithPasswordEncryption() {
        // 准备测试数据
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setPassword("plainpassword");
        
        // 模拟用户不存在（防止重复）
        when(userMapper.selectByUsername("newuser")).thenReturn(null);
        when(userMapper.selectByEmail(null)).thenReturn(null);
        when(userMapper.selectByPhone(null)).thenReturn(null);
        // 模拟Mapper行为
        when(userMapper.insert(any(User.class))).thenReturn(1);
        
        // 执行测试
        boolean result = userService.saveUser(newUser);
        
        // 验证结果
        Assertions.assertTrue(result);
        // 验证密码已被加密
        Assertions.assertNotEquals("plainpassword", newUser.getPassword());
        Assertions.assertTrue(newUser.getPassword().length() > 10); // 加密后的密码应该更长
        verify(userMapper, times(1)).insert(newUser);
    }
    
    @Test
    public void testUpdateUser_WithPasswordUpdate() {
        // 准备测试数据
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setPassword("encrypted-old-password");
        
        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setPassword("newpassword");
        
        // 模拟Mapper行为
        when(userMapper.selectById(1L)).thenReturn(existingUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);
        when(passwordEncoder.encode("newpassword")).thenReturn("encrypted-new-password");
        
        // 执行测试
        boolean result = userService.updateUser(updatedUser);
        
        // 验证结果
        Assertions.assertTrue(result);
        // 验证密码已被加密且与旧密码不同
        Assertions.assertNotEquals("newpassword", updatedUser.getPassword());
        Assertions.assertNotEquals(existingUser.getPassword(), updatedUser.getPassword());
        verify(userMapper, times(1)).updateById(updatedUser);
    }
    
    @Test
    public void testResetPassword_Success() {
        // 准备测试数据
        User user = new User();
        user.setId(1L);
        
        // 模拟Mapper行为
        when(userMapper.selectById(1L)).thenReturn(user);
        when(userMapper.updateById(any(User.class))).thenReturn(1);
        
        // 执行测试
        boolean result = userService.resetPassword(1L, "newpassword123");
        
        // 验证结果
        Assertions.assertTrue(result);
        // 验证密码已被加密
        Assertions.assertNotEquals("newpassword123", user.getPassword());
        verify(userMapper, times(1)).updateById(any(User.class));
    }
    
    @Test
    public void testGenerateAndResetPassword_Success() {
        // 准备测试数据
        User user = new User();
        user.setId(1L);
        
        // 模拟Mapper行为
        when(userMapper.selectById(1L)).thenReturn(user);
        when(userMapper.updateById(any(User.class))).thenReturn(1);
        
        // 执行测试
        String randomPassword = userService.generateAndResetPassword(1L);
        
        // 验证生成的密码
        Assertions.assertNotNull(randomPassword);
        Assertions.assertEquals(8, randomPassword.length()); // 默认生成8位密码
        // 验证密码已被加密
        Assertions.assertNotEquals(randomPassword, user.getPassword());
        verify(userMapper, times(1)).updateById(any(User.class));
    }
    
    @Test
    public void testPasswordEncryption() {
        // 测试密码加密功能
        String plainPassword = "Test@123456";
        
        // 模拟密码编码器行为
        when(passwordEncoder.encode(plainPassword)).thenReturn("encrypted-password");
        when(passwordEncoder.matches(plainPassword, "encrypted-password")).thenReturn(true);
        when(passwordEncoder.matches("wrongpassword", "encrypted-password")).thenReturn(false);
        
        // 执行密码加密
        String encryptedPassword = passwordEncoder.encode(plainPassword);
        
        // 验证加密后的密码与原密码不同
        Assertions.assertNotEquals(plainPassword, encryptedPassword);
        
        // 验证密码验证功能
        Assertions.assertTrue(passwordEncoder.matches(plainPassword, encryptedPassword));
        Assertions.assertFalse(passwordEncoder.matches("wrongpassword", encryptedPassword));
    }
}