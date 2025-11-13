package com.bing.framework.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bing.framework.entity.WhiteList;
import com.bing.framework.mapper.WhiteListMapper;
import com.bing.framework.service.WhiteListService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 白名单服务实现类。
 * 实现白名单管理的业务逻辑。
 *
 * @author zhengbing
 * @date 2024-11-03
 */
@Service
@Slf4j
public class WhiteListServiceImpl extends ServiceImpl<WhiteListMapper, WhiteList> implements WhiteListService {

    @Autowired
    private WhiteListMapper whiteListMapper;

    /**
     * 日志记录器。
     */


    /**
     * Ant风格路径匹配器。
     */
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * 缓存键名。
     */
    private static final String WHITE_LIST_CACHE_KEY = "white_list:patterns";

    @Override
    @Cacheable(value = "whiteListCache", key = WHITE_LIST_CACHE_KEY, unless = "#result == null or #result.isEmpty()")
    public Set<String> getEnabledPatterns() {
        log.info("白名单缓存未命中，从数据库查询白名单模式");
        List<WhiteList> whiteLists = whiteListMapper.selectEnabledWhiteLists();
        log.info("从数据库获取到 {} 条白名单记录", whiteLists.size());
        
        Set<String> patterns = new HashSet<>();
        for (WhiteList whiteList : whiteLists) {
            if (whiteList.getEnabled()) {
                patterns.add(whiteList.getPattern());
                log.info("添加白名单模式: '{}'", whiteList.getPattern());
            }
        }
        
        log.info("转换后获取到 {} 个启用的白名单模式，即将存入Redis缓存", patterns.size());
        return patterns;
    }

    @Override
    @CacheEvict(value = "whiteListCache", key = WHITE_LIST_CACHE_KEY, beforeInvocation = true)
    public void refreshWhiteListCache() {
        log.info("开始刷新白名单缓存");
        // 缓存驱逐后，下次调用getEnabledPatterns会自动从数据库重新加载
        log.info("白名单缓存已驱逐，下次访问将重新加载");
    }

    @Override
    public boolean isInWhiteList(String path) {
        log.debug("[DEBUG] 检查路径 '{}' 是否在白名单中", path);
        
        if (path == null || path.isEmpty()) {
            log.debug("[DEBUG] 路径为空，不在白名单中");
            return false;
        }
        
        Set<String> patterns = getEnabledPatterns();
        log.debug("[DEBUG] 当前启用的白名单模式数量: {}", patterns.size());
        
        for (String pattern : patterns) {
            boolean matches = pathMatcher.match(pattern, path);
            log.debug("[DEBUG] 路径 '{}' 匹配模式 '{}': {}", path, pattern, matches);
            if (matches) {
                log.debug("[DEBUG] 路径 '{}' 在白名单中，匹配成功", path);
                return true;
            }
        }
        
        log.debug("[DEBUG] 路径 '{}' 不在白名单中，所有模式都不匹配", path);
        return false;
    }
}