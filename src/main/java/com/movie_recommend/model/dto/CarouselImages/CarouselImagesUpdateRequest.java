package com.movie_recommend.model.dto.CarouselImages;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * 轮播图
 */

@Data
public class CarouselImagesUpdateRequest implements Serializable {


    private List<String>  carouselImages;

    private static final long serialVersionUID = 1L;
}