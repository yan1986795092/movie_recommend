package com.yupi.springbootinit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yupi.springbootinit.model.entity.Movie;
import com.yupi.springbootinit.model.entity.Post;

import java.util.Date;
import java.util.List;

/**
 * @author 86178
 * @description 针对表【movie(电影表)】的数据库操作Mapper
 * @createDate 2024-04-18 17:05:35
 * @Entity com.yupi.springbootinit.model.entity.Movie
 */
public interface MovieMapper extends BaseMapper<Movie> {
    /**
     * 查询电影列表（包括已被删除的数据）
     */
    List<Movie> listMovieWithDelete(Date minUpdateTime);
}




