package com.bing.framework.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bing.framework.entity.WhiteList;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 白名单Mapper接口。
 * 用于对白名单数据进行数据库操作。
 *
 * @author zhengbing
 * @date 2024-11-03
 */
@Mapper
public interface WhiteListMapper extends BaseMapper<WhiteList> {

    /**
     * 查询所有启用的白名单记录。
     *
     * @return 启用的白名单列表
     */
    List<WhiteList> selectEnabledWhiteLists();
}