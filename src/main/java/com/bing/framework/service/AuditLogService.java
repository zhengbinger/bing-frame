package com.bing.framework.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bing.framework.entity.AuditLog;

/**
 * 审计日志Service接口
 * 继承MyBatis-Plus的IService接口，提供审计日志相关的业务服务
 * 负责审计日志的记录、查询等操作，支持同步和异步记录方式
 * 
 * @author zhengbing
 * @date 2025-11-05
 */
public interface AuditLogService extends IService<AuditLog> {
    
    /**
     * 记录审计日志
     * 
     * @param auditLog 审计日志对象
     */
    void recordAuditLog(AuditLog auditLog);
    
    /**
     * 异步记录审计日志
     * 
     * @param auditLog 审计日志对象
     */
    void recordAuditLogAsync(AuditLog auditLog);
}