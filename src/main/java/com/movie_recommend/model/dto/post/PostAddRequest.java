package com.movie_recommend.model.dto.post;

import lombok.Data;

import java.io.Serializable;

/**
 * 创建请求
 */
@Data
public class PostAddRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * id
     */
    private Long id;
    /**
     * 电影id
     */
    private Long movieId;
    /**
     * 创建用户 id
     */
    private Long userId;
    /**
     * 内容
     */
    private String content;
}