package com.movie_recommend.model.dto.movie;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.io.Serializable;
import java.util.Date;


/**
 * 电影 ES 包装类
 **/
@Document(indexName = "movie")
@Data
public class MovieEsDTO implements Serializable {

    /**
     * 电影id
     */
    @Id
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
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    private Integer isDelete;

    private static final long serialVersionUID = 1L;

}
