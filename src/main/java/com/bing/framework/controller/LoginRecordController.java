package com.bing.framework.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import lombok.extern.slf4j.Slf4j;

import com.bing.framework.common.ErrorCode;
import com.bing.framework.common.Result;
import com.bing.framework.context.RequestContext;
import com.bing.framework.dto.LoginRecordQueryDTO;
import com.bing.framework.entity.LoginRecord;
import com.bing.framework.exception.BusinessException;
import com.bing.framework.service.LoginRecordService;

/**
 * 登录记录控制器
 * 基于MyBatis-Plus实现的RESTful API接口
 * 提供登录记录的查询、清理等操作，集成Swagger文档，支持接口描述和参数说明
 * 
 * @author zhengbing
 * @date 2025-11-11
 */
@Api(tags = "登录记录管理", description = "提供登录记录查询、清理等功能")
@RestController
@RequestMapping("/api/loginRecords")
@Slf4j
public class LoginRecordController {



    @Autowired
    private LoginRecordService loginRecordService;

    /**
     * 查询系统登录记录列表
     * 
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    @ApiOperation(value = "查询系统登录记录列表", notes = "查询所有用户的登录记录，支持分页和多条件筛选")
    @ApiResponses({
        @ApiResponse(code = 200, message = "查询成功"),
        @ApiResponse(code = 400, message = "参数错误"),
        @ApiResponse(code = 500, message = "查询失败")
    })
    @PostMapping("/query")
    public Result<?> queryLoginRecords(@ApiParam(name = "queryDTO", value = "查询条件", required = true) @Validated @RequestBody LoginRecordQueryDTO queryDTO) {
        try {
            // 设置默认分页参数
            if (queryDTO.getPage() == null || queryDTO.getPage() < 1) {
                queryDTO.setPage(1);
            }
            if (queryDTO.getSize() == null || queryDTO.getSize() < 1 || queryDTO.getSize() > 100) {
                queryDTO.setSize(10);
            }
            
            return Result.success(loginRecordService.queryLoginRecords(queryDTO));
        } catch (Exception e) {
            log.error("查询登录记录失败：{}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
    }

    /**
     * 获取当前用户的登录记录
     * 
     * @param page 页码
     * @param size 每页数量
     * @param request HTTP请求
     * @return 分页结果
     */
    @ApiOperation(value = "获取当前用户登录记录", notes = "查询当前登录用户的登录历史记录")
    @ApiResponses({
        @ApiResponse(code = 200, message = "查询成功"),
        @ApiResponse(code = 401, message = "未登录或令牌失效"),
        @ApiResponse(code = 500, message = "查询失败")
    })
    @GetMapping("/my")
    public Result<?> getMyLoginRecords(
            @ApiParam(name = "page", value = "页码，默认1", defaultValue = "1") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam(name = "size", value = "每页数量，默认10", defaultValue = "10") @RequestParam(defaultValue = "10") Integer size) {
        try {
            Long userId = (Long) RequestContext.getRequest().getAttribute("userId");
            return Result.success(loginRecordService.getLoginRecordsByUserId(userId, page, size));
        } catch (Exception e) {
            log.error("查询当前用户登录记录失败：{}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
    }

    /**
     * 获取最近的登录记录
     * 
     * @param limit 查询数量
     * @return 登录记录列表
     */
    @ApiOperation(value = "获取最近登录记录", notes = "查询最近的登录记录，默认50条")
    @ApiResponses({
        @ApiResponse(code = 200, message = "查询成功"),
        @ApiResponse(code = 400, message = "参数错误"),
        @ApiResponse(code = 500, message = "查询失败")
    })
    @GetMapping("/recent")
    public Result<?> getRecentLoginRecords(
            @ApiParam(name = "limit", value = "查询数量，默认50，最大100", defaultValue = "50") @RequestParam(defaultValue = "50") Integer limit) {
        try {
            // 限制查询数量
            if (limit < 1 || limit > 100) {
                limit = 50;
            }
            
            List<LoginRecord> records = loginRecordService.getRecentLoginRecords(limit);
            return Result.success(records);
        } catch (Exception e) {
            log.error("查询最近登录记录失败：{}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
    }

    /**
     * 获取失败的登录记录
     * 
     * @param days 查询天数
     * @param page 页码
     * @param size 每页数量
     * @return 分页结果
     */
    @ApiOperation(value = "获取失败登录记录", notes = "查询最近几天内的失败登录记录")
    @ApiResponses({
        @ApiResponse(code = 200, message = "查询成功"),
        @ApiResponse(code = 400, message = "参数错误"),
        @ApiResponse(code = 500, message = "查询失败")
    })
    @GetMapping("/failed")
    public Result<?> getFailedLoginRecords(
            @ApiParam(name = "days", value = "查询天数，默认7天", defaultValue = "7") @RequestParam(defaultValue = "7") Integer days,
            @ApiParam(name = "page", value = "页码，默认1", defaultValue = "1") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam(name = "size", value = "每页数量，默认10", defaultValue = "10") @RequestParam(defaultValue = "10") Integer size) {
        try {
            // 限制查询天数
            if (days < 1 || days > 90) {
                days = 7;
            }
            
            return Result.success(loginRecordService.getFailedLoginRecords(days, page, size));
        } catch (Exception e) {
            log.error("查询失败登录记录失败：{}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
    }

    /**
     * 清理过期的登录记录
     * 
     * @param days 保留天数
     * @return 清理的记录数
     */
    @ApiOperation(value = "清理过期登录记录", notes = "清理指定天数之前的登录记录")
    @ApiResponses({
        @ApiResponse(code = 200, message = "清理成功"),
        @ApiResponse(code = 400, message = "参数错误"),
        @ApiResponse(code = 500, message = "清理失败")
    })
    @DeleteMapping("/clean")
    public Result<?> cleanExpiredRecords(
            @ApiParam(name = "days", value = "保留天数，默认90天", defaultValue = "90") @RequestParam(defaultValue = "90") Integer days) {
        try {
            // 限制保留天数
            if (days < 7 || days > 365) {
                days = 90;
            }
            
            int deleteCount = loginRecordService.cleanExpiredRecords(days);
            return Result.success(deleteCount);
        } catch (Exception e) {
            log.error("清理过期登录记录失败：{}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
    }
}