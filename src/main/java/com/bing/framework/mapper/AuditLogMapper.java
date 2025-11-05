package com.bing.framework.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bing.framework.entity.AuditLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 审计日志Mapper接口
 */
@Mapper
public interface AuditLogMapper extends BaseMapper<AuditLog> {
    
    // 继承BaseMapper后，自动拥有insert、update、delete、select等通用方法
    // 无需手动编写@Insert等SQL注解

}