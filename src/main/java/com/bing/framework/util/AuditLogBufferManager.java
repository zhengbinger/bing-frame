package com.bing.framework.util;

import com.bing.framework.entity.AuditLog;
import com.bing.framework.mapper.AuditLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 审计日志缓冲池管理器
 * 提供缓冲池功能，将审计日志先放入内存队列，然后批量写入数据库
 * 减少数据库I/O操作，提高系统性能
 * 
 * @author zhengbing
 * @date 2025-11-05
 */
// 添加@Lazy注解实现延迟初始化，提升启动性能
@Component
@Lazy
@Slf4j
public class AuditLogBufferManager {
    
    // 缓冲队列，用于存储待写入的审计日志
    private final BlockingQueue<AuditLog> bufferQueue;
    
    // 批量写入的阈值
    private final int batchSize = 50;
    
    // 定时写入的时间间隔（毫秒）
    private final long flushInterval = 10000;
    
    // 数据库操作接口
    private final AuditLogMapper auditLogMapper;
    
    // 标记是否正在执行批量写入操作
    private final AtomicBoolean isFlushing = new AtomicBoolean(false);
    
    @Autowired
    public AuditLogBufferManager(AuditLogMapper auditLogMapper) {
        this.auditLogMapper = auditLogMapper;
        // 初始化缓冲队列，容量为10000
        this.bufferQueue = new LinkedBlockingQueue<>(10000);
    }
    
    /**
     * 添加审计日志到缓冲池
     * 
     * @param auditLog 审计日志对象
     * @return 是否添加成功
     */
    public boolean addLog(AuditLog auditLog) {
        boolean result = bufferQueue.offer(auditLog);
        if (!result) {
            log.warn("审计日志缓冲池已满，尝试直接写入数据库");
            // 当缓冲池满时，尝试直接写入数据库
            try {
                auditLogMapper.insert(auditLog);
            } catch (Exception e) {
                log.error("直接写入审计日志失败", e);
                return false;
            }
        } else {
            // 当缓冲池中日志数量达到阈值时，触发批量写入
            if (bufferQueue.size() >= batchSize && isFlushing.compareAndSet(false, true)) {
                flushBuffer();
            }
        }
        return true;
    }
    
    /**
     * 批量写入缓冲池中的审计日志
     */
    public void flushBuffer() {
        try {
            List<AuditLog> logsToSave = new ArrayList<>(batchSize);
            // 从缓冲队列中取出最多batchSize个日志
            bufferQueue.drainTo(logsToSave, batchSize);
            
            if (!logsToSave.isEmpty()) {
                try {
                    // 批量插入数据库
                    auditLogMapper.insertBatch(logsToSave);
                    log.debug("成功批量写入{}条审计日志", logsToSave.size());
                } catch (Exception e) {
                    log.error("批量写入审计日志失败，将保留在缓冲队列中", e);
                    // 写入失败时，将日志重新放入队列尾部
                    for (AuditLog log : logsToSave) {
                        bufferQueue.offer(log);
                    }
                    // 添加短暂延迟，避免快速重试
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        } finally {
            // 重置刷新标记
            isFlushing.set(false);
        }
    }
    
    /**
     * 定时任务，定期刷新缓冲池
     * 确保即使日志数量未达到阈值，也能及时写入数据库
     */
    @Scheduled(fixedRate = 10000)
    public void scheduledFlush() {
        if (!bufferQueue.isEmpty() && isFlushing.compareAndSet(false, true)) {
            flushBuffer();
        }
    }
    
    /**
     * 获取当前缓冲池中的日志数量
     */
    public int getBufferSize() {
        return bufferQueue.size();
    }
    
    /**
     * 在应用关闭前，确保将所有缓冲的日志写入数据库
     */
    @PreDestroy
    public void flushOnShutdown() {
        log.info("应用关闭，正在刷新审计日志缓冲池");
        
        int maxAttempts = 3; // 最大重试次数
        int attempts = 0;
        
        while (!bufferQueue.isEmpty() && attempts < maxAttempts) {
            flushBuffer();
            attempts++;
            
            // 短暂等待，让数据库操作完成
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            
            // 如果仍然有数据，且不是第一次尝试，说明可能数据库有问题
            if (!bufferQueue.isEmpty() && attempts == maxAttempts) {
                log.warn("刷新审计日志缓冲池失败，仍有{}条日志未写入", bufferQueue.size());
                // 可以选择在这里将剩余数据写入日志文件或做其他处理
            }
        }
        
        log.info("审计日志缓冲池刷新完成");
    }
}