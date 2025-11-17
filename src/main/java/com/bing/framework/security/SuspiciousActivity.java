package com.bing.framework.security;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 可疑活动记录类
 * 用于记录检测到的可疑安全活动
 * 
 * @author zhengbing
 * @date 2025-11-17
 */
@Data
@Builder
public class SuspiciousActivity {
    
    /**
     * 可疑活动ID
     */
    private Long id;
    
    /**
     * 客户端IP
     */
    private String clientIp;
    
    /**
     * 活动类型
     */
    private String activityType;
    
    /**
     * 活动详情
     */
    private String details;
    
    /**
     * 发生时间
     */
    private LocalDateTime timestamp;
    
    /**
     * 严重级别 (1-5, 5为最高)
     */
    private Integer severityLevel;
    
    /**
     * 是否已处理
     */
    private Boolean isProcessed;
    
    /**
     * 处理时间
     */
    private LocalDateTime processedTime;
    
    /**
     * 处理备注
     */
    private String processingNote;
}