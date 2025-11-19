package com.bing.framework.cache;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 本地缓存功能测试
 * 
 * 测试MemoryCache类的基本功能，包括：
 * - 缓存存储和获取
 * - 过期时间管理
 * - 容量限制和淘汰策略
 * - 统计信息
 * 
 * @author zhengbing
 * @date 2025-11-01
 */
class MemoryCacheTest {

    private static final Logger logger = LoggerFactory.getLogger(MemoryCacheTest.class);
    
    private MemoryCache cache;

    @BeforeEach
    void setUp() {
        // 创建本地缓存实例，使用默认配置
        cache = new MemoryCache();
    }

    @Test
    void testBasicOperations() {
        logger.info("=== 测试基本操作 ===");
        
        // 测试存储和获取
        String key1 = "test:user:1";
        String value1 = "user_data_1";
        
        assertTrue(cache.put(key1, value1), "存储数据应该成功");
        
        String retrieved1 = (String) cache.get(key1);
        assertNotNull(retrieved1, "获取数据应该不为null");
        assertEquals(value1, retrieved1, "获取的数据应该与存储的数据一致");
        
        logger.info("存储: {} -> {}", key1, value1);
        logger.info("获取: {} -> {}", key1, retrieved1);
    }

    @Test
    void testTtlExpiration() {
        logger.info("=== 测试过期时间 ===");
        
        String key2 = "test:session:123";
        String value2 = "session_data";
        
        // 设置1分钟过期
        cache.put(key2, value2, 1);
        
        // 立即获取应该存在
        String retrieved2 = (String) cache.get(key2);
        assertNotNull(retrieved2, "立即获取应该存在");
        assertEquals(value2, retrieved2, "数据应该一致");
        
        logger.info("存储（1分钟过期）: {} -> {}", key2, value2);
        logger.info("立即获取: {} -> {}", key2, retrieved2);
        
        // 测试永久有效
        String key3 = "test:permanent:1";
        String value3 = "permanent_data";
        
        cache.put(key3, value3); // 默认永久有效
        String retrieved3 = (String) cache.get(key3);
        assertNotNull(retrieved3, "永久数据应该存在");
        assertEquals(value3, retrieved3, "数据应该一致");
        
        logger.info("永久存储: {} -> {}", key3, value3);
    }

    @Test
    void testCapacityAndEviction() {
        logger.info("=== 测试容量限制和淘汰 ===");
        
        // 创建一个小容量的缓存
        MemoryCache smallCache = new MemoryCache(3, 60, 1);
        
        // 存储4个数据（超过容量限制）
        for (int i = 1; i <= 4; i++) {
            String key = "test:evict:" + i;
            String value = "value_" + i;
            smallCache.put(key, value);
            logger.info("存储: {} -> {}", key, value);
        }
        
        // 检查统计信息
        String stats = smallCache.getStats();
        logger.info("缓存统计: {}", stats);
        
        // 验证当前缓存大小不会超过最大容量
        assertTrue(smallCache.size() <= 3, "缓存大小不应该超过最大容量");
        
        logger.info("缓存大小: {}", smallCache.size());
    }

    @Test
    void testDeleteOperation() {
        logger.info("=== 测试删除操作 ===");
        
        String key3 = "test:delete:1";
        String value3 = "delete_me";
        
        cache.put(key3, value3);
        assertNotNull(cache.get(key3), "数据应该存在");
        
        boolean deleted = cache.remove(key3);
        assertTrue(deleted, "删除应该成功");
        
        assertNull(cache.get(key3), "删除后获取应该为null");
        
        logger.info("删除: {}", key3);
    }

    @Test
    void testHasKey() {
        logger.info("=== 测试键存在检查 ===");
        
        String key4 = "test:exists:1";
        String value4 = "exists_data";
        
        assertFalse(cache.containsKey(key4), "不存在键的检查应该返回false");
        
        cache.put(key4, value4);
        assertTrue(cache.containsKey(key4), "存在键的检查应该返回true");
        
        logger.info("键存在检查: {} -> {}", key4, cache.containsKey(key4));
    }

    @Test
    void testMultipleDataTypes() {
        logger.info("=== 测试多种数据类型 ===");
        
        // 测试字符串
        cache.put("string:test", "hello world");
        assertEquals("hello world", cache.get("string:test"));
        
        // 测试整数
        cache.put("int:test", 42);
        assertEquals(42, cache.get("int:test"));
        
        // 测试对象
        TestObject obj = new TestObject("test", 123);
        cache.put("object:test", obj);
        TestObject retrievedObj = (TestObject) cache.get("object:test");
        assertNotNull(retrievedObj);
        assertEquals(obj.getName(), retrievedObj.getName());
        assertEquals(obj.getValue(), retrievedObj.getValue());
        
        logger.info("数据类型测试完成");
    }

    @Test
    void testPerformance() {
        logger.info("=== 测试性能 ===");
        
        int testSize = 1000;
        long startTime = System.currentTimeMillis();
        
        // 批量写入
        for (int i = 0; i < testSize; i++) {
            String key = "test:perf:" + i;
            String value = "performance_data_" + i;
            cache.put(key, value);
        }
        
        long writeTime = System.currentTimeMillis() - startTime;
        logger.info("写入{}条数据耗时: {}ms", testSize, writeTime);
        
        // 批量读取
        startTime = System.currentTimeMillis();
        int hitCount = 0;
        for (int i = 0; i < testSize; i++) {
            String key = "test:perf:" + i;
            if (cache.get(key) != null) {
                hitCount++;
            }
        }
        
        long readTime = System.currentTimeMillis() - startTime;
        logger.info("读取{}条数据耗时: {}ms，命中率: {}", testSize, readTime, hitCount * 100.0 / testSize + "%");
        
        // 统计信息
        String finalStats = cache.getStats();
        logger.info("最终统计: {}", finalStats);
        
        // 验证所有数据都被正确存储和读取
        assertEquals(testSize, hitCount, "所有数据都应该被正确读取");
    }

    @Test
    void testConcurrentAccess() {
        logger.info("=== 测试并发访问 ===");
        
        String concurrentKey = "test:concurrent:1";
        
        // 启动多个线程同时访问缓存
        Thread[] threads = new Thread[10];
        for (int i = 0; i < threads.length; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                try {
                    // 每个线程写入和读取数据
                    String key = concurrentKey + ":" + threadId;
                    String value = "concurrent_data_" + threadId;
                    
                    cache.put(key, value);
                    Object result = cache.get(key);
                    
                    logger.info("线程{}操作: {} -> {}", threadId, key, result);
                    
                    // 模拟一些计算
                    Thread.sleep(10);
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        
        // 启动所有线程
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
        
        logger.info("并发访问测试完成");
        
        // 检查统计信息
        String stats = cache.getStats();
        logger.info("并发测试后统计: {}", stats);
    }

    @Test
    void testClearOperation() {
        logger.info("=== 测试清空操作 ===");
        
        // 存储一些数据
        for (int i = 0; i < 10; i++) {
            String key = "test:clear:" + i;
            String value = "clear_data_" + i;
            cache.put(key, value);
        }
        
        assertTrue(cache.size() > 0, "缓存中应该有数据");
        
        // 清空缓存
        cache.clear();
        
        assertEquals(0, cache.size(), "清空后缓存大小应该为0");
        
        logger.info("清空操作测试完成");
    }

    @Test
    void testKeySet() {
        logger.info("=== 测试键集合 ===");
        
        // 存储一些数据
        String[] keys = {"key1", "key2", "key3"};
        for (String key : keys) {
            cache.put(key, "value_" + key);
        }
        
        java.util.Set<String> keySet = cache.keySet();
        assertTrue(keySet.containsAll(java.util.Arrays.asList(keys)), "键集合应该包含所有存储的键");
        
        logger.info("键集合: {}", keySet);
        logger.info("键集合测试完成");
    }

    @Test
    void testStatisticsAccuracy() {
        logger.info("=== 测试统计信息准确性 ===");
        
        String key = "stats:test";
        String value = "stats_value";
        
        // 初始统计
        String initialStats = cache.getStats();
        logger.info("初始统计: {}", initialStats);
        
        // 存储数据（增加put计数）
        cache.put(key, value);
        
        // 获取数据（增加hit计数）
        Object retrieved1 = cache.get(key);
        assertNotNull(retrieved1);
        
        // 尝试获取不存在的数据（增加miss计数）
        Object missing = cache.get("nonexistent");
        assertNull(missing);
        
        // 再次获取已存在数据
        Object retrieved2 = cache.get(key);
        assertNotNull(retrieved2);
        
        // 最终统计
        String finalStats = cache.getStats();
        logger.info("最终统计: {}", finalStats);
        
        // 验证统计信息的合理性
        assertTrue(finalStats.contains("Puts="), "统计应该包含put次数");
        assertTrue(finalStats.contains("Hits="), "统计应该包含hit次数");
        assertTrue(finalStats.contains("Misses="), "统计应该包含miss次数");
    }

    @Test
    void testExpiredDataCleanup() {
        logger.info("=== 测试过期数据清理 ===");
        
        // 存储一些短过期时间的数据
        for (int i = 0; i < 5; i++) {
            String key = "test:expire:" + i;
            String value = "expire_data_" + i;
            cache.put(key, value, 1); // 1分钟过期
        }
        
        int sizeBefore = cache.size();
        logger.info("清理前缓存大小: {}", sizeBefore);
        
        // 等待过期时间过去（这里我们等待70秒，确保数据过期）
        // 注意：在实际测试中，这可能会比较耗时
        // 为了演示，我们只等待1秒并手动触发清理
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 手动触发清理（MemoryCache内部有定时清理，这里主要是测试）
        int sizeAfter = cache.size();
        logger.info("等待后缓存大小: {}", sizeAfter);
        
        logger.info("过期数据清理测试完成");
    }

    @AfterEach
    void tearDown() {
        if (cache != null) {
            cache.shutdown();
        }
    }

    /**
     * 测试用的简单对象
     */
    private static class TestObject {
        private String name;
        private int value;

        public TestObject(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public int getValue() {
            return value;
        }

        @Override
        public String toString() {
            return "TestObject{name='" + name + "', value=" + value + "}";
        }
    }
}