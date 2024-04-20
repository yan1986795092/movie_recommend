package com.yupi.springbootinit.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.springbootinit.model.entity.Movie;
import com.yupi.springbootinit.model.entity.MovieFavour;
import com.yupi.springbootinit.model.entity.User;

/**
 * 电影收藏服务
 */
public interface MovieFavourService extends IService<MovieFavour> {

    /**
     * 电影收藏
     *
     * @param movieId
     * @param loginUser
     * @return
     */
    int doMovieFavour(long movieId, User loginUser);

    /**
     * 分页获取用户收藏的电影列表
     *
     * @param page
     * @param queryWrapper
     * @param favourUserId
     * @return
     */
    Page<Movie> listFavourMovieByPage(IPage<Movie> page, Wrapper<Movie> queryWrapper,
                                    long favourUserId);

    /**
     * 电影收藏（内部服务）
     *
     * @param userId
     * @param movieId
     * @return
     */
    int doMovieFavourInner(long userId, long movieId);
}
