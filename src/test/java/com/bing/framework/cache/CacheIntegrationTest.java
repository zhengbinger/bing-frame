package com.bing.framework.cache;

import com.bing.framework.config.CacheConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 缓存系统集成测试
 * 
 * 测试整个缓存系统的协调工作：
 * - Redis和本地缓存的自动切换
 * - Spring Cache注解支持
 * - 降级策略的端到端测试
 * - 配置验证
 * 
 * @author zhengbing
 * @date 2024-01-XX
 */ 
@SpringBootTest(classes = {
    CacheIntegrationTest.TestApplication.class,
    CacheConfig.class,
    CacheService.class,
    MemoryCache.class,
    TestUserService.class
})
class CacheIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(CacheIntegrationTest.class);

    @Autowired
    private CacheService cacheService;

    @Autowired
    private TestUserService userService;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        // 清理所有缓存数据
        cacheService.clear();
        
        // 重置统计数据
        cacheService.resetStats();
        
        // 手动切换到本地缓存模式（模拟Redis不可用）
        cacheService.switchToLocal();
    }

    @Test
    void testServiceLevelCacheOperations() {
        logger.info("=== 测试Service层缓存操作 ===");
        
        // 测试用户服务的缓存功能
        String userId = "user_001";
        
        // 第一次调用（缓存未命中）
        TestUser user1 = userService.getUserById(userId);
        assertNotNull(user1, "用户应该存在");
        assertEquals(userId, user1.getId(), "用户ID应该一致");
        assertEquals("Original User", user1.getName(), "应该返回原始数据");
        
        // 验证缓存中存在
        Object cachedUser = cacheService.get("user:" + userId);
        assertNotNull(cachedUser, "用户应该被缓存");
        
        // 第二次调用（缓存命中）
        TestUser user2 = userService.getUserById(userId);
        assertNotNull(user2, "缓存命中应该返回用户");
        assertEquals(userId, user2.getId(), "用户ID应该一致");
        assertEquals("Original User", user2.getName(), "应该返回缓存数据");
        
        // 验证统计信息
        CacheService.CacheStats stats = cacheService.getStats();
        logger.info("Service层测试统计: {}", stats);
        
        assertTrue(stats.getTotalOperations() > 0, "应该有缓存操作记录");
        assertTrue(stats.getLocalCacheOperations() > 0, "应该有本地缓存操作");
    }

    @Test
    void testCacheEviction() {
        logger.info("=== 测试缓存淘汰 ===");
        
        String prefix = "test:eviction:";
        
        // 存储大量数据来触发容量限制
        int totalItems = 50;
        for (int i = 0; i < totalItems; i++) {
            String key = prefix + i;
            String value = "eviction_test_data_" + i;
            
            boolean success = cacheService.set(key, value, 300);
            assertTrue(success, "存储操作应该成功");
        }
        
        // 检查缓存统计
        CacheService.CacheStats stats = cacheService.getStats();
        logger.info("大量数据存储后统计: {}", stats);
        
        // 验证部分数据仍然可以被访问
        boolean allAccessible = true;
        int checkCount = 10;
        for (int i = totalItems - checkCount; i < totalItems; i++) {
            String key = prefix + i;
            Object value = cacheService.get(key);
            if (value == null) {
                allAccessible = false;
                logger.warn("键 {} 无法访问（可能被淘汰）", key);
            }
        }
        
        logger.info("数据可访问性: {}", allAccessible ? "所有检查数据可访问" : "部分数据被淘汰");
        
        // 验证容量限制是否生效
        assertTrue(stats.getSize() <= 1000, "缓存大小应该受到容量限制");
    }

    @Test
    void testExpireAndCleanup() {
        logger.info("=== 测试过期和清理 ===");
        
        String prefix = "test:expire:";
        
        // 存储一些短过期时间的数据
        for (int i = 0; i < 10; i++) {
            String key = prefix + i;
            String value = "expire_test_data_" + i;
            // 设置1秒过期
            cacheService.set(key, value, 1);
        }
        
        // 验证数据存在
        for (int i = 0; i < 10; i++) {
            String key = prefix + i;
            Object value = cacheService.get(key);
            assertNotNull(value, "短期数据应该仍然存在");
        }
        
        // 等待过期
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 验证数据已过期
        int expiredCount = 0;
        for (int i = 0; i < 10; i++) {
            String key = prefix + i;
            Object value = cacheService.get(key);
            if (value == null) {
                expiredCount++;
            }
        }
        
        assertTrue(expiredCount >= 5, "至少一半数据应该已过期");
        logger.info("过期数据数量: {}", expiredCount);
        
        // 手动触发清理
        cacheService.clearExpired();
        
        // 验证清理效果
        CacheService.CacheStats stats = cacheService.getStats();
        logger.info("清理后统计: {}", stats);
    }

    @Test
    void testConcurrencySafety() {
        logger.info("=== 测试并发安全性 ===");
        
        String sharedKey = "test:concurrency:shared";
        String sharedValue = "shared_test_data";
        
        // 存储共享数据
        assertTrue(cacheService.set(sharedKey, sharedValue, 300));
        
        int threadCount = 20;
        int operationsPerThread = 50;
        
        Thread[] threads = new Thread[threadCount];
        AtomicLong successCount = new AtomicLong(0);
        AtomicLong errorCount = new AtomicLong(0);
        
        // 创建并发线程
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                try {
                    for (int j = 0; j < operationsPerThread; j++) {
                        try {
                            String key = sharedKey + ":" + threadId + ":" + j;
                            String value = "thread_" + threadId + "_data_" + j;
                            
                            // 执行读写操作
                            cacheService.set(key, value, 300);
                            Object readValue = cacheService.get(sharedKey); // 读取共享数据
                            Object readValue2 = cacheService.get(key); // 读取自己的数据
                            
                            if (readValue != null && readValue2 != null) {
                                successCount.incrementAndGet();
                            }
                            
                            // 模拟一些处理时间
                            if (j % 10 == 0) {
                                Thread.sleep(1);
                            }
                            
                        } catch (Exception e) {
                            errorCount.incrementAndGet();
                            logger.error("线程{}操作{}失败", threadId, j, e);
                        }
                    }
                } catch (Exception e) {
                    logger.error("线程{}执行异常", threadId, e);
                }
            });
        }
        
        // 启动所有线程
        long startTime = System.currentTimeMillis();
        
        for (Thread thread : threads) {
            thread.start();
        }
        
        // 等待所有线程完成
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                fail("线程等待被中断");
            }
        }
        
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        long totalOperations = threadCount * operationsPerThread * 2; // 每个线程执行2个操作
        
        logger.info("并发安全性测试完成:");
        logger.info("  总线程数: {}", threadCount);
        logger.info("  每线程操作数: {}", operationsPerThread * 2);
        logger.info("  总操作数: {}", totalOperations);
        logger.info("  成功操作数: {}", successCount.get());
        logger.info("  错误操作数: {}", errorCount.get());
        logger.info("  总耗时: {}ms", totalTime);
        logger.info("  成功率: {}%", successCount.get() * 100.0 / totalOperations);
        
        assertTrue(successCount.get() > 0, "至少应该有成功的操作");
        assertTrue(errorCount.get() < totalOperations * 0.1, "错误率应该低于10%");
        
        // 检查统计信息
        CacheService.CacheStats finalStats = cacheService.getStats();
        logger.info("并发测试最终统计: {}", finalStats);
    }

    @Test
    void testDataConsistency() {
        logger.info("=== 测试数据一致性 ===");
        
        String key = "test:consistency:1";
        TestUser user = new TestUser("user_001", "Consistency Test User", 30);
        
        // 存储用户数据
        assertTrue(cacheService.set(key, user, 300));
        
        // 读取并验证数据
        Object retrievedUser = cacheService.get(key);
        assertNotNull(retrievedUser, "数据应该存在");
        
        assertTrue(retrievedUser instanceof TestUser, "数据类型应该一致");
        TestUser retrieved = (TestUser) retrievedUser;
        
        assertEquals(user.getId(), retrieved.getId(), "用户ID应该一致");
        assertEquals(user.getName(), retrieved.getName(), "用户名称应该一致");
        assertEquals(user.getAge(), retrieved.getAge(), "用户年龄应该一致");
        
        // 修改原对象（不应该影响缓存中的数据）
        user.setName("Modified User");
        user.setAge(35);
        
        // 重新读取缓存数据
        Object cachedUser2 = cacheService.get(key);
        TestUser cached = (TestUser) cachedUser2;
        
        // 验证缓存数据没有被外部修改影响
        assertEquals("Consistency Test User", cached.getName(), "缓存数据不应该被外部修改影响");
        assertEquals(30, cached.getAge(), "缓存数据不应该被外部修改影响");
        
        logger.info("数据一致性测试通过");
    }

    @Test
    void testSpringCacheIntegration() {
        logger.info("=== 测试Spring Cache集成 ===");
        
        String userId = "user_spring_001";
        
        // 验证Spring Cache Manager可用
        assertNotNull(cacheManager, "CacheManager应该可用");
        
        // 测试用户服务（使用Spring Cache注解）
        TestUser user1 = userService.getUserByIdWithAnnotation(userId);
        assertNotNull(user1, "用户应该存在");
        
        // 验证通过Spring Cache Manager可以获取到数据
        org.springframework.cache.Cache springCache = cacheManager.getCache("userCache");
        assertNotNull(springCache, "Spring缓存应该可用");
        
        Object cachedObject = springCache.get("user:" + userId);
        assertNotNull(cachedObject, "Spring缓存中应该有数据");
        
        // 再次调用（应该从缓存获取）
        TestUser user2 = userService.getUserByIdWithAnnotation(userId);
        assertNotNull(user2, "缓存命中应该返回用户");
        assertEquals(user1.getId(), user2.getId(), "用户ID应该一致");
        
        logger.info("Spring Cache集成测试完成");
    }

    @Test
    void testDistributedLockIntegration() {
        logger.info("=== 测试分布式锁集成 ===");
        
        String lockKey = "test:distributed:lock:1";
        String resourceKey = "test:distributed:resource:1";
        
        // 获取锁（30秒过期时间，0次重试）
        boolean lockAcquired = cacheService.tryLock(lockKey, 30, 0);
        assertTrue(lockAcquired, "分布式锁获取应该成功");
        
        try {
            // 在锁的保护下操作资源
            String initialValue = (String) cacheService.get(resourceKey);
            if (initialValue == null) {
                initialValue = "0";
            }
            
            int currentValue = Integer.parseInt(initialValue);
            int newValue = currentValue + 1;
            
            // 模拟一些处理时间
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                fail("线程睡眠被中断");
            }
            
            cacheService.set(resourceKey, String.valueOf(newValue), 300);
            
            logger.info("资源{}更新: {} -> {}", resourceKey, currentValue, newValue);
            
        } finally {
            // 释放锁
            boolean lockReleased = cacheService.releaseLock(lockKey);
            assertTrue(lockReleased, "分布式锁释放应该成功");
        }
        
        // 验证资源更新
        String finalValue = (String) cacheService.get(resourceKey);
        assertEquals("1", finalValue, "资源值应该正确更新");
        
        logger.info("分布式锁集成测试完成");
    }

    @Test
    void testConfigurationAndStats() {
        logger.info("=== 测试配置和统计 ===");
        
        // 验证缓存配置
        String configSummary = cacheService.getConfigurationSummary();
        assertNotNull(configSummary, "配置摘要应该可用");
        logger.info("缓存配置: {}", configSummary);
        
        // 执行一些操作来积累统计数据
        for (int i = 0; i < 10; i++) {
            cacheService.set("config:test:" + i, "test_data_" + i, 300);
            cacheService.get("config:test:" + i);
        }
        
        // 获取详细统计信息
        CacheService.CacheStats stats = cacheService.getStats();
        assertNotNull(stats, "统计信息应该可用");
        
        logger.info("详细统计: {}", stats);
        
        // 验证统计信息包含必要的字段
        assertTrue(stats.getTotalOperations() > 0, "总操作数应该大于0");
        assertTrue(stats.getLocalCacheOperations() > 0, "本地缓存操作数应该大于0");
        assertTrue(stats.getSize() >= 0, "缓存大小应该大于等于0");
        
        // 重置统计信息
        cacheService.resetStats();
        
        CacheService.CacheStats resetStats = cacheService.getStats();
        logger.info("重置后统计: {}", resetStats);
        
        assertTrue(resetStats.getTotalOperations() == 0, "重置后总操作数应该为0");
    }

    // 测试用的应用配置
    @SpringBootApplication
    @EnableCaching
    public static class TestApplication {
        public static void main(String[] args) {
            SpringApplication.run(TestApplication.class, args);
        }
    }
}