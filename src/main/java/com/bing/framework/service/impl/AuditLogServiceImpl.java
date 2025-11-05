package com.bing.framework.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bing.framework.entity.AuditLog;
import com.bing.framework.mapper.AuditLogMapper;
import com.bing.framework.service.AuditLogService;
import com.bing.framework.util.AuditLogBufferManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * 审计日志Service实现类
 * 继承MyBatis-Plus的ServiceImpl，实现AuditLogService接口
 * 提供审计日志的同步和异步记录功能，包含异常处理机制
 * 
 * @author zhengbing
 * @date 2025-11-05
 */
@Service
public class AuditLogServiceImpl extends ServiceImpl<AuditLogMapper, AuditLog> implements AuditLogService {

    private static final Logger logger = LoggerFactory.getLogger(AuditLogServiceImpl.class);
    
    private final AuditLogBufferManager bufferManager;
    
    @Autowired
    public AuditLogServiceImpl(AuditLogBufferManager bufferManager) {
        this.bufferManager = bufferManager;
    }
    
    /**
     * 同步记录审计日志
     * 
     * @param auditLog 审计日志对象
     */
    @Override
    @Transactional
    public void recordAuditLog(AuditLog auditLog) {
        try {
            // 设置创建时间
            if (auditLog.getCreatedAt() == null) {
                auditLog.setCreatedAt(new Date());
            }
            
            // 使用缓冲池管理器添加审计日志
            bufferManager.addLog(auditLog);
        } catch (Exception e) {
            // 记录日志保存失败的情况
            logger.error("保存审计日志失败", e);
        }
    }
    
    /**
     * 异步记录审计日志
     * 
     * @param auditLog 审计日志对象
     */
    @Override
    @Async("auditLogExecutor")
    public void recordAuditLogAsync(AuditLog auditLog) {
        recordAuditLog(auditLog);
    }
}