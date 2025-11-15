package com.bing.framework.service.impl;

import com.bing.framework.entity.SystemConfig;
import com.bing.framework.mapper.SystemConfigMapper;
import com.bing.framework.util.SystemConfigCacheManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * SystemConfigServiceImpl单元测试
 * 使用Mockito框架模拟依赖，JUnit5进行测试
 * 覆盖缓存逻辑、数据库操作、配置验证等核心功能
 * 
 * @author zhengbing
 * @date 2025-11-15
 */
@SpringBootTest
public class SystemConfigServiceImplTest {

    @Mock
    private SystemConfigMapper systemConfigMapper;
    
    @Mock
    private SystemConfigCacheManager cacheManager;
    
    @InjectMocks
    private SystemConfigServiceImpl systemConfigService;
    
    private SystemConfig testConfig;
    
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        testConfig = new SystemConfig();
        testConfig.setId(1L);
        testConfig.setConfigKey("test.config.key");
        testConfig.setConfigValue("test.config.value");
        testConfig.setConfigType("string");
        testConfig.setDescription("测试配置项");
        testConfig.setEnabled(1);
    }
    
    @Test
    public void testGetConfigValue_SuccessFromCache() {
        // 模拟缓存命中
        when(cacheManager.getConfigFromCache("test.config.key")).thenReturn("cached.value");
        
        // 执行测试
        String result = systemConfigService.getConfigValue("test.config.key");
        
        // 验证结果
        Assertions.assertEquals("cached.value", result);
        verify(cacheManager, times(1)).getConfigFromCache("test.config.key");
        verify(systemConfigMapper, never()).selectByConfigKey(anyString());
    }
    
    @Test
    public void testGetConfigValue_SuccessFromDatabase() {
        // 模拟缓存未命中，但数据库有数据
        when(cacheManager.getConfigFromCache("test.config.key")).thenReturn(null);
        when(systemConfigMapper.selectByConfigKey("test.config.key")).thenReturn(testConfig);
        
        // 执行测试
        String result = systemConfigService.getConfigValue("test.config.key");
        
        // 验证结果
        Assertions.assertEquals("test.config.value", result);
        verify(cacheManager, times(1)).getConfigFromCache("test.config.key");
        verify(systemConfigMapper, times(1)).selectByConfigKey("test.config.key");
        verify(cacheManager, times(1)).putConfigToCache("test.config.key", "test.config.value");
    }
    
    @Test
    public void testGetConfigValue_ConfigNotExists() {
        // 模拟缓存和数据库都未命中
        when(cacheManager.getConfigFromCache("non.existent.key")).thenReturn(null);
        when(systemConfigMapper.selectByConfigKey("non.existent.key")).thenReturn(null);
        
        // 执行测试
        String result = systemConfigService.getConfigValue("non.existent.key");
        
        // 验证结果
        Assertions.assertNull(result);
        verify(cacheManager, times(1)).getConfigFromCache("non.existent.key");
        verify(systemConfigMapper, times(1)).selectByConfigKey("non.existent.key");
    }
    
    @Test
    public void testGetIntConfigValue_Success() {
        // 模拟缓存命中
        when(cacheManager.getConfigFromCache("int.config.key")).thenReturn("123");
        
        // 执行测试
        Integer result = systemConfigService.getIntConfigValue("int.config.key", 0);
        
        // 验证结果
        Assertions.assertEquals(Integer.valueOf(123), result);
    }
    
    @Test
    public void testGetIntConfigValue_DefaultValue() {
        // 模拟缓存未命中，返回默认值
        when(cacheManager.getConfigFromCache("non.existent.key")).thenReturn(null);
        when(systemConfigMapper.selectByConfigKey("non.existent.key")).thenReturn(null);
        
        // 执行测试
        Integer result = systemConfigService.getIntConfigValue("non.existent.key", 99);
        
        // 验证结果
        Assertions.assertEquals(Integer.valueOf(99), result);
    }
    
    @Test
    public void testGetBooleanConfigValue_Success() {
        // 模拟缓存命中
        when(cacheManager.getConfigFromCache("bool.config.key")).thenReturn("true");
        
        // 执行测试
        Boolean result = systemConfigService.getBooleanConfigValue("bool.config.key", false);
        
        // 验证结果
        Assertions.assertEquals(Boolean.TRUE, result);
    }
    
    @Test
    public void testGetStringConfigValue_Success() {
        // 模拟缓存命中
        when(cacheManager.getConfigFromCache("string.config.key")).thenReturn("config.value");
        
        // 执行测试
        String result = systemConfigService.getStringConfigValue("string.config.key", "default.value");
        
        // 验证结果
        Assertions.assertEquals("config.value", result);
    }
    
    @Test
    public void testGetStringConfigValue_DefaultValue() {
        // 模拟缓存未命中，返回默认值
        when(cacheManager.getConfigFromCache("non.existent.key")).thenReturn(null);
        when(systemConfigMapper.selectByConfigKey("non.existent.key")).thenReturn(null);
        
        // 执行测试
        String result = systemConfigService.getStringConfigValue("non.existent.key", "default.value");
        
        // 验证结果
        Assertions.assertEquals("default.value", result);
    }
    
    @Test
    public void testUpdateConfigValue_Success() {
        // 模拟配置存在
        when(systemConfigMapper.selectByConfigKey("test.config.key")).thenReturn(testConfig);
        when(systemConfigMapper.updateConfigValue(1L, "new.value")).thenReturn(1);
        
        // 执行测试
        boolean result = systemConfigService.updateConfigValue("test.config.key", "new.value");
        
        // 验证结果
        Assertions.assertTrue(result);
        verify(systemConfigMapper, times(1)).updateConfigValue(1L, "new.value");
        verify(cacheManager, times(1)).updateConfigInCache("test.config.key", "new.value");
    }
    
    @Test
    public void testUpdateConfigValue_ConfigNotExists() {
        // 模拟配置不存在
        when(systemConfigMapper.selectByConfigKey("non.existent.key")).thenReturn(null);
        
        // 执行测试
        boolean result = systemConfigService.updateConfigValue("non.existent.key", "value");
        
        // 验证结果
        Assertions.assertFalse(result);
        verify(systemConfigMapper, never()).updateConfigValue(anyLong(), anyString());
        verify(cacheManager, never()).updateConfigInCache(anyString(), anyString());
    }
    
    @Test
    public void testUpdateBatchStatus_Success() {
        // 模拟批量更新成功
        List<Long> ids = Arrays.asList(1L, 2L, 3L);
        when(systemConfigMapper.updateBatchStatus(ids, 1)).thenReturn(3);
        
        // 执行测试
        boolean result = systemConfigService.updateBatchStatus(ids, 1);
        
        // 验证结果
        Assertions.assertTrue(result);
        verify(systemConfigMapper, times(1)).updateBatchStatus(ids, 1);
        verify(cacheManager, times(1)).clearConfigsFromCache(ids);
    }
    
    @Test
    public void testUpdateBatchStatus_NoRowsAffected() {
        // 模拟批量更新失败
        List<Long> ids = Arrays.asList(1L, 2L, 3L);
        when(systemConfigMapper.updateBatchStatus(ids, 1)).thenReturn(0);
        
        // 执行测试
        boolean result = systemConfigService.updateBatchStatus(ids, 1);
        
        // 验证结果
        Assertions.assertFalse(result);
    }
    
    @Test
    public void testIsConfigKeyExists_True() {
        // 模拟配置键存在
        when(systemConfigMapper.checkConfigKeyExists("existing.key", null)).thenReturn(1);
        
        // 执行测试
        boolean result = systemConfigService.isConfigKeyExists("existing.key");
        
        // 验证结果
        Assertions.assertTrue(result);
    }
    
    @Test
    public void testIsConfigKeyExists_False() {
        // 模拟配置键不存在
        when(systemConfigMapper.checkConfigKeyExists("non.existent.key", null)).thenReturn(0);
        
        // 执行测试
        boolean result = systemConfigService.isConfigKeyExists("non.existent.key");
        
        // 验证结果
        Assertions.assertFalse(result);
    }
    
    @Test
    public void testReloadAllConfigs_Success() {
        // 模拟从数据库获取所有启用的配置
        List<SystemConfig> allConfigs = new ArrayList<>();
        allConfigs.add(testConfig);
        when(systemConfigMapper.selectEnabledConfigs()).thenReturn(allConfigs);
        
        // 执行测试
        boolean result = systemConfigService.reloadAllConfigs();
        
        // 验证结果
        Assertions.assertTrue(result);
        verify(systemConfigMapper, times(1)).selectEnabledConfigs();
        verify(cacheManager, times(1)).reloadAllConfigs(allConfigs);
    }
    
    @Test
    public void testValidateConfigValue_StringType() {
        // 测试字符串类型验证
        boolean result = systemConfigService.validateConfigValue("any value", "string");
        Assertions.assertTrue(result);
    }
    
    @Test
    public void testValidateConfigValue_IntType_Success() {
        // 测试整数类型验证成功
        boolean result = systemConfigService.validateConfigValue("123", "int");
        Assertions.assertTrue(result);
    }
    
    @Test
    public void testValidateConfigValue_IntType_Failure() {
        // 测试整数类型验证失败
        boolean result = systemConfigService.validateConfigValue("not.a.number", "int");
        Assertions.assertFalse(result);
    }
    
    @Test
    public void testValidateConfigValue_BooleanType() {
        // 测试布尔类型验证
        Assertions.assertTrue(systemConfigService.validateConfigValue("true", "boolean"));
        Assertions.assertTrue(systemConfigService.validateConfigValue("false", "boolean"));
        Assertions.assertTrue(systemConfigService.validateConfigValue("1", "boolean"));
        Assertions.assertTrue(systemConfigService.validateConfigValue("0", "boolean"));
        Assertions.assertFalse(systemConfigService.validateConfigValue("invalid", "boolean"));
    }
    
    @Test
    public void testValidateConfigValue_JsonType_Success() {
        // 测试JSON类型验证成功
        boolean result = systemConfigService.validateConfigValue("{\"key\": \"value\"}", "json");
        Assertions.assertTrue(result);
    }
    
    @Test
    public void testValidateConfigValue_JsonType_Failure() {
        // 测试JSON类型验证失败
        boolean result = systemConfigService.validateConfigValue("invalid.json", "json");
        Assertions.assertFalse(result);
    }
    
    @Test
    public void testValidateConfigValue_EmailType_Success() {
        // 测试邮箱类型验证成功
        boolean result = systemConfigService.validateConfigValue("test@example.com", "email");
        Assertions.assertTrue(result);
    }
    
    @Test
    public void testValidateConfigValue_EmailType_Failure() {
        // 测试邮箱类型验证失败
        boolean result = systemConfigService.validateConfigValue("invalid.email", "email");
        Assertions.assertFalse(result);
    }
    
    @Test
    public void testValidateConfigValue_UrlType_Success() {
        // 测试URL类型验证成功
        boolean result = systemConfigService.validateConfigValue("https://www.example.com", "url");
        Assertions.assertTrue(result);
    }
    
    @Test
    public void testValidateConfigValue_UrlType_Failure() {
        // 测试URL类型验证失败
        boolean result = systemConfigService.validateConfigValue("invalid.url", "url");
        Assertions.assertFalse(result);
    }
    
    @Test
    public void testGetSystemConfig_Success() {
        // 模拟从数据库获取配置对象
        when(systemConfigMapper.selectByConfigKey("test.config.key")).thenReturn(testConfig);
        
        // 执行测试
        SystemConfig result = systemConfigService.getSystemConfig("test.config.key");
        
        // 验证结果
        Assertions.assertNotNull(result);
        Assertions.assertEquals(testConfig.getId(), result.getId());
        Assertions.assertEquals(testConfig.getConfigKey(), result.getConfigKey());
    }
    
    @Test
    public void testGetSystemConfig_NotFound() {
        // 模拟配置不存在
        when(systemConfigMapper.selectByConfigKey("non.existent.key")).thenReturn(null);
        
        // 执行测试
        SystemConfig result = systemConfigService.getSystemConfig("non.existent.key");
        
        // 验证结果
        Assertions.assertNull(result);
    }
    
    @Test
    public void testGetConfigsByType_Success() {
        // 模拟按类型查询
        List<SystemConfig> configs = new ArrayList<>();
        configs.add(testConfig);
        when(systemConfigMapper.selectByConfigType("string")).thenReturn(configs);
        
        // 执行测试
        List<SystemConfig> result = systemConfigService.getConfigsByType("string");
        
        // 验证结果
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
    }
    
    @Test
    public void testGetAllEnabledConfigs_Success() {
        // 模拟获取所有启用的配置
        List<SystemConfig> allConfigs = new ArrayList<>();
        allConfigs.add(testConfig);
        when(systemConfigMapper.selectEnabledConfigs()).thenReturn(allConfigs);
        
        // 执行测试
        List<SystemConfig> result = systemConfigService.getAllEnabledConfigs();
        
        // 验证结果
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
    }
}