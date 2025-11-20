package com.bing.framework.controller;

import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.bing.framework.common.ErrorCode;
import com.bing.framework.common.Result;
import com.bing.framework.entity.SystemConfig;
import com.bing.framework.service.SystemConfigService;
import com.bing.framework.util.SystemConfigCacheManager;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * 系统配置管理控制类
 * 基于Spring Security权限控制和Redis缓存机制实现的RESTful API接口
 * 提供系统配置管理的查询、更新、验证等操作，支持配置缓存和批量操作，包含配置值格式验证和缓存统计功能
 * 
 * @author zhengbing
 * @date 2025-11-20
 */
@Api(value = "SystemConfig", description = "系统配置管理相关接口")
@RestController
@RequestMapping("/api/system-config")
@Validated
public class SystemConfigController {

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private SystemConfigCacheManager cacheManager;

    @ApiOperation(value = "获取配置值", notes = "根据配置键获取配置值")
    @GetMapping("/value/{configKey}")
    @PreAuthorize("hasAuthority('system:config:query')")
    public Result<String> getConfigValue(
            @ApiParam(value = "配置键", required = true)
            @PathVariable @NotBlank String configKey) {
        
        String value = systemConfigService.getConfigValue(configKey);
        return Result.success(value);
    }

    @ApiOperation(value = "获取配置项", notes = "根据配置键获取完整配置项信息")
    @GetMapping("/{configKey}")
    @PreAuthorize("hasAuthority('system:config:query')")
    public Result<SystemConfig> getSystemConfig(
            @ApiParam(value = "配置键", required = true)
            @PathVariable @NotBlank String configKey) {
        
        SystemConfig config = systemConfigService.getSystemConfig(configKey);
        return Result.success(config);
    }

    @ApiOperation(value = "获取所有启用的配置", notes = "获取所有启用的系统配置项")
    @GetMapping("/enabled")
    @PreAuthorize("hasAuthority('system:config:query')")
    public Result<List<SystemConfig>> getAllEnabledConfigs() {
        List<SystemConfig> configs = systemConfigService.getAllEnabledConfigs();
        return Result.success(configs);
    }

    @ApiOperation(value = "根据类型获取配置", notes = "根据配置类型获取所有相关配置项")
    @GetMapping("/type/{configType}")
    @PreAuthorize("hasAuthority('system:config:query')")
    public Result<List<SystemConfig>> getConfigsByType(
            @ApiParam(value = "配置类型", required = true)
            @PathVariable @NotBlank String configType) {
        
        List<SystemConfig> configs = systemConfigService.getConfigsByType(configType);
        return Result.success(configs);
    }

    @ApiOperation(value = "更新配置值", notes = "更新指定配置键的配置值")
    @PutMapping("/value")
    @PreAuthorize("hasAuthority('system:config:update')")
    public Result<Boolean> updateConfigValue(
            @ApiParam(value = "配置键", required = true)
            @RequestParam @NotBlank String configKey,
            @ApiParam(value = "配置值", required = true)
            @RequestParam @NotBlank String configValue) {
        
        boolean success = systemConfigService.updateConfigValue(configKey, configValue);
        return Result.success(success);
    }

    @ApiOperation(value = "批量更新配置状态", notes = "批量更新多个配置项的启用状态")
    @PutMapping("/batch-status")
    @PreAuthorize("hasAuthority('system:config:update')")
    public Result<Boolean> updateBatchStatus(
            @ApiParam(value = "配置ID列表", required = true)
            @RequestBody @NotEmpty List<@NotNull Long> configIds,
            @ApiParam(value = "启用状态(0:禁用, 1:启用)", required = true)
            @RequestParam @NotNull Integer enabled) {
        
        if (enabled != 0 && enabled != 1) {
            return Result.error(ErrorCode.PARAM_ERROR.getCode(), "启用状态参数无效，只能为：0（禁用）或1（启用）");
        }
        
        boolean success = systemConfigService.updateBatchStatus(configIds, enabled);
        return Result.success(success);
    }

    @ApiOperation(value = "重新加载配置缓存", notes = "重新从数据库加载所有配置项到缓存")
    @PostMapping("/reload-cache")
    @PreAuthorize("hasAuthority('system:config:update')")
    public Result<Boolean> reloadCache() {
        boolean success = systemConfigService.reloadAllConfigs();
        return Result.success(success);
    }

    @ApiOperation(value = "验证配置键是否存在", notes = "检查指定配置键是否已存在（排除指定ID）")
    @GetMapping("/exists/{configKey}")
    @PreAuthorize("hasAuthority('system:config:query')")
    public Result<Boolean> checkConfigKeyExists(
            @ApiParam(value = "配置键", required = true)
            @PathVariable @NotBlank String configKey,
            @ApiParam(value = "排除的ID（可选）")
            @RequestParam(required = false) Long excludeId) {
        
        boolean exists = excludeId != null 
            ? systemConfigService.isConfigKeyExists(configKey, excludeId)
            : systemConfigService.isConfigKeyExists(configKey);
        
        return Result.success(exists);
    }

    @ApiOperation(value = "验证配置值格式", notes = "验证配置值是否符合指定类型的格式要求")
    @PostMapping("/validate-value")
    @PreAuthorize("hasAuthority('system:config:query')")
    public Result<Boolean> validateConfigValue(
            @ApiParam(value = "配置值", required = true)
            @RequestParam @NotBlank String configValue,
            @ApiParam(value = "配置类型", required = true)
            @RequestParam @NotBlank String configType) {
        
        boolean valid = systemConfigService.validateConfigValue(configValue, configType);
        return Result.success(valid);
    }

    @ApiOperation(value = "获取缓存统计信息", notes = "获取系统配置缓存的统计信息")
    @GetMapping("/cache-stats")
    @PreAuthorize("hasAuthority('system:config:query')")
    public Result<SystemConfigCacheManager.CacheStats> getCacheStats() {
        SystemConfigCacheManager.CacheStats stats = cacheManager.getCacheStats();
        return Result.success(stats);
    }

    @ApiOperation(value = "获取所有缓存的键", notes = "获取所有缓存在内存中的配置键")
    @GetMapping("/cache-keys")
    @PreAuthorize("hasAuthority('system:config:query')")
    public Result<List<String>> getAllCacheKeys() {
        List<String> keys = new java.util.ArrayList<>(cacheManager.getAllConfigKeys());
        return Result.success(keys);
    }

    @ApiOperation(value = "从缓存中获取配置", notes = "直接从缓存中获取配置值")
    @GetMapping("/cache-value/{configKey}")
    @PreAuthorize("hasAuthority('system:config:query')")
    public Result<String> getConfigFromCache(
            @ApiParam(value = "配置键", required = true)
            @PathVariable @NotBlank String configKey) {
        
        String value = cacheManager.getConfigFromCache(configKey);
        return Result.success(value);
    }

    @ApiOperation(value = "清空所有缓存", notes = "清空所有系统配置缓存")
    @DeleteMapping("/clear-cache")
    @PreAuthorize("hasAuthority('system:config:update')")
    public Result<Boolean> clearAllCache() {
        cacheManager.clearAllCache();
        return Result.success(true);
    }

    @ApiOperation(value = "从缓存中移除配置", notes = "从缓存中移除指定配置键的缓存")
    @DeleteMapping("/cache/{configKey}")
    @PreAuthorize("hasAuthority('system:config:update')")
    public Result<Boolean> removeConfigFromCache(
            @ApiParam(value = "配置键", required = true)
            @PathVariable @NotBlank String configKey) {
        
        cacheManager.removeConfigFromCache(configKey);
        return Result.success(true);
    }

    @ApiOperation(value = "获取配置类型", notes = "获取指定配置键的配置类型")
    @GetMapping("/type/{configKey}")
    @PreAuthorize("hasAuthority('system:config:query')")
    public Result<String> getConfigType(
            @ApiParam(value = "配置键", required = true)
            @PathVariable @NotBlank String configKey) {
        
        String configType = cacheManager.getConfigType(configKey);
        return Result.success(configType);
    }

    @ApiOperation(value = "检查配置是否启用", notes = "检查指定配置键的配置是否启用")
    @GetMapping("/enabled/{configKey}")
    @PreAuthorize("hasAuthority('system:config:query')")
    public Result<Boolean> isConfigEnabled(
            @ApiParam(value = "配置键", required = true)
            @PathVariable @NotBlank String configKey) {
        
        boolean enabled = cacheManager.isConfigEnabled(configKey);
        return Result.success(enabled);
    }
}