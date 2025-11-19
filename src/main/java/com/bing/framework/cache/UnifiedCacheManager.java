package com.bing.framework.cache;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bing.framework.util.RedisUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * 统一缓存管理器
 * 支持Redis和本地缓存的自动切换，提供统一的高可用缓存服务
 * 
 * @author zhengbing
 * @date 2025-11-01
 */
@Slf4j
@Component
public class UnifiedCacheManager {
    
    @Autowired(required = false)
    private RedisUtil redisUtil;
    
    // 本地缓存实例
    private final MemoryCache memoryCache;
    
    // 缓存状态管理
    private final AtomicBoolean redisAvailable = new AtomicBoolean(false);
    private final AtomicReference<String> currentCacheType = new AtomicReference<>("UNKNOWN");
    
    // Redis连接测试相关
    private static final String REDIS_TEST_KEY = "unified_cache_manager:test_connection";
    private static final int REDIS_TEST_TIMEOUT = 3000; // 3秒超时
    
    /**
     * 构造函数
     */
    public UnifiedCacheManager() {
        this.memoryCache = new MemoryCache(2000, 60, 5); // 最大2000个，默认60分钟过期，5分钟清理一次
        initializeCacheSystem();
    }
    
    /**
     * 初始化缓存系统
     */
    private void initializeCacheSystem() {
        log.info("初始化统一缓存管理器...");
        
        // 启动时检查Redis连接状态
        checkRedisConnection();
        
        // 定期检查Redis连接状态（每30秒检查一次）
        startPeriodicCheck();
        
        log.info("统一缓存管理器初始化完成，当前使用缓存: {}", currentCacheType.get());
    }
    
    /**
     * 启动定期检查Redis连接状态
     */
    private void startPeriodicCheck() {
        Thread redisCheckThread = new Thread(() -> {
            while (true) {
                try {
                    checkRedisConnection();
                    Thread.sleep(30000); // 30秒检查一次
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.warn("定期检查Redis连接时发生异常", e);
                    try {
                        Thread.sleep(10000); // 异常时10秒后重试
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }, "Redis-Connection-Check");
        
        redisCheckThread.setDaemon(true);
        redisCheckThread.start();
        
        log.info("启动Redis连接定期检查线程");
    }
    
    /**
     * 检查Redis连接状态
     */
    private void checkRedisConnection() {
        boolean wasRedisAvailable = redisAvailable.get();
        boolean nowRedisAvailable = false;
        String previousType = currentCacheType.get();
        
        try {
            if (redisUtil != null) {
                log.info("开始重连Redis操作，测试连接可用性...");
                
                // 尝试ping Redis
                redisUtil.set(REDIS_TEST_KEY, "test", 10, TimeUnit.SECONDS);
                Object testValue = redisUtil.get(REDIS_TEST_KEY);
                redisUtil.delete(REDIS_TEST_KEY);
                
                if ("test".equals(testValue)) {
                    nowRedisAvailable = true;
                    log.info("Redis连接测试成功，Redis服务可用");
                } else {
                    nowRedisAvailable = false;
                    log.info("Redis连接测试失败，返回值不匹配，使用本地缓存");
                }
            } else {
                log.info("RedisUtil未初始化，使用本地缓存");
                nowRedisAvailable = false;
            }
        } catch (Exception e) {
            log.info("Redis暂时无法连接");
            nowRedisAvailable = false;
        }
        
        // 更新连接状态
        redisAvailable.set(nowRedisAvailable);
        
        // 判断当前使用的缓存类型
        String newCacheType = nowRedisAvailable ? "REDIS" : "MEMORY";
        currentCacheType.set(newCacheType);
        
        // 记录状态变化
        if (wasRedisAvailable != nowRedisAvailable) {
            if (nowRedisAvailable) {
                log.info("Redis连接已恢复，切换到Redis缓存");
            } else {
                log.info("Redis连接暂时不可用，继续使用本地缓存，程序正常运行不受影响");
            }
        } else if (!previousType.equals(newCacheType)) {
            log.info("缓存类型切换: {} -> {}", previousType, newCacheType);
        }
    }
    
    /**
     * 统一设置缓存
     * 
     * @param key 缓存键
     * @param value 缓存值
     * @param ttlMinutes 过期时间（分钟），-1表示永久有效
     * @return boolean 是否成功
     */
    public boolean set(String key, Object value, long ttlMinutes) {
        try {
            if (redisAvailable.get()) {
                // 使用Redis
                if (ttlMinutes > 0) {
                    return redisUtil.set(key, value, ttlMinutes, TimeUnit.MINUTES);
                } else {
                    return redisUtil.set(key, value);
                }
            } else {
                // 使用本地缓存
                return memoryCache.put(key, value, ttlMinutes);
            }
        } catch (Exception e) {
            log.info("Redis暂时无法连接");
            return memoryCache.put(key, value, ttlMinutes);
        }
    }
    
    /**
     * 统一获取缓存
     * 
     * @param key 缓存键
     * @return Object 缓存值
     */
    public Object get(String key) {
        try {
            if (redisAvailable.get()) {
                // 使用Redis
                return redisUtil.get(key);
            } else {
                // 使用本地缓存
                return memoryCache.get(key);
            }
        } catch (Exception e) {
            log.info("Redis暂时无法连接");
            return memoryCache.get(key);
        }
    }
    
    /**
     * 统一删除缓存
     * 
     * @param key 缓存键
     * @return boolean 是否成功
     */
    public boolean delete(String key) {
        try {
            if (redisAvailable.get()) {
                // 使用Redis
                return redisUtil.delete(key);
            } else {
                // 使用本地缓存
                return memoryCache.remove(key);
            }
        } catch (Exception e) {
            log.info("Redis暂时无法连接");
            return memoryCache.remove(key);
        }
    }
    
    /**
     * 批量删除缓存
     * 
     * @param keys 缓存键集合
     * @return long 删除的数量
     */
    public long delete(String... keys) {
        if (keys == null || keys.length == 0) {
            return 0;
        }
        
        try {
            if (redisAvailable.get()) {
                // 使用Redis
                return redisUtil.delete(keys);
            } else {
                // 使用本地缓存
                return memoryCache.remove(keys);
            }
        } catch (Exception e) {
            log.info("Redis暂时无法连接");
            return memoryCache.remove(keys);
        }
    }
    
    /**
     * 检查缓存是否存在
     * 
     * @param key 缓存键
     * @return boolean 是否存在
     */
    public boolean hasKey(String key) {
        try {
            if (redisAvailable.get()) {
                // 使用Redis
                return Boolean.TRUE.equals(redisUtil.hasKey(key));
            } else {
                // 使用本地缓存
                return memoryCache.containsKey(key);
            }
        } catch (Exception e) {
            log.info("Redis暂时无法连接");
            return memoryCache.containsKey(key);
        }
    }
    
    /**
     * 清空所有缓存
     */
    public void clear() {
        try {
            if (redisAvailable.get()) {
                // 使用Redis - 注意：生产环境要谨慎使用flushAll
                log.warn("执行Redis FLUSHALL操作，这会清空整个Redis数据库");
                redisUtil.clear();
            } else {
                // 使用本地缓存
                memoryCache.clear();
            }
        } catch (Exception e) {
            log.info("Redis暂时无法连接");
        }
    }
    
    /**
     * 获取缓存统计信息
     * 
     * @return 统计信息
     */
    public String getStats() {
        StringBuilder stats = new StringBuilder();
        stats.append("统一缓存管理器状态:\n");
        stats.append("当前缓存类型: ").append(currentCacheType.get()).append("\n");
        stats.append("Redis连接状态: ").append(redisAvailable.get() ? "可用" : "不可用").append("\n");
        
        if (!redisAvailable.get()) {
            stats.append("本地缓存统计:\n");
            stats.append(memoryCache.getStats());
        }
        
        return stats.toString();
    }
    
    /**
     * 强制切换到指定缓存类型
     * 
     * @param cacheType 缓存类型 ("REDIS" 或 "MEMORY")
     */
    public void forceSwitchCacheType(String cacheType) {
        if ("REDIS".equalsIgnoreCase(cacheType)) {
            redisAvailable.set(true);
            currentCacheType.set("REDIS");
            log.warn("强制切换到Redis缓存");
        } else if ("MEMORY".equalsIgnoreCase(cacheType)) {
            redisAvailable.set(false);
            currentCacheType.set("MEMORY");
            log.warn("强制切换到本地缓存");
        } else {
            log.error("不支持的缓存类型: {}", cacheType);
        }
    }
    
    /**
     * 获取当前使用的缓存类型
     * 
     * @return 缓存类型 ("REDIS" 或 "MEMORY")
     */
    public String getCurrentCacheType() {
        return currentCacheType.get();
    }
    
    /**
     * 检查Redis是否可用
     * 
     * @return boolean
     */
    public boolean isRedisAvailable() {
        return redisAvailable.get();
    }
    
    /**
     * 手动触发Redis连接检查
     */
    public void checkRedisConnectionManually() {
        log.info("手动触发Redis连接检查");
        checkRedisConnection();
    }
    
    /**
     * 清理过期缓存
     */
    public void clearExpired() {
        try {
            if (redisAvailable.get()) {
                // Redis会自动清理过期键，这里可以考虑添加清理逻辑
                log.debug("Redis缓存会自动清理过期键，无需手动清理");
            } else {
                // 使用本地缓存的清理过期功能
                memoryCache.clearExpired();
            }
        } catch (Exception e) {
            log.info("Redis暂时无法连接");
        }
    }
    
    /**
     * 获取缓存大小
     * 
     * @return 缓存条目数量
     */
    public int getSize() {
        try {
            if (redisAvailable.get()) {
                // 使用Redis - 这里返回0，因为RedisUtil没有直接的db大小方法
                // 实际项目中可能需要Redis的 DBSIZE 命令
                log.debug("Redis缓存大小统计需要DBSIZE命令支持，当前返回0");
                return 0;
            } else {
                // 使用本地缓存
                return memoryCache.size();
            }
        } catch (Exception e) {
            log.info("Redis暂时无法连接");
            return memoryCache.size();
        }
    }
    
    /**
     * 关闭缓存管理器
     */
    public void shutdown() {
        log.info("关闭统一缓存管理器");
        memoryCache.shutdown();
    }
}