package com.movie_recommend.controller;

/**
 * @Author yqq
 * @Date 2024/5/18 14:48
 * @Description 评分
 * @Version 1.0
 */

import com.movie_recommend.common.BaseResponse;
import com.movie_recommend.common.ErrorCode;
import com.movie_recommend.common.ResultUtils;
import com.movie_recommend.exception.BusinessException;
import com.movie_recommend.model.dto.moviescore.MovieScoreAddRequest;
import com.movie_recommend.model.dto.moviethumb.MovieThumbAddRequest;
import com.movie_recommend.model.entity.User;
import com.movie_recommend.service.MovieScoreService;
import com.movie_recommend.service.MovieService;
import com.movie_recommend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 帖子点赞接口
 */
@RestController
@RequestMapping("/movie_score")
@Slf4j
public class MovieScoreController {

    @Resource
    private UserService userService;
    @Resource
    private MovieScoreService movieScoreService;

    @PostMapping("/")
    public BaseResponse<Integer> doMovieRating(@RequestBody MovieScoreAddRequest movieScoreAddRequest,
                                               HttpServletRequest request) {
        if (movieScoreAddRequest == null || movieScoreAddRequest.getMovieId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 登录才能评分
        final User loginUser = userService.getLoginUser(request);
        long movieId = movieScoreAddRequest.getMovieId();
        String rating = movieScoreAddRequest.getRating();
        int result = movieScoreService.doMovieRating(movieId, loginUser,rating);
        return ResultUtils.success(result);
    }

}
