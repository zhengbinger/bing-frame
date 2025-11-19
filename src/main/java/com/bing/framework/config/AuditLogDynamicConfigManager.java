package com.bing.framework.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 审计日志动态配置管理器
 * 支持实时配置变更、配置验证、配置历史跟踪和配置回滚
 * 适用于需要动态调整审计日志参数的复杂业务场景
 * 
 * @author zhengbing
 * @date 2024-12-28
 */
@Component
@Validated
@Slf4j
public class AuditLogDynamicConfigManager {
    
    @Autowired
    private AuditLogConfigProperties configProperties;
    
    // 配置变更监听器
    private final Map<String, ConfigChangeListener> configChangeListeners = new ConcurrentHashMap<>();
    
    // 配置历史记录
    private final List<ConfigHistory> configHistory = new ArrayList<>();
    
    // 配置版本号生成器
    private final AtomicLong versionGenerator = new AtomicLong(1);
    
    // 定时任务执行器
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    
    // 配置验证器
    @Autowired(required = false)
    private ConfigValidator configValidator;
    
    // 配置通知器
    @Autowired(required = false)
    private ConfigNotificationService configNotificationService;
    
    // 配置更新标志
    private final AtomicBoolean hasChanges = new AtomicBoolean(false);
    
    /**
     * 配置变更监听器接口
     */
    public interface ConfigChangeListener {
        void onConfigChange(String propertyName, Object oldValue, Object newValue);
    }
    
    /**
     * 配置验证器接口
     */
    public interface ConfigValidator {
        boolean validate(AuditLogConfigProperties config);
        String getValidationMessage();
    }
    
    /**
     * 配置通知服务接口
     */
    public interface ConfigNotificationService {
        void notifyConfigChange(String configKey, Object oldValue, Object newValue);
        void notifyConfigChangeComplete(String version, boolean success);
    }
    
    /**
     * 配置历史记录
     */
    public static class ConfigHistory {
        private final Long version;
        private final Date timestamp;
        private final AuditLogConfigProperties config;
        private final String changeDescription;
        private final String user;
        
        public ConfigHistory(Long version, AuditLogConfigProperties config, 
                           String changeDescription, String user) {
            this.version = version;
            this.timestamp = new Date();
            this.config = copyConfig(config);
            this.changeDescription = changeDescription;
            this.user = user;
        }
        
        private static AuditLogConfigProperties copyConfig(AuditLogConfigProperties config) {
            AuditLogConfigProperties copy = new AuditLogConfigProperties();
            copy.setEnabled(config.getEnabled());
            copy.setAsyncEnabled(config.getAsyncEnabled());
            copy.setBatchSize(config.getBatchSize());
            copy.setFlushInterval(config.getFlushInterval());
            copy.setBufferQueueSize(config.getBufferQueueSize());
            copy.setThreadPoolCoreSize(config.getThreadPoolCoreSize());
            copy.setThreadPoolMaxSize(config.getThreadPoolMaxSize());
            copy.setThreadPoolQueueCapacity(config.getThreadPoolQueueCapacity());
            copy.setThreadPoolKeepAliveTime(config.getThreadPoolKeepAliveTime());
            copy.setAuditLevel(config.getAuditLevel());
            copy.setBufferPoolEnabled(config.getBufferPoolEnabled());
            copy.setExceptionHandlingEnabled(config.getExceptionHandlingEnabled());
            copy.setExceptionRetryTimes(config.getExceptionRetryTimes());
            copy.setExceptionRetryInterval(config.getExceptionRetryInterval());
            copy.setDeadLoopProtectionEnabled(config.getDeadLoopProtectionEnabled());
            copy.setMaxExecutionTime(config.getMaxExecutionTime());
            copy.setDedicatedDataSourceEnabled(config.getDedicatedDataSourceEnabled());
            copy.setDatasourceMonitoringEnabled(config.getDatasourceMonitoringEnabled());
            copy.setPerformanceOptimizationEnabled(config.getPerformanceOptimizationEnabled());
            copy.setDynamicConfigEnabled(config.getDynamicConfigEnabled());
            copy.setConfigChangeNotificationEnabled(config.getConfigChangeNotificationEnabled());
            copy.setUserCacheEnabled(config.getUserCacheEnabled());
            copy.setUserCacheSize(config.getUserCacheSize());
            copy.setUserCacheExpireTime(config.getUserCacheExpireTime());
            copy.setConfigVersion(config.getConfigVersion());
            copy.setUpdateTimestamp(config.getUpdateTimestamp());
            return copy;
        }
        
        // Getters
        public Long getVersion() { return version; }
        public Date getTimestamp() { return timestamp; }
        public AuditLogConfigProperties getConfig() { return config; }
        public String getChangeDescription() { return changeDescription; }
        public String getUser() { return user; }
    }
    
    /**
     * 初始化动态配置管理器
     */
    @PostConstruct
    public void init() {
        log.info("初始化审计日志动态配置管理器");
        
        // 初始化配置版本
        configProperties.setConfigVersion("1.0.0");
        configProperties.setUpdateTimestamp(System.currentTimeMillis());
        
        // 记录初始配置
        saveConfigHistory("系统初始化", "system");
        
        // 启动配置监控任务
        startConfigMonitoring();
        
        // 启动定期配置验证任务
        schedulePeriodicValidation();
        
        log.info("审计日志动态配置管理器初始化完成");
    }
    
    /**
     * 销毁动态配置管理器
     */
    @PreDestroy
    public void destroy() {
        log.info("销毁审计日志动态配置管理器");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * 更新配置
     */
    public boolean updateConfig(AuditLogConfigProperties newConfig, String changeDescription, String user) {
        log.info("开始更新审计日志配置，版本: {}, 操作人: {}, 描述: {}", 
                configProperties.getConfigVersion(), user, changeDescription);
        
        try {
            // 验证新配置
            if (!validateConfig(newConfig)) {
                log.error("配置验证失败: {}", configValidator != null ? configValidator.getValidationMessage() : "未知错误");
                return false;
            }
            
            // 保存当前配置到历史
            AuditLogConfigProperties oldConfig = copyCurrentConfig();
            
            // 应用新配置
            applyNewConfig(newConfig);
            
            // 记录配置变更
            recordConfigChange(oldConfig, newConfig, changeDescription, user);
            
            // 通知配置变更
            notifyConfigChange(oldConfig, newConfig);
            
            // 更新配置版本
            updateConfigVersion();
            
            hasChanges.set(false);
            
            log.info("审计日志配置更新成功，新版本: {}", configProperties.getConfigVersion());
            return true;
            
        } catch (Exception e) {
            log.error("更新审计日志配置失败", e);
            return false;
        }
    }
    
    /**
     * 获取当前配置
     */
    public AuditLogConfigProperties getCurrentConfig() {
        return copyCurrentConfig();
    }
    
    /**
     * 获取配置历史
     */
    public List<ConfigHistory> getConfigHistory() {
        return new ArrayList<>(configHistory);
    }
    
    /**
     * 回滚到指定版本
     */
    public boolean rollbackToVersion(Long targetVersion, String reason, String user) {
        log.info("开始回滚配置到版本: {}, 操作人: {}, 原因: {}", targetVersion, user, reason);
        
        ConfigHistory targetHistory = configHistory.stream()
                .filter(h -> h.getVersion().equals(targetVersion))
                .findFirst()
                .orElse(null);
        
        if (targetHistory == null) {
            log.error("未找到目标版本: {}", targetVersion);
            return false;
        }
        
        return updateConfig(targetHistory.getConfig(), 
                          String.format("回滚到版本 %s: %s", targetVersion, reason), 
                          user);
    }
    
    /**
     * 添加配置变更监听器
     */
    public void addConfigChangeListener(String listenerName, ConfigChangeListener listener) {
        configChangeListeners.put(listenerName, listener);
        log.debug("添加配置变更监听器: {}", listenerName);
    }
    
    /**
     * 移除配置变更监听器
     */
    public void removeConfigChangeListener(String listenerName) {
        configChangeListeners.remove(listenerName);
        log.debug("移除配置变更监听器: {}", listenerName);
    }
    
    /**
     * 设置配置验证器
     */
    public void setConfigValidator(ConfigValidator validator) {
        this.configValidator = validator;
        log.debug("设置配置验证器");
    }
    
    /**
     * 设置配置通知服务
     */
    public void setConfigNotificationService(ConfigNotificationService service) {
        this.configNotificationService = service;
        log.debug("设置配置通知服务");
    }
    
    /**
     * 检查是否有未同步的配置变更
     */
    public boolean hasUnsyncedChanges() {
        return hasChanges.get();
    }
    
    /**
     * 获取配置变更统计信息
     */
    public ConfigChangeStatistics getConfigChangeStatistics() {
        long totalChanges = configHistory.size();
        long recentChanges = configHistory.stream()
                .filter(h -> System.currentTimeMillis() - h.getTimestamp().getTime() < 24 * 60 * 60 * 1000)
                .count();
        
        Map<String, Long> changesByUser = configHistory.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    ConfigHistory::getUser, 
                    java.util.stream.Collectors.counting()
                ));
        
        return new ConfigChangeStatistics(totalChanges, recentChanges, changesByUser);
    }
    
    /**
     * 配置变更统计信息
     */
    public static class ConfigChangeStatistics {
        private final long totalChanges;
        private final long recentChanges;
        private final Map<String, Long> changesByUser;
        
        public ConfigChangeStatistics(long totalChanges, long recentChanges, Map<String, Long> changesByUser) {
            this.totalChanges = totalChanges;
            this.recentChanges = recentChanges;
            this.changesByUser = changesByUser;
        }
        
        public long getTotalChanges() { return totalChanges; }
        public long getRecentChanges() { return recentChanges; }
        public Map<String, Long> getChangesByUser() { return changesByUser; }
    }
    
    // 私有方法
    
    /**
     * 启动配置监控
     */
    private void startConfigMonitoring() {
        scheduler.scheduleWithFixedDelay(this::checkForConfigChanges, 10, 30, TimeUnit.SECONDS);
    }
    
    /**
     * 启动定期配置验证
     */
    private void schedulePeriodicValidation() {
        scheduler.scheduleWithFixedDelay(() -> {
            try {
                validateConfig(configProperties);
            } catch (Exception e) {
                log.warn("定期配置验证失败", e);
            }
        }, 5, 60, TimeUnit.MINUTES);
    }
    
    /**
     * 检查配置变更
     */
    private void checkForConfigChanges() {
        if (!configProperties.getDynamicConfigEnabled()) {
            return;
        }
        
        // 这里可以从外部配置源（如Nacos、Apollo等）检查配置变更
        // 示例：从数据库或其他配置中心获取最新配置
        // boolean hasExternalChanges = checkExternalConfigChanges();
        // if (hasExternalChanges) {
        //     hasChanges.set(true);
        //     log.info("检测到外部配置变更");
        // }
    }
    
    /**
     * 验证配置
     */
    private boolean validateConfig(AuditLogConfigProperties config) {
        if (configValidator != null) {
            return configValidator.validate(config);
        }
        
        // 基础验证逻辑
        if (config.getThreadPoolMaxSize() <= config.getThreadPoolCoreSize()) {
            log.error("线程池最大大小必须大于核心大小");
            return false;
        }
        
        if (config.getExceptionRetryTimes() > 0 && config.getExceptionRetryInterval() < 100) {
            log.error("异常重试间隔必须大于100毫秒");
            return false;
        }
        
        return true;
    }
    
    /**
     * 复制当前配置
     */
    private AuditLogConfigProperties copyCurrentConfig() {
        return ConfigHistory.copyConfig(configProperties);
    }
    
    /**
     * 应用新配置
     */
    private void applyNewConfig(AuditLogConfigProperties newConfig) {
        // 这里应该将新配置应用到实际的组件中
        // 例如：更新线程池配置、缓存配置等
        
        // 示例：更新配置属性
        configProperties.setEnabled(newConfig.getEnabled());
        configProperties.setBatchSize(newConfig.getBatchSize());
        configProperties.setFlushInterval(newConfig.getFlushInterval());
        // ... 其他属性
        
        hasChanges.set(true);
    }
    
    /**
     * 记录配置变更
     */
    private void recordConfigChange(AuditLogConfigProperties oldConfig, 
                                  AuditLogConfigProperties newConfig, 
                                  String changeDescription, 
                                  String user) {
        Long newVersion = versionGenerator.getAndIncrement();
        ConfigHistory history = new ConfigHistory(newVersion, newConfig, changeDescription, user);
        configHistory.add(history);
        
        // 保持历史记录数量在合理范围内
        if (configHistory.size() > 100) {
            configHistory.remove(0);
        }
    }
    
    /**
     * 通知配置变更
     */
    private void notifyConfigChange(AuditLogConfigProperties oldConfig, 
                                  AuditLogConfigProperties newConfig) {
        if (!configProperties.getConfigChangeNotificationEnabled()) {
            return;
        }
        
        // 通知监听器
        configChangeListeners.forEach((name, listener) -> {
            try {
                // 比较关键配置项的变更
                compareAndNotifyChange("batchSize", oldConfig.getBatchSize(), newConfig.getBatchSize(), listener);
                compareAndNotifyChange("flushInterval", oldConfig.getFlushInterval(), newConfig.getFlushInterval(), listener);
                compareAndNotifyChange("auditLevel", oldConfig.getAuditLevel(), newConfig.getAuditLevel(), listener);
            } catch (Exception e) {
                log.warn("通知配置变更失败: {}", name, e);
            }
        });
        
        // 通知外部服务
        if (configNotificationService != null) {
            configNotificationService.notifyConfigChangeComplete(configProperties.getConfigVersion(), true);
        }
    }
    
    /**
     * 比较并通知配置变更
     */
    private void compareAndNotifyChange(String propertyName, Object oldValue, Object newValue, 
                                      ConfigChangeListener listener) {
        if (!Objects.equals(oldValue, newValue)) {
            listener.onConfigChange(propertyName, oldValue, newValue);
        }
    }
    
    /**
     * 更新配置版本
     */
    private void updateConfigVersion() {
        configProperties.setUpdateTimestamp(System.currentTimeMillis());
        configProperties.setConfigVersion("v" + versionGenerator.get());
    }
    
    /**
     * 保存配置历史
     */
    private void saveConfigHistory(String changeDescription, String user) {
        recordConfigChange(null, configProperties, changeDescription, user);
    }
    
    /**
     * 导出配置为JSON
     */
    public String exportConfigAsJson() {
        // 这里应该使用Jackson等库将配置序列化为JSON
        // 为简化示例，返回字符串表示
        return String.format("{\"version\":\"%s\",\"timestamp\":%d,\"enabled\":%b}",
                configProperties.getConfigVersion(),
                configProperties.getUpdateTimestamp(),
                configProperties.getEnabled());
    }
    
    /**
     * 从JSON导入配置
     */
    public boolean importConfigFromJson(String jsonConfig, String changeDescription, String user) {
        try {
            // 这里应该使用Jackson等库将JSON反序列化为配置对象
            // 为简化示例，创建一个基本的解析逻辑
            AuditLogConfigProperties newConfig = parseJsonConfig(jsonConfig);
            if (newConfig != null) {
                return updateConfig(newConfig, changeDescription, user);
            }
            return false;
        } catch (Exception e) {
            log.error("导入配置失败", e);
            return false;
        }
    }
    
    /**
     * 解析JSON配置（简化实现）
     */
    private AuditLogConfigProperties parseJsonConfig(String jsonConfig) {
        // 实际项目中应使用JSON库进行解析
        // 这里仅作示例
        if (jsonConfig.contains("\"enabled\":false")) {
            AuditLogConfigProperties config = copyCurrentConfig();
            config.setEnabled(false);
            return config;
        }
        return null;
    }
}