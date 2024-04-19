package com.yupi.springbootinit.model.vo;

import cn.hutool.json.JSONUtil;
import com.yupi.springbootinit.model.entity.Movie;
import com.yupi.springbootinit.model.entity.Post;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.Date;
import java.util.List;

/**
 * @Author yqq
 * @Date 2024/4/18 16:19
 * @Description 包装类
 * @Version 1.0
 */
@Data
public class MovieVO {
    /**
     * 电影id
     */
    private Long movieId;

    /**
     * 电影名
     */
    private String name;

    /**
     * 导演
     */
    private String director;

    /**
     * 编剧
     */
    private String scenarist;

    /**
     * 主演
     */
    private String actors;

    /**
     * 类型
     */
    private List<String> type;

    /**
     * 制片国家/地区
     */
    private String country;


    /**
     * 片长
     */
    private String runtime;

    /**
     * 豆瓣评分
     */
    private String ratingNum;

    /**
     * 电影简介
     */
    private String tags;

    /**
     * 创建用户 id
     */

    private Long userId;

    /**
     * 创建人信息
     */
    private UserVO user;
    /**
     * 评论信息
     */
    private CommentsVO commentsVO;
    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    public static Movie voToObj(MovieVO movieVO) {
        if (movieVO == null) {
            return null;
        }
       Movie movie = new Movie();
        BeanUtils.copyProperties(movieVO, movie);
        List<String> typeList = movieVO.getType();
        movie.setTags(JSONUtil.toJsonStr(typeList));
        return movie;
    }
    /**
     * 对象转包装类
     *
     * @param movie
     * @return
     */
    public static MovieVO objToVo(Movie movie) {
        if (movie == null) {
            return null;
        }
        MovieVO movieVO = new MovieVO();
        BeanUtils.copyProperties(movie, movieVO);
        movieVO.setType(JSONUtil.toList(movie.getType(), String.class));
        return movieVO;
    }
}
