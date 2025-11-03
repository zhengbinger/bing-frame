package com.bing.framework.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bing.framework.entity.WhiteList;
import com.bing.framework.mapper.WhiteListMapper;
import com.bing.framework.service.WhiteListService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class WhiteListServiceImpl extends ServiceImpl<WhiteListMapper, WhiteList> implements WhiteListService {

    @Autowired
    private WhiteListMapper whiteListMapper;

    /**
     * 日志记录器。
     */
    private static final Logger log = LoggerFactory.getLogger(WhiteListServiceImpl.class);

    /**
     * Ant风格路径匹配器。
     */
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * 缓存键名。
     */
    private static final String WHITE_LIST_CACHE_KEY = "white_list:patterns";

    @Override
    @Cacheable(value = "whiteListCache", key = WHITE_LIST_CACHE_KEY)
    public Set<String> getEnabledPatterns() {
        List<WhiteList> whiteLists = whiteListMapper.selectEnabledWhiteLists();
        Set<String> patterns = new HashSet<>();
        for (WhiteList whiteList : whiteLists) {
            if (whiteList.getEnabled()) {
                patterns.add(whiteList.getPattern());
            }
        }
        return patterns;
    }

    @Override
    @CacheEvict(value = "whiteListCache", key = WHITE_LIST_CACHE_KEY)
    public void refreshWhiteListCache() {
        // 缓存驱逐后，下次调用getEnabledPatterns会自动从数据库重新加载
        // 这里可以添加日志记录
        log.info("白名单缓存已刷新");
    }

    @Override
    public boolean isInWhiteList(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        
        Set<String> patterns = getEnabledPatterns();
        for (String pattern : patterns) {
            if (pathMatcher.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }
}