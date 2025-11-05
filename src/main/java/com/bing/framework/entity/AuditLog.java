package com.bing.framework.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

/**
 * 审计日志实体类
 * 用于存储重要操作的审计信息到数据库
 */
@Data
@TableName("audit_log")
public class AuditLog {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 操作用户ID
     */
    private Long userId;
    
    /**
     * 操作用户名
     */
    private String username;
    
    /**
     * 操作IP地址
     */
    private String ipAddress;
    
    /**
     * 操作时间
     */
    private Date operationTime;
    
    /**
     * 操作模块
     */
    private String module;
    
    /**
     * 操作类型（查询、新增、修改、删除等）
     */
    private String operationType;
    
    /**
     * 操作描述
     */
    private String description;
    
    /**
     * 请求参数
     */
    private String requestParams;
    
    /**
     * 操作结果（成功/失败）
     */
    private String result;
    
    /**
     * 错误信息（如果有）
     */
    private String errorMessage;
    
    /**
     * 操作耗时（毫秒）
     */
    private Long executionTime;
}