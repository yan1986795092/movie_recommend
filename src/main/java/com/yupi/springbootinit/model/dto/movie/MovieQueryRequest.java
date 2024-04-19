package com.yupi.springbootinit.model.dto.movie;

import com.yupi.springbootinit.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;


/**
 * @Author yqq
 * @Date 2024/4/18 12:47
 * @Description 查询电影
 * @Version 1.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MovieQueryRequest extends PageRequest implements Serializable {

    /**
     * 电影id
     */
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
     * 搜索词
     */
    private String searchText;

    /**
     * 点赞用户 id
     */
    private Long thumbUserId;

    /**
     * 收藏用户 id
     */
    private Long favourUserId;


    private static final long serialVersionUID = 1L;

}
