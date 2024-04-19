package com.yupi.springbootinit.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.springbootinit.model.entity.Comments;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.springbootinit.model.entity.Comments;
import com.yupi.springbootinit.model.vo.CommentsVO;

import javax.servlet.http.HttpServletRequest;

/**
* @author 86178
* @description 针对表【comments(影评表)】的数据库操作Service
* @createDate 2024-04-19 20:43:19
*/
public interface CommentsService extends IService<Comments> {
    /**
     * 校验
     *
     * @param comments
     * @param add
     */
    void validComments(Comments comments, boolean add);

    /**
     * 获取电影封装
     *
     * @param comments
     * @param request
     * @return
     */
    CommentsVO getCommentsVO(Comments comments, HttpServletRequest request);

    /**
     * 分页获取电影封装
     *
     * @param commentsPage
     * @param request
     * @return
     */
    Page<CommentsVO> getCommentsVOPage(Page<Comments> commentsPage, HttpServletRequest request);
}
