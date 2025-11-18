package com.bing.framework.controller;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ApiResponse;

import com.bing.framework.entity.DataDictItem;
import com.bing.framework.service.DataDictItemService;

/**
 * 数据字典项控制器
 * 基于MyBatis-Plus实现的RESTful API接口
 * 提供数据字典项的增删改查等操作，使用Swagger注解生成API文档，便于前端开发和接口测试
 * 
 * @author zhengbing
 * @date 2025-11-14
 */
@Api(tags = "数据字典项管理")
@RestController
@RequestMapping("/api/data-dict-item")
@Slf4j
public class DataDictItemController {

    @Autowired
    private DataDictItemService dataDictItemService;

    /**
     * 根据ID查询字典项
     */
    @ApiOperation(value = "根据ID查询字典项", notes = "根据字典项ID获取字典项详细信息")
    @ApiResponses({
        @ApiResponse(code = 200, message = "查询成功"),
        @ApiResponse(code = 404, message = "字典项不存在")
    })
    @GetMapping("/{id}")
    public ResponseEntity<DataDictItem> getDataDictItemById(
            @ApiParam(name = "id", value = "字典项ID", required = true) @PathVariable Long id) {
        log.debug("根据ID查询字典项: {}", id);
        DataDictItem dataDictItem = dataDictItemService.getDataDictItemById(id);
        if (dataDictItem == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dataDictItem);
    }

    /**
     * 根据字典ID查询字典项列表
     */
    @ApiOperation(value = "根据字典ID查询字典项列表", notes = "获取指定字典下的所有字典项")
    @ApiResponse(code = 200, message = "查询成功")
    @GetMapping("/list-by-dict/{dictId}")
    public ResponseEntity<List<DataDictItem>> getDataDictItemsByDictId(
            @ApiParam(name = "dictId", value = "字典ID", required = true) @PathVariable Long dictId) {
        log.debug("根据字典ID查询字典项列表: {}", dictId);
        List<DataDictItem> items = dataDictItemService.getDataDictItemsByDictId(dictId);
        return ResponseEntity.ok(items);
    }

    /**
     * 根据字典编码查询字典项列表
     */
    @ApiOperation(value = "根据字典编码查询字典项列表", notes = "获取指定字典编码下的所有字典项")
    @ApiResponse(code = 200, message = "查询成功")
    @GetMapping("/list-by-code/{dictCode}")
    public ResponseEntity<List<DataDictItem>> getDataDictItemsByCode(
            @ApiParam(name = "code", value = "字典编码", required = true) @PathVariable String code) {
        List<DataDictItem> items = dataDictItemService.getDataDictItemsByCode(code);
        return ResponseEntity.ok(items);
    }

    /**
     * 根据字典编码查询启用的字典项列表
     */
    @ApiOperation(value = "根据字典编码查询启用的字典项列表", notes = "获取指定字典编码下的所有启用状态的字典项")
    @ApiResponse(code = 200, message = "查询成功")
    @GetMapping("/list-enabled-by-code/{dictCode}")
    public ResponseEntity<List<DataDictItem>> getEnabledDataDictItemsByCode(
            @ApiParam(name = "code", value = "字典编码", required = true) @PathVariable String code) {
        List<DataDictItem> items = dataDictItemService.getEnabledDataDictItemsByCode(code);
        return ResponseEntity.ok(items);
    }

    /**
     * 新增字典项
     */
    @ApiOperation(value = "新增字典项", notes = "创建新的数据字典项")
    @ApiResponses({
        @ApiResponse(code = 200, message = "创建成功"),
        @ApiResponse(code = 400, message = "参数错误或字典项值重复")
    })
    @PostMapping
    public ResponseEntity<Boolean> saveDataDictItem(
            @ApiParam(name = "dataDictItem", value = "字典项对象", required = true) @RequestBody DataDictItem dataDictItem) {
        log.debug("新增字典项: {}", dataDictItem.getLabel());
        try {
            boolean result = dataDictItemService.saveDataDictItem(dataDictItem);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            log.error("新增字典项失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(false);
        }
    }

    /**
     * 更新字典项
     */
    @ApiOperation(value = "更新字典项", notes = "更新指定的字典项信息")
    @ApiResponses({
        @ApiResponse(code = 200, message = "更新成功"),
        @ApiResponse(code = 400, message = "参数错误或字典项值重复"),
        @ApiResponse(code = 404, message = "字典项不存在")
    })
    @PutMapping
    public ResponseEntity<Boolean> updateDataDictItem(
            @ApiParam(name = "dataDictItem", value = "字典项对象", required = true) @RequestBody DataDictItem dataDictItem) {
        log.debug("更新字典项: {}", dataDictItem.getLabel());
        try {
            boolean result = dataDictItemService.updateDataDictItem(dataDictItem);
            if (!result) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            log.error("更新字典项失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(false);
        }
    }

    /**
     * 删除字典项
     */
    @ApiOperation(value = "删除字典项", notes = "删除指定的字典项")
    @ApiResponses({
        @ApiResponse(code = 200, message = "删除成功"),
        @ApiResponse(code = 404, message = "字典项不存在")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteDataDictItem(
            @ApiParam(name = "id", value = "字典项ID", required = true) @PathVariable Long id) {
        log.debug("删除字典项: {}", id);
        try {
            boolean result = dataDictItemService.deleteDataDictItem(id);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            log.error("删除字典项失败: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 批量删除字典项
     */
    @ApiOperation(value = "批量删除字典项", notes = "批量删除指定的字典项列表")
    @ApiResponse(code = 200, message = "删除成功")
    @DeleteMapping("/batch")
    public ResponseEntity<Boolean> deleteBatchDataDictItems(
            @ApiParam(name = "ids", value = "字典项ID列表", required = true) @RequestBody List<Long> ids) {
        log.debug("批量删除字典项，数量: {}", ids.size());
        boolean result = dataDictItemService.deleteBatchDataDictItems(ids);
        return ResponseEntity.ok(result);
    }

    /**
     * 启用/禁用字典项
     */
    @ApiOperation(value = "启用/禁用字典项", notes = "修改字典项的启用状态")
    @ApiResponses({
        @ApiResponse(code = 200, message = "修改成功"),
        @ApiResponse(code = 400, message = "参数错误")
    })
    @PutMapping("/status")
    public ResponseEntity<Boolean> changeStatus(
            @ApiParam(name = "id", value = "字典项ID", required = true) @RequestParam Long id,
            @ApiParam(name = "status", value = "状态（0-禁用，1-启用）", required = true) @RequestParam Integer status) {
        log.debug("修改字典项状态，ID: {}, 状态: {}", id, status);
        if (status != 0 && status != 1) {
            return ResponseEntity.badRequest().body(false);
        }
        boolean result = dataDictItemService.changeStatus(id, status);
        return ResponseEntity.ok(result);
    }
}