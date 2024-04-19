package com.yupi.springbootinit.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 电影表
 *
 * @TableName movie
 */
@TableName(value = "movie")
@Data
public class Movie implements Serializable {
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
    /**
     * 电影id
     */
    @TableId(type = IdType.AUTO)
    private Long movieId;
    /**
     * 电影名
     */
    private String name;
    /**
     * 导演
     */
    private String director;
    /**
     * 主演
     */
    private String actors;
    /**
     * 类型
     */
    private String type;
    /**
     * 制片国家/地区
     */
    private String country;
    /**
     * 片长
     */
    private String runtime;
    /**
     * 豆瓣评分
     */
    private String ratingNum;
    /**
     * 照片地址
     */
    private String movieUrl;
    /**
     * 电影简介
     */
    private String tags;
    /**
     * 照片本地地址
     */
    private String local;
    /**
     * 创建用户 id
     */
    private Long userId;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 更新时间
     */
    private Date updateTime;

}