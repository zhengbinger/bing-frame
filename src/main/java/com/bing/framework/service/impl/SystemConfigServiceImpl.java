package com.bing.framework.service.impl;

import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bing.framework.entity.SystemConfig;
import com.bing.framework.mapper.SystemConfigMapper;
import com.bing.framework.service.SystemConfigService;
import com.bing.framework.util.SystemConfigCacheManager;

/**
 * 系统配置服务实现类
 * 继承ServiceImpl类并实现SystemConfigService接口
 * 实现系统配置管理的核心业务逻辑，包括CRUD操作、缓存管理等
 * 
 * @author zhengbing
 * @date 2025-11-15
 */
@Service
public class SystemConfigServiceImpl extends ServiceImpl<SystemConfigMapper, SystemConfig> implements SystemConfigService {

    private static final Logger log = LoggerFactory.getLogger(SystemConfigServiceImpl.class);

    @Autowired
    private SystemConfigCacheManager cacheManager;

    @Override
    public String getConfigValue(String configKey) {
        try {
            // 先从缓存中获取配置值
            String configValue = cacheManager.getConfigFromCache(configKey);
            if (configValue != null) {
                return configValue;
            }
            
            // 缓存中没有，从数据库查询
            SystemConfig config = baseMapper.selectByConfigKey(configKey);
            if (config != null) {
                // 放入缓存
                cacheManager.putConfigToCache(configKey, config.getConfigValue());
                return config.getConfigValue();
            }
            
            log.warn("系统配置项不存在: {}", configKey);
            return null;
        } catch (Exception e) {
            log.error("获取系统配置失败: {}", configKey, e);
            return null;
        }
    }

    @Override
    public <T> T getConfigValue(String configKey, T defaultValue, Class<T> targetType) {
        String value = getConfigValue(configKey);
        if (value == null) {
            return defaultValue;
        }
        
        try {
            return convertValue(value, targetType);
        } catch (Exception e) {
            log.warn("配置值转换失败，键: {}, 值: {}, 类型: {}", configKey, value, targetType.getSimpleName());
            return defaultValue;
        }
    }

    @Override
    public Integer getIntConfigValue(String configKey, Integer defaultValue) {
        return getConfigValue(configKey, defaultValue, Integer.class);
    }

    @Override
    public Boolean getBooleanConfigValue(String configKey, Boolean defaultValue) {
        return getConfigValue(configKey, defaultValue, Boolean.class);
    }

    @Override
    public String getStringConfigValue(String configKey, String defaultValue) {
        String value = getConfigValue(configKey);
        return value != null ? value : defaultValue;
    }

    @Override
    public SystemConfig getSystemConfig(String configKey) {
        return baseMapper.selectByConfigKey(configKey);
    }

    @Override
    public List<SystemConfig> getConfigsByType(String configType) {
        return baseMapper.selectByConfigType(configType);
    }

    @Override
    public List<SystemConfig> getAllEnabledConfigs() {
        return baseMapper.selectEnabledConfigs();
    }

    @Override
    public boolean isConfigKeyExists(String configKey) {
        return baseMapper.checkConfigKeyExists(configKey, null) > 0;
    }

    @Override
    public boolean isConfigKeyExists(String configKey, Long excludeId) {
        return baseMapper.checkConfigKeyExists(configKey, excludeId) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateConfigValue(String configKey, String configValue) {
        try {
            SystemConfig config = baseMapper.selectByConfigKey(configKey);
            if (config == null) {
                log.error("系统配置项不存在: {}", configKey);
                return false;
            }
            
            // 验证配置值类型
            if (!validateConfigValue(configValue, config.getConfigType())) {
                log.error("配置值类型验证失败: {} = {}, 类型: {}", configKey, configValue, config.getConfigType());
                return false;
            }
            
            int result = baseMapper.updateConfigValue(config.getId(), configValue);
            if (result > 0) {
                // 更新缓存
                cacheManager.updateConfigInCache(configKey, configValue);
                log.info("更新系统配置成功: {} = {}", configKey, configValue);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("更新系统配置失败: {}", configKey, e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateBatchStatus(List<Long> ids, Integer enabled) {
        try {
            int result = baseMapper.updateBatchStatus(ids, enabled);
            if (result > 0) {
                // 清除缓存中相关配置
                cacheManager.clearConfigsFromCache(ids);
                log.info("批量更新系统配置状态成功: {} 条记录", ids.size());
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("批量更新系统配置状态失败", e);
            return false;
        }
    }

    @Override
    public boolean reloadAllConfigs() {
        try {
            List<SystemConfig> configs = baseMapper.selectEnabledConfigs();
            cacheManager.reloadAllConfigs(configs);
            log.info("重新加载系统配置到缓存成功，共 {} 条配置", configs.size());
            return true;
        } catch (Exception e) {
            log.error("重新加载系统配置到缓存失败", e);
            return false;
        }
    }

    @Override
    public boolean validateConfigValue(String configValue, String configType) {
        if (configValue == null || configValue.trim().isEmpty()) {
            return false;
        }
        
        switch (configType.toLowerCase()) {
            case "string":
                return true;
            case "int":
                try {
                    Integer.parseInt(configValue);
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
            case "long":
                try {
                    Long.parseLong(configValue);
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
            case "boolean":
                return "true".equalsIgnoreCase(configValue) || 
                       "false".equalsIgnoreCase(configValue) ||
                       "1".equals(configValue) || 
                       "0".equals(configValue);
            case "double":
                try {
                    Double.parseDouble(configValue);
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
            case "json":
                return isValidJson(configValue);
            case "email":
                return isValidEmail(configValue);
            case "url":
                return isValidUrl(configValue);
            default:
                return true;
        }
    }

    /**
     * 转换配置值到指定类型
     */
    @SuppressWarnings("unchecked")
    private <T> T convertValue(String value, Class<T> targetType) {
        if (targetType == String.class) {
            return (T) value;
        } else if (targetType == Integer.class) {
            return (T) Integer.valueOf(Integer.parseInt(value));
        } else if (targetType == Long.class) {
            return (T) Long.valueOf(Long.parseLong(value));
        } else if (targetType == Boolean.class) {
            return (T) Boolean.valueOf("true".equalsIgnoreCase(value) || "1".equals(value));
        } else if (targetType == Double.class) {
            return (T) Double.valueOf(Double.parseDouble(value));
        } else {
            throw new IllegalArgumentException("不支持的类型转换: " + targetType.getName());
        }
    }

    /**
     * 验证JSON格式
     */
    private boolean isValidJson(String value) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            objectMapper.readTree(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 验证邮箱格式
     */
    private boolean isValidEmail(String value) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@" +
                           "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(value).matches();
    }

    /**
     * 验证URL格式
     */
    private boolean isValidUrl(String value) {
        try {
            new java.net.URL(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}