package com.movie_recommend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.movie_recommend.model.entity.Movie;
import com.movie_recommend.model.vo.MovieVO;
import com.movie_recommend.model.dto.movie.MovieQueryRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @Author yqq
 * @Date 2024/4/18 14:04
 * @Description
 * @Version 1.0
 */
public interface MovieService extends IService<Movie> {
    /**
     * 校验
     *
     * @param movie
     * @param add
     */
    void validMovie(Movie movie, boolean add);

    /**
     * 获取查询条件
     *
     * @param movieQueryRequest
     * @return
     */
    QueryWrapper<Movie> getQueryWrapper(MovieQueryRequest movieQueryRequest);
    

    /**
     * 获取电影封装
     *
     * @param movie
     * @param request
     * @return
     */
    MovieVO getMovieVO(Movie movie, HttpServletRequest request);

    /**
     * 分页获取电影封装
     *
     * @param moviePage
     * @param request
     * @return
     */
    Page<MovieVO> getMovieVOPage(Page<Movie> moviePage, HttpServletRequest request);

    Movie getTopRatedMovie();

    boolean userHasData(Long userId);

    List<Movie> recommendMovies(Movie targetMovie);

    List<Movie> recommendMoviesUser(Long userId);

    double calculateCosineSimilarity(Movie m1, Movie m2);


}
