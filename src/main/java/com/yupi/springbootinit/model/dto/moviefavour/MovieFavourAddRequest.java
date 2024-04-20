package com.yupi.springbootinit.model.dto.moviefavour;

import java.io.Serializable;

import lombok.Data;

/**
 * 帖子收藏 / 取消收藏请求
 */
@Data
public class MovieFavourAddRequest implements Serializable {
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
    private Long userId;

    private static final long serialVersionUID = 1L;
}