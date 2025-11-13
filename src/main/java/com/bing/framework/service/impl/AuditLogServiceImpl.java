package com.bing.framework.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bing.framework.entity.AuditLog;
import com.bing.framework.mapper.AuditLogMapper;
import com.bing.framework.service.AuditLogService;
import com.bing.framework.util.AuditLogBufferManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * 审计日志Service实现类
 * 继承MyBatis-Plus的ServiceImpl，实现AuditLogService接口
 * 提供审计日志的同步和异步记录功能，包含异常处理机制
 * 
 * @author zhengbing
 * @date 2025-11-05
 */
// 添加@Lazy注解实现延迟初始化，提升启动性能
@Service
@Lazy
@Slf4j
public class AuditLogServiceImpl extends ServiceImpl<AuditLogMapper, AuditLog> implements AuditLogService {


    
    private final AuditLogBufferManager bufferManager;
    private final Clock clock;
    
    @Autowired
    public AuditLogServiceImpl(AuditLogBufferManager bufferManager, Clock clock) {
        this.bufferManager = bufferManager;
        this.clock = clock;
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
            // 设置操作时间
            if (auditLog.getOperationTime() == null) {
                // 使用Clock抽象获取当前时间，提高可测试性
                LocalDateTime now = LocalDateTime.now(clock);
                auditLog.setOperationTime(Date.from(now.atZone(ZoneId.systemDefault()).toInstant()));
            }
            
            // 使用缓冲池管理器添加审计日志
            bufferManager.addLog(auditLog);
        } catch (Exception e) {
            // 记录日志保存失败的情况
            log.error("保存审计日志失败", e);
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