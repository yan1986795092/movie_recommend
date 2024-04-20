package com.yupi.springbootinit.model.dto.post;

import java.io.Serializable;
import java.util.List;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * 编辑请求
 */
@Data
public class PostEditRequest implements Serializable {

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

    private static final long serialVersionUID = 1L;
}