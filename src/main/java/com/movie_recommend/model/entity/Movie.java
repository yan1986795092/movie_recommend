package com.movie_recommend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/**
 * 电影表
 * @TableName movie
 */
@TableName(value ="movie")
@Data
public class Movie implements Serializable {
    /**
     * 电影id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long movieId;

    /**
     * 创建用户id
     */
    private Long userId;

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
     * 上映日期
     */
    private String releaseDate;

    /**
     * 片长
     */
    private String runtime;

    /**
     * 豆瓣评分
     */
    private String ratingNum;

    /**
     * 点赞数
     */
    private Integer thumbNum;

    /**
     * 收藏数
     */
    private Integer favourNum;

    /**
     * 电影简介
     */
    private String tags;

    /**
     * 电影地址
     */
    private String movieUrl;

    /**
     * 照片本地地址
     */
    private String local;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}