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
 * 数据字典项实体类
 * 使用MyBatis-Plus注解定义表映射关系，通过Lombok简化Getter/Setter等方法
 * 对应数据库data_dict_item表，存储具体的字典项值和配置信息
 * 
 * @author zhengbing
 * @date 2025-11-14
 */
@ApiModel(value = "数据字典项信息", description = "系统数据字典项实体类，存储具体的字典项值和显示文本等信息")
@Data
@TableName("data_dict_item")
public class DataDictItem implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 字典项ID
     */
    @ApiModelProperty(value = "字典项ID", notes = "自增主键", dataType = "Long", example = "1")
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 字典ID
     */
    @ApiModelProperty(value = "字典ID", notes = "关联到数据字典主表", required = true, dataType = "Long", example = "1")
    private Long dictId;

    /**
     * 显示文本
     */
    @ApiModelProperty(value = "显示文本", notes = "显示的文本内容", required = true, dataType = "String", example = "启用")
    private String label;

    /**
     * 字典项值
     */
    @ApiModelProperty(value = "字典项值", notes = "实际存储的值", required = true, dataType = "String", example = "1")
    private String value;

    /**
     * 字典项描述
     */
    @ApiModelProperty(value = "字典项描述", dataType = "String", example = "用户账户处于启用状态")
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