package com.bing.framework.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bing.framework.entity.LoginRecord;
import com.bing.framework.dto.LoginRecordQueryDTO;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 登录记录Mapper接口
 * 创建日期：2024-11-11
 */
public interface LoginRecordMapper extends BaseMapper<LoginRecord> {

    /**
     * 根据用户ID查询登录记录
     * 
     * @param userId 用户ID
     * @param page 分页参数
     * @return 登录记录列表
     */
    Page<LoginRecord> selectByUserId(@Param("userId") Long userId, Page<LoginRecord> page);

    /**
     * 查询最近的登录记录
     * 
     * @param limit 查询数量限制
     * @return 登录记录列表
     */
    List<LoginRecord> selectRecentRecords(@Param("limit") Integer limit);

    /**
     * 查询失败的登录记录
     * 
     * @param page 分页参数
     * @param startTime 开始时间
     * @return 失败登录记录列表
     */
    Page<LoginRecord> selectFailedRecords(Page<LoginRecord> page, @Param("startTime") String startTime);

    /**
     * 根据条件分页查询登录记录
     * 
     * @param page 分页参数
     * @param queryDTO 查询条件
     * @return 登录记录列表
     */
    Page<LoginRecord> selectByCondition(Page<LoginRecord> page, @Param("query") LoginRecordQueryDTO queryDTO);

    /**
     * 清理过期的登录记录
     * 
     * @param beforeDate 清理时间点
     * @return 删除的记录数
     */
    int deleteExpiredRecords(@Param("beforeDate") String beforeDate);
}