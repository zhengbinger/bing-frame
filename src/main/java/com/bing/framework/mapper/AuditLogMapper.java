package com.bing.framework.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bing.framework.entity.AuditLog;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 审计日志Mapper接口
 * 继承MyBatis-Plus的BaseMapper接口，自动获得通用CRUD操作方法
 * 用于审计日志的数据库访问操作，提供增删改查等功能
 * 
 * @author zhengbing
 * @date 2025-11-05
 */
@Mapper
public interface AuditLogMapper extends BaseMapper<AuditLog> {
    
    // 继承BaseMapper后，自动拥有insert、update、delete、select等通用方法
    // 无需手动编写@Insert等SQL注解
    
    /**
     * 批量插入审计日志
     * 使用MyBatis-Plus的批量插入功能
     * 
     * @param logs 审计日志列表
     * @return 插入成功的记录数
     */
    int insertBatch(List<AuditLog> logs);

}