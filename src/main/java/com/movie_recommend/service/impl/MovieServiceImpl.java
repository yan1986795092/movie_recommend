package com.movie_recommend.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.movie_recommend.common.ErrorCode;
import com.movie_recommend.constant.CommonConstant;
import com.movie_recommend.exception.BusinessException;
import com.movie_recommend.mapper.MovieFavourMapper;
import com.movie_recommend.mapper.MovieMapper;
import com.movie_recommend.mapper.MovieThumbMapper;
import com.movie_recommend.model.entity.Movie;
import com.movie_recommend.model.entity.MovieFavour;
import com.movie_recommend.model.entity.MovieThumb;
import com.movie_recommend.model.entity.User;
import com.movie_recommend.model.vo.MovieVO;
import com.movie_recommend.model.vo.UserVO;
import com.movie_recommend.service.UserService;
import com.movie_recommend.utils.SqlUtils;
import com.movie_recommend.exception.ThrowUtils;
import com.movie_recommend.model.dto.movie.MovieQueryRequest;
import com.movie_recommend.service.MovieService;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author yqq
 * @Date 2024/4/18 13:25
 * @Description 爬虫
 * @Version 1.0
 */
@Service
public class MovieServiceImpl extends ServiceImpl<MovieMapper, Movie>
        implements MovieService {

    @Resource
    private UserService userService;

    @Resource
    private MovieThumbMapper movieThumbMapper;

    @Resource
    private MovieFavourMapper movieFavourMapper;

    @Override
    public void validMovie(Movie movie, boolean add) {
        if (movie == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String name = movie.getName();
        String director = movie.getDirector();
        String actors = movie.getActors();

        // 创建时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StringUtils.isAnyBlank(name, director, actors), ErrorCode.PARAMS_ERROR);
        }
        // 有参数则校验
        if (StringUtils.isNotBlank(name) && name.length() > 80) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "名字过长");
        }
    }

    /**
     * 获取查询包装类
     *
     * @param movieQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Movie> getQueryWrapper(MovieQueryRequest movieQueryRequest) {
        QueryWrapper<Movie> queryWrapper = new QueryWrapper<>();
        if (movieQueryRequest == null) {
            return queryWrapper;
        }
        String searchText = movieQueryRequest.getSearchText();
        String sortField = movieQueryRequest.getSortField();
        String sortOrder = movieQueryRequest.getSortOrder();

        Long userId = movieQueryRequest.getUserId();
        Long movieId = movieQueryRequest.getMovieId();
        String name = movieQueryRequest.getName();
        String director = movieQueryRequest.getDirector();
        List<String> actorsList = movieQueryRequest.getActors();


        // 拼接查询条件
        if (StringUtils.isNotBlank(searchText)) {
            queryWrapper.and(qw -> qw.like("name", searchText).or().like("actors", searchText));
        }
        queryWrapper.like(StringUtils.isNotBlank(name), "name", name);
        queryWrapper.like(StringUtils.isNotBlank(director), "content", director);

        if (CollUtil.isNotEmpty(actorsList)) {
            for (String tag : actorsList) {
                queryWrapper.like("type", "\"" + tag + "\"");
            }
        }
        queryWrapper.eq(ObjectUtils.isNotEmpty(movieId), "movieId", movieId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }


    @Override
    public MovieVO getMovieVO(Movie movie, HttpServletRequest request) {
        MovieVO movieVO = MovieVO.objToVo(movie);
        // 1. 关联查询用户信息
        long movieId = movie.getMovieId();
        Long userId = movie.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        movieVO.setUser(userVO);


        // 2. 已登录，获取用户点赞、收藏状态
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            // 获取点赞
            QueryWrapper<MovieThumb> movieThumbQueryWrapper = new QueryWrapper<>();
            movieThumbQueryWrapper.in("movieId", movieId);
            movieThumbQueryWrapper.eq("userId", loginUser.getId());
            MovieThumb movieThumb = movieThumbMapper.selectOne(movieThumbQueryWrapper);
            movieVO.setHasThumb(movieThumb != null);
            // 获取收藏
            QueryWrapper<MovieFavour> movieFavourQueryWrapper = new QueryWrapper<>();
            movieFavourQueryWrapper.in("movieId", movieId);
            movieFavourQueryWrapper.eq("userId", loginUser.getId());
            MovieFavour movieFavour = movieFavourMapper.selectOne(movieFavourQueryWrapper);
            movieVO.setHasFavour(movieFavour != null);
        }
        return movieVO;
    }

    @Override
    public Page<MovieVO> getMovieVOPage(Page<Movie> moviePage, HttpServletRequest request) {
        List<Movie> movieList = moviePage.getRecords();
        Page<MovieVO> movieVOPage = new Page<>(moviePage.getCurrent(), moviePage.getSize(), moviePage.getTotal());
        if (CollUtil.isEmpty(movieList)) {
            return movieVOPage;
        }
        // 1. 关联查询用户信息
        Set<Long> userIdSet = movieList.stream().map(Movie::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 已登录，获取用户点赞、收藏状态
        Map<Long, Boolean> movieIdHasThumbMap = new HashMap<>();
        Map<Long, Boolean> movieIdHasFavourMap = new HashMap<>();
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            Set<Long> movieIdSet = movieList.stream().map(Movie::getMovieId).collect(Collectors.toSet());
            loginUser = userService.getLoginUser(request);
            // 获取点赞
            QueryWrapper<MovieThumb> movieThumbQueryWrapper = new QueryWrapper<>();
            movieThumbQueryWrapper.in("movieId", movieIdSet);
            movieThumbQueryWrapper.eq("userId", loginUser.getId());
            List<MovieThumb> movieMovieThumbList = movieThumbMapper.selectList(movieThumbQueryWrapper);
            movieMovieThumbList.forEach(movieMovieThumb -> movieIdHasThumbMap.put(movieMovieThumb.getMovieId(), true));
            // 获取收藏
            QueryWrapper<MovieFavour> movieFavourQueryWrapper = new QueryWrapper<>();
            movieFavourQueryWrapper.in("movieId", movieIdSet);
            movieFavourQueryWrapper.eq("userId", loginUser.getId());
            List<MovieFavour> movieFavourList = movieFavourMapper.selectList(movieFavourQueryWrapper);
            movieFavourList.forEach(movieFavour -> movieIdHasFavourMap.put(movieFavour.getMovieId(), true));
        }
        // 填充信息
        List<MovieVO> movieVOList = movieList.stream().map(movie -> {
            MovieVO movieVO = MovieVO.objToVo(movie);
            Long userId = movie.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            movieVO.setUser(userService.getUserVO(user));
            movieVO.setHasThumb(movieIdHasThumbMap.getOrDefault(movie.getMovieId(), false));
            movieVO.setHasFavour(movieIdHasFavourMap.getOrDefault(movie.getMovieId(), false));
            return movieVO;
        }).collect(Collectors.toList());
        movieVOPage.setRecords(movieVOList);
        return movieVOPage;
    }

}
