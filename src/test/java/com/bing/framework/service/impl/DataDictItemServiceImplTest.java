package com.bing.framework.service.impl;

import com.bing.framework.entity.DataDictItem;
import com.bing.framework.mapper.DataDictItemMapper;
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
 * 数据字典项服务实现类的单元测试
 * 使用Mockito框架模拟依赖，Junit5进行测试
 * 测试数据字典项的CRUD操作、状态管理等核心业务逻辑
 * 
 * @author zhengbing
 * @date 2025-11-14
 */
@SpringBootTest
public class DataDictItemServiceImplTest {

    @Mock
    private DataDictItemMapper dataDictItemMapper;

    @InjectMocks
    private DataDictItemServiceImpl dataDictItemService;

    private DataDictItem testDataDictItem;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        testDataDictItem = new DataDictItem();
        testDataDictItem.setId(1L);
        testDataDictItem.setDictId(1L);
        testDataDictItem.setDictCode("GENDER");
        testDataDictItem.setItemValue("1");
        testDataDictItem.setItemText("男");
        testDataDictItem.setDescription("男性");
        testDataDictItem.setSort(1);
        testDataDictItem.setStatus(1);
    }

    @Test
    public void testGetDataDictItemById_Success() {
        // 模拟Mapper行为
        when(dataDictItemMapper.selectById(1L)).thenReturn(testDataDictItem);

        // 执行测试
        DataDictItem result = dataDictItemService.getDataDictItemById(1L);

        // 验证结果
        Assertions.assertNotNull(result);
        Assertions.assertEquals("1", result.getItemValue());
        verify(dataDictItemMapper, times(1)).selectById(1L);
    }

    @Test
    public void testGetDataDictItemsByDictId() {
        // 模拟Mapper行为
        List<DataDictItem> itemList = Collections.singletonList(testDataDictItem);
        when(dataDictItemMapper.selectByDictId(1L)).thenReturn(itemList);

        // 执行测试
        List<DataDictItem> result = dataDictItemService.getDataDictItemsByDictId(1L);

        // 验证结果
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        verify(dataDictItemMapper, times(1)).selectByDictId(1L);
    }

    @Test
    public void testGetDataDictItemsByDictCode() {
        // 模拟Mapper行为
        List<DataDictItem> itemList = Collections.singletonList(testDataDictItem);
        when(dataDictItemMapper.selectByDictCode("GENDER")).thenReturn(itemList);

        // 执行测试
        List<DataDictItem> result = dataDictItemService.getDataDictItemsByDictCode("GENDER");

        // 验证结果
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        verify(dataDictItemMapper, times(1)).selectByDictCode("GENDER");
    }

    @Test
    public void testGetEnabledDataDictItemsByDictCode() {
        // 模拟Mapper行为
        List<DataDictItem> itemList = Collections.singletonList(testDataDictItem);
        when(dataDictItemMapper.selectEnabledItemsByDictCode("GENDER")).thenReturn(itemList);

        // 执行测试
        List<DataDictItem> result = dataDictItemService.getEnabledDataDictItemsByDictCode("GENDER");

        // 验证结果
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        verify(dataDictItemMapper, times(1)).selectEnabledItemsByDictCode("GENDER");
    }

    @Test
    public void testGetDataDictItemByDictIdAndItemValue() {
        // 模拟Mapper行为
        when(dataDictItemMapper.selectByDictIdAndItemValue(1L, "1")).thenReturn(testDataDictItem);

        // 执行测试
        DataDictItem result = dataDictItemService.getDataDictItemByDictIdAndItemValue(1L, "1");

        // 验证结果
        Assertions.assertNotNull(result);
        Assertions.assertEquals("男", result.getItemText());
        verify(dataDictItemMapper, times(1)).selectByDictIdAndItemValue(1L, "1");
    }

    @Test
    public void testGetDataDictItemByDictCodeAndItemValue() {
        // 模拟Mapper行为
        when(dataDictItemMapper.selectByDictCodeAndItemValue("GENDER", "1")).thenReturn(testDataDictItem);

        // 执行测试
        DataDictItem result = dataDictItemService.getDataDictItemByDictCodeAndItemValue("GENDER", "1");

        // 验证结果
        Assertions.assertNotNull(result);
        Assertions.assertEquals("男", result.getItemText());
        verify(dataDictItemMapper, times(1)).selectByDictCodeAndItemValue("GENDER", "1");
    }

    @Test
    public void testSaveDataDictItem_Success() {
        // 模拟字典项值不存在
        when(dataDictItemMapper.checkItemValueExists(1L, "1", null)).thenReturn(0);
        // 模拟插入成功
        when(dataDictItemMapper.insert(testDataDictItem)).thenReturn(1);

        // 执行测试
        boolean result = dataDictItemService.saveDataDictItem(testDataDictItem);

        // 验证结果
        Assertions.assertTrue(result);
        verify(dataDictItemMapper, times(1)).insert(testDataDictItem);
    }

    @Test
    public void testSaveDataDictItem_Failure_DuplicateValue() {
        // 模拟字典项值已存在
        when(dataDictItemMapper.checkItemValueExists(1L, "1", null)).thenReturn(1);

        // 执行测试并验证异常
        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> {
            dataDictItemService.saveDataDictItem(testDataDictItem);
        });

        // 验证异常信息
        Assertions.assertEquals("字典项值已存在", exception.getMessage());
        // 不应该调用insert方法
        verify(dataDictItemMapper, never()).insert(testDataDictItem);
    }

    @Test
    public void testUpdateDataDictItem_Success() {
        // 模拟字典项值不存在（排除当前ID）
        when(dataDictItemMapper.checkItemValueExists(1L, "1", 1L)).thenReturn(0);
        // 模拟更新成功
        when(dataDictItemMapper.updateById(testDataDictItem)).thenReturn(1);

        // 执行测试
        boolean result = dataDictItemService.updateDataDictItem(testDataDictItem);

        // 验证结果
        Assertions.assertTrue(result);
        verify(dataDictItemMapper, times(1)).updateById(testDataDictItem);
    }

    @Test
    public void testDeleteDataDictItem_Success() {
        // 模拟字典项存在
        when(dataDictItemMapper.selectById(1L)).thenReturn(testDataDictItem);
        // 模拟删除成功
        when(dataDictItemMapper.deleteById(1L)).thenReturn(1);

        // 执行测试
        boolean result = dataDictItemService.deleteDataDictItem(1L);

        // 验证结果
        Assertions.assertTrue(result);
        verify(dataDictItemMapper, times(1)).deleteById(1L);
    }

    @Test
    public void testDeleteDataDictItem_Failure_NotFound() {
        // 模拟字典项不存在
        when(dataDictItemMapper.selectById(1L)).thenReturn(null);

        // 执行测试并验证异常
        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> {
            dataDictItemService.deleteDataDictItem(1L);
        });

        // 验证异常信息
        Assertions.assertEquals("字典项不存在", exception.getMessage());
        // 不应该调用delete方法
        verify(dataDictItemMapper, never()).deleteById(1L);
    }

    @Test
    public void testDeleteBatchDataDictItems() {
        // 准备测试数据
        List<Long> ids = Arrays.asList(1L, 2L, 3L);
        // 模拟批量删除成功
        when(dataDictItemMapper.deleteBatch(ids)).thenReturn(3);

        // 执行测试
        boolean result = dataDictItemService.deleteBatchDataDictItems(ids);

        // 验证结果
        Assertions.assertTrue(result);
        verify(dataDictItemMapper, times(1)).deleteBatch(ids);
    }

    @Test
    public void testDeleteDataDictItemsByDictId() {
        // 模拟删除成功
        when(dataDictItemMapper.deleteByDictId(1L)).thenReturn(2);

        // 执行测试
        boolean result = dataDictItemService.deleteDataDictItemsByDictId(1L);

        // 验证结果
        Assertions.assertTrue(result);
        verify(dataDictItemMapper, times(1)).deleteByDictId(1L);
    }

    @Test
    public void testChangeStatus() {
        // 准备测试数据
        Long id = 1L;
        Integer status = 0; // 禁用
        // 模拟更新成功
        when(dataDictItemMapper.updateById(any(DataDictItem.class))).thenReturn(1);

        // 执行测试
        boolean result = dataDictItemService.changeStatus(id, status);

        // 验证结果
        Assertions.assertTrue(result);
        verify(dataDictItemMapper, times(1)).updateById(any(DataDictItem.class));
    }
}