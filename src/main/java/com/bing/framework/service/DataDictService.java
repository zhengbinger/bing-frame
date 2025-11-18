package com.bing.framework.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bing.framework.entity.DataDict;
import java.util.List;

/**
 * 数据字典服务接口
 * 继承MyBatis-Plus的IService接口，扩展自定义业务方法，实现数据字典管理的核心业务逻辑
 * 定义字典的查询、新增、更新、删除等基础操作的方法规范
 * 
 * @author zhengbing
 * @date 2025-11-14
 */
public interface DataDictService extends IService<DataDict> {

    /**
     * 根据ID查询字典
     * @param id 字典ID
     * @return 字典对象
     */
    DataDict getDataDictById(Long id);

    /**
     * 根据字典编码查询字典
     * @param code 字典编码
     * @return 字典对象
     */
    DataDict getDataDictByCode(String code);

    /**
     * 查询所有字典
     * @return 字典列表
     */
    List<DataDict> getAllDataDicts();

    /**
     * 查询启用的字典列表
     * @return 启用的字典列表
     */
    List<DataDict> getEnabledDataDicts();

    /**
     * 新增字典
     * @param dataDict 字典对象
     * @return 是否成功
     */
    boolean saveDataDict(DataDict dataDict);

    /**
     * 更新字典
     * @param dataDict 字典对象
     * @return 是否成功
     */
    boolean updateDataDict(DataDict dataDict);

    /**
     * 删除字典
     * @param id 字典ID
     * @return 是否成功
     */
    boolean deleteDataDict(Long id);

    /**
     * 批量删除字典
     * @param ids 字典ID列表
     * @return 是否成功
     */
    boolean deleteBatchDataDicts(List<Long> ids);

    /**
     * 启用/禁用字典
     * @param id 字典ID
     * @param status 状态（0-禁用，1-启用）
     * @return 是否成功
     */
    boolean changeStatus(Long id, Integer status);

    /**
     * 检查字典编码是否已存在
     * @param dictCode 字典编码
     * @param excludeId 排除的ID（用于更新时）
     * @return 是否已存在
     */
    boolean isDictCodeExists(String dictCode, Long excludeId);
}