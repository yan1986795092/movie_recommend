package com.yupi.springbootinit.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 影评表
 * @TableName comments
 */
@TableName(value ="comments")
@Data
public class Comments implements Serializable {
    /**
     * 评论id
     */
    @TableId(type = IdType.AUTO)
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

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}