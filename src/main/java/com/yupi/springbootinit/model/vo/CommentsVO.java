package com.yupi.springbootinit.model.vo;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.yupi.springbootinit.model.entity.Comments;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.Date;
import java.util.List;

/**
 * @Author yqq
 * @Date 2024/4/19 21:28
 * @Description
 * @Version 1.0
 */
@Data
public class CommentsVO {
    /**
     * 评论id
     */
    private Long commentsId;

    /**
     * 电影id
     */
    private Long movieId;

    /**
     * 评论内容
     */
    private String commentInfo;

    /**
     * 评论者
     */
    private String commentAuthor;

    /**
     * 评论者头像
     */
    private String commentAuthorImgUrl;

    /**
     * 评论点赞数
     */
    private String commentVote;

    /**
     * 评论时间
     */
    private String commentDate;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
    
    
    public static Comments voToObj(CommentsVO commentsVO) {
        if (commentsVO == null) {
            return null;
        }
        Comments comments = new Comments();
        BeanUtils.copyProperties(commentsVO, comments);
        return comments;
    }
    /**
     * 对象转包装类
     *
     * @param comments
     * @return
     */
    public static CommentsVO objToVo(Comments comments) {
        if (comments == null) {
            return null;
        }
        CommentsVO commentsVO = new CommentsVO();
        BeanUtils.copyProperties(comments, commentsVO);
        return commentsVO;
    }
}
