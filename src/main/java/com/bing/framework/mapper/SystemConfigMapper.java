package com.bing.framework.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bing.framework.entity.SystemConfig;

/**
 * 系统配置数据访问层接口
 * 继承BaseMapper接口提供基础CRUD操作，扩展自定义查询方法
 * 
 * @author zhengbing
 * @date 2025-11-15
 */
@Mapper
public interface SystemConfigMapper extends BaseMapper<SystemConfig> {

    /**
     * 根据配置键查询配置
     *
     * @param configKey 配置键
     * @return 系统配置
     */
    @Select("SELECT * FROM system_config WHERE config_key = #{configKey} AND enabled = 1")
    SystemConfig selectByConfigKey(@Param("configKey") String configKey);

    /**
     * 根据配置类型查询配置列表
     *
     * @param configType 配置类型
     * @return 配置列表
     */
    @Select("SELECT * FROM system_config WHERE config_type = #{configType} AND enabled = 1 ORDER BY sort_order ASC")
    List<SystemConfig> selectByConfigType(@Param("configType") String configType);

    /**
     * 查询所有启用的配置
     *
     * @return 配置列表
     */
    @Select("SELECT * FROM system_config WHERE enabled = 1 ORDER BY sort_order ASC, config_key ASC")
    List<SystemConfig> selectEnabledConfigs();

    /**
     * 检查配置键是否存在
     *
     * @param configKey 配置键
     * @param excludeId 排除的ID（用于更新时检查）
     * @return 数量
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM system_config WHERE config_key = #{configKey} " +
            "<if test='excludeId != null'> AND id != #{excludeId} </if>" +
            "</script>")
    int checkConfigKeyExists(@Param("configKey") String configKey, @Param("excludeId") Long excludeId);

    /**
     * 更新配置值
     *
     * @param id 配置ID
     * @param configValue 配置值
     * @return 结果
     */
    @Update("UPDATE system_config SET config_value = #{configValue}, updated_time = NOW() WHERE id = #{id}")
    int updateConfigValue(@Param("id") Long id, @Param("configValue") String configValue);

    /**
     * 根据配置键更新配置值
     *
     * @param configKey 配置键
     * @param configValue 配置值
     * @return 结果
     */
    @Update("UPDATE system_config SET config_value = #{configValue}, updated_time = NOW() WHERE config_key = #{configKey} AND enabled = 1")
    int updateConfigValueByKey(@Param("configKey") String configKey, @Param("configValue") String configValue);

    /**
     * 批量更新配置状态
     *
     * @param ids 配置ID列表
     * @param enabled 状态
     * @return 结果
     */
    @Update("<script>" +
            "UPDATE system_config SET enabled = #{enabled}, updated_time = NOW() WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' close=')' separator=','>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int updateBatchStatus(@Param("ids") List<Long> ids, @Param("enabled") Integer enabled);
}