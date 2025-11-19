package com.bing.framework.cache;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试用户服务类
 * 
 * 用于缓存集成测试的模拟用户服务
 * 
 * @author zhengbing
 * @date 2024-01-XX
 */
@Service
public class TestUserService {
    
    // 模拟用户数据存储
    private static final Map<String, TestUser> USER_STORE = new HashMap<>();
    
    static {
        // 初始化测试数据
        USER_STORE.put("user_001", new TestUser("user_001", "Original User", 25));
        USER_STORE.put("user_spring_001", new TestUser("user_spring_001", "Spring Test User", 30));
        USER_STORE.put("user_redis_001", new TestUser("user_redis_001", "Redis Test User", 28));
    }
    
    /**
     * 获取用户信息（使用自定义缓存）
     */
    public TestUser getUserById(String userId) {
        return getUserData(userId);
    }
    
    /**
     * 获取用户信息（使用Spring Cache注解）
     */
    @Cacheable(value = "userCache", key = "'user:' + #userId")
    public TestUser getUserByIdWithAnnotation(String userId) {
        return getUserData(userId);
    }
    
    /**
     * 实际获取用户数据的模拟方法
     */
    private TestUser getUserData(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return null;
        }
        
        TestUser user = USER_STORE.get(userId);
        if (user != null) {
            // 返回副本以避免外部修改影响缓存数据
            return new TestUser(user.getId(), user.getName(), user.getAge());
        }
        
        // 如果用户不存在，返回默认用户
        return new TestUser(userId, "Default User", 20);
    }
}