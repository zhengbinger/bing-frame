package com.bing.framework.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import lombok.extern.slf4j.Slf4j;

import com.bing.framework.entity.DataDict;
import com.bing.framework.service.DataDictService;

/**
 * 数据字典控制器
 * 基于MyBatis-Plus实现的RESTful API接口
 * 提供数据字典的增删改查等操作，使用Swagger注解生成API文档，便于前端开发和接口测试
 * 
 * @author zhengbing
 * @date 2025-11-14
 */
@Api(tags = "数据字典管理")
@RestController
@RequestMapping("/api/data-dict")
@Slf4j
public class DataDictController {

    @Autowired
    private DataDictService dataDictService;

    /**
     * 根据ID查询字典
     */
    @ApiOperation(value = "根据ID查询字典", notes = "根据字典ID获取字典详细信息")
    @ApiResponses({
        @ApiResponse(code = 200, message = "查询成功"),
        @ApiResponse(code = 404, message = "字典不存在")
    })
    @GetMapping("/{id}")
    public ResponseEntity<DataDict> getDataDictById(
            @ApiParam(name = "id", value = "字典ID", required = true) @PathVariable Long id) {
        log.debug("根据ID查询字典: {}", id);
        DataDict dataDict = dataDictService.getDataDictById(id);
        if (dataDict == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dataDict);
    }

    /**
     * 根据字典编码查询字典
     */
    @ApiOperation(value = "根据字典编码查询字典", notes = "根据字典编码获取字典信息")
    @ApiResponses({
        @ApiResponse(code = 200, message = "查询成功"),
        @ApiResponse(code = 404, message = "字典不存在")
    })
    @GetMapping("/code/{dictCode}")
    public ResponseEntity<DataDict> getDataDictByDictCode(
            @ApiParam(name = "dictCode", value = "字典编码", required = true) @PathVariable String dictCode) {
        log.debug("根据字典编码查询字典: {}", dictCode);
        DataDict dataDict = dataDictService.getDataDictByDictCode(dictCode);
        if (dataDict == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dataDict);
    }

    /**
     * 查询所有字典
     */
    @ApiOperation(value = "查询所有字典", notes = "获取所有字典列表")
    @ApiResponse(code = 200, message = "查询成功")
    @GetMapping("/list")
    public ResponseEntity<List<DataDict>> getAllDataDicts() {
        log.debug("查询所有字典");
        List<DataDict> dataDicts = dataDictService.getAllDataDicts();
        return ResponseEntity.ok(dataDicts);
    }

    /**
     * 查询启用的字典列表
     */
    @ApiOperation(value = "查询启用的字典列表", notes = "获取所有启用状态的字典")
    @ApiResponse(code = 200, message = "查询成功")
    @GetMapping("/list/enabled")
    public ResponseEntity<List<DataDict>> getEnabledDataDicts() {
        log.debug("查询启用的字典列表");
        List<DataDict> dataDicts = dataDictService.getEnabledDataDicts();
        return ResponseEntity.ok(dataDicts);
    }

    /**
     * 新增字典
     */
    @ApiOperation(value = "新增字典", notes = "创建新的数据字典")
    @ApiResponses({
        @ApiResponse(code = 200, message = "创建成功"),
        @ApiResponse(code = 400, message = "参数错误或字典编码重复")
    })
    @PostMapping
    public ResponseEntity<Boolean> saveDataDict(
            @ApiParam(name = "dataDict", value = "字典对象", required = true) @RequestBody DataDict dataDict) {
        log.debug("新增字典: {}", dataDict.getDictName());
        try {
            boolean result = dataDictService.saveDataDict(dataDict);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            log.error("新增字典失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(false);
        }
    }

    /**
     * 更新字典
     */
    @ApiOperation(value = "更新字典", notes = "更新指定的字典信息")
    @ApiResponses({
        @ApiResponse(code = 200, message = "更新成功"),
        @ApiResponse(code = 400, message = "参数错误或字典编码重复"),
        @ApiResponse(code = 404, message = "字典不存在")
    })
    @PutMapping
    public ResponseEntity<Boolean> updateDataDict(
            @ApiParam(name = "dataDict", value = "字典对象", required = true) @RequestBody DataDict dataDict) {
        log.debug("更新字典: {}", dataDict.getDictName());
        try {
            boolean result = dataDictService.updateDataDict(dataDict);
            if (!result) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            log.error("更新字典失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(false);
        }
    }

    /**
     * 删除字典
     */
    @ApiOperation(value = "删除字典", notes = "删除指定的字典")
    @ApiResponses({
        @ApiResponse(code = 200, message = "删除成功"),
        @ApiResponse(code = 404, message = "字典不存在")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteDataDict(
            @ApiParam(name = "id", value = "字典ID", required = true) @PathVariable Long id) {
        log.debug("删除字典: {}", id);
        try {
            boolean result = dataDictService.deleteDataDict(id);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            log.error("删除字典失败: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 批量删除字典
     */
    @ApiOperation(value = "批量删除字典", notes = "批量删除指定的字典列表")
    @ApiResponse(code = 200, message = "删除成功")
    @DeleteMapping("/batch")
    public ResponseEntity<Boolean> deleteBatchDataDicts(
            @ApiParam(name = "ids", value = "字典ID列表", required = true) @RequestBody List<Long> ids) {
        log.debug("批量删除字典，数量: {}", ids.size());
        boolean result = dataDictService.deleteBatchDataDicts(ids);
        return ResponseEntity.ok(result);
    }

    /**
     * 启用/禁用字典
     */
    @ApiOperation(value = "启用/禁用字典", notes = "修改字典的启用状态")
    @ApiResponses({
        @ApiResponse(code = 200, message = "修改成功"),
        @ApiResponse(code = 400, message = "参数错误")
    })
    @PutMapping("/status")
    public ResponseEntity<Boolean> changeStatus(
            @ApiParam(name = "id", value = "字典ID", required = true) @RequestParam Long id,
            @ApiParam(name = "status", value = "状态（0-禁用，1-启用）", required = true) @RequestParam Integer status) {
        log.debug("修改字典状态，ID: {}, 状态: {}", id, status);
        if (status != 0 && status != 1) {
            return ResponseEntity.badRequest().body(false);
        }
        boolean result = dataDictService.changeStatus(id, status);
        return ResponseEntity.ok(result);
    }
}