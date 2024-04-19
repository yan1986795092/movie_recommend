package com.yupi.springbootinit.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.constant.CommonConstant;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.exception.ThrowUtils;
import com.yupi.springbootinit.mapper.CommentsMapper;
import com.yupi.springbootinit.model.entity.Comments;
import com.yupi.springbootinit.model.entity.Movie;
import com.yupi.springbootinit.model.entity.User;
import com.yupi.springbootinit.model.vo.CommentsVO;
import com.yupi.springbootinit.model.vo.UserVO;
import com.yupi.springbootinit.service.CommentsService;
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
 * @author 86178
 * @description 针对表【comments(影评表)】的数据库操作Service实现
 * @createDate 2024-04-19 20:43:19
 */
@Service
public class CommentsServiceImpl extends ServiceImpl<CommentsMapper, Comments>
        implements CommentsService {

    @Resource
    private UserService userService;

    @Resource
    private MovieService movieService;
    @Override
    public void validComments(Comments comments, boolean add) {
        if (comments == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String commentInfo = comments.getCommentInfo();
        String commentAuthor = comments.getCommentAuthor();

        // 创建时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StringUtils.isAnyBlank(commentInfo,commentAuthor), ErrorCode.PARAMS_ERROR);
        }
        // 有参数则校验
        if (StringUtils.isNotBlank(commentAuthor) && commentAuthor.length() > 80) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "名字过长");
        }
    }


    @Override
    public CommentsVO getCommentsVO(Comments comments, HttpServletRequest request) {
        CommentsVO commentsVO = CommentsVO.objToVo(comments);
        long commentsId = comments.getCommentsId();
        // 1. 关联查询用户信息
        Long movieId = comments.getMovieId();
        Movie Movie = null;
        if (Id != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        commentsVO.setUser(userVO);
        return commentsVO;
    }

    @Override
    public Page<CommentsVO> getCommentsVOPage(Page<Comments> commentsPage, HttpServletRequest request) {
        List<Comments> commentsList = commentsPage.getRecords();
        Page<CommentsVO> commentsVOPage = new Page<>(commentsPage.getCurrent(), commentsPage.getSize(), commentsPage.getTotal());
        if (CollUtil.isEmpty(commentsList)) {
            return commentsVOPage;
        }
        // 1. 关联查询用户信息
        Set<Long> userIdSet = commentsList.stream().map(Comments::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));

        // 填充信息
        List<CommentsVO> commentsVOList = commentsList.stream().map(comments -> {
            CommentsVO commentsVO = CommentsVO.objToVo(comments);
            Long userId = comments.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            commentsVO.setUser(userService.getUserVO(user));

            return commentsVO;
        }).collect(Collectors.toList());
        commentsVOPage.setRecords(commentsVOList);
        return commentsVOPage;
    }

}




