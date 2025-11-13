package com.bing.framework.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bing.framework.entity.DataDict;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

/**
 * 数据字典Mapper接口
 * 继承MyBatis-Plus的BaseMapper接口，通过@Mapper注解注册到Spring容器
 * 提供数据字典表的基本CRUD操作和自定义查询方法
 * 
 * @author zhengbing
 * @date 2025-11-14
 */
@Mapper
public interface DataDictMapper extends BaseMapper<DataDict> {

    /**
     * 根据字典编码查询字典
     * @param dictCode 字典编码
     * @return 字典对象
     */
    DataDict selectByDictCode(String dictCode);

    /**
     * 查询启用的字典列表
     * @return 字典列表
     */
    List<DataDict> selectEnabledDicts();

    /**
     * 批量删除字典
     * @param ids 字典ID列表
     * @return 删除成功的数量
     */
    int deleteBatch(List<Long> ids);

    /**
     * 检查字典编码是否已存在
     * @param dictCode 字典编码
     * @param excludeId 排除的ID（用于更新时）
     * @return 存在的数量
     */
    int checkDictCodeExists(String dictCode, Long excludeId);
}