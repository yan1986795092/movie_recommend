package com.yupi.springbootinit.model.dto.post;



import com.yupi.springbootinit.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;
/**
 * 查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class  PostQueryRequest extends PageRequest implements Serializable {

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
    private Long userid;

    /**
     * 内容
     */
    private String content;

    private static final long serialVersionUID = 1L;
}