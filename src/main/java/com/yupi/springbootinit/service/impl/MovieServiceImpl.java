package com.yupi.springbootinit.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.constant.CommonConstant;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.exception.ThrowUtils;
import com.yupi.springbootinit.mapper.MovieMapper;
import com.yupi.springbootinit.model.dto.movie.MovieQueryRequest;
import com.yupi.springbootinit.model.entity.Movie;
import com.yupi.springbootinit.model.entity.User;
import com.yupi.springbootinit.model.vo.MovieVO;
import com.yupi.springbootinit.model.vo.UserVO;
import com.yupi.springbootinit.service.MovieService;
import com.yupi.springbootinit.service.UserService;
import com.yupi.springbootinit.utils.SqlUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
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

        Long movieId = movieQueryRequest.getMovieId();
        String searchText = movieQueryRequest.getSearchText();
        String name = movieQueryRequest.getName();
        String actors = movieQueryRequest.getActors();
        List<String> typeList = movieQueryRequest.getType();
        String sortField = movieQueryRequest.getSortField();
        String sortOrder = movieQueryRequest.getSortOrder();

        // 拼接查询条件
        if (StringUtils.isNotBlank(searchText)) {
            queryWrapper.and(qw -> qw.like("name", searchText).or().like("actors", searchText));
        }
        queryWrapper.like(StringUtils.isNotBlank(name), "name", name);
        queryWrapper.like(StringUtils.isNotBlank(actors), "content", actors);

        if (CollUtil.isNotEmpty(typeList)) {
            for (String tag : typeList) {
                queryWrapper.like("type", "\"" + tag + "\"");
            }
        }
        queryWrapper.eq(ObjectUtils.isNotEmpty(movieId), "movieId", movieId);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }


    @Override
    public MovieVO getMovieVO(Movie movie, HttpServletRequest request) {
        MovieVO movieVO = MovieVO.objToVo(movie);
        long movieId = movie.getMovieId();
        // 1. 关联查询用户信息
        Long userId = movie.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        movieVO.setUser(userVO);
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

        // 填充信息
        List<MovieVO> movieVOList = movieList.stream().map(movie -> {
            MovieVO movieVO = MovieVO.objToVo(movie);
            Long userId = movie.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            movieVO.setUser(userService.getUserVO(user));

            return movieVO;
        }).collect(Collectors.toList());
        movieVOPage.setRecords(movieVOList);
        return movieVOPage;
    }

}
