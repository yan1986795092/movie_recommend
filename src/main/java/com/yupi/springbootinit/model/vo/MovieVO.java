package com.yupi.springbootinit.model.vo;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.yupi.springbootinit.model.entity.Movie;
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
    private static final long serialVersionUID = 1L;
    /**
     * 电影id
     */
    @TableId(type = IdType.AUTO)
    private Long movieId;
    /**
     * 创建用户id
     */
    private Long userId;
    /**
     * 电影名
     */
    private String name;
    /**
     * 导演
     */
    private String director;
    /**
     * 主演
     */
    private List<String> actors;
    /**
     * 类型
     */
    private String type;
    /**
     * 制片国家/地区
     */
    private String country;
    /**
     * 上映日期
     */
    private String releaseDate;
    /**
     * 片长
     */
    private String runtime;
    /**
     * 豆瓣评分
     */
    private String ratingNum;
    /**
     * 点赞数
     */
    private Integer thumbNum;
    /**
     * 收藏数
     */
    private Integer favourNum;
    /**
     * 创建人信息
     */
    private UserVO user;
    /**
     * 评论信息
     */
    private PostVO postVO;
    /**
     * 是否已点赞
     */
    private Boolean hasThumb;
    /**
     * 是否已收藏
     */
    private Boolean hasFavour;
    /**
     * 电影简介
     */
    private String tags;
    /**
     * 电影地址
     */
    private String movieUrl;
    /**
     * 照片本地地址
     */
    private String local;
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
        List<String> actorsList = movieVO.getActors();
        movie.setActors(JSONUtil.toJsonStr(actorsList));
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
        return movieVO;
    }
}
