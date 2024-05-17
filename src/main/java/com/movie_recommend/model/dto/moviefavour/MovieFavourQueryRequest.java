package com.movie_recommend.model.dto.moviefavour;

import com.movie_recommend.common.PageRequest;
import com.movie_recommend.model.dto.movie.MovieQueryRequest;

import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 电影收藏查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MovieFavourQueryRequest extends PageRequest implements Serializable {

    /**
     * 电影查询请求
     */
    private MovieQueryRequest movieQueryRequest;

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