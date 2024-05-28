package com.movie_recommend.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.movie_recommend.model.entity.Movie;
import com.movie_recommend.model.entity.MovieFavour;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 电影收藏数据库操作
 */
public interface MovieFavourMapper extends BaseMapper<MovieFavour> {

    /**
     * 分页查询收藏电影列表
     *
     * @param page
     * @param queryWrapper
     * @param favourUserId
     * @return
     */
    Page<Movie> listFavourMovieByPage(IPage<Movie> page, @Param(Constants.WRAPPER) Wrapper<Movie> queryWrapper,
                                    long favourUserId);

    @Select("SELECT COUNT(DISTINCT movieId) FROM post_favour")
    Integer getTotalFavoritedMovies();


}




