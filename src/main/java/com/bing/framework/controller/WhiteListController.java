package com.bing.framework.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bing.framework.common.ErrorCode;
import com.bing.framework.common.Result;
import com.bing.framework.entity.WhiteList;
import com.bing.framework.service.WhiteListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 白名单控制器。
 * 提供白名单的增删改查等管理功能。
 *
 * @author zhengbing
 * @date 2024-11-03
 */
@RestController
@RequestMapping("/api/white-list")
public class WhiteListController {

    @Autowired
    private WhiteListService whiteListService;

    /**
     * 分页查询白名单列表。
     *
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    @GetMapping("/page")
    public Result<IPage<WhiteList>> page(@RequestParam(defaultValue = "1") Long page, 
                                        @RequestParam(defaultValue = "10") Long size) {
        Page<WhiteList> whiteListPage = new Page<>(page, size);
        IPage<WhiteList> resultPage = whiteListService.page(whiteListPage);
        return Result.success(resultPage);
    }

    /**
     * 查询所有白名单。
     *
     * @return 白名单列表
     */
    @GetMapping("/list")
    public Result<List<WhiteList>> list() {
        List<WhiteList> whiteLists = whiteListService.list();
        return Result.success(whiteLists);
    }

    /**
     * 根据ID查询白名单。
     *
     * @param id 白名单ID
     * @return 白名单信息
     */
    @GetMapping("/{id}")
    public Result<WhiteList> getById(@PathVariable Long id) {
        WhiteList whiteList = whiteListService.getById(id);
        return whiteList != null ? Result.success(whiteList) : Result.error(ErrorCode.BUSINESS_ERROR.getCode(), "白名单不存在");
    }

    /**
     * 添加白名单。
     *
     * @param whiteList 白名单信息
     * @return 操作结果
     */
    @PostMapping
    public Result<Boolean> add(@RequestBody WhiteList whiteList) {
        boolean saved = whiteListService.save(whiteList);
        if (saved) {
            // 添加成功后刷新缓存
            whiteListService.refreshWhiteListCache();
            return Result.success(true);
        }
        return Result.error(ErrorCode.BUSINESS_ERROR.getCode(), "添加失败");
    }

    /**
     * 更新白名单。
     *
     * @param whiteList 白名单信息
     * @return 操作结果
     */
    @PutMapping
    public Result<Boolean> update(@RequestBody WhiteList whiteList) {
        boolean updated = whiteListService.updateById(whiteList);
        if (updated) {
            // 更新成功后刷新缓存
            whiteListService.refreshWhiteListCache();
            return Result.success(true);
        }
        return Result.error(ErrorCode.BUSINESS_ERROR.getCode(), "更新失败");
    }

    /**
     * 删除白名单。
     *
     * @param id 白名单ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean deleted = whiteListService.removeById(id);
        if (deleted) {
            // 删除成功后刷新缓存
            whiteListService.refreshWhiteListCache();
            return Result.success(true);
        }
        return Result.error(ErrorCode.BUSINESS_ERROR.getCode(), "删除失败");
    }

    /**
     * 刷新白名单缓存。
     *
     * @return 操作结果
     */
    @PostMapping("/refresh-cache")
    public Result<Boolean> refreshCache() {
        whiteListService.refreshWhiteListCache();
        Result<Boolean> result = Result.success(true);
        result.setMessage("白名单缓存已刷新");
        return result;
    }
}