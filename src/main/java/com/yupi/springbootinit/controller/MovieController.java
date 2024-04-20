package com.yupi.springbootinit.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.springbootinit.common.BaseResponse;
import com.yupi.springbootinit.common.DeleteRequest;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.common.ResultUtils;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.exception.ThrowUtils;
import com.yupi.springbootinit.model.dto.movie.MovieAddRequest;
import com.yupi.springbootinit.model.dto.movie.MovieQueryRequest;
import com.yupi.springbootinit.model.dto.movie.MovieUpdateRequest;
import com.yupi.springbootinit.model.entity.Movie;
import com.yupi.springbootinit.model.entity.User;
import com.yupi.springbootinit.model.vo.MovieVO;
import com.yupi.springbootinit.service.MovieService;
import com.yupi.springbootinit.service.UserService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @Author yqq
 * @Date 2024/4/18 16:08
 * @Description 电影
 * @Version 1.0
 */
@RestController
@Slf4j
@RequestMapping("/movie")
public class MovieController {
    @Resource
    private MovieService movieService;

    @Resource
    private UserService userService;

    // region 增删改查

    /**
     * 创建
     * @param movieAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    // @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addMovie(@RequestBody MovieAddRequest movieAddRequest, HttpServletRequest request) {

        if (movieAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Movie movie = new Movie();
        BeanUtils.copyProperties(movieAddRequest, movie);
        List<String> actors = movieAddRequest.getActors();
        if (actors != null) {
            movie.setActors(JSONUtil.toJsonStr(actors));
        }
        movieService.validMovie(movie, true);
        User loginUser = userService.getLoginUser(request);
        movie.setUserId(loginUser.getId());
        movie.setFavourNum(0);
        movie.setThumbNum(0);
        boolean result = movieService.save(movie);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newMovieId = movie.getMovieId();
        return ResultUtils.success(newMovieId);
    }

    /**
     * 删除
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    //  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteMovie(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Movie oldMovie = movieService.getById(id);
        ThrowUtils.throwIf(oldMovie == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldMovie.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = movieService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     * @param movieUpdateRequest
     * @return
     */
    @PostMapping("/update")
  //  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateMovie(@RequestBody MovieUpdateRequest movieUpdateRequest) {
        if (movieUpdateRequest == null || movieUpdateRequest.getMovieId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Movie movie = new Movie();
        BeanUtils.copyProperties(movieUpdateRequest, movie);
        List<String> actors = movieUpdateRequest.getActors();
        if (actors != null) {
            movie.setType(JSONUtil.toJsonStr(actors));
        }
        // 参数校验
        movieService.validMovie(movie, false);
        long movieId = movieUpdateRequest.getMovieId();
        // 判断是否存在
        Movie oldMovie = movieService.getById(movieId);
        ThrowUtils.throwIf(oldMovie == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = movieService.updateById(movie);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<MovieVO> getMovieVOById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Movie movie = movieService.getById(id);
        if (movie == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(movieService.getMovieVO(movie, request));
    }

    /**
     * 分页获取列表（仅管理员）
     * @param movieQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<Movie>> listMovieByPage(@RequestBody MovieQueryRequest movieQueryRequest) {
        long current = movieQueryRequest.getCurrent();
        long size = movieQueryRequest.getPageSize();
        Page<Movie> moviePage = movieService.page(new Page<>(current, size),
                movieService.getQueryWrapper(movieQueryRequest));
        return ResultUtils.success(moviePage);
    }

    /**
     * 分页获取列表（封装类）
     * @param movieQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<MovieVO>> listMovieVOByPage(@RequestBody MovieQueryRequest movieQueryRequest,
                                                         HttpServletRequest request) {
        long current = movieQueryRequest.getCurrent();
        long size = movieQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Movie> moviePage = movieService.page(new Page<>(current, size),
                movieService.getQueryWrapper(movieQueryRequest));
        return ResultUtils.success(movieService.getMovieVOPage(moviePage, request));
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param movieQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<MovieVO>> listMyMovieVOByPage(@RequestBody MovieQueryRequest movieQueryRequest,
                                                                 HttpServletRequest request) {
        log.info("分页获取当前用户创建的资源列表:", movieQueryRequest);
        //判断前端请求
        if (movieQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //获取登录状态
        User loginUser = userService.getLoginUser(request);
        movieQueryRequest.setUserId(loginUser.getId());
        //判断当前页码和页面大小
        long current = movieQueryRequest.getCurrent();
        long size = movieQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        //分页查询
        Page<Movie> moviePage = movieService.page(new Page<>(current, size),
                movieService.getQueryWrapper(movieQueryRequest));
        return ResultUtils.success(movieService.getMovieVOPage(moviePage, request));
    }
}
