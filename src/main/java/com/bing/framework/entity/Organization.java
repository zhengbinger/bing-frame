package com.bing.framework.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.Date;
import java.util.List;

/**
 * 组织实体类
 * 对应数据库organization表，用于管理组织架构信息
 * 支持层级结构，可构建树形组织结构
 * 
 * @author zhengbing
 * @date 2025-11-12
 */
@Data
@TableName("organization")
@ApiModel(value = "Organization对象", description = "组织实体类")
public class Organization {

    @ApiModelProperty(value = "组织ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "组织名称")
    private String name;

    @ApiModelProperty(value = "组织代码")
    private String code;

    @ApiModelProperty(value = "父组织ID，0表示顶级组织")
    private Long parentId;

    @ApiModelProperty(value = "组织路径，用于快速查找层级关系")
    private String path;

    @ApiModelProperty(value = "排序字段")
    private Integer sort;

    @ApiModelProperty(value = "是否启用，1启用，0禁用")
    private Boolean enabled;

    @ApiModelProperty(value = "组织描述")
    private String description;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    /**
     * 非数据库字段，用于树形结构展示
     * 存储子组织列表
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "子组织列表，非数据库字段")
    private List<Organization> children;
}