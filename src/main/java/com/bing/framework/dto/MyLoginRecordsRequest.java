/**
 * 查询当前用户登录记录请求类
 * 封装查询当前用户登录历史记录所需的分页参数
 * 用于LoginRecordController的getMyLoginRecords方法
 * 
 * @author zhengbing
 * @date 2025-11-13
 */
package com.bing.framework.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.Min;

@Data
@ApiModel(value = "MyLoginRecordsRequest", description = "查询当前用户登录记录请求参数")
public class MyLoginRecordsRequest {

    @ApiModelProperty(value = "页码，默认1", example = "1", notes = "分页查询的页码")
    @Min(value = 1, message = "页码不能小于1")
    private Integer page = 1;
    
    @ApiModelProperty(value = "每页数量，默认10", example = "10", notes = "每页显示的记录数量")
    @Min(value = 1, message = "每页数量不能小于1")
    private Integer size = 10;
}