package com.movie_recommend.controller;

import com.movie_recommend.exception.BusinessException;
import com.movie_recommend.common.BaseResponse;
import com.movie_recommend.common.ErrorCode;
import com.movie_recommend.common.ResultUtils;
import com.movie_recommend.model.dto.moviethumb.MovieThumbAddRequest;
import com.movie_recommend.model.entity.User;
import com.movie_recommend.service.MovieService;
import com.movie_recommend.service.MovieThumbService;
import com.movie_recommend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 帖子点赞接口
 */
@RestController
@RequestMapping("/movie_thumb")
@Slf4j
public class MovieThumbController {

    @Resource
    private MovieThumbService movieThumbService;
    @Resource
    private UserService userService;

    /**
     * 点赞 / 取消点赞
     *
     * @param movieThumbAddRequest
     * @param request
     * @return resultNum 本次点赞变化数
     */
    @PostMapping("/")
    public BaseResponse<Integer> doThumb(@RequestBody MovieThumbAddRequest movieThumbAddRequest,
                                         HttpServletRequest request) {
        if (movieThumbAddRequest == null || movieThumbAddRequest.getMovieId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 登录才能点赞
        final User loginUser = userService.getLoginUser(request);
        long movieId = movieThumbAddRequest.getMovieId();
        int result = movieThumbService.doMovieThumb(movieId, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 获取被点赞的电影总数
     * @return Map<String, Integer>
     */
    @GetMapping("/movie/total/likes")
    public BaseResponse<Integer> getTotalLikedMovies() {
        int totalLikedMovies = movieThumbService.getTotalLikedMovies();
        return ResultUtils.success(totalLikedMovies);
    }
}
