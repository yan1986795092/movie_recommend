package com.yupi.springbootinit.model.dto.movie;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author yqq
 * @Date 2024/4/18 12:50
 * @Description 更新电影信息
 * @Version 1.0
 */
@Data
public class MovieUpdateRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 电影id
     */
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
     * 编剧
     */
    private String scenarist;
    /**
     * 主演
     */
    private String actors;
    /**
     * 类型
     */
    private List<String> type;
    /**
     * 制片国家/地区
     */
    private String country;
    /**
     * 语言
     */
    private String language;
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
     * 标签
     */
    private String tags;
    /**
     * 评论内容
     */
    private String commentInfo;
    /**
     * 评论者
     */
    private String commentAuthor;
    /**
     * 评论者头像
     */
    private String commentAuthorImgUrl;
    /**
     * 评论点赞数
     */
    private String commentVote;
    /**
     * 照片地址
     */
    private String movieUrl;
    /**
     * 照片本地地址
     */
    private String local;
    /**
     * 评论时间
     */
    private String commentDate;
}
