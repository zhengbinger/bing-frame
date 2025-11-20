package com.bing.framework.config;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 审计日志配置属性类
 * 支持动态配置变更和属性验证
 * 
 * @author zhengbing
 * @date 2024-12-28
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.audit")
@ApiModel(description = "审计日志配置属性类，支持动态配置变更和属性验证")
public class AuditLogConfigProperties {
    
    /**
     * 是否启用审计日志
     */
    @NotNull(message = "审计日志启用状态不能为空")
    @ApiModelProperty(value = "是否启用审计日志", example = "true", required = true)
    private Boolean enabled = true;
    
    /**
     * 是否启用异步记录
     */
    @NotNull(message = "异步记录启用状态不能为空")
    @ApiModelProperty(value = "是否启用异步记录", example = "true", required = true)
    private Boolean asyncEnabled = true;
    
    /**
     * 批量写入大小
     */
    @Min(value = 10, message = "批量大小不能小于10")
    @Max(value = 1000, message = "批量大小不能大于1000")
    @ApiModelProperty(value = "批量写入大小", example = "100", allowableValues = "range[10,1000]", required = false)
    private Integer batchSize = 100;
    
    /**
     * 刷新间隔时间（毫秒）
     */
    @Min(value = 1000, message = "刷新间隔不能小于1000毫秒")
    @Max(value = 60000, message = "刷新间隔不能大于60000毫秒")
    @ApiModelProperty(value = "刷新间隔时间", notes = "单位：毫秒", example = "5000", allowableValues = "range[1000,60000]", required = false)
    private Integer flushInterval = 5000;
    
    /**
     * 缓冲队列大小
     */
    @Min(value = 100, message = "缓冲队列大小不能小于100")
    @Max(value = 10000, message = "缓冲队列大小不能大于10000")
    @ApiModelProperty(value = "缓冲队列大小", example = "1000", allowableValues = "range[100,10000]", required = false)
    private Integer bufferQueueSize = 1000;
    
    /**
     * 线程池核心大小
     */
    @Min(value = 2, message = "线程池核心大小不能小于2")
    @Max(value = 50, message = "线程池核心大小不能大于50")
    @ApiModelProperty(value = "线程池核心大小", example = "10", allowableValues = "range[2,50]", required = false)
    private Integer threadPoolCoreSize = 10;
    
    /**
     * 线程池最大大小
     */
    @Min(value = 5, message = "线程池最大大小不能小于5")
    @Max(value = 100, message = "线程池最大大小不能大于100")
    @ApiModelProperty(value = "线程池最大大小", example = "50", allowableValues = "range[5,100]", required = false)
    private Integer threadPoolMaxSize = 50;
    
    /**
     * 线程池队列容量
     */
    @Min(value = 100, message = "线程池队列容量不能小于100")
    @Max(value = 10000, message = "线程池队列容量不能大于10000")
    @ApiModelProperty(value = "线程池队列容量", example = "200", allowableValues = "range[100,10000]", required = false)
    private Integer threadPoolQueueCapacity = 200;
    
    /**
     * 线程池保持活跃时间（秒）
     */
    @Min(value = 30, message = "线程池保持活跃时间不能小于30秒")
    @Max(value = 3600, message = "线程池保持活跃时间不能大于3600秒")
    @ApiModelProperty(value = "线程池保持活跃时间", notes = "单位：秒", example = "60", allowableValues = "range[30,3600]", required = false)
    private Integer threadPoolKeepAliveTime = 60;
    
    /**
     * 审计级别（NONE, BASIC, FULL）
     */
    @NotBlank(message = "审计级别不能为空")
    @ApiModelProperty(value = "审计级别", notes = "可选值：NONE, BASIC, FULL", example = "BASIC", allowableValues = "NONE, BASIC, FULL", required = true)
    private String auditLevel = "BASIC";
    
    /**
     * 是否启用缓冲池
     */
    @NotNull(message = "缓冲池启用状态不能为空")
    @ApiModelProperty(value = "是否启用缓冲池", example = "true", required = true)
    private Boolean bufferPoolEnabled = true;
    
    /**
     * 是否启用异常处理
     */
    @NotNull(message = "异常处理启用状态不能为空")
    @ApiModelProperty(value = "是否启用异常处理", example = "true", required = true)
    private Boolean exceptionHandlingEnabled = true;
    
    /**
     * 异常重试次数
     */
    @Min(value = 0, message = "异常重试次数不能小于0")
    @Max(value = 10, message = "异常重试次数不能大于10")
    @ApiModelProperty(value = "异常重试次数", example = "3", allowableValues = "range[0,10]", required = false)
    private Integer exceptionRetryTimes = 3;
    
    /**
     * 异常重试间隔（毫秒）
     */
    @Min(value = 100, message = "异常重试间隔不能小于100毫秒")
    @Max(value = 10000, message = "异常重试间隔不能大于10000毫秒")
    @ApiModelProperty(value = "异常重试间隔", notes = "单位：毫秒", example = "1000", allowableValues = "range[100,10000]", required = false)
    private Integer exceptionRetryInterval = 1000;
    
    /**
     * 是否启用死循环防护
     */
    @NotNull(message = "死循环防护启用状态不能为空")
    @ApiModelProperty(value = "是否启用死循环防护", example = "true", required = true)
    private Boolean deadLoopProtectionEnabled = true;
    
    /**
     * 最大执行时间（毫秒）
     */
    @Min(value = 1000, message = "最大执行时间不能小于1000毫秒")
    @Max(value = 60000, message = "最大执行时间不能大于60000毫秒")
    @ApiModelProperty(value = "最大执行时间", notes = "单位：毫秒", example = "5000", allowableValues = "range[1000,60000]", required = false)
    private Integer maxExecutionTime = 5000;
    
    /**
     * 是否启用专用数据源
     */
    @NotNull(message = "专用数据源启用状态不能为空")
    @ApiModelProperty(value = "是否启用专用数据源", example = "false", required = true)
    private Boolean dedicatedDataSourceEnabled = false;
    
    /**
     * 数据源监控启用状态
     */
    @NotNull(message = "数据源监控启用状态不能为空")
    @ApiModelProperty(value = "数据源监控启用状态", example = "true", required = true)
    private Boolean datasourceMonitoringEnabled = true;
    
    /**
     * 性能调优启用状态
     */
    @NotNull(message = "性能调优启用状态不能为空")
    @ApiModelProperty(value = "性能调优启用状态", example = "true", required = true)
    private Boolean performanceOptimizationEnabled = true;
    
    /**
     * 配置版本号
     */
    @ApiModelProperty(value = "配置版本号", example = "1.0.0", required = false)
    private String configVersion = "1.0.0";
    
    /**
     * 配置更新时间戳
     */
    @ApiModelProperty(value = "配置更新时间戳", notes = "时间戳格式：毫秒", example = "1640995200000", required = false)
    private Long updateTimestamp = System.currentTimeMillis();
    
    /**
     * 动态配置启用状态
     */
    @NotNull(message = "动态配置启用状态不能为空")
    @ApiModelProperty(value = "动态配置启用状态", example = "true", required = true)
    private Boolean dynamicConfigEnabled = true;
    
    /**
     * 配置变更通知启用状态
     */
    @NotNull(message = "配置变更通知启用状态不能为空")
    @ApiModelProperty(value = "配置变更通知启用状态", example = "true", required = true)
    private Boolean configChangeNotificationEnabled = true;
    
    /**
     * 用户信息缓存启用状态
     */
    @NotNull(message = "用户信息缓存启用状态不能为空")
    @ApiModelProperty(value = "用户信息缓存启用状态", example = "true", required = true)
    private Boolean userCacheEnabled = true;
    
    /**
     * 用户缓存大小
     */
    @Min(value = 100, message = "用户缓存大小不能小于100")
    @Max(value = 10000, message = "用户缓存大小不能大于10000")
    @ApiModelProperty(value = "用户缓存大小", example = "1000", allowableValues = "range[100,10000]", required = false)
    private Integer userCacheSize = 1000;
    
    /**
     * 用户缓存过期时间（分钟）
     */
    @Min(value = 5, message = "用户缓存过期时间不能小于5分钟")
    @Max(value = 1440, message = "用户缓存过期时间不能大于1440分钟")
    @ApiModelProperty(value = "用户缓存过期时间", notes = "单位：分钟", example = "30", allowableValues = "range[5,1440]", required = false)
    private Integer userCacheExpireTime = 30;
    
    /**
     * 日志保留天数
     */
    @Min(value = 1, message = "日志保留天数不能小于1")
    @Max(value = 365, message = "日志保留天数不能大于365")
    @ApiModelProperty(value = "日志保留天数", example = "30", allowableValues = "range[1,365]", required = false)
    private Integer retentionDays = 30;
    
    /**
     * 是否启用字段过滤
     */
    @NotNull(message = "字段过滤启用状态不能为空")
    @ApiModelProperty(value = "是否启用字段过滤", example = "false", required = true)
    private Boolean fieldFilterEnabled = false;
    
    /**
     * 敏感字段列表（JSON格式）
     */
    @NotBlank(message = "敏感字段列表不能为空")
    @ApiModelProperty(value = "敏感字段列表", notes = "JSON格式的敏感字段名称数组", example = "[\"password\", \"token\", \"secret\"]", required = true)
    private String sensitiveFields = "[\"password\", \"token\", \"secret\"]";
    
    /**
     * 是否启用配置变更审计
     */
    @NotNull(message = "配置变更审计启用状态不能为空")
    @ApiModelProperty(value = "是否启用配置变更审计", example = "true", required = true)
    private Boolean configChangeAuditEnabled = true;
    
    /**
     * 配置变更审计级别
     */
    @NotBlank(message = "配置变更审计级别不能为空")
    @ApiModelProperty(value = "配置变更审计级别", notes = "可选值：INFO, WARN, ERROR", example = "INFO", allowableValues = "INFO, WARN, ERROR", required = true)
    private String configChangeAuditLevel = "INFO";
    
    /**
     * 存储类型
     */
    @NotBlank(message = "存储类型不能为空")
    @ApiModelProperty(value = "存储类型", notes = "可选值：FILE, DATABASE, BOTH", example = "FILE", allowableValues = "FILE, DATABASE, BOTH", required = true)
    private String storageType = "FILE";
}