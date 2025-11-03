package com.bing.framework.service.impl;

import com.bing.framework.common.ErrorCode;
import com.bing.framework.common.Result;
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
import java.util.Optional;

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
        // 模拟Mapper抛出异常
        when(userMapper.insert(testUser)).thenThrow(new RuntimeException("数据库异常"));
        
        // 执行测试并验证异常
        BusinessException exception = Assertions.assertThrows(BusinessException.class, () -> {
            userService.saveUser(testUser);
        });
        
        verify(userMapper, times(1)).insert(testUser);
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
}