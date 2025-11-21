package com.bing.framework.controller;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ApiResponse;

import com.bing.framework.common.ErrorCode;
import com.bing.framework.common.Result;
import com.bing.framework.entity.WhiteList;
import com.bing.framework.service.WhiteListService;

/**
 * 白名单控制器
 * 基于MyBatis-Plus和分页插件实现的RESTful API接口
 * 提供白名单的增删改查等管理功能，支持分页查询和缓存刷新
 *
 * @author zhengbing
 * @date 2024-11-03
 */
@Api(tags = "白名单管理")
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
    @ApiOperation(value = "分页查询白名单", notes = "支持分页查询白名单列表，默认页码为1，每页10条数据")
    @ApiResponses({
        @ApiResponse(code = 200, message = "查询成功"),
        @ApiResponse(code = 500, message = "查询失败")
    })
    @GetMapping("/page")
    public Result<IPage<WhiteList>> page(@ApiParam(name = "page", value = "页码", required = false, defaultValue = "1") @RequestParam(defaultValue = "1") Long page, 
                                        @ApiParam(name = "size", value = "每页大小", required = false, defaultValue = "10") @RequestParam(defaultValue = "10") Long size) {
        Page<WhiteList> whiteListPage = new Page<>(page, size);
        IPage<WhiteList> resultPage = whiteListService.page(whiteListPage);
        return Result.success(resultPage);
    }

    /**
     * 查询所有白名单。
     *
     * @return 白名单列表
     */
    @ApiOperation(value = "查询所有白名单", notes = "返回系统中所有的白名单数据")
    @ApiResponses({
        @ApiResponse(code = 200, message = "查询成功"),
        @ApiResponse(code = 500, message = "查询失败")
    })
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
    @ApiOperation(value = "根据ID查询白名单", notes = "通过ID获取指定的白名单详情")
    @ApiResponses({
        @ApiResponse(code = 200, message = "查询成功"),
        @ApiResponse(code = 404, message = "白名单不存在"),
        @ApiResponse(code = 500, message = "查询失败")
    })
    @GetMapping("/{id}")
    public Result<WhiteList> getById(@ApiParam(name = "id", value = "白名单ID", required = true) @PathVariable Long id) {
        WhiteList whiteList = whiteListService.getById(id);
        return whiteList != null ? Result.success(whiteList) : Result.error(ErrorCode.BUSINESS_ERROR.getCode(), "白名单不存在");
    }

    /**
     * 添加白名单。
     *
     * @param whiteList 白名单信息
     * @return 操作结果
     */
    @ApiOperation(value = "添加白名单", notes = "新增白名单记录，添加成功后自动刷新缓存")
    @ApiResponses({
        @ApiResponse(code = 200, message = "添加成功"),
        @ApiResponse(code = 400, message = "参数错误"),
        @ApiResponse(code = 500, message = "添加失败")
    })
    @PostMapping
    public Result<Boolean> add(@ApiParam(name = "whiteList", value = "白名单信息", required = true) @RequestBody WhiteList whiteList) {
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
    @ApiOperation(value = "更新白名单", notes = "更新指定的白名单信息，更新成功后自动刷新缓存")
    @ApiResponses({
        @ApiResponse(code = 200, message = "更新成功"),
        @ApiResponse(code = 400, message = "参数错误"),
        @ApiResponse(code = 500, message = "更新失败")
    })
    @PutMapping
    public Result<Boolean> update(@ApiParam(name = "whiteList", value = "白名单信息", required = true) @RequestBody WhiteList whiteList) {
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
    @ApiOperation(value = "删除白名单", notes = "根据ID删除指定的白名单，删除成功后自动刷新缓存")
    @ApiResponses({
        @ApiResponse(code = 200, message = "删除成功"),
        @ApiResponse(code = 404, message = "白名单不存在"),
        @ApiResponse(code = 500, message = "删除失败")
    })
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@ApiParam(name = "id", value = "白名单ID", required = true) @PathVariable Long id) {
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
    @ApiOperation(value = "刷新白名单缓存", notes = "手动刷新白名单缓存，使最新的白名单数据生效")
    @ApiResponses({
        @ApiResponse(code = 200, message = "刷新成功"),
        @ApiResponse(code = 500, message = "刷新失败")
    })
    @PostMapping("/refresh-cache")
    public Result<Boolean> refreshCache() {
        whiteListService.refreshWhiteListCache();
        Result<Boolean> result = Result.success(true);
        result.setMessage("白名单缓存已刷新");
        return result;
    }
}