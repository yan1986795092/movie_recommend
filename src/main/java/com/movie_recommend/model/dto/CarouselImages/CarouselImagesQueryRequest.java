package com.movie_recommend.model.dto.CarouselImages;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.movie_recommend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;


/**
 * @Author yqq
 * @Date 2024/4/18 12:47
 * @Description 查询轮播图
 * @Version 1.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CarouselImagesQueryRequest extends PageRequest implements Serializable {

    /**
     * id
     */

    private Long id;

    /**
     * 内容
     */
    private String carouselImages;


    private static final long serialVersionUID = 1L;

}
