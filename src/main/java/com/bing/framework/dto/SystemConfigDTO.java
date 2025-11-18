package com.bing.framework.dto;

import java.time.LocalDateTime;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 系统配置数据传输对象
 * 用于系统配置的创建、更新和查询操作
 * 包含完整的数据验证和API文档注释
 * 
 * @author zhengbing
 * @date 2025-11-15
 */
@Schema(description = "系统配置数据传输对象")
public class SystemConfigDTO {

    @Schema(description = "配置ID（更新时必填）", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "配置键", example = "system.name", required = true)
    @NotBlank(message = "配置键不能为空")
    @Size(max = 100, message = "配置键长度不能超过100个字符")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "配置键只能包含字母、数字、点、下划线和横线")
    private String configKey;

    @Schema(description = "配置值", example = "Bing Framework", required = true)
    @NotBlank(message = "配置值不能为空")
    @Size(max = 5000, message = "配置值长度不能超过5000个字符")
    private String configValue;

    @Schema(description = "配置类型", example = "string", allowableValues = {"string", "int", "long", "boolean", "double", "json", "email", "url"}, required = true)
    @NotBlank(message = "配置类型不能为空")
    @Pattern(regexp = "^(string|int|long|boolean|double|json|email|url)$", message = "配置类型只能是: string, int, long, boolean, double, json, email, url")
    private String configType;

    @Schema(description = "配置描述", example = "系统名称配置")
    @Size(max = 500, message = "配置描述长度不能超过500个字符")
    private String description;

    @Schema(description = "配置分类", example = "system")
    @Size(max = 50, message = "配置分类长度不能超过50个字符")
    private String configCategory;

    @Schema(description = "启用状态(0:禁用, 1:启用)", example = "1", allowableValues = {"0", "1"})
    private Integer enabled;

    @Schema(description = "排序权重", example = "1")
    private Integer sortOrder;

    @Schema(description = "创建时间", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdTime;

    @Schema(description = "更新时间", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedTime;

    @Schema(description = "是否支持动态更新", example = "true")
    private Boolean supportDynamicUpdate;

    @Schema(description = "默认值", example = "Bing Framework")
    private String defaultValue;

    @Schema(description = "最小值（数字类型配置）", example = "0")
    private String minValue;

    @Schema(description = "最大值（数字类型配置）", example = "999999")
    private String maxValue;

    @Schema(description = "正则表达式验证（字符串类型配置）", example = "^[a-zA-Z0-9]+$")
    private String validationRegex;

    @Schema(description = "是否敏感配置，敏感配置在日志和响应中会被脱敏处理", example = "false")
    private Boolean sensitive;

    // 构造函数
    public SystemConfigDTO() {}

    public SystemConfigDTO(String configKey, String configValue, String configType) {
        this.configKey = configKey;
        this.configValue = configValue;
        this.configType = configType;
    }

    public SystemConfigDTO(String configKey, String configValue, String configType, String description) {
        this(configKey, configValue, configType);
        this.description = description;
    }

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public String getConfigValue() {
        return configValue;
    }

    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }

    public String getConfigType() {
        return configType;
    }

    public void setConfigType(String configType) {
        this.configType = configType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getConfigCategory() {
        return configCategory;
    }

    public void setConfigCategory(String configCategory) {
        this.configCategory = configCategory;
    }

    public Integer getEnabled() {
        return enabled;
    }

    public void setEnabled(Integer enabled) {
        this.enabled = enabled;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(LocalDateTime updatedTime) {
        this.updatedTime = updatedTime;
    }

    public Boolean getSupportDynamicUpdate() {
        return supportDynamicUpdate;
    }

    public void setSupportDynamicUpdate(Boolean supportDynamicUpdate) {
        this.supportDynamicUpdate = supportDynamicUpdate;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getMinValue() {
        return minValue;
    }

    public void setMinValue(String minValue) {
        this.minValue = minValue;
    }

    public String getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(String maxValue) {
        this.maxValue = maxValue;
    }

    public String getValidationRegex() {
        return validationRegex;
    }

    public void setValidationRegex(String validationRegex) {
        this.validationRegex = validationRegex;
    }

    public Boolean getSensitive() {
        return sensitive;
    }

    public void setSensitive(Boolean sensitive) {
        this.sensitive = sensitive;
    }

    // 业务判断方法
    public boolean isEnabled() {
        return enabled != null && enabled == 1;
    }

    public boolean isSensitive() {
        return sensitive != null && sensitive;
    }

    public boolean supportsDynamicUpdate() {
        return supportDynamicUpdate != null && supportDynamicUpdate;
    }

    public boolean isNumericType() {
        return "int".equals(configType) || "long".equals(configType) || "double".equals(configType);
    }

    public boolean isBooleanType() {
        return "boolean".equals(configType);
    }

    // 重写toString方法
    @Override
    public String toString() {
        return "SystemConfigDTO{" +
                "id=" + id +
                ", configKey='" + configKey + '\'' +
                ", configValue='" + (isSensitive() ? "***" : configValue) + '\'' +
                ", configType='" + configType + '\'' +
                ", description='" + description + '\'' +
                ", configCategory='" + configCategory + '\'' +
                ", enabled=" + enabled +
                ", sortOrder=" + sortOrder +
                ", supportDynamicUpdate=" + supportDynamicUpdate +
                ", sensitive=" + sensitive +
                '}';
    }

    // 重写equals和hashCode方法
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SystemConfigDTO that = (SystemConfigDTO) obj;
        return id != null ? id.equals(that.id) : that.id == null && 
               configKey != null ? configKey.equals(that.configKey) : that.configKey == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (configKey != null ? configKey.hashCode() : 0);
        return result;
    }
}