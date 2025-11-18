package com.bing.framework.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bing.framework.entity.DataDictItem;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

/**
 * 数据字典项Mapper接口
 * 继承MyBatis-Plus的BaseMapper接口，通过@Mapper注解注册到Spring容器
 * 提供数据字典项表的基本CRUD操作和自定义查询方法
 * 
 * @author zhengbing
 * @date 2025-11-14
 */
@Mapper
public interface DataDictItemMapper extends BaseMapper<DataDictItem> {

    /**
     * 根据字典ID查询字典项列表
     * @param dictId 字典ID
     * @return 字典项列表
     */
    List<DataDictItem> selectByDictId(Long dictId);

    /**
     * 根据字典编码查询字典项列表
     * @param code 字典编码
     * @return 字典项列表
     */
    List<DataDictItem> selectByCode(String code);

    /**
     * 根据字典编码查询启用的字典项列表
     * @param code 字典编码
     * @return 启用的字典项列表
     */
    List<DataDictItem> selectEnabledItemsByCode(String code);

    /**
     * 根据字典编码和项值查询字典项
     * @param code 字典编码
     * @param value 项值
     * @return 字典项对象
     */
    DataDictItem selectByCodeAndValue(String code, String value);

    /**
     * 批量删除字典项
     * @param ids 字典项ID列表
     * @return 删除成功的数量
     */
    int deleteBatch(List<Long> ids);

    /**
     * 删除指定字典下的所有字典项
     * @param dictId 字典ID
     * @return 删除成功的数量
     */
    int deleteByDictId(Long dictId);

    /**
     * 检查字典项值是否已存在
     * @param dictId 字典ID
     * @param itemValue 项值
     * @param excludeId 排除的ID（用于更新时）
     * @return 存在的数量
     */
    int checkItemValueExists(Long dictId, String itemValue, Long excludeId);
}