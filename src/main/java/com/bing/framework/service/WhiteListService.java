package com.bing.framework.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bing.framework.entity.WhiteList;

import java.util.Set;

/**
 * 白名单服务接口。
 * 提供白名单管理相关的业务逻辑操作。
 *
 * @author zhengbing
 * @date 2024-11-03
 */
public interface WhiteListService extends IService<WhiteList> {

    /**
     * 获取所有启用的白名单模式。
     *
     * @return 启用的白名单模式集合
     */
    Set<String> getEnabledPatterns();

    /**
     * 刷新白名单缓存。
     * 从数据库重新加载白名单数据到缓存。
     */
    void refreshWhiteListCache();

    /**
     * 检查请求路径是否在白名单中。
     *
     * @param path 请求路径
     * @return 是否在白名单中
     */
    boolean isInWhiteList(String path);
}