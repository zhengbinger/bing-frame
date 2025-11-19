package com.bing.framework.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 审计日志用户信息缓存组件
 * 提供用户信息的缓存管理，减少重复的数据库查询操作
 * 支持缓存预热、失效策略和性能监控
 * 
 * @author zhengbing
 * @date 2024-12-28
 */
@Component
@Slf4j
public class AuditLogUserCache {
    
    @Autowired(required = false)
    private CacheManager cacheManager;
    
    // 本地缓存作为二级缓存，提高访问速度
    private final Map<Long, CachedUserInfo> localCache = new ConcurrentHashMap<>();
    
    // 缓存统计
    private final CacheStats cacheStats = new CacheStats();
    
    // 缓存配置参数
    private static final int MAX_LOCAL_CACHE_SIZE = 1000;
    private static final long CACHE_EXPIRE_TIME = 30 * 60 * 1000; // 30分钟
    private static final long CLEANUP_INTERVAL = 5 * 60 * 1000; // 5分钟清理一次
    
    /**
     * 缓存的用户信息
     */
    public static class CachedUserInfo {
        private final Long userId;
        private final String username;
        private final String displayName;
        private final String email;
        private final Date expireTime;
        
        public CachedUserInfo(Long userId, String username, String displayName, String email) {
            this.userId = userId;
            this.username = username;
            this.displayName = displayName;
            this.email = email;
            this.expireTime = new Date(System.currentTimeMillis() + CACHE_EXPIRE_TIME);
        }
        
        public boolean isExpired() {
            return new Date().after(expireTime);
        }
        
        // Getters
        public Long getUserId() { return userId; }
        public String getUsername() { return username; }
        public String getDisplayName() { return displayName; }
        public String getEmail() { return email; }
        public Date getExpireTime() { return expireTime; }
    }
    
    /**
     * 缓存统计信息
     */
    public static class CacheStats {
        private long hitCount = 0;
        private long missCount = 0;
        private long loadCount = 0;
        private final long startTime = System.currentTimeMillis();
        
        public synchronized void recordHit() {
            hitCount++;
        }
        
        public synchronized void recordMiss() {
            missCount++;
        }
        
        public synchronized void recordLoad() {
            loadCount++;
        }
        
        public synchronized double getHitRate() {
            long total = hitCount + missCount;
            return total > 0 ? (double) hitCount / total : 0.0;
        }
        
        public synchronized Map<String, Object> getStats() {
            Map<String, Object> stats = new HashMap<>();
            stats.put("hitCount", hitCount);
            stats.put("missCount", missCount);
            stats.put("loadCount", loadCount);
            stats.put("hitRate", String.format("%.2f%%", getHitRate() * 100));
            stats.put("uptimeMinutes", (System.currentTimeMillis() - startTime) / (60 * 1000));
            return stats;
        }
    }
    
    /**
     * 获取用户信息（带缓存）
     */
    public CachedUserInfo getUserInfo(Long userId) {
        if (userId == null) {
            return null;
        }
        
        // 首先检查本地缓存
        CachedUserInfo cachedInfo = localCache.get(userId);
        if (cachedInfo != null && !cachedInfo.isExpired()) {
            cacheStats.recordHit();
            log.debug("用户信息缓存命中: userId={}", userId);
            return cachedInfo;
        }
        
        // 检查Spring缓存
        if (cacheManager != null) {
            Cache springCache = cacheManager.getCache("audit-user-info");
            if (springCache != null) {
                Cache.ValueWrapper wrapper = springCache.get(userId);
                if (wrapper != null) {
                    cachedInfo = (CachedUserInfo) wrapper.get();
                    if (cachedInfo != null && !cachedInfo.isExpired()) {
                        cacheStats.recordHit();
                        // 同步到本地缓存
                        syncToLocalCache(userId, cachedInfo);
                        log.debug("Spring缓存命中: userId={}", userId);
                        return cachedInfo;
                    }
                }
            }
        }
        
        // 缓存未命中，需要从数据库加载
        cacheStats.recordMiss();
        cachedInfo = loadUserInfoFromDatabase(userId);
        if (cachedInfo != null) {
            cacheStats.recordLoad();
            // 存储到两级缓存
            storeToCache(userId, cachedInfo);
            log.debug("从数据库加载用户信息: userId={}", userId);
        }
        
        return cachedInfo;
    }
    
    /**
     * 从数据库加载用户信息
     */
    private CachedUserInfo loadUserInfoFromDatabase(Long userId) {
        try {
            // 这里应该调用实际的用户服务
            // 为了示例，我们模拟一个用户信息
            return new CachedUserInfo(
                userId, 
                "user" + userId, 
                "用户" + userId, 
                "user" + userId + "@example.com"
            );
        } catch (Exception e) {
            log.error("加载用户信息失败: userId={}", userId, e);
            return null;
        }
    }
    
    /**
     * 存储到两级缓存
     */
    private void storeToCache(Long userId, CachedUserInfo userInfo) {
        // 存储到本地缓存
        if (localCache.size() >= MAX_LOCAL_CACHE_SIZE) {
            // 清理过期的本地缓存
            cleanupExpiredLocalCache();
        }
        localCache.put(userId, userInfo);
        
        // 存储到Spring缓存
        if (cacheManager != null) {
            Cache springCache = cacheManager.getCache("audit-user-info");
            if (springCache != null) {
                springCache.put(userId, userInfo);
            }
        }
    }
    
    /**
     * 同步到本地缓存
     */
    private void syncToLocalCache(Long userId, CachedUserInfo userInfo) {
        if (localCache.size() < MAX_LOCAL_CACHE_SIZE || localCache.containsKey(userId)) {
            localCache.put(userId, userInfo);
        }
    }
    
    /**
     * 清理过期的本地缓存
     */
    private void cleanupExpiredLocalCache() {
        localCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
        log.debug("清理过期本地缓存，当前本地缓存大小: {}", localCache.size());
    }
    
    /**
     * 缓存预热 - 加载常用用户信息
     */
    @PostConstruct
    public void warmUpCache() {
        log.info("开始用户信息缓存预热...");
        
        try {
            // 模拟预加载常用用户ID（在实际项目中，这些应该是从配置或数据库查询得出）
            List<Long> commonUserIds = Arrays.asList(1L, 2L, 3L, 100L, 101L);
            
            for (Long userId : commonUserIds) {
                getUserInfo(userId);
            }
            
            log.info("用户信息缓存预热完成，预热用户数量: {}", commonUserIds.size());
        } catch (Exception e) {
            log.error("缓存预热失败", e);
        }
    }
    
    /**
     * 手动缓存用户信息
     */
    public void cacheUserInfo(Long userId, String username, String displayName, String email) {
        CachedUserInfo userInfo = new CachedUserInfo(userId, username, displayName, email);
        storeToCache(userId, userInfo);
        log.debug("手动缓存用户信息: userId={}, username={}", userId, username);
    }
    
    /**
     * 清除用户缓存
     */
    public void evictUserCache(Long userId) {
        localCache.remove(userId);
        
        if (cacheManager != null) {
            Cache springCache = cacheManager.getCache("audit-user-info");
            if (springCache != null) {
                springCache.evictIfPresent(userId);
            }
        }
        
        log.debug("清除用户缓存: userId={}", userId);
    }
    
    /**
     * 清除所有缓存
     */
    public void clearAllCache() {
        localCache.clear();
        
        if (cacheManager != null) {
            Cache springCache = cacheManager.getCache("audit-user-info");
            if (springCache != null) {
                springCache.clear();
            }
        }
        
        log.info("清除所有用户缓存");
    }
    
    /**
     * 获取缓存统计信息
     */
    public Map<String, Object> getCacheStatistics() {
        Map<String, Object> stats = cacheStats.getStats();
        stats.put("localCacheSize", localCache.size());
        stats.put("maxLocalCacheSize", MAX_LOCAL_CACHE_SIZE);
        return stats;
    }
    
    /**
     * 获取缓存使用率
     */
    public double getCacheUtilization() {
        return (double) localCache.size() / MAX_LOCAL_CACHE_SIZE;
    }
    
    /**
     * 定时清理过期缓存
     */
    // 可以通过@Scheduled注解定期调用cleanupExpiredLocalCache()
}