package com.yupi.springbootinit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.mapper.MovieThumbMapper;
import com.yupi.springbootinit.model.entity.MovieThumb;
import com.yupi.springbootinit.model.entity.Movie;
import com.yupi.springbootinit.model.entity.User;
import com.yupi.springbootinit.service.MovieService;
import com.yupi.springbootinit.service.MovieThumbService;
import javax.annotation.Resource;
import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 电影点赞服务实现
 */
@Service
public class MovieThumbServiceImpl extends ServiceImpl<MovieThumbMapper, MovieThumb>
        implements MovieThumbService {

    @Resource
    private MovieService movieService;

    /**
     * 点赞
     *
     * @param movieId
     * @param loginUser
     * @return
     */
    @Override
    public int doMovieThumb(long movieId, User loginUser) {
        // 判断实体是否存在，根据类别获取实体
        Movie movie = movieService.getById(movieId);
        if (movie == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 是否已点赞
        long userId = loginUser.getId();
        // 每个用户串行点赞
        // 锁必须要包裹住事务方法
        MovieThumbService movieThumbService = (MovieThumbService) AopContext.currentProxy();
        synchronized (String.valueOf(userId).intern()) {
            return movieThumbService.doMovieThumbInner(userId, movieId);
        }
    }

    /**
     * 封装了事务的方法
     * @param userId
     * @param movieId
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int doMovieThumbInner(long userId, long movieId) {
        MovieThumb movieThumb = new MovieThumb();
        movieThumb.setUserId(userId);
        movieThumb.setMovieId(movieId);
        QueryWrapper<MovieThumb> thumbQueryWrapper = new QueryWrapper<>(movieThumb);
        MovieThumb oldMovieThumb = this.getOne(thumbQueryWrapper);
        boolean result;
        // 已点赞
        if (oldMovieThumb != null) {
            result = this.remove(thumbQueryWrapper);
            if (result) {
                // 点赞数 - 1
                result = movieService.update()
                        .eq("movieId", movieId)
                        .gt("thumbNum", 0)
                        .setSql("thumbNum = thumbNum - 1")
                        .update();
                return result ? -1 : 0;
            } else {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
        } else {
            // 未点赞
            result = this.save(movieThumb);
            if (result) {
                // 点赞数 + 1
                result = movieService.update()
                        .eq("movieId", movieId)
                        .set("thumbNum" ,1)
                        .update();
                return result ? 1 : 0;
            } else {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
        }
    }

}




