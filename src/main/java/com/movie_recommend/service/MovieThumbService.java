package com.movie_recommend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.movie_recommend.model.entity.MovieThumb;
import com.movie_recommend.model.entity.User;

/**
 * 帖子点赞服务
 */
public interface MovieThumbService extends IService<MovieThumb> {

    /**
     * 点赞
     *
     * @param movieId
     * @param loginUser
     * @return
     */
    int doMovieThumb(long movieId, User loginUser);

    /**
     * 电影点赞（内部服务）
     *
     * @param userId
     * @param movieId
     * @return
     */
    int doMovieThumbInner(long userId, long movieId);
}
