package com.bing.framework.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bing.framework.entity.DataDictItem;
import java.util.List;

/**
 * 数据字典项服务接口
 * 继承MyBatis-Plus的IService接口，扩展自定义业务方法，实现数据字典项管理的核心业务逻辑
 * 定义字典项的查询、新增、更新、删除等基础操作的方法规范
 * 
 * @author zhengbing
 * @date 2025-11-14
 */
public interface DataDictItemService extends IService<DataDictItem> {

    /**
     * 根据ID查询字典项
     * @param id 字典项ID
     * @return 字典项对象
     */
    DataDictItem getDataDictItemById(Long id);

    /**
     * 根据字典ID查询字典项列表
     * @param dictId 字典ID
     * @return 字典项列表
     */
    List<DataDictItem> getDataDictItemsByDictId(Long dictId);

    /**
     * 根据字典编码查询字典项列表
     * @param dictCode 字典编码
     * @return 字典项列表
     */
    List<DataDictItem> getDataDictItemsByDictCode(String dictCode);

    /**
     * 根据字典编码查询启用的字典项列表
     * @param dictCode 字典编码
     * @return 启用的字典项列表
     */
    List<DataDictItem> getEnabledDataDictItemsByDictCode(String dictCode);

    /**
     * 根据字典ID和项值查询字典项
     * @param dictId 字典ID
     * @param itemValue 项值
     * @return 字典项对象
     */
    DataDictItem getDataDictItemByDictIdAndItemValue(Long dictId, String itemValue);

    /**
     * 根据字典编码和项值查询字典项
     * @param dictCode 字典编码
     * @param itemValue 项值
     * @return 字典项对象
     */
    DataDictItem getDataDictItemByDictCodeAndItemValue(String dictCode, String itemValue);

    /**
     * 新增字典项
     * @param dataDictItem 字典项对象
     * @return 是否成功
     */
    boolean saveDataDictItem(DataDictItem dataDictItem);

    /**
     * 更新字典项
     * @param dataDictItem 字典项对象
     * @return 是否成功
     */
    boolean updateDataDictItem(DataDictItem dataDictItem);

    /**
     * 删除字典项
     * @param id 字典项ID
     * @return 是否成功
     */
    boolean deleteDataDictItem(Long id);

    /**
     * 批量删除字典项
     * @param ids 字典项ID列表
     * @return 是否成功
     */
    boolean deleteBatchDataDictItems(List<Long> ids);

    /**
     * 删除指定字典下的所有字典项
     * @param dictId 字典ID
     * @return 是否成功
     */
    boolean deleteDataDictItemsByDictId(Long dictId);

    /**
     * 启用/禁用字典项
     * @param id 字典项ID
     * @param status 状态（0-禁用，1-启用）
     * @return 是否成功
     */
    boolean changeStatus(Long id, Integer status);

    /**
     * 检查字典项值是否已存在
     * @param dictId 字典ID
     * @param itemValue 项值
     * @param excludeId 排除的ID（用于更新时）
     * @return 是否已存在
     */
    boolean isItemValueExists(Long dictId, String itemValue, Long excludeId);
}