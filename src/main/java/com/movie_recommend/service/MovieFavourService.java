package com.movie_recommend.service;

import co.elastic.clients.elasticsearch.sql.QueryRequest;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.movie_recommend.model.dto.user.UserQueryRequest;
import com.movie_recommend.model.entity.Movie;
import com.movie_recommend.model.entity.MovieFavour;
import com.movie_recommend.model.entity.User;

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
    Integer getTotalFavoritedMovies();
}
