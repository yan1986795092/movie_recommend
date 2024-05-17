package com.movie_recommend.model.dto.moviethumb;

import java.io.Serializable;

import lombok.Data;

/**
 * 帖子点赞请求
 */
@Data
public class MovieThumbAddRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 电影 id
     */
    private Long movieId;

    /**
     * 创建用户 id
     */
    private Long userid;

    private static final long serialVersionUID = 1L;
}