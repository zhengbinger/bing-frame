package com.bing.framework.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bing.framework.entity.LoginRecord;
import com.bing.framework.dto.LoginRecordQueryDTO;
import java.util.List;

/**
 * 登录记录服务接口
 * 提供登录记录的保存、查询、清理等业务逻辑处理
 * 继承自MyBatis-Plus的IService接口，扩展了登录记录相关的业务方法
 * 
 * @author zhengbing
 * @date 2025-11-11
 */
public interface LoginRecordService extends IService<LoginRecord> {

    /**
     * 保存登录记录
     * 
     * @param loginRecord 登录记录
     * @return 是否保存成功
     */
    boolean saveLoginRecord(LoginRecord loginRecord);

    /**
     * 根据用户ID查询登录记录
     * 
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页数量
     * @return 分页结果
     */
    Page<LoginRecord> getLoginRecordsByUserId(Long userId, Integer page, Integer size);

    /**
     * 查询系统登录记录
     * 
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    Page<LoginRecord> queryLoginRecords(LoginRecordQueryDTO queryDTO);

    /**
     * 获取最近的登录记录
     * 
     * @param limit 查询数量
     * @return 登录记录列表
     */
    List<LoginRecord> getRecentLoginRecords(Integer limit);

    /**
     * 获取失败的登录记录
     * 
     * @param days 查询天数
     * @param page 页码
     * @param size 每页数量
     * @return 分页结果
     */
    Page<LoginRecord> getFailedLoginRecords(Integer days, Integer page, Integer size);

    /**
     * 清理过期的登录记录
     * 
     * @param days 保留天数
     * @return 清理的记录数
     */
    int cleanExpiredRecords(Integer days);
}