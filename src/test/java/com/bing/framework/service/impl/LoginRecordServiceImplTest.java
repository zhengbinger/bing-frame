package com.bing.framework.service.impl;

import com.bing.framework.entity.LoginRecord;
import com.bing.framework.mapper.LoginRecordMapper;
import com.bing.framework.dto.LoginRecordQueryDTO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Date;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * LoginRecordServiceImpl的单元测试类
 * 用于测试登录记录相关的业务逻辑
 * @author zhengbing
 * @date 2025-11-11
 */
public class LoginRecordServiceImplTest {

    @Mock
    private LoginRecordMapper loginRecordMapper;

    @InjectMocks
    private LoginRecordServiceImpl loginRecordService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * 测试保存登录记录功能
     */
    @Test
    void testSaveLoginRecord() {
        // 准备测试数据
        LoginRecord loginRecord = new LoginRecord();
        loginRecord.setUserId(1L);
        loginRecord.setUsername("testUser");
        loginRecord.setIpAddress("192.168.1.1");
        loginRecord.setUserAgent("Mozilla/5.0");
        loginRecord.setLoginTime(new Date());
        loginRecord.setStatus(1);

        // 模拟mapper行为
        when(loginRecordMapper.insert(loginRecord)).thenReturn(1);

        // 执行测试
        boolean result = loginRecordService.saveLoginRecord(loginRecord);

        // 验证结果
        assertTrue(result);
        verify(loginRecordMapper, times(1)).insert(loginRecord);
    }

    /**
     * 测试查询登录记录分页功能
     */
    @Test
    void testQueryLoginRecords() {
        // 准备测试数据
        LoginRecordQueryDTO queryDTO = new LoginRecordQueryDTO();
        queryDTO.setUsername("test");
        queryDTO.setPage(1);
        queryDTO.setSize(10);

        // 模拟分页数据
        Page<LoginRecord> mockPage = new Page<>(1, 10);
        LoginRecord record1 = new LoginRecord();
        record1.setId(1L);
        record1.setUserId(1L);
        record1.setUsername("testUser1");
        record1.setIpAddress("192.168.1.1");
        record1.setUserAgent("Mozilla/5.0");
        record1.setLoginTime(new Date());
        record1.setStatus(1);
        
        LoginRecord record2 = new LoginRecord();
        record2.setId(2L);
        record2.setUserId(2L);
        record2.setUsername("testUser2");
        record2.setIpAddress("192.168.1.2");
        record2.setUserAgent("Chrome/90");
        record2.setLoginTime(new Date());
        record2.setStatus(1);
        
        List<LoginRecord> mockRecords = Arrays.asList(record1, record2);
        mockPage.setRecords(mockRecords);
        mockPage.setTotal(2);

        // 模拟mapper行为
        when(loginRecordMapper.selectByCondition(any(Page.class), any(LoginRecordQueryDTO.class))).thenReturn(mockPage);

        // 执行测试
        Page<LoginRecord> result = loginRecordService.queryLoginRecords(queryDTO);

        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.getTotal());
        verify(loginRecordMapper, times(1)).selectByCondition(any(Page.class), any(LoginRecordQueryDTO.class));
    }

    /**
     * 测试清理指定天数前的登录记录功能
     */
    @Test
    void testCleanExpiredRecords() {
        // 模拟mapper行为
        when(loginRecordMapper.deleteExpiredRecords(anyString())).thenReturn(5);

        // 执行测试
        int deletedCount = loginRecordService.cleanExpiredRecords(90);

        // 验证结果
        assertEquals(5, deletedCount);
        verify(loginRecordMapper, times(1)).deleteExpiredRecords(anyString());
    }

    /**
     * 测试获取指定用户的登录记录功能
     */
    @Test
    void testGetLoginRecordsByUserId() {
        // 准备测试数据
        Long userId = 1L;
        Integer page = 1;
        Integer size = 10;
        
        // 模拟分页数据
        Page<LoginRecord> mockPage = new Page<>(page, size);
        LoginRecord record1 = new LoginRecord();
        record1.setId(1L);
        record1.setUserId(1L);
        record1.setUsername("testUser");
        record1.setIpAddress("192.168.1.1");
        record1.setUserAgent("Mozilla/5.0");
        record1.setLoginTime(new Date());
        record1.setStatus(1);
        
        LoginRecord record2 = new LoginRecord();
        record2.setId(2L);
        record2.setUserId(1L);
        record2.setUsername("testUser");
        record2.setIpAddress("192.168.1.2");
        record2.setUserAgent("Chrome/90");
        record2.setLoginTime(new Date());
        record2.setStatus(1);
        
        mockPage.setRecords(Arrays.asList(record1, record2));
        mockPage.setTotal(2);

        // 模拟mapper行为
        when(loginRecordMapper.selectByUserId(eq(userId), any(Page.class))).thenReturn(mockPage);

        // 执行测试
        Page<LoginRecord> result = loginRecordService.getLoginRecordsByUserId(userId, page, size);

        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.getTotal());
        verify(loginRecordMapper, times(1)).selectByUserId(eq(userId), any(Page.class));
    }
    
    /**
     * 测试获取最近登录记录功能
     */
    @Test
    void testGetRecentLoginRecords() {
        // 准备测试数据
        Integer limit = 5;
        LoginRecord record = new LoginRecord();
        record.setId(1L);
        record.setUserId(1L);
        record.setUsername("testUser");
        record.setIpAddress("192.168.1.1");
        record.setUserAgent("Mozilla/5.0");
        record.setLoginTime(new Date());
        record.setStatus(1);
        
        List<LoginRecord> mockRecords = Arrays.asList(record);

        // 模拟mapper行为
        when(loginRecordMapper.selectRecentRecords(limit)).thenReturn(mockRecords);

        // 执行测试
        List<LoginRecord> result = loginRecordService.getRecentLoginRecords(limit);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(loginRecordMapper, times(1)).selectRecentRecords(limit);
    }
}