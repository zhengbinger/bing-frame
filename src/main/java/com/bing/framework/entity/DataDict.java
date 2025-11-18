package com.bing.framework.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * 数据字典实体类
 * 使用MyBatis-Plus注解定义表映射关系，通过Lombok简化Getter/Setter等方法
 * 对应数据库data_dict表，存储数据字典的类型信息和基本配置
 * 
 * @author zhengbing
 * @date 2025-11-14
 */
@ApiModel(value = "数据字典信息", description = "系统数据字典实体类，存储字典类型和基本配置信息")
@Data
@TableName("data_dict")
public class DataDict implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 字典ID
     */
    @ApiModelProperty(value = "字典ID", notes = "自增主键", dataType = "Long", example = "1")
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 字典编码
     */
    @ApiModelProperty(value = "字典编码", notes = "唯一标识字典类型", required = true, dataType = "String", example = "USER_STATUS")
    private String code;

    /**
     * 字典名称
     */
    @ApiModelProperty(value = "字典名称", required = true, dataType = "String", example = "用户状态")
    private String name;

    /**
     * 字典描述
     */
    @ApiModelProperty(value = "字典描述", dataType = "String", example = "用户状态字典")
    private String description;

    /**
     * 是否启用：0-禁用，1-启用
     */
    @ApiModelProperty(value = "状态", notes = "0-禁用，1-启用", dataType = "Integer", example = "1")
    private Integer status;

    /**
     * 排序号
     */
    @ApiModelProperty(value = "排序号", dataType = "Integer", example = "1")
    private Integer sort;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间", dataType = "Date")
    private Date createTime;

    /**
     * 更新时间
     */
    @ApiModelProperty(value = "更新时间", dataType = "Date")
    private Date updateTime;
}