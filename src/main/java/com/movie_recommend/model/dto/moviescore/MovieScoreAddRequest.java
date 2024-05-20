package com.movie_recommend.model.dto.moviescore;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;

/**
 * 评分
 */
@Data
public class MovieScoreAddRequest implements Serializable {
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

    /**
     * 评分
     */
    private String rating;

    private static final long serialVersionUID = 1L;
}