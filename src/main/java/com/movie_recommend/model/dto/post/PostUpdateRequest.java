package com.movie_recommend.model.dto.post;

import java.io.Serializable;
import java.util.List;
import lombok.Data;

/**
 * 更新请求
 */
@Data
public class PostUpdateRequest implements Serializable {

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

    private static final long serialVersionUID = 1L;
}