package com.movie_recommend.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.movie_recommend.common.ErrorCode;
import com.movie_recommend.exception.BusinessException;
import com.movie_recommend.mapper.MovieFavourMapper;
import com.movie_recommend.model.entity.Movie;
import com.movie_recommend.model.entity.MovieFavour;
import com.movie_recommend.model.entity.User;
import com.movie_recommend.service.MovieFavourService;
import com.movie_recommend.service.MovieService;
import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * 电影收藏服务实现
 */
@Service
public class MovieFavourServiceImpl extends ServiceImpl<MovieFavourMapper, MovieFavour>
        implements MovieFavourService {

    @Resource
    private MovieService movieService;

    @Resource
    private MovieFavourMapper movieFavourMapper;
    /**
     * 电影收藏
     *
     * @param movieId
     * @param loginUser
     * @return
     */
    @Override
    public int doMovieFavour(long movieId, User loginUser) {
        // 判断是否存在
        Movie movie = movieService.getById(movieId);
        if (movie == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 是否已电影收藏
        long userId = loginUser.getId();
        // 每个用户串行电影收藏
        // 锁必须要包裹住事务方法
        MovieFavourService movieFavourService = (MovieFavourService) AopContext.currentProxy();
        synchronized (String.valueOf(userId).intern()) {
            return movieFavourService.doMovieFavourInner(userId, movieId);
        }
    }

    @Override
    public Page<Movie> listFavourMovieByPage(IPage<Movie> page, Wrapper<Movie> queryWrapper, long favourUserId) {
        if (favourUserId <= 0) {
            return new Page<>();
        }
        return baseMapper.listFavourMovieByPage(page, queryWrapper, favourUserId);
    }

    /**
     * 封装了事务的方法
     *
     * @param userId
     * @param movieId
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int doMovieFavourInner(long userId, long movieId) {
        MovieFavour movieFavour = new MovieFavour();
        movieFavour.setUserId(userId);
        movieFavour.setMovieId(movieId);
        QueryWrapper<MovieFavour> movieFavourQueryWrapper = new QueryWrapper<>(movieFavour);
        MovieFavour oldMovieFavour = this.getOne(movieFavourQueryWrapper);
        boolean result;
        // 已收藏
        if (oldMovieFavour != null) {
            result = this.remove(movieFavourQueryWrapper);
            if (result) {
                // 电影收藏数 - 1
                result = movieService.update()
                        .eq("movieId", movieId)
                        .gt("favourNum", 0)
                        .setSql("favourNum = favourNum - 1")
                        .update();
                return result ? -1 : 0;
            } else {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
        } else {
            // 未电影收藏
            result = this.save(movieFavour);
            if (result) {
                // 电影收藏数 + 1
                result = movieService.update()
                        .eq("movieId", movieId)
                        .setSql("favourNum = favourNum + 1")
                        .update();
                return result ? 1 : 0;
            } else {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
        }
    }

    @Override
    public Integer getTotalFavoritedMovies() {
        return movieFavourMapper.getTotalFavoritedMovies();
    }

}




