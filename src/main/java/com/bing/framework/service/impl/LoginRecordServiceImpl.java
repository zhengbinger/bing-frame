package com.bing.framework.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bing.framework.entity.LoginRecord;
import com.bing.framework.mapper.LoginRecordMapper;
import com.bing.framework.service.LoginRecordService;
import com.bing.framework.dto.LoginRecordQueryDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Collections;
import java.util.List;

/**
 * 登录记录服务实现
 * 实现LoginRecordService接口中定义的登录记录相关业务逻辑
 * 包括登录记录的保存、查询、清理等功能
 * 
 * @author zhengbing
 * @date 2025-11-11
 */
@Service
public class LoginRecordServiceImpl extends ServiceImpl<LoginRecordMapper, LoginRecord> implements LoginRecordService {

    private static final Logger log = LoggerFactory.getLogger(LoginRecordServiceImpl.class);
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public boolean saveLoginRecord(LoginRecord loginRecord) {
        try {
            // 设置默认登录时间
            if (loginRecord.getLoginTime() == null) {
                loginRecord.setLoginTime(new Date());
            }
            boolean result = this.save(loginRecord);
            log.info("保存登录记录成功：用户={}, 状态={}", loginRecord.getUsername(), loginRecord.getStatus());
            return result;
        } catch (Exception e) {
            log.error("保存登录记录失败：{}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public Page<LoginRecord> getLoginRecordsByUserId(Long userId, Integer page, Integer size) {
        try {
            Page<LoginRecord> pageObj = new Page<>(page, size);
            return baseMapper.selectByUserId(userId, pageObj);
        } catch (Exception e) {
            log.error("查询用户登录记录失败：userId={}, {}", userId, e.getMessage(), e);
            return new Page<>();
        }
    }

    @Override
    public Page<LoginRecord> queryLoginRecords(LoginRecordQueryDTO queryDTO) {
        try {
            Page<LoginRecord> pageObj = new Page<>(queryDTO.getPage(), queryDTO.getSize());
            return baseMapper.selectByCondition(pageObj, queryDTO);
        } catch (Exception e) {
            log.error("查询登录记录失败：{}", e.getMessage(), e);
            return new Page<>();
        }
    }

    @Override
    public List<LoginRecord> getRecentLoginRecords(Integer limit) {
        try {
            return baseMapper.selectRecentRecords(limit);
        } catch (Exception e) {
            log.error("查询最近登录记录失败：{}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public Page<LoginRecord> getFailedLoginRecords(Integer days, Integer page, Integer size) {
        try {
            // 计算开始时间
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, -days);
            String startTime = dateFormat.format(calendar.getTime());
            
            Page<LoginRecord> pageObj = new Page<>(page, size);
            return baseMapper.selectFailedRecords(pageObj, startTime);
        } catch (Exception e) {
            log.error("查询失败登录记录失败：{}", e.getMessage(), e);
            return new Page<>();
        }
    }

    @Override
    public int cleanExpiredRecords(Integer days) {
        try {
            // 计算清理时间点
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, -days);
            String beforeDate = dateFormat.format(calendar.getTime());
            
            int deleteCount = baseMapper.deleteExpiredRecords(beforeDate);
            log.info("清理过期登录记录成功，删除记录数：{}", deleteCount);
            return deleteCount;
        } catch (Exception e) {
            log.error("清理过期登录记录失败：{}", e.getMessage(), e);
            return 0;
        }
    }
}