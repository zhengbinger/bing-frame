package com.bing.framework.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 系统配置实体类
 * 使用MyBatis-Plus注解定义表映射关系，通过Lombok简化Getter/Setter等方法
 * 对应数据库system_config表，存储系统级别的配置信息
 * 
 * @author zhengbing
 * @date 2025-11-15
 */
@ApiModel(value = "系统配置信息", description = "系统配置实体类，存储系统级别的配置信息")
@Data
@TableName("system_config")
public class SystemConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 配置ID
     */
    @ApiModelProperty(value = "配置ID", notes = "自增主键", dataType = "Long", example = "1")
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 配置键
     */
    @ApiModelProperty(value = "配置键", notes = "唯一标识配置的键", required = true, dataType = "String", example = "system.name")
    private String configKey;

    /**
     * 配置值
     */
    @ApiModelProperty(value = "配置值", notes = "配置的具体值", required = true, dataType = "String", example = "Bing Framework")
    private String configValue;

    /**
     * 配置类型
     */
    @ApiModelProperty(value = "配置类型", notes = "字符串类型：string, int, boolean, json等", required = true, dataType = "String", example = "string")
    private String configType;

    /**
     * 配置描述
     */
    @ApiModelProperty(value = "配置描述", notes = "配置的详细说明", dataType = "String", example = "系统名称配置")
    private String description;

    /**
     * 配置分类
     */
    @ApiModelProperty(value = "配置分类", notes = "配置所属分类", dataType = "String", example = "system")
    private String configCategory;

    /**
     * 是否启用
     */
    @ApiModelProperty(value = "是否启用", notes = "0-禁用，1-启用", dataType = "Integer", example = "1")
    private Integer enabled;

    /**
     * 排序权重
     */
    @ApiModelProperty(value = "排序权重", notes = "用于排序的权重值", dataType = "Integer", example = "1")
    private Integer sortOrder;

    /**
     * 是否敏感配置
     */
    @ApiModelProperty(value = "是否敏感配置", notes = "0-非敏感，1-敏感", dataType = "Integer", example = "0")
    private Integer isSensitive;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间", dataType = "Date")
    private Date createdTime;

    /**
     * 更新时间
     */
    @ApiModelProperty(value = "更新时间", dataType = "Date")
    private Date updatedTime;
}