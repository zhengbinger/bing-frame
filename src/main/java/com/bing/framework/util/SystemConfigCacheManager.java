package com.bing.framework.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 系统配置缓存管理器
 * 使用本地内存缓存管理所有系统配置项
 * 提供高效的配置读取、更新和批量操作
 * 
 * @author zhengbing
 * @date 2025-11-15
 */
@Component
public class SystemConfigCacheManager {

    private static final Logger log = LoggerFactory.getLogger(SystemConfigCacheManager.class);

    private final ConcurrentMap<String, String> configCache = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, String> configTypeCache = new ConcurrentHashMap<>();
    private final ReadWriteLock cacheLock = new ReentrantReadWriteLock();
    
    // 缓存配置项的描述信息
    private final ConcurrentMap<String, String> configDescriptionCache = new ConcurrentHashMap<>();
    
    // 缓存开关状态
    private final ConcurrentMap<String, Boolean> configEnabledCache = new ConcurrentHashMap<>();

    /**
     * 启动时预热配置
     * 此方法将在系统启动后由Spring自动调用
     */
    public void preloadConfigs(List<com.bing.framework.entity.SystemConfig> configs) {
        if (configs == null || configs.isEmpty()) {
            log.info("没有配置项需要预热");
            return;
        }
        
        cacheLock.writeLock().lock();
        try {
            int preloadedCount = 0;
            for (com.bing.framework.entity.SystemConfig config : configs) {
                if (config.getEnabled() != null && config.getEnabled() == 1) {
                    putConfigToCache(config.getConfigKey(), config.getConfigValue());
                    configTypeCache.put(config.getConfigKey(), config.getConfigType());
                    configDescriptionCache.put(config.getConfigKey(), config.getDescription());
                    configEnabledCache.put(config.getConfigKey(), true);
                    preloadedCount++;
                }
            }
            log.info("预热系统配置完成，共加载 {} 条配置", preloadedCount);
        } finally {
            cacheLock.writeLock().unlock();
        }
    }

    /**
     * 从缓存中获取配置值
     */
    public String getConfigFromCache(String configKey) {
        cacheLock.readLock().lock();
        try {
            return configCache.get(configKey);
        } finally {
            cacheLock.readLock().unlock();
        }
    }

    /**
     * 将配置放入缓存
     */
    public void putConfigToCache(String configKey, String configValue) {
        if (configKey == null || configKey.trim().isEmpty()) {
            log.warn("配置键不能为空");
            return;
        }
        
        cacheLock.writeLock().lock();
        try {
            configCache.put(configKey, configValue);
            log.debug("配置已放入缓存: {} = {}", configKey, configValue);
        } finally {
            cacheLock.writeLock().unlock();
        }
    }

    /**
     * 更新缓存中的配置
     */
    public void updateConfigInCache(String configKey, String configValue) {
        cacheLock.writeLock().lock();
        try {
            configCache.put(configKey, configValue);
            log.debug("缓存中的配置已更新: {} = {}", configKey, configValue);
        } finally {
            cacheLock.writeLock().unlock();
        }
    }

    /**
     * 从缓存中清除指定配置
     */
    public void removeConfigFromCache(String configKey) {
        cacheLock.writeLock().lock();
        try {
            configCache.remove(configKey);
            configTypeCache.remove(configKey);
            configDescriptionCache.remove(configKey);
            configEnabledCache.remove(configKey);
            log.debug("配置已从缓存中清除: {}", configKey);
        } finally {
            cacheLock.writeLock().unlock();
        }
    }

    /**
     * 清空所有缓存
     */
    public void clearAllCache() {
        cacheLock.writeLock().lock();
        try {
            configCache.clear();
            configTypeCache.clear();
            configDescriptionCache.clear();
            configEnabledCache.clear();
            log.info("系统配置缓存已清空");
        } finally {
            cacheLock.writeLock().unlock();
        }
    }

    /**
     * 批量从缓存中清除配置
     */
    public void clearConfigsFromCache(List<Long> configIds) {
        if (configIds == null || configIds.isEmpty()) {
            return;
        }
        
        // 这里需要根据实际情况实现，可以通过ID反向查找键
        // 或者由调用方传入要清除的键集合
        log.info("批量清除缓存配置，ID数量: {}", configIds.size());
    }

    /**
     * 重新加载所有配置到缓存
     */
    public void reloadAllConfigs(List<com.bing.framework.entity.SystemConfig> configs) {
        cacheLock.writeLock().lock();
        try {
            // 清空旧缓存
            clearAllCache();
            
            // 加载新配置
            preloadConfigs(configs);
            
            log.info("系统配置缓存重新加载完成，共加载 {} 条配置", configs != null ? configs.size() : 0);
        } finally {
            cacheLock.writeLock().unlock();
        }
    }

    /**
     * 获取配置类型
     */
    public String getConfigType(String configKey) {
        cacheLock.readLock().lock();
        try {
            return configTypeCache.get(configKey);
        } finally {
            cacheLock.readLock().unlock();
        }
    }

    /**
     * 获取配置描述
     */
    public String getConfigDescription(String configKey) {
        cacheLock.readLock().lock();
        try {
            return configDescriptionCache.get(configKey);
        } finally {
            cacheLock.readLock().unlock();
        }
    }

    /**
     * 检查配置是否启用
     */
    public boolean isConfigEnabled(String configKey) {
        cacheLock.readLock().lock();
        try {
            Boolean enabled = configEnabledCache.get(configKey);
            return enabled != null && enabled;
        } finally {
            cacheLock.readLock().unlock();
        }
    }

    /**
     * 设置配置启用状态
     */
    public void setConfigEnabled(String configKey, boolean enabled) {
        cacheLock.writeLock().lock();
        try {
            configEnabledCache.put(configKey, enabled);
            log.debug("配置启用状态已更新: {} = {}", configKey, enabled);
        } finally {
            cacheLock.writeLock().unlock();
        }
    }

    /**
     * 获取缓存大小统计信息
     */
    public CacheStats getCacheStats() {
        cacheLock.readLock().lock();
        try {
            return new CacheStats(
                configCache.size(),
                configTypeCache.size(),
                configDescriptionCache.size(),
                configEnabledCache.size()
            );
        } finally {
            cacheLock.readLock().unlock();
        }
    }

    /**
     * 获取所有缓存的键集合
     */
    public java.util.Set<String> getAllConfigKeys() {
        cacheLock.readLock().lock();
        try {
            return new java.util.HashSet<>(configCache.keySet());
        } finally {
            cacheLock.readLock().unlock();
        }
    }

    /**
     * 检查缓存中是否存在指定键
     */
    public boolean containsKey(String configKey) {
        cacheLock.readLock().lock();
        try {
            return configCache.containsKey(configKey);
        } finally {
            cacheLock.readLock().unlock();
        }
    }

    /**
     * 缓存统计信息
     */
    public static class CacheStats {
        private final int configCount;
        private final int typeCount;
        private final int descriptionCount;
        private final int enabledCount;

        public CacheStats(int configCount, int typeCount, int descriptionCount, int enabledCount) {
            this.configCount = configCount;
            this.typeCount = typeCount;
            this.descriptionCount = descriptionCount;
            this.enabledCount = enabledCount;
        }

        public int getConfigCount() {
            return configCount;
        }

        public int getTypeCount() {
            return typeCount;
        }

        public int getDescriptionCount() {
            return descriptionCount;
        }

        public int getEnabledCount() {
            return enabledCount;
        }

        @Override
        public String toString() {
            return String.format("CacheStats{配置数量=%d, 类型数量=%d, 描述数量=%d, 启用数量=%d}", 
                configCount, typeCount, descriptionCount, enabledCount);
        }
    }
}