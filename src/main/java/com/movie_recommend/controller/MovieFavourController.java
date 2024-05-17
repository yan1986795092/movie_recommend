package com.movie_recommend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.movie_recommend.common.BaseResponse;
import com.movie_recommend.common.ErrorCode;
import com.movie_recommend.common.ResultUtils;
import com.movie_recommend.exception.BusinessException;
import com.movie_recommend.exception.ThrowUtils;
import com.movie_recommend.model.dto.moviefavour.MovieFavourAddRequest;
import com.movie_recommend.model.dto.moviefavour.MovieFavourQueryRequest;
import com.movie_recommend.model.entity.Movie;
import com.movie_recommend.model.entity.User;
import com.movie_recommend.model.vo.MovieVO;
import com.movie_recommend.service.MovieFavourService;
import com.movie_recommend.service.MovieService;
import com.movie_recommend.service.UserService;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 电影收藏接口
 */
@RestController
@RequestMapping("/movie_favour")
@Slf4j
public class MovieFavourController {

    @Resource
    private MovieFavourService movieFavourService;
    @Resource
    private MovieService movieService;
    @Resource
    private UserService userService;

    /**
     * 收藏 / 取消收藏
     * @param movieFavourAddRequest
     * @param request
     * @return resultNum 收藏变化数
     */
    @PostMapping("/")
    public BaseResponse<Integer> doMovieFavour(@RequestBody MovieFavourAddRequest movieFavourAddRequest,
            HttpServletRequest request) {
        if (movieFavourAddRequest == null || movieFavourAddRequest.getMovieId()<= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 登录才能操作
        final User loginUser = userService.getLoginUser(request);
        long movieId = movieFavourAddRequest.getMovieId();
        int result = movieFavourService.doMovieFavour(movieId, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 获取我收藏的电影列表
     * @param MovieFavourQueryRequest
     * @param request
     */
    @PostMapping("/my/list/page")
    public BaseResponse<Page<MovieVO>> listMyFavourMovieByPage(@RequestBody MovieFavourQueryRequest MovieFavourQueryRequest,
            HttpServletRequest request) {
        if (MovieFavourQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        long current = MovieFavourQueryRequest.getCurrent();
        long size = MovieFavourQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Movie> moviePage = movieFavourService.listFavourMovieByPage(new Page<>(current, size),
                movieService.getQueryWrapper(MovieFavourQueryRequest.getMovieQueryRequest()), loginUser.getId());
        return ResultUtils.success(movieService.getMovieVOPage(moviePage, request));
    }

    /**
     * 获取用户收藏的电影列表
     *
     * @param movieFavourQueryRequest
     * @param request
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<MovieVO>> listFavourMovieByPage(@RequestBody MovieFavourQueryRequest movieFavourQueryRequest,
            HttpServletRequest request) {
        if (movieFavourQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = movieFavourQueryRequest.getCurrent();
        long size = movieFavourQueryRequest.getPageSize();
        Long userId = movieFavourQueryRequest.getUserId();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20 || userId == null, ErrorCode.PARAMS_ERROR);
        Page<Movie> moviePage = movieFavourService.listFavourMovieByPage(new Page<>(current, size),
                movieService.getQueryWrapper(movieFavourQueryRequest.getMovieQueryRequest()), userId);
        return ResultUtils.success(movieService.getMovieVOPage(moviePage, request));
    }
}
