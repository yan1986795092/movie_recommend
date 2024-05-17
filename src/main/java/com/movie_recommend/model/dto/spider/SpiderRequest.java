package com.movie_recommend.model.dto.spider;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author yqq
 * @Date 2024/4/20 19:05
 * @Description 爬虫
 * @Version 1.0
 */
@Data
public class SpiderRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long userId;

    private int num;

}
