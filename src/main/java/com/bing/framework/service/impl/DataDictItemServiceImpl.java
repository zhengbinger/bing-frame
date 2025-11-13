package com.bing.framework.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bing.framework.entity.DataDictItem;
import com.bing.framework.mapper.DataDictItemMapper;
import com.bing.framework.service.DataDictItemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * 数据字典项服务实现类
 * 继承ServiceImpl类并实现DataDictItemService接口
 * 实现数据字典项管理的核心业务逻辑，包括CRUD操作、状态管理等
 * 
 * @author zhengbing
 * @date 2025-11-14
 */
@Service
@Slf4j
public class DataDictItemServiceImpl extends ServiceImpl<DataDictItemMapper, DataDictItem> implements DataDictItemService {

    @Autowired
    private DataDictItemMapper dataDictItemMapper;

    @Override
    @Cacheable(value = "dataDictItem", key = "#id")
    public DataDictItem getDataDictItemById(Long id) {
        log.debug("查询字典项ID: {}", id);
        return baseMapper.selectById(id);
    }

    @Override
    @Cacheable(value = "dataDictItem", key = "'dictId:' + #dictId")
    public List<DataDictItem> getDataDictItemsByDictId(Long dictId) {
        log.debug("查询字典ID: {} 的所有字典项", dictId);
        return dataDictItemMapper.selectByDictId(dictId);
    }

    @Override
    @Cacheable(value = "dataDictItem", key = "'dictCode:' + #dictCode")
    public List<DataDictItem> getDataDictItemsByDictCode(String dictCode) {
        log.debug("查询字典编码: {} 的所有字典项", dictCode);
        return dataDictItemMapper.selectByDictCode(dictCode);
    }

    @Override
    @Cacheable(value = "dataDictItem", key = "'enabled:' + #dictCode")
    public List<DataDictItem> getEnabledDataDictItemsByDictCode(String dictCode) {
        log.debug("查询字典编码: {} 的启用字典项", dictCode);
        return dataDictItemMapper.selectEnabledItemsByDictCode(dictCode);
    }

    @Override
    public DataDictItem getDataDictItemByDictIdAndItemValue(Long dictId, String itemValue) {
        log.debug("查询字典ID: {}, 项值: {} 的字典项", dictId, itemValue);
        return dataDictItemMapper.selectByDictIdAndItemValue(dictId, itemValue);
    }

    @Override
    public DataDictItem getDataDictItemByDictCodeAndItemValue(String dictCode, String itemValue) {
        log.debug("查询字典编码: {}, 项值: {} 的字典项", dictCode, itemValue);
        return dataDictItemMapper.selectByDictCodeAndItemValue(dictCode, itemValue);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "dataDictItem", allEntries = true)
    public boolean saveDataDictItem(DataDictItem dataDictItem) {
        // 检查字典项值是否已存在
        if (isItemValueExists(dataDictItem.getDictId(), dataDictItem.getItemValue(), null)) {
            log.error("字典项值已存在: {}", dataDictItem.getItemValue());
            throw new RuntimeException("字典项值已存在");
        }
        log.info("新增字典项: {}", dataDictItem.getItemText());
        return baseMapper.insert(dataDictItem) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "dataDictItem", allEntries = true)
    public boolean updateDataDictItem(DataDictItem dataDictItem) {
        // 检查字典项值是否已存在（排除当前ID）
        if (isItemValueExists(dataDictItem.getDictId(), dataDictItem.getItemValue(), dataDictItem.getId())) {
            log.error("字典项值已存在: {}", dataDictItem.getItemValue());
            throw new RuntimeException("字典项值已存在");
        }
        log.info("更新字典项: {}", dataDictItem.getItemText());
        return baseMapper.updateById(dataDictItem) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "dataDictItem", allEntries = true)
    public boolean deleteDataDictItem(Long id) {
        DataDictItem item = getDataDictItemById(id);
        if (item == null) {
            log.error("字典项不存在: {}", id);
            throw new RuntimeException("字典项不存在");
        }
        log.info("删除字典项: {}", item.getItemText());
        return baseMapper.deleteById(id) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "dataDictItem", allEntries = true)
    public boolean deleteBatchDataDictItems(List<Long> ids) {
        log.info("批量删除字典项，数量: {}", ids.size());
        return dataDictItemMapper.deleteBatch(ids) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "dataDictItem", allEntries = true)
    public boolean deleteDataDictItemsByDictId(Long dictId) {
        log.info("删除字典ID: {} 的所有字典项", dictId);
        return dataDictItemMapper.deleteByDictId(dictId) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "dataDictItem", allEntries = true)
    public boolean changeStatus(Long id, Integer status) {
        DataDictItem item = new DataDictItem();
        item.setId(id);
        item.setStatus(status);
        log.info("修改字典项状态，ID: {}, 状态: {}", id, status);
        return baseMapper.updateById(item) > 0;
    }

    @Override
    public boolean isItemValueExists(Long dictId, String itemValue, Long excludeId) {
        int count = dataDictItemMapper.checkItemValueExists(dictId, itemValue, excludeId);
        return count > 0;
    }
}