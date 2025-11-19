package com.bing.framework.cache;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import lombok.extern.slf4j.Slf4j;

/**
 * 本地内存缓存实现
 * 提供高性能的本地缓存功能，支持过期时间、统计和自动清理
 * 
 * @author zhengbing
 * @date 2025-11-01
 */
@Slf4j
public class MemoryCache {
    
    /**
     * 缓存项接口
     */
    public interface CacheEntry {
        Object getValue();
        LocalDateTime getExpireTime();
        boolean isExpired();
    }
    
    /**
     * 缓存项实现
     */
    private static class SimpleCacheEntry implements CacheEntry {
        private final Object value;
        private final LocalDateTime expireTime;
        
        public SimpleCacheEntry(Object value, LocalDateTime expireTime) {
            this.value = value;
            this.expireTime = expireTime;
        }
        
        @Override
        public Object getValue() {
            return value;
        }
        
        @Override
        public LocalDateTime getExpireTime() {
            return expireTime;
        }
        
        @Override
        public boolean isExpired() {
            return expireTime != null && LocalDateTime.now().isAfter(expireTime);
        }
    }
    
    // 缓存存储
    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();
    
    // 缓存统计
    private final AtomicLong hitCount = new AtomicLong(0);
    private final AtomicLong missCount = new AtomicLong(0);
    private final AtomicLong putCount = new AtomicLong(0);
    private final AtomicLong deleteCount = new AtomicLong(0);
    
    // 配置信息
    private final long maxSize;
    private final long defaultTtlMinutes;
    private final long cleanupIntervalMinutes;
    
    // 定时清理任务执行器
    private final ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "MemoryCache-Cleanup");
        thread.setDaemon(true);
        return thread;
    });
    
    /**
     * 构造函数
     * 
     * @param maxSize 最大缓存数量，默认1000
     * @param defaultTtlMinutes 默认过期时间（分钟），默认60分钟
     * @param cleanupIntervalMinutes 清理间隔（分钟），默认10分钟
     */
    public MemoryCache(long maxSize, long defaultTtlMinutes, long cleanupIntervalMinutes) {
        this.maxSize = maxSize;
        this.defaultTtlMinutes = defaultTtlMinutes;
        this.cleanupIntervalMinutes = cleanupIntervalMinutes;
        startCleanupTask();
    }
    
    /**
     * 默认构造函数
     * 使用默认配置：最大1000个缓存项，默认60分钟过期，每10分钟清理一次
     */
    public MemoryCache() {
        this(1000, 60, 10);
    }
    
    /**
     * 启动定时清理任务
     */
    private void startCleanupTask() {
        cleanupExecutor.scheduleAtFixedRate(this::cleanupExpired, 
            cleanupIntervalMinutes, cleanupIntervalMinutes, TimeUnit.MINUTES);
        log.info("启动本地缓存清理任务，清理间隔: {} 分钟", cleanupIntervalMinutes);
    }
    
    /**
     * 停止清理任务
     */
    public void shutdown() {
        cleanupExecutor.shutdown();
        try {
            if (!cleanupExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                cleanupExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            cleanupExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * 清理过期缓存项
     */
    private void cleanupExpired() {
        int removedCount = 0;
        try {
            for (String key : cache.keySet()) {
                CacheEntry entry = cache.get(key);
                if (entry != null && entry.isExpired()) {
                    if (cache.remove(key) != null) {
                        removedCount++;
                    }
                }
            }
            if (removedCount > 0) {
                log.debug("本地缓存清理完成，移除了 {} 个过期缓存项", removedCount);
            }
        } catch (Exception e) {
            log.error("清理本地缓存失败", e);
        }
    }
    
    /**
     * 手动清理过期缓存项
     * 公开接口，供外部调用
     */
    public void clearExpired() {
        cleanupExpired();
    }
    
    /**
     * 放入缓存（永久有效）
     * 
     * @param key 缓存键
     * @param value 缓存值
     * @return boolean 是否成功放入
     */
    public boolean put(String key, Object value) {
        return put(key, value, -1);
    }
    
    /**
     * 放入缓存
     * 
     * @param key 缓存键
     * @param value 缓存值
     * @param ttlMinutes 过期时间（分钟），-1表示永久有效
     * @return boolean 是否成功放入
     */
    public boolean put(String key, Object value, long ttlMinutes) {
        try {
            // 检查容量限制
            if (cache.size() >= maxSize) {
                log.warn("本地缓存已达到最大容量: {}", maxSize);
                return false;
            }
            
            // 创建缓存项
            CacheEntry entry;
            if (ttlMinutes > 0) {
                LocalDateTime expireTime = LocalDateTime.now().plus(ttlMinutes, ChronoUnit.MINUTES);
                entry = new SimpleCacheEntry(value, expireTime);
            } else {
                entry = new SimpleCacheEntry(value, null);
            }
            
            cache.put(key, entry);
            putCount.incrementAndGet();
            log.debug("放入本地缓存: key={}, value={}, ttlMinutes={}", key, value, ttlMinutes);
            return true;
        } catch (Exception e) {
            log.error("放入本地缓存失败: key={}", key, e);
            return false;
        }
    }
    
    /**
     * 获取缓存
     * 
     * @param key 缓存键
     * @return Object 缓存值，如果不存在或已过期则返回null
     */
    public Object get(String key) {
        try {
            CacheEntry entry = cache.get(key);
            if (entry == null) {
                missCount.incrementAndGet();
                return null;
            }
            
            // 检查是否过期
            if (entry.isExpired()) {
                cache.remove(key);
                missCount.incrementAndGet();
                return null;
            }
            
            hitCount.incrementAndGet();
            Object value = entry.getValue();
            log.debug("从本地缓存获取: key={}, value={}", key, value);
            return value;
        } catch (Exception e) {
            log.error("从本地缓存获取失败: key={}", key, e);
            missCount.incrementAndGet();
            return null;
        }
    }
    
    /**
     * 删除缓存
     * 
     * @param key 缓存键
     * @return boolean 是否成功删除
     */
    public boolean remove(String key) {
        try {
            boolean removed = cache.remove(key) != null;
            if (removed) {
                deleteCount.incrementAndGet();
                log.debug("从本地缓存删除: key={}", key);
            }
            return removed;
        } catch (Exception e) {
            log.error("从本地缓存删除失败: key={}", key, e);
            return false;
        }
    }
    
    /**
     * 批量删除缓存
     * 
     * @param keys 缓存键集合
     * @return int 删除的数量
     */
    public int remove(String... keys) {
        int count = 0;
        if (keys != null) {
            for (String key : keys) {
                if (remove(key)) {
                    count++;
                }
            }
        }
        return count;
    }
    
    /**
     * 清空所有缓存
     */
    public void clear() {
        try {
            int size = cache.size();
            cache.clear();
            log.debug("清空本地缓存，移除了 {} 个缓存项", size);
        } catch (Exception e) {
            log.error("清空本地缓存失败", e);
        }
    }
    
    /**
     * 检查缓存是否存在且未过期
     * 
     * @param key 缓存键
     * @return boolean 是否存在且有效
     */
    public boolean containsKey(String key) {
        CacheEntry entry = cache.get(key);
        if (entry == null) {
            return false;
        }
        
        if (entry.isExpired()) {
            cache.remove(key);
            return false;
        }
        
        return true;
    }
    
    /**
     * 获取缓存统计信息
     * 
     * @return 统计信息字符串
     */
    public String getStats() {
        return String.format(
            "MemoryCache Stats: Size=%d, MaxSize=%d, Hits=%d, Misses=%d, Puts=%d, Deletes=%d, HitRate=%.2f%%",
            cache.size(),
            maxSize,
            hitCount.get(),
            missCount.get(),
            putCount.get(),
            deleteCount.get(),
            getHitRate()
        );
    }
    
    /**
     * 获取命中率
     * 
     * @return 命中率（百分比）
     */
    public double getHitRate() {
        long hits = hitCount.get();
        long misses = missCount.get();
        long total = hits + misses;
        return total > 0 ? (double) hits / total * 100 : 0.0;
    }
    
    /**
     * 获取当前缓存数量
     * 
     * @return 缓存数量
     */
    public int size() {
        cleanupExpired(); // 清理过期项后返回准确数量
        return cache.size();
    }
    
    /**
     * 获取缓存键集合
     * 
     * @return 键集合副本
     */
    public java.util.Set<String> keySet() {
        cleanupExpired(); // 清理过期项后返回准确键集合
        return new java.util.HashSet<>(cache.keySet());
    }
}