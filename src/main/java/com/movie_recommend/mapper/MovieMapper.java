package com.movie_recommend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.movie_recommend.model.entity.Movie;

import java.util.Date;
import java.util.List;

/**
 * @author 86178
 * @description 针对表【movie(电影表)】的数据库操作Mapper
 * @createDate 2024-04-18 17:05:35
 * @Entity entity.model.com.movie_recommend.Movie
 */
public interface MovieMapper extends BaseMapper<Movie> {
    /**
     * 查询电影列表（包括已被删除的数据）
     */
    List<Movie> listMovieWithDelete(Date minUpdateTime);
}




