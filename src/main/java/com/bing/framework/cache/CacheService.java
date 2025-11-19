package com.bing.framework.cache;

import java.util.concurrent.TimeUnit;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 缓存服务类
 * 提供统一的高可用缓存服务，支持Redis和本地缓存的自动降级
 * 
 * @author zhengbing
 * @date 2025-11-01
 */
@Slf4j
@Component
public class CacheService {
    
    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired(required = false)
    private UnifiedCacheManager unifiedCacheManager;
    
    public void setUnifiedCacheManager(UnifiedCacheManager unifiedCacheManager) {
        this.unifiedCacheManager = unifiedCacheManager;
    }
    
    // 降级机制配置
    private final AtomicBoolean redisAvailable = new AtomicBoolean(false);
    private final AtomicInteger consecutiveFailures = new AtomicInteger(0);
    private static final int MAX_CONSECUTIVE_FAILURES = 3; // 最大连续失败次数
    private static final long FALLBACK_CHECK_INTERVAL = 30000; // 30秒检查一次
    
    // 统计信息
    private final AtomicInteger redisOperations = new AtomicInteger(0);
    private final AtomicInteger fallbackOperations = new AtomicInteger(0);
    private final AtomicInteger failedOperations = new AtomicInteger(0);
    
    // ================================ String类型操作 ================================
    
    /**
     * 设置缓存（永久有效）
     * 
     * @param key 缓存键
     * @param value 缓存值
     * @return boolean 操作是否成功
     */
    public boolean set(String key, Object value) {
        return set(key, value, -1);
    }
    
    /**
     * 设置缓存并指定过期时间
     * 
     * @param key 缓存键
     * @param value 缓存值
     * @param ttlMinutes 过期时间（分钟），-1表示永久有效
     * @return boolean 操作是否成功
     */
    public boolean set(String key, Object value, long ttlMinutes) {
        try {
            if (redisAvailable.get()) {
                // 优先使用Redis
                if (ttlMinutes > 0) {
                    redisTemplate.opsForValue().set(key, value, ttlMinutes, TimeUnit.MINUTES);
                } else {
                    redisTemplate.opsForValue().set(key, value);
                }
                redisOperations.incrementAndGet();
                return true;
            } else {
                // 使用本地缓存
                fallbackOperations.incrementAndGet();
                return unifiedCacheManager.set(key, value, ttlMinutes);
            }
        } catch (Exception e) {
            handleRedisFailure();
            log.warn("Redis操作失败，使用本地缓存: key={}", key, e);
            
            // 降级到本地缓存
            fallbackOperations.incrementAndGet();
            return unifiedCacheManager.set(key, value, ttlMinutes);
        }
    }
    
    /**
     * 获取缓存
     * 
     * @param key 缓存键
     * @return Object 缓存值
     */
    public Object get(String key) {
        try {
            if (redisAvailable.get()) {
                // 优先使用Redis
                Object value = redisTemplate.opsForValue().get(key);
                redisOperations.incrementAndGet();
                return value;
            } else {
                // 使用本地缓存
                fallbackOperations.incrementAndGet();
                return unifiedCacheManager.get(key);
            }
        } catch (Exception e) {
            handleRedisFailure();
            log.warn("Redis操作失败，使用本地缓存: key={}", key, e);
            
            // 降级到本地缓存
            fallbackOperations.incrementAndGet();
            return unifiedCacheManager.get(key);
        }
    }
    
    /**
     * 删除缓存
     * 
     * @param key 缓存键
     * @return boolean 是否成功删除
     */
    public boolean delete(String key) {
        try {
            if (redisAvailable.get()) {
                // 优先使用Redis
                boolean result = Boolean.TRUE.equals(redisTemplate.delete(key));
                if (result) {
                    redisOperations.incrementAndGet();
                }
                return result;
            } else {
                // 使用本地缓存
                fallbackOperations.incrementAndGet();
                return unifiedCacheManager.delete(key);
            }
        } catch (Exception e) {
            handleRedisFailure();
            log.warn("Redis操作失败，使用本地缓存: key={}", key, e);
            
            // 降级到本地缓存
            fallbackOperations.incrementAndGet();
            return unifiedCacheManager.delete(key);
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
                // 优先使用Redis
                long count = redisTemplate.delete(java.util.Arrays.asList(keys));
                if (count > 0) {
                    redisOperations.incrementAndGet();
                }
                return count;
            } else {
                // 使用本地缓存
                fallbackOperations.incrementAndGet();
                return unifiedCacheManager.delete(keys);
            }
        } catch (Exception e) {
            handleRedisFailure();
            log.warn("Redis批量删除失败，使用本地缓存: keyCount={}", keys.length, e);
            
            // 降级到本地缓存
            fallbackOperations.incrementAndGet();
            return unifiedCacheManager.delete(keys);
        }
    }
    
    /**
     * 设置缓存过期时间
     * 
     * @param key 缓存键
     * @param time 过期时间
     * @param timeUnit 时间单位
     * @return boolean 操作是否成功
     */
    public boolean expire(String key, long time, TimeUnit timeUnit) {
        try {
            if (time > 0) {
                if (redisAvailable.get()) {
                    redisOperations.incrementAndGet();
                    redisTemplate.expire(key, time, timeUnit);
                } else {
                    fallbackOperations.incrementAndGet();
                    // 本地缓存的过期时间通过set方法指定，这里不直接支持
                    log.warn("本地缓存不支持动态设置过期时间: key={}", key);
                }
                return true;
            }
        } catch (Exception e) {
            handleRedisFailure();
            log.warn("Redis设置过期时间失败: key={}", key, e);
            failedOperations.incrementAndGet();
        }
        return false;
    }
    
    /**
     * 获取缓存过期时间
     * 
     * @param key 缓存键
     * @param timeUnit 时间单位
     * @return long 剩余过期时间
     */
    public long getExpire(String key, TimeUnit timeUnit) {
        try {
            if (redisAvailable.get()) {
                redisOperations.incrementAndGet();
                return redisTemplate.getExpire(key, timeUnit);
            } else {
                fallbackOperations.incrementAndGet();
                log.warn("本地缓存不支持查询过期时间: key={}", key);
                return -1; // 表示未知
            }
        } catch (Exception e) {
            handleRedisFailure();
            log.warn("Redis获取过期时间失败: key={}", key, e);
            failedOperations.incrementAndGet();
            return -1;
        }
    }
    
    /**
     * 判断键是否存在
     * 
     * @param key 缓存键
     * @return boolean 是否存在
     */
    public boolean hasKey(String key) {
        try {
            if (redisAvailable.get()) {
                redisOperations.incrementAndGet();
                return Boolean.TRUE.equals(redisTemplate.hasKey(key));
            } else {
                fallbackOperations.incrementAndGet();
                return unifiedCacheManager.hasKey(key);
            }
        } catch (Exception e) {
            handleRedisFailure();
            log.warn("Redis检查键存在性失败: key={}", key, e);
            failedOperations.incrementAndGet();
            return false;
        }
    }
    
    // ================================ 分布式锁操作 ================================
    
    /**
     * 获取分布式锁
     * 
     * @param lockKey 锁键
     * @param expireSeconds 锁过期时间（秒）
     * @return boolean 是否成功获取锁
     */
    public boolean tryLock(String lockKey, long expireSeconds) {
        return tryLock(lockKey, expireSeconds, 0);
    }
    
    /**
     * 获取分布式锁（带重试）
     * 
     * @param lockKey 锁键
     * @param expireSeconds 锁过期时间（秒）
     * @param retryTimes 重试次数
     * @return boolean 是否成功获取锁
     */
    public boolean tryLock(String lockKey, long expireSeconds, int retryTimes) {
        try {
            if (redisAvailable.get()) {
                // 使用Redis实现分布式锁
                String script = "if redis.call('setnx', KEYS[1], ARGV[1]) == 1 then " +
                              "return redis.call('expire', KEYS[1], ARGV[2]) " +
                              "else return 0 end";
                DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(script, Long.class);
                
                for (int i = 0; i <= retryTimes; i++) {
                    Long result = redisTemplate.execute(redisScript, 
                        java.util.Collections.singletonList(lockKey), 
                        String.valueOf(System.currentTimeMillis() + expireSeconds * 1000),
                        String.valueOf(expireSeconds));
                    
                    if (result != null && result == 1) {
                        redisOperations.incrementAndGet();
                        return true;
                    }
                    
                    if (i < retryTimes) {
                        Thread.sleep(100); // 100ms后重试
                    }
                }
            } else {
                // 使用本地锁（基于synchronized）
                fallbackOperations.incrementAndGet();
                return localLock(lockKey, expireSeconds);
            }
        } catch (Exception e) {
            handleRedisFailure();
            log.warn("Redis分布式锁失败，使用本地锁: lockKey={}", lockKey, e);
            fallbackOperations.incrementAndGet();
            return localLock(lockKey, expireSeconds);
        }
        return false;
    }
    
    /**
     * 释放分布式锁
     * 
     * @param lockKey 锁键
     * @return boolean 是否成功释放
     */
    public boolean releaseLock(String lockKey) {
        try {
            if (redisAvailable.get()) {
                // 释放Redis锁
                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                              "return redis.call('del', KEYS[1]) " +
                              "else return 0 end";
                DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(script, Long.class);
                
                Long result = redisTemplate.execute(redisScript, 
                    java.util.Collections.singletonList(lockKey), 
                    String.valueOf(System.currentTimeMillis()));
                
                redisOperations.incrementAndGet();
                return result != null && result == 1;
            } else {
                // 释放本地锁
                fallbackOperations.incrementAndGet();
                return localUnlock(lockKey);
            }
        } catch (Exception e) {
            handleRedisFailure();
            log.warn("Redis释放锁失败: lockKey={}", lockKey, e);
            failedOperations.incrementAndGet();
            return false;
        }
    }
    
    // ================================ 私有方法 ================================
    
    /**
     * 本地锁实现（简单版本）
     * 注意：这只在单实例环境下有效，集群环境需要使用Redis分布式锁
     */
    private static final Map<String, LocalLockInfo> localLocks = new java.util.concurrent.ConcurrentHashMap<>();
    
    private boolean localLock(String lockKey, long expireSeconds) {
        LocalLockInfo lockInfo = localLocks.get(lockKey);
        if (lockInfo != null && !lockInfo.isExpired()) {
            return false; // 锁已被占用
        }
        
        localLocks.put(lockKey, new LocalLockInfo(System.currentTimeMillis() + expireSeconds * 1000));
        return true;
    }
    
    private boolean localUnlock(String lockKey) {
        return localLocks.remove(lockKey) != null;
    }
    
    private static class LocalLockInfo {
        private final long expireTime;
        
        public LocalLockInfo(long expireTime) {
            this.expireTime = expireTime;
        }
        
        public boolean isExpired() {
            return System.currentTimeMillis() > expireTime;
        }
    }
    
    /**
     * 处理Redis连接失败
     */
    private void handleRedisFailure() {
        consecutiveFailures.incrementAndGet();
        failedOperations.incrementAndGet();
        
        if (consecutiveFailures.get() >= MAX_CONSECUTIVE_FAILURES) {
            redisAvailable.set(false);
            consecutiveFailures.set(0);
            log.warn("Redis连续失败{}次，切换到本地缓存", MAX_CONSECUTIVE_FAILURES);
        }
    }
    
    /**
     * 检查Redis是否可用
     * 
     * @return boolean Redis是否可用
     */
    public boolean isRedisAvailable() {
        return redisAvailable.get();
    }
    
    /**
     * 检查Redis连接状态
     */
    public void checkRedisConnection() {
        if (redisTemplate == null) {
            redisAvailable.set(false);
            return;
        }
        
        try {
            Object result = redisTemplate.getConnectionFactory()
                .getConnection()
                .ping();
            
            boolean nowAvailable = "PONG".equals(result);
            if (nowAvailable) {
                consecutiveFailures.set(0);
                if (!redisAvailable.get()) {
                    redisAvailable.set(true);
                    log.info("Redis连接已恢复，切换到Redis缓存");
                }
            } else {
                handleRedisFailure();
            }
        } catch (Exception e) {
            log.debug("Redis连接检查失败", e);
            handleRedisFailure();
        }
    }
    
    /**
     * 手动切换到Redis
     */
    public void switchToRedis() {
        redisAvailable.set(true);
        consecutiveFailures.set(0);
        log.info("手动切换到Redis缓存");
    }
    
    /**
     * 手动切换到本地缓存
     */
    public void switchToLocal() {
        redisAvailable.set(false);
        consecutiveFailures.set(0);
        log.info("手动切换到本地缓存");
    }
    
    /**
     * 获取统计信息字符串
     * 
     * @return 统计信息字符串
     */
    public String getStatsString() {
        StringBuilder stats = new StringBuilder();
        stats.append("缓存服务统计:\n");
        stats.append("当前使用: ").append(redisAvailable.get() ? "Redis" : "本地缓存").append("\n");
        stats.append("Redis操作次数: ").append(redisOperations.get()).append("\n");
        stats.append("降级操作次数: ").append(fallbackOperations.get()).append("\n");
        stats.append("失败操作次数: ").append(failedOperations.get()).append("\n");
        stats.append("连续失败次数: ").append(consecutiveFailures.get()).append("\n");
        
        if (!redisAvailable.get()) {
            stats.append("\n本地缓存详情:\n");
            stats.append(unifiedCacheManager.getStats());
        }
        
        return stats.toString();
    }
    
    /**
     * 清空所有缓存
     */
    public void clear() {
        try {
            if (redisAvailable.get()) {
                redisOperations.incrementAndGet();
                redisTemplate.getConnectionFactory().getConnection().flushAll();
            } else {
                fallbackOperations.incrementAndGet();
                unifiedCacheManager.clear();
            }
        } catch (Exception e) {
            log.error("清空缓存失败", e);
            failedOperations.incrementAndGet();
        }
    }
    
    /**
     * 清理过期缓存项
     */
    public void clearExpired() {
        try {
            if (redisAvailable.get()) {
                redisOperations.incrementAndGet();
                // Redis会自动处理过期，这里主要是对本地缓存执行清理
                if (unifiedCacheManager != null) {
                    unifiedCacheManager.clearExpired();
                }
            } else {
                fallbackOperations.incrementAndGet();
                if (unifiedCacheManager != null) {
                    unifiedCacheManager.clearExpired();
                }
            }
        } catch (Exception e) {
            log.error("清理过期缓存失败", e);
            failedOperations.incrementAndGet();
        }
    }
    
    /**
     * 缓存统计信息类
     */
    public static class CacheStats {
        private final long totalOperations;
        private final long redisOperations;
        private final long fallbackOperations;
        private final long failedOperations;
        private final int currentMode; // 0: redis, 1: local
        private final int size;
        private final int consecutiveFailures;
        
        public CacheStats(long totalOperations, long redisOperations, long fallbackOperations, 
                         long failedOperations, int currentMode, int size, int consecutiveFailures) {
            this.totalOperations = totalOperations;
            this.redisOperations = redisOperations;
            this.fallbackOperations = fallbackOperations;
            this.failedOperations = failedOperations;
            this.currentMode = currentMode;
            this.size = size;
            this.consecutiveFailures = consecutiveFailures;
        }
        
        public long getTotalOperations() {
            return totalOperations;
        }
        
        public long getRedisOperations() {
            return redisOperations;
        }
        
        public long getLocalCacheOperations() {
            return fallbackOperations;
        }
        
        public long getFailedOperations() {
            return failedOperations;
        }
        
        public int getCurrentMode() {
            return currentMode;
        }
        
        public int getSize() {
            return size;
        }
        
        public int getConsecutiveFailures() {
            return consecutiveFailures;
        }
        
        public String getCurrentModeString() {
            return currentMode == 0 ? "Redis" : "本地缓存";
        }
        
        @Override
        public String toString() {
            return String.format("CacheStats{total=%d, redis=%d, local=%d, failed=%d, mode=%s, size=%d, failures=%d}",
                totalOperations, redisOperations, fallbackOperations, failedOperations,
                getCurrentModeString(), size, consecutiveFailures);
        }
    }
    
    /**
     * 获取详细统计信息
     * 
     * @return CacheStats 统计信息对象
     */
    public CacheStats getStats() {
        long totalOps = redisOperations.get() + fallbackOperations.get() + failedOperations.get();
        int currentMode = redisAvailable.get() ? 0 : 1;
        int cacheSize = 0;
        
        try {
            if (unifiedCacheManager != null) {
                cacheSize = unifiedCacheManager.getSize();
            }
        } catch (Exception e) {
            log.debug("获取缓存大小失败", e);
        }
        
        return new CacheStats(
            totalOps,
            redisOperations.get(),
            fallbackOperations.get(),
            failedOperations.get(),
            currentMode,
            cacheSize,
            consecutiveFailures.get()
        );
    }
    
    /**
     * 获取配置摘要
     * 
     * @return String 配置摘要信息
     */
    public String getConfigurationSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("缓存服务配置:\n");
        summary.append("当前模式: ").append(redisAvailable.get() ? "Redis优先" : "本地缓存优先").append("\n");
        summary.append("最大连续失败次数: ").append(MAX_CONSECUTIVE_FAILURES).append("\n");
        summary.append("降级检查间隔: ").append(FALLBACK_CHECK_INTERVAL / 1000).append("秒\n");
        summary.append("Redis可用: ").append(redisTemplate != null ? "已配置" : "未配置").append("\n");
        summary.append("本地缓存管理: ").append(unifiedCacheManager != null ? "已配置" : "未配置").append("\n");
        
        if (unifiedCacheManager != null) {
            summary.append("本地缓存详情:\n");
            summary.append(unifiedCacheManager.getStats());
        }
        
        return summary.toString();
    }
    
    /**
     * 重置统计信息
     */
    public void resetStats() {
        redisOperations.set(0);
        fallbackOperations.set(0);
        failedOperations.set(0);
        consecutiveFailures.set(0);
        log.info("缓存统计信息已重置");
    }
}