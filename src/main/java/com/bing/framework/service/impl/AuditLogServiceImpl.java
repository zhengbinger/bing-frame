package com.bing.framework.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bing.framework.entity.AuditLog;
import com.bing.framework.mapper.AuditLogMapper;
import com.bing.framework.service.AuditLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 审计日志Service实现类
 */
@Service
public class AuditLogServiceImpl extends ServiceImpl<AuditLogMapper, AuditLog> implements AuditLogService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuditLogServiceImpl.class);
    
    /**
     * 同步记录审计日志
     * @param auditLog 审计日志对象
     */
    @Override
    public void recordAuditLog(AuditLog auditLog) {
        try {
            // 使用MyBatis-Plus的save方法替代直接调用mapper.insert
            this.save(auditLog);
        } catch (Exception e) {
            // 记录日志失败时，记录到文件日志中，避免影响主业务流程
            logger.error("记录审计日志失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 异步记录审计日志
     * @param auditLog 审计日志对象
     */
    @Async
    @Override
    public void recordAuditLogAsync(AuditLog auditLog) {
        recordAuditLog(auditLog);
    }
}