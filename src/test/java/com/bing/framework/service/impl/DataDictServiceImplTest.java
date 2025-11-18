package com.bing.framework.service.impl;

import com.bing.framework.entity.DataDict;
import com.bing.framework.mapper.DataDictMapper;
import com.bing.framework.service.DataDictItemService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * 数据字典服务实现类的单元测试
 * 使用Mockito框架模拟依赖，Junit5进行测试
 * 测试数据字典的CRUD操作、状态管理等核心业务逻辑
 * 
 * @author zhengbing
 * @date 2025-11-14
 */
@SpringBootTest
public class DataDictServiceImplTest {

    @Mock
    private DataDictMapper dataDictMapper;

    @Mock
    private DataDictItemService dataDictItemService;

    @InjectMocks
    private DataDictServiceImpl dataDictService;

    private DataDict testDataDict;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        testDataDict = new DataDict();
        testDataDict.setId(1L);
        testDataDict.setCode("GENDER");
        testDataDict.setName("性别");
        testDataDict.setDescription("用户性别字典");
        testDataDict.setStatus(1);
    }

    @Test
    public void testGetDataDictById_Success() {
        // 模拟Mapper行为
        when(dataDictMapper.selectById(1L)).thenReturn(testDataDict);

        // 执行测试
        DataDict result = dataDictService.getDataDictById(1L);

        // 验证结果
        Assertions.assertNotNull(result);
        Assertions.assertEquals("GENDER", result.getCode());
        verify(dataDictMapper, times(1)).selectById(1L);
    }

    @Test
    public void testGetDataDictByDictCode_Success() {
        // 模拟Mapper行为
        when(dataDictMapper.selectByCode("GENDER")).thenReturn(testDataDict);

        // 执行测试
        DataDict result = dataDictService.getDataDictByCode("GENDER");

        // 验证结果
        Assertions.assertNotNull(result);
        Assertions.assertEquals("性别", result.getName());
        verify(dataDictMapper, times(1)).selectByCode("GENDER");
    }

    @Test
    public void testGetAllDataDicts() {
        // 模拟Mapper行为
        List<DataDict> dictList = Collections.singletonList(testDataDict);
        when(dataDictMapper.selectList(null)).thenReturn(dictList);

        // 执行测试
        List<DataDict> result = dataDictService.getAllDataDicts();

        // 验证结果
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        verify(dataDictMapper, times(1)).selectList(null);
    }

    @Test
    public void testGetEnabledDataDicts() {
        // 模拟Mapper行为
        List<DataDict> dictList = Collections.singletonList(testDataDict);
        when(dataDictMapper.selectEnabledDicts()).thenReturn(dictList);

        // 执行测试
        List<DataDict> result = dataDictService.getEnabledDataDicts();

        // 验证结果
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        verify(dataDictMapper, times(1)).selectEnabledDicts();
    }

    @Test
    public void testSaveDataDict_Success() {
        // 模拟字典编码不存在
        when(dataDictMapper.checkDictCodeExists("GENDER", null)).thenReturn(0);
        // 模拟插入成功
        when(dataDictMapper.insert(testDataDict)).thenReturn(1);

        // 执行测试
        boolean result = dataDictService.saveDataDict(testDataDict);

        // 验证结果
        Assertions.assertTrue(result);
        verify(dataDictMapper, times(1)).insert(testDataDict);
    }

    @Test
    public void testSaveDataDict_Failure_DuplicateCode() {
        // 模拟字典编码已存在
        when(dataDictMapper.checkDictCodeExists("GENDER", null)).thenReturn(1);

        // 执行测试并验证异常
        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> {
            dataDictService.saveDataDict(testDataDict);
        });

        // 验证异常信息
        Assertions.assertEquals("字典编码已存在", exception.getMessage());
        // 不应该调用insert方法
        verify(dataDictMapper, never()).insert(testDataDict);
    }

    @Test
    public void testUpdateDataDict_Success() {
        // 模拟字典编码不存在（排除当前ID）
        when(dataDictMapper.checkDictCodeExists("GENDER", 1L)).thenReturn(0);
        // 模拟更新成功
        when(dataDictMapper.updateById(testDataDict)).thenReturn(1);

        // 执行测试
        boolean result = dataDictService.updateDataDict(testDataDict);

        // 验证结果
        Assertions.assertTrue(result);
        verify(dataDictMapper, times(1)).updateById(testDataDict);
    }

    @Test
    public void testDeleteDataDict_Success() {
        // 模拟字典存在
        when(dataDictMapper.selectById(1L)).thenReturn(testDataDict);
        // 模拟删除成功
        when(dataDictMapper.deleteById(1L)).thenReturn(1);

        // 执行测试
        boolean result = dataDictService.deleteDataDict(1L);

        // 验证结果
        Assertions.assertTrue(result);
        // 验证删除了关联的字典项
        verify(dataDictItemService, times(1)).deleteDataDictItemsByDictId(1L);
        verify(dataDictMapper, times(1)).deleteById(1L);
    }

    @Test
    public void testDeleteDataDict_Failure_NotFound() {
        // 模拟字典不存在
        when(dataDictMapper.selectById(1L)).thenReturn(null);

        // 执行测试并验证异常
        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> {
            dataDictService.deleteDataDict(1L);
        });

        // 验证异常信息
        Assertions.assertEquals("字典不存在", exception.getMessage());
        // 不应该调用delete方法
        verify(dataDictMapper, never()).deleteById(1L);
    }

    @Test
    public void testDeleteBatchDataDicts() {
        // 准备测试数据
        List<Long> ids = Arrays.asList(1L, 2L, 3L);
        // 模拟批量删除成功
        when(dataDictMapper.deleteBatch(ids)).thenReturn(3);

        // 执行测试
        boolean result = dataDictService.deleteBatchDataDicts(ids);

        // 验证结果
        Assertions.assertTrue(result);
        // 验证删除了关联的字典项
        verify(dataDictItemService, times(3)).deleteDataDictItemsByDictId(anyLong());
        verify(dataDictMapper, times(1)).deleteBatch(ids);
    }

    @Test
    public void testChangeStatus() {
        // 准备测试数据
        Long id = 1L;
        Integer status = 0; // 禁用
        // 模拟更新成功
        when(dataDictMapper.updateById(any(DataDict.class))).thenReturn(1);

        // 执行测试
        boolean result = dataDictService.changeStatus(id, status);

        // 验证结果
        Assertions.assertTrue(result);
        verify(dataDictMapper, times(1)).updateById(any(DataDict.class));
    }
}