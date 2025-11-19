package com.bing.framework.config;

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
public class AuditLogConfigProperties {
    
    /**
     * 是否启用审计日志
     */
    @NotNull(message = "审计日志启用状态不能为空")
    private Boolean enabled = true;
    
    /**
     * 是否启用异步记录
     */
    @NotNull(message = "异步记录启用状态不能为空")
    private Boolean asyncEnabled = true;
    
    /**
     * 批量写入大小
     */
    @Min(value = 10, message = "批量大小不能小于10")
    @Max(value = 1000, message = "批量大小不能大于1000")
    private Integer batchSize = 100;
    
    /**
     * 刷新间隔时间（毫秒）
     */
    @Min(value = 1000, message = "刷新间隔不能小于1000毫秒")
    @Max(value = 60000, message = "刷新间隔不能大于60000毫秒")
    private Integer flushInterval = 5000;
    
    /**
     * 缓冲队列大小
     */
    @Min(value = 100, message = "缓冲队列大小不能小于100")
    @Max(value = 10000, message = "缓冲队列大小不能大于10000")
    private Integer bufferQueueSize = 1000;
    
    /**
     * 线程池核心大小
     */
    @Min(value = 2, message = "线程池核心大小不能小于2")
    @Max(value = 50, message = "线程池核心大小不能大于50")
    private Integer threadPoolCoreSize = 10;
    
    /**
     * 线程池最大大小
     */
    @Min(value = 5, message = "线程池最大大小不能小于5")
    @Max(value = 100, message = "线程池最大大小不能大于100")
    private Integer threadPoolMaxSize = 50;
    
    /**
     * 线程池队列容量
     */
    @Min(value = 100, message = "线程池队列容量不能小于100")
    @Max(value = 10000, message = "线程池队列容量不能大于10000")
    private Integer threadPoolQueueCapacity = 200;
    
    /**
     * 线程池保持活跃时间（秒）
     */
    @Min(value = 30, message = "线程池保持活跃时间不能小于30秒")
    @Max(value = 3600, message = "线程池保持活跃时间不能大于3600秒")
    private Integer threadPoolKeepAliveTime = 60;
    
    /**
     * 审计级别（NONE, BASIC, FULL）
     */
    @NotBlank(message = "审计级别不能为空")
    private String auditLevel = "BASIC";
    
    /**
     * 是否启用缓冲池
     */
    @NotNull(message = "缓冲池启用状态不能为空")
    private Boolean bufferPoolEnabled = true;
    
    /**
     * 是否启用异常处理
     */
    @NotNull(message = "异常处理启用状态不能为空")
    private Boolean exceptionHandlingEnabled = true;
    
    /**
     * 异常重试次数
     */
    @Min(value = 0, message = "异常重试次数不能小于0")
    @Max(value = 10, message = "异常重试次数不能大于10")
    private Integer exceptionRetryTimes = 3;
    
    /**
     * 异常重试间隔（毫秒）
     */
    @Min(value = 100, message = "异常重试间隔不能小于100毫秒")
    @Max(value = 10000, message = "异常重试间隔不能大于10000毫秒")
    private Integer exceptionRetryInterval = 1000;
    
    /**
     * 是否启用死循环防护
     */
    @NotNull(message = "死循环防护启用状态不能为空")
    private Boolean deadLoopProtectionEnabled = true;
    
    /**
     * 最大执行时间（毫秒）
     */
    @Min(value = 1000, message = "最大执行时间不能小于1000毫秒")
    @Max(value = 60000, message = "最大执行时间不能大于60000毫秒")
    private Integer maxExecutionTime = 5000;
    
    /**
     * 是否启用专用数据源
     */
    @NotNull(message = "专用数据源启用状态不能为空")
    private Boolean dedicatedDataSourceEnabled = false;
    
    /**
     * 数据源监控启用状态
     */
    @NotNull(message = "数据源监控启用状态不能为空")
    private Boolean datasourceMonitoringEnabled = true;
    
    /**
     * 性能调优启用状态
     */
    @NotNull(message = "性能调优启用状态不能为空")
    private Boolean performanceOptimizationEnabled = true;
    
    /**
     * 配置版本号
     */
    private String configVersion = "1.0.0";
    
    /**
     * 配置更新时间戳
     */
    private Long updateTimestamp = System.currentTimeMillis();
    
    /**
     * 动态配置启用状态
     */
    @NotNull(message = "动态配置启用状态不能为空")
    private Boolean dynamicConfigEnabled = true;
    
    /**
     * 配置变更通知启用状态
     */
    @NotNull(message = "配置变更通知启用状态不能为空")
    private Boolean configChangeNotificationEnabled = true;
    
    /**
     * 用户信息缓存启用状态
     */
    @NotNull(message = "用户信息缓存启用状态不能为空")
    private Boolean userCacheEnabled = true;
    
    /**
     * 用户缓存大小
     */
    @Min(value = 100, message = "用户缓存大小不能小于100")
    @Max(value = 10000, message = "用户缓存大小不能大于10000")
    private Integer userCacheSize = 1000;
    
    /**
     * 用户缓存过期时间（分钟）
     */
    @Min(value = 5, message = "用户缓存过期时间不能小于5分钟")
    @Max(value = 1440, message = "用户缓存过期时间不能大于1440分钟")
    private Integer userCacheExpireTime = 30;
}