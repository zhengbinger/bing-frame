package com.bing.framework.listener;

import com.bing.framework.entity.SystemConfig;
import com.bing.framework.service.SystemConfigService;
import com.bing.framework.util.SystemConfigCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 系统配置启动加载器
 * 实现CommandLineRunner接口，在系统启动时自动加载配置到缓存
 * 确保系统启动后所有配置项都已经在缓存中可用
 * 
 * @author zhengbing
 * @date 2025-11-15
 */
@Component
public class SystemConfigStartupLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SystemConfigStartupLoader.class);

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private SystemConfigCacheManager cacheManager;

    @Override
    public void run(String... args) throws Exception {
        try {
            log.info("开始加载系统配置到缓存...");
            
            // 获取所有启用的配置项
            List<SystemConfig> enabledConfigs = systemConfigService.getAllEnabledConfigs();
            
            if (enabledConfigs != null && !enabledConfigs.isEmpty()) {
                // 加载到缓存
                cacheManager.preloadConfigs(enabledConfigs);
                
                log.info("系统配置加载完成，共加载 {} 条配置", enabledConfigs.size());
                
                // 打印缓存统计信息
                SystemConfigCacheManager.CacheStats stats = cacheManager.getCacheStats();
                log.info("缓存统计信息: {}", stats);
                
                // 输出部分重要配置项的加载情况
                printKeyConfigsLoadingStatus(enabledConfigs);
                
            } else {
                log.warn("未找到启用的系统配置项，请检查数据库中是否存在配置数据");
            }
            
        } catch (Exception e) {
            log.error("系统配置加载失败", e);
            throw new RuntimeException("系统配置启动加载失败", e);
        }
    }

    /**
     * 打印关键配置项的加载状态
     */
    private void printKeyConfigsLoadingStatus(List<SystemConfig> configs) {
        if (configs == null || configs.isEmpty()) {
            return;
        }
        
        // 关键配置项列表
        String[] keyConfigKeys = {
            "system.name",
            "system.version", 
            "system.debug",
            "system.timezone",
            "security.jwt.secret",
            "security.jwt.expiration",
            "cache.enabled",
            "logging.level.root"
        };
        
        log.info("=== 关键配置项加载状态 ===");
        for (String keyConfigKey : keyConfigKeys) {
            SystemConfig config = findConfigByKey(configs, keyConfigKey);
            if (config != null) {
                String cachedValue = cacheManager.getConfigFromCache(keyConfigKey);
                boolean enabled = cacheManager.isConfigEnabled(keyConfigKey);
                
                log.info("✅ {} = {} (启用: {}, 类型: {})", 
                    keyConfigKey, 
                    cachedValue != null ? maskedValue(cachedValue) : "N/A",
                    enabled ? "是" : "否",
                    config.getConfigType()
                );
            } else {
                log.debug("⏸️  {} - 未在配置中设置", keyConfigKey);
            }
        }
        log.info("=== 关键配置项加载状态 ===");
    }

    /**
     * 在配置列表中查找指定键的配置项
     */
    private SystemConfig findConfigByKey(List<SystemConfig> configs, String configKey) {
        return configs.stream()
            .filter(config -> configKey.equals(config.getConfigKey()))
            .findFirst()
            .orElse(null);
    }

    /**
     * 对敏感配置值进行脱敏处理
     */
    private String maskedValue(String value) {
        if (value == null || value.length() <= 8) {
            return "***";
        }
        
        // 如果是JWT密钥或密码等敏感信息，进行脱敏
        if (value.contains("key") || value.contains("secret") || value.contains("password")) {
            return value.substring(0, 4) + "****" + value.substring(value.length() - 4);
        }
        
        // 对于普通配置，长度超过20的只显示前10位和后4位
        if (value.length() > 20) {
            return value.substring(0, 10) + "..." + value.substring(value.length() - 4);
        }
        
        return value;
    }
}