package com.yupi.springbootinit.model.vo;

import cn.hutool.json.JSONUtil;
import com.yupi.springbootinit.model.entity.Movie;
import com.yupi.springbootinit.model.entity.Post;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 帖子视图
 */
@Data
public class PostVO implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * id
     */
    private Long id;
    /**
     * 电影id
     */
    private Long movieId;
    /**
     * 创建用户 id
     */
    private Long userId;
    /**
     * 内容
     */
    private String content;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 更新时间
     */
    private Date updateTime;
    /**
     * 是否删除
     */
    private Integer isDelete;
    /**
     * 电影封装类
     */
    private MovieVO movieVO;
    /**
     * 用户封装类
     */
    private UserVO userVO;


    /**
     * 包装类转对象
     * @param postVO
     * @return
     */
    public static Post voToObj(PostVO postVO) {
        if (postVO == null) {
            return null;
        }
        Post post = new Post();
        BeanUtils.copyProperties(postVO, post);

        return post;
    }

    /**
     * 对象转包装类
     *
     * @param post
     * @return
     */
    public static PostVO objToVo(Post post) {
        if (post == null) {
            return null;
        }
        PostVO postVO = new PostVO();
        BeanUtils.copyProperties(post, postVO);
        return postVO;
    }
}
