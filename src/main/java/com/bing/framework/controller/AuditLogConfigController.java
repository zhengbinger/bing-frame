package com.bing.framework.controller;

import com.bing.framework.config.AuditLogConfigProperties;
import com.bing.framework.config.AuditLogDynamicConfigManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 审计日志配置管理控制器
 * 提供动态配置更新、配置查询、配置历史和配置回滚等REST API
 * 适用于需要通过API动态管理审计日志参数的场景
 * 
 * @author zhengbing
 * @date 2024-12-28
 */
@RestController
@RequestMapping("/api/audit-log/config")
@Validated
@Slf4j
public class AuditLogConfigController {
    
    @Autowired
    private AuditLogDynamicConfigManager configManager;
    
    /**
     * 创建错误响应（Java 8兼容）
     */
    private Map<String, Object> createErrorResponse(String error, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", error);
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
    
    /**
     * 创建成功响应（Java 8兼容）
     */
    private Map<String, Object> createSuccessResponse(Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
    
    /**
     * 获取当前审计日志配置
     */
    @GetMapping("/current")
    public ResponseEntity<?> getCurrentConfig(HttpServletRequest request) {
        try {
            AuditLogConfigProperties currentConfig = configManager.getCurrentConfig();
            log.info("获取当前审计日志配置，请求来源: {}", getClientInfo(request));
            return ResponseEntity.ok(currentConfig);
        } catch (Exception e) {
            log.error("获取当前审计日志配置失败", e);
            return ResponseEntity.internalServerError()
                    .body(createErrorResponse("获取配置失败", e.getMessage()));
        }
    }
    
    /**
     * 更新审计日志配置
     */
    @PostMapping("/update")
    public ResponseEntity<?> updateConfig(@RequestBody Map<String, Object> configMap, 
                                        HttpServletRequest request) {
        try {
            // 验证请求参数
            if (configMap == null || configMap.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("参数错误", "配置参数不能为空"));
            }
            
            String changeDescription = (String) configMap.get("changeDescription");
            String user = getCurrentUser(request);
            
            if (changeDescription == null || changeDescription.trim().isEmpty()) {
                changeDescription = "通过API更新配置";
            }
            
            // 从请求参数构建配置对象
            AuditLogConfigProperties newConfig = buildConfigFromMap(configMap);
            
            // 执行配置更新
            boolean success = configManager.updateConfig(newConfig, changeDescription, user);
            
            if (success) {
                log.info("审计日志配置更新成功，操作人: {}, 描述: {}", user, changeDescription);
                Map<String, Object> successResponse = new HashMap<>();
                successResponse.put("success", true);
                successResponse.put("message", "配置更新成功");
                successResponse.put("configVersion", newConfig.getConfigVersion());
                successResponse.put("user", user);
                successResponse.put("timestamp", System.currentTimeMillis());
                return ResponseEntity.ok(successResponse);
            } else {
                log.warn("审计日志配置更新失败，操作人: {}, 描述: {}", user, changeDescription);
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("配置更新失败", "请检查配置参数是否正确"));
            }
        } catch (Exception e) {
            log.error("更新审计日志配置时发生异常", e);
            return ResponseEntity.internalServerError()
                    .body(createErrorResponse("系统异常", e.getMessage()));
        }
    }
    
    /**
     * 获取配置变更历史
     */
    @GetMapping("/history")
    public ResponseEntity<?> getConfigHistory(@RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "50") int size,
                                            HttpServletRequest request) {
        try {
            List<AuditLogDynamicConfigManager.ConfigHistory> historyList = configManager.getConfigHistory();
            
            // 计算总数和分页
            int totalCount = historyList.size();
            int fromIndex = (page - 1) * size;
            int toIndex = Math.min(fromIndex + size, totalCount);
            
            // 确保索引有效
            if (fromIndex >= totalCount) {
                historyList = new ArrayList<>();
            } else {
                historyList = historyList.subList(fromIndex, toIndex);
            }
            
            log.info("获取配置历史记录，请求来源: {}, 页码: {}, 页大小: {}, 记录数量: {}", 
                    getClientInfo(request), page, size, historyList.size());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", historyList);
            response.put("total", totalCount);
            response.put("page", page);
            response.put("size", size);
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取配置历史失败", e);
            return ResponseEntity.internalServerError()
                    .body(createErrorResponse("获取配置历史失败", e.getMessage()));
        }
    }
    
    /**
     * 回滚配置到指定版本
     */
    @PostMapping("/rollback")
    public ResponseEntity<?> rollbackConfig(@RequestBody Map<String, Object> rollbackMap,
                                          HttpServletRequest request) {
        try {
            // 验证请求参数
            if (!rollbackMap.containsKey("targetVersion")) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("参数错误", "缺少targetVersion参数"));
            }
            
            Long targetVersion = null;
            try {
                targetVersion = Long.valueOf(rollbackMap.get("targetVersion").toString());
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("参数错误", "targetVersion必须是数字"));
            }
            
            String reason = (String) rollbackMap.get("reason");
            String user = getCurrentUser(request);
            
            if (reason == null || reason.trim().isEmpty()) {
                reason = "通过API回滚配置";
            }
            
            // 执行配置回滚
            boolean success = configManager.rollbackToVersion(targetVersion, reason, user);
            
            if (success) {
                log.info("审计日志配置回滚成功，目标版本: {}, 操作人: {}, 原因: {}", 
                        targetVersion, user, reason);
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "配置回滚成功");
                response.put("targetVersion", targetVersion);
                response.put("user", user);
                return ResponseEntity.ok(response);
            } else {
                log.warn("审计日志配置回滚失败，目标版本: {}, 操作人: {}", targetVersion, user);
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "配置回滚失败");
                response.put("message", "未找到指定的版本或回滚失败");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("回滚审计日志配置时发生异常", e);
            return ResponseEntity.internalServerError()
                    .body(createErrorResponse("系统异常", e.getMessage()));
        }
    }
    
    /**
     * 获取配置变更统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getConfigStatistics(HttpServletRequest request) {
        try {
            AuditLogDynamicConfigManager.ConfigChangeStatistics statistics = configManager.getConfigChangeStatistics();
            log.info("获取配置统计信息，请求来源: {}", getClientInfo(request));
            
            // 构建统计信息响应
            Map<String, Object> response = new HashMap<>();
            response.put("totalChanges", statistics.getTotalChanges());
            response.put("recentChanges", statistics.getRecentChanges());
            response.put("changesByUser", statistics.getChangesByUser());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取配置统计信息失败", e);
            return ResponseEntity.internalServerError()
                    .body(createErrorResponse("获取统计信息失败", e.getMessage()));
        }
    }
    
    /**
     * 检查是否有未同步的配置变更
     */
    @GetMapping("/check-changes")
    public ResponseEntity<?> checkConfigChanges(HttpServletRequest request) {
        try {
            boolean hasChanges = configManager.hasUnsyncedChanges();
            log.debug("检查配置变更状态，请求来源: {}", getClientInfo(request));
            Map<String, Object> response = new HashMap<>();
            response.put("hasUnsyncedChanges", hasChanges);
            response.put("checkTime", System.currentTimeMillis());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("检查配置变更状态失败", e);
            return ResponseEntity.internalServerError()
                    .body(createErrorResponse("检查失败", e.getMessage()));
        }
    }
    
    /**
     * 导出配置数据
     */
    @GetMapping("/export")
    public ResponseEntity<?> exportConfig() {
        try {
            AuditLogConfigProperties currentConfig = configManager.getCurrentConfig();
            
            log.info("导出审计日志配置");
            
            Map<String, Object> response = new HashMap<>();
            response.put("exportTime", LocalDateTime.now().toString());
            response.put("config", currentConfig);
            response.put("version", currentConfig.getConfigVersion());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"audit-log-config.json\"")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
        } catch (Exception e) {
            log.error("导出配置失败", e);
            return ResponseEntity.internalServerError()
                    .body(createErrorResponse("导出配置失败", e.getMessage()));
        }
    }
    
    /**
     * 从JSON导入配置
     */
    @PostMapping("/import")
    public ResponseEntity<?> importConfig(@RequestBody Map<String, Object> importMap,
                                        HttpServletRequest request) {
        try {
            // 验证请求参数
            if (!importMap.containsKey("jsonConfig")) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("参数错误", "缺少jsonConfig参数"));
            }
            
            String jsonConfig = (String) importMap.get("jsonConfig");
            String changeDescription = (String) importMap.get("changeDescription");
            String user = getCurrentUser(request);
            
            if (changeDescription == null || changeDescription.trim().isEmpty()) {
                changeDescription = "通过API导入配置";
            }
            
            // 执行配置导入
            boolean success = configManager.importConfigFromJson(jsonConfig, changeDescription, user);
            
            if (success) {
                log.info("审计日志配置导入成功，操作人: {}", user);
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "配置导入成功");
                response.put("user", user);
                response.put("timestamp", System.currentTimeMillis());
                return ResponseEntity.ok(response);
            } else {
                log.warn("审计日志配置导入失败，操作人: {}", user);
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("配置导入失败", "JSON格式错误或配置验证失败"));
            }
        } catch (Exception e) {
            log.error("导入审计日志配置时发生异常", e);
            return ResponseEntity.internalServerError()
                    .body(createErrorResponse("系统异常", e.getMessage()));
        }
    }
    
    /**
     * 获取配置验证结果
     */
    @PostMapping("/validate")
    public ResponseEntity<?> validateConfig(@RequestBody AuditLogConfigProperties config,
                                          HttpServletRequest request) {
        try {
            // 这里应该调用配置验证逻辑
            // 为简化示例，返回固定的验证结果
            boolean isValid = validateConfigInternal(config);
            
            log.info("验证审计日志配置，请求来源: {}, 结果: {}", getClientInfo(request), isValid);
            
            Map<String, Object> response = new HashMap<>();
            response.put("valid", isValid);
            response.put("configVersion", config.getConfigVersion());
            response.put("validationTime", System.currentTimeMillis());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("验证配置时发生异常", e);
            return ResponseEntity.internalServerError()
                    .body(createErrorResponse("验证异常", e.getMessage()));
        }
    }
    
    /**
     * 重置配置为默认值
     */
    @PostMapping("/reset")
    public ResponseEntity<?> resetConfig(@RequestBody Map<String, Object> resetMap,
                                       HttpServletRequest request) {
        try {
            String user = getCurrentUser(request);
            String reason = (String) resetMap.get("reason");
            
            if (reason == null || reason.trim().isEmpty()) {
                reason = "通过API重置配置为默认值";
            }
            
            // 创建默认配置
            AuditLogConfigProperties defaultConfig = createDefaultConfig();
            
            // 更新配置
            boolean success = configManager.updateConfig(defaultConfig, reason, user);
            
            if (success) {
                log.info("审计日志配置重置成功，操作人: {}", user);
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "配置重置成功");
                response.put("user", user);
                response.put("timestamp", System.currentTimeMillis());
                return ResponseEntity.ok(response);
            } else {
                log.warn("审计日志配置重置失败，操作人: {}", user);
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("配置重置失败", "重置配置操作失败"));
            }
        } catch (Exception e) {
            log.error("重置配置时发生异常", e);
            return ResponseEntity.internalServerError()
                    .body(createErrorResponse("系统异常", e.getMessage()));
        }
    }
    
    // 私有辅助方法
    
    /**
     * 从Map构建配置对象
     */
    private AuditLogConfigProperties buildConfigFromMap(Map<String, Object> configMap) {
        AuditLogConfigProperties config = new AuditLogConfigProperties();
        
        // 基础配置
        if (configMap.containsKey("enabled")) {
            config.setEnabled(getBoolean(configMap.get("enabled")));
        }
        if (configMap.containsKey("asyncEnabled")) {
            config.setAsyncEnabled(getBoolean(configMap.get("asyncEnabled")));
        }
        if (configMap.containsKey("batchSize")) {
            config.setBatchSize(getInt(configMap.get("batchSize")));
        }
        if (configMap.containsKey("flushInterval")) {
            config.setFlushInterval(getInt(configMap.get("flushInterval")));
        }
        if (configMap.containsKey("bufferQueueSize")) {
            config.setBufferQueueSize(getInt(configMap.get("bufferQueueSize")));
        }
        if (configMap.containsKey("auditLevel")) {
            config.setAuditLevel(getString(configMap.get("auditLevel")));
        }
        
        // 线程池配置
        if (configMap.containsKey("threadPoolCoreSize")) {
            config.setThreadPoolCoreSize(getInt(configMap.get("threadPoolCoreSize")));
        }
        if (configMap.containsKey("threadPoolMaxSize")) {
            config.setThreadPoolMaxSize(getInt(configMap.get("threadPoolMaxSize")));
        }
        if (configMap.containsKey("threadPoolQueueCapacity")) {
            config.setThreadPoolQueueCapacity(getInt(configMap.get("threadPoolQueueCapacity")));
        }
        if (configMap.containsKey("threadPoolKeepAliveTime")) {
            config.setThreadPoolKeepAliveTime(getInt(configMap.get("threadPoolKeepAliveTime")));
        }
        
        // 异常处理配置
        if (configMap.containsKey("exceptionHandlingEnabled")) {
            config.setExceptionHandlingEnabled(getBoolean(configMap.get("exceptionHandlingEnabled")));
        }
        if (configMap.containsKey("exceptionRetryTimes")) {
            config.setExceptionRetryTimes(getInt(configMap.get("exceptionRetryTimes")));
        }
        if (configMap.containsKey("exceptionRetryInterval")) {
            config.setExceptionRetryInterval(getInt(configMap.get("exceptionRetryInterval")));
        }
        
        // 缓存配置
        if (configMap.containsKey("userCacheEnabled")) {
            config.setUserCacheEnabled(getBoolean(configMap.get("userCacheEnabled")));
        }
        if (configMap.containsKey("userCacheSize")) {
            config.setUserCacheSize(getInt(configMap.get("userCacheSize")));
        }
        if (configMap.containsKey("userCacheExpireTime")) {
            config.setUserCacheExpireTime(getInt(configMap.get("userCacheExpireTime")));
        }
        
        // 高级配置
        if (configMap.containsKey("performanceOptimizationEnabled")) {
            config.setPerformanceOptimizationEnabled(getBoolean(configMap.get("performanceOptimizationEnabled")));
        }
        if (configMap.containsKey("dynamicConfigEnabled")) {
            config.setDynamicConfigEnabled(getBoolean(configMap.get("dynamicConfigEnabled")));
        }
        if (configMap.containsKey("configChangeNotificationEnabled")) {
            config.setConfigChangeNotificationEnabled(getBoolean(configMap.get("configChangeNotificationEnabled")));
        }
        
        return config;
    }
    
    /**
     * 获取布尔值
     */
    private boolean getBoolean(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return false;
    }
    
    /**
     * 获取整数值
     */
    private int getInt(Object value) {
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }
    
    /**
     * 获取长整数值
     */
    private long getLong(Object value) {
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof Integer) {
            return ((Integer) value).longValue();
        }
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                return 0L;
            }
        }
        return 0L;
    }
    
    /**
     * 获取字符串值
     */
    private String getString(Object value) {
        return value != null ? value.toString() : null;
    }
    
    /**
     * 内部配置验证逻辑
     */
    private boolean validateConfigInternal(AuditLogConfigProperties config) {
        if (config.getThreadPoolMaxSize() <= config.getThreadPoolCoreSize()) {
            return false;
        }
        if (config.getExceptionRetryTimes() > 0 && config.getExceptionRetryInterval() < 100) {
            return false;
        }
        if (config.getUserCacheSize() <= 0) {
            return false;
        }
        return true;
    }
    
    /**
     * 创建默认配置
     */
    private AuditLogConfigProperties createDefaultConfig() {
        AuditLogConfigProperties config = new AuditLogConfigProperties();
        config.setEnabled(true);
        config.setAsyncEnabled(true);
        config.setBatchSize(100);
        config.setFlushInterval(5000);
        config.setBufferQueueSize(1000);
        config.setThreadPoolCoreSize(5);
        config.setThreadPoolMaxSize(20);
        config.setThreadPoolQueueCapacity(500);
        config.setThreadPoolKeepAliveTime(60);
        config.setAuditLevel("INFO");
        config.setExceptionHandlingEnabled(true);
        config.setExceptionRetryTimes(3);
        config.setExceptionRetryInterval(1000);
        config.setUserCacheEnabled(true);
        config.setUserCacheSize(1000);
        config.setUserCacheExpireTime(3600000); // 1小时
        config.setPerformanceOptimizationEnabled(true);
        config.setDynamicConfigEnabled(true);
        config.setConfigChangeNotificationEnabled(true);
        return config;
    }
    
    /**
     * 获取当前用户
     */
    private String getCurrentUser(HttpServletRequest request) {
        // 从请求头获取用户信息
        String user = request.getHeader("X-User-Name");
        if (user == null || user.trim().isEmpty()) {
            user = request.getHeader("X-Forwarded-For");
            if (user == null || user.trim().isEmpty()) {
                user = request.getRemoteAddr();
            }
        }
        return user;
    }
    
    /**
     * 获取客户端信息
     */
    private String getClientInfo(HttpServletRequest request) {
        return String.format("%s:%s", request.getRemoteAddr(), request.getRemotePort());
    }
}