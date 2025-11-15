package com.bing.framework.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bing.framework.entity.SystemConfig;

import java.util.List;

/**
 * 系统配置服务接口
 * 提供系统配置的基础CRUD操作和业务逻辑
 * 
 * @author zhengbing
 * @date 2025-11-15
 */
public interface SystemConfigService extends IService<SystemConfig> {

    /**
     * 根据配置键获取配置值
     *
     * @param configKey 配置键
     * @return 配置值
     */
    String getConfigValue(String configKey);

    /**
     * 根据配置键获取配置（带类型转换）
     *
     * @param configKey 配置键
     * @param defaultValue 默认值
     * @param targetType 目标类型
     * @return 转换后的配置值
     */
    <T> T getConfigValue(String configKey, T defaultValue, Class<T> targetType);

    /**
     * 根据配置键获取整型配置值
     *
     * @param configKey 配置键
     * @param defaultValue 默认值
     * @return 整型配置值
     */
    Integer getIntConfigValue(String configKey, Integer defaultValue);

    /**
     * 根据配置键获取布尔型配置值
     *
     * @param configKey 配置键
     * @param defaultValue 默认值
     * @return 布尔型配置值
     */
    Boolean getBooleanConfigValue(String configKey, Boolean defaultValue);

    /**
     * 根据配置键获取字符串配置值
     *
     * @param configKey 配置键
     * @param defaultValue 默认值
     * @return 字符串配置值
     */
    String getStringConfigValue(String configKey, String defaultValue);

    /**
     * 根据配置键获取系统配置对象
     *
     * @param configKey 配置键
     * @return 系统配置对象
     */
    SystemConfig getSystemConfig(String configKey);

    /**
     * 根据配置类型获取配置列表
     *
     * @param configType 配置类型
     * @return 配置列表
     */
    List<SystemConfig> getConfigsByType(String configType);

    /**
     * 获取所有启用的配置
     *
     * @return 配置列表
     */
    List<SystemConfig> getAllEnabledConfigs();

    /**
     * 检查配置键是否存在
     *
     * @param configKey 配置键
     * @return 是否存在
     */
    boolean isConfigKeyExists(String configKey);

    /**
     * 检查配置键是否存在（排除指定ID）
     *
     * @param configKey 配置键
     * @param excludeId 排除的ID
     * @return 是否存在
     */
    boolean isConfigKeyExists(String configKey, Long excludeId);

    /**
     * 更新配置值
     *
     * @param configKey 配置键
     * @param configValue 配置值
     * @return 是否成功
     */
    boolean updateConfigValue(String configKey, String configValue);

    /**
     * 批量更新配置状态
     *
     * @param ids 配置ID列表
     * @param enabled 启用状态
     * @return 是否成功
     */
    boolean updateBatchStatus(List<Long> ids, Integer enabled);

    /**
     * 重新加载所有配置到缓存
     *
     * @return 是否成功
     */
    boolean reloadAllConfigs();

    /**
     * 验证配置值的类型是否正确
     *
     * @param configValue 配置值
     * @param configType 配置类型
     * @return 是否有效
     */
    boolean validateConfigValue(String configValue, String configType);
}