package com.bing.framework.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bing.framework.entity.AuditLog;

/**
 * 审计日志Service接口
 */
public interface AuditLogService extends IService<AuditLog> {
    
    /**
     * 记录审计日志
     * @param auditLog 审计日志对象
     */
    void recordAuditLog(AuditLog auditLog);
    
    /**
     * 异步记录审计日志
     * @param auditLog 审计日志对象
     */
    void recordAuditLogAsync(AuditLog auditLog);
}