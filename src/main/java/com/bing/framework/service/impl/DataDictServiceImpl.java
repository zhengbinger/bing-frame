package com.bing.framework.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bing.framework.entity.DataDict;
import com.bing.framework.mapper.DataDictMapper;
import com.bing.framework.service.DataDictItemService;
import com.bing.framework.service.DataDictService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * 数据字典服务实现类
 * 继承ServiceImpl类并实现DataDictService接口
 * 实现数据字典管理的核心业务逻辑，包括CRUD操作、状态管理等
 * 
 * @author zhengbing
 * @date 2025-11-14
 */
@Service
@Slf4j
public class DataDictServiceImpl extends ServiceImpl<DataDictMapper, DataDict> implements DataDictService {

    @Autowired
    private DataDictMapper dataDictMapper;

    @Autowired
    private DataDictItemService dataDictItemService;

    @Override
    @Cacheable(value = "dataDict", key = "#id")
    public DataDict getDataDictById(Long id) {
        log.debug("查询字典ID: {}", id);
        return baseMapper.selectById(id);
    }

    @Override
    @Cacheable(value = "dataDict", key = "#dictCode")
    public DataDict getDataDictByDictCode(String dictCode) {
        log.debug("查询字典编码: {}", dictCode);
        return dataDictMapper.selectByDictCode(dictCode);
    }

    @Override
    public List<DataDict> getAllDataDicts() {
        log.debug("查询所有字典");
        return baseMapper.selectList(null);
    }

    @Override
    public List<DataDict> getEnabledDataDicts() {
        log.debug("查询启用的字典列表");
        return dataDictMapper.selectEnabledDicts();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "dataDict", key = "#dataDict.dictCode")
    public boolean saveDataDict(DataDict dataDict) {
        // 检查字典编码是否已存在
        if (isDictCodeExists(dataDict.getDictCode(), null)) {
            log.error("字典编码已存在: {}", dataDict.getDictCode());
            throw new RuntimeException("字典编码已存在");
        }
        log.info("新增字典: {}", dataDict.getDictName());
        return baseMapper.insert(dataDict) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "dataDict", key = "#dataDict.dictCode")
    public boolean updateDataDict(DataDict dataDict) {
        // 检查字典编码是否已存在（排除当前ID）
        if (isDictCodeExists(dataDict.getDictCode(), dataDict.getId())) {
            log.error("字典编码已存在: {}", dataDict.getDictCode());
            throw new RuntimeException("字典编码已存在");
        }
        log.info("更新字典: {}", dataDict.getDictName());
        return baseMapper.updateById(dataDict) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "dataDict", allEntries = true)
    public boolean deleteDataDict(Long id) {
        DataDict dataDict = getDataDictById(id);
        if (dataDict == null) {
            log.error("字典不存在: {}", id);
            throw new RuntimeException("字典不存在");
        }
        // 先删除关联的字典项
        dataDictItemService.deleteDataDictItemsByDictId(id);
        log.info("删除字典: {}", dataDict.getDictName());
        return baseMapper.deleteById(id) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "dataDict", allEntries = true)
    public boolean deleteBatchDataDicts(List<Long> ids) {
        // 删除关联的字典项
        for (Long id : ids) {
            dataDictItemService.deleteDataDictItemsByDictId(id);
        }
        log.info("批量删除字典，数量: {}", ids.size());
        return dataDictMapper.deleteBatch(ids) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "dataDict", allEntries = true)
    public boolean changeStatus(Long id, Integer status) {
        DataDict dataDict = new DataDict();
        dataDict.setId(id);
        dataDict.setStatus(status);
        log.info("修改字典状态，ID: {}, 状态: {}", id, status);
        return baseMapper.updateById(dataDict) > 0;
    }

    @Override
    public boolean isDictCodeExists(String dictCode, Long excludeId) {
        int count = dataDictMapper.checkDictCodeExists(dictCode, excludeId);
        return count > 0;
    }
}