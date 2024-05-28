package com.movie_recommend.controller;

/**
 * @Author yqq
 * @Date 2024/5/18 14:48
 * @Description 评分
 * @Version 1.0
 */

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.movie_recommend.common.BaseResponse;
import com.movie_recommend.common.ErrorCode;
import com.movie_recommend.common.ResultUtils;
import com.movie_recommend.exception.BusinessException;
import com.movie_recommend.exception.ThrowUtils;
import com.movie_recommend.model.dto.CarouselImages.CarouselImagesQueryRequest;
import com.movie_recommend.model.dto.CarouselImages.CarouselImagesUpdateRequest;
import com.movie_recommend.model.dto.moviefavour.MovieFavourQueryRequest;
import com.movie_recommend.model.entity.CarouselImages;
import com.movie_recommend.model.entity.Movie;
import com.movie_recommend.model.entity.User;
import com.movie_recommend.model.vo.MovieVO;
import com.movie_recommend.service.CarouselImagesService;
import com.movie_recommend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 帖子点赞接口
 */
@RestController
@RequestMapping("/carousel_images")
@Slf4j
public class CarouselImagesController {

    @Resource
    private UserService userService;
    @Resource
    private CarouselImagesService carouselImagesService;

    /**
     * 储存轮播图
     * @param carouselImagesUpdateRequest
     * @param httpServletRequest
     * @return
     */
    @PostMapping("/")
    public BaseResponse<Integer> doCarouselImage(@RequestBody CarouselImagesUpdateRequest carouselImagesUpdateRequest, HttpServletRequest httpServletRequest) {
        if (carouselImagesUpdateRequest == null || carouselImagesUpdateRequest.getCarouselImages() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 登录才能操作
        final User loginUser = userService.getLoginUser(httpServletRequest);
        List<String> carouselImages = carouselImagesUpdateRequest.getCarouselImages();
        log.info("Received carousel images: {}", carouselImages);

        // 删除旧数据
        boolean removed = carouselImagesService.remove(null);
        log.info("Old carousel images removed: {}", removed);

        // 插入新数据
        int insertCount = 0;
        for (String imageUrl : carouselImages) {
            String processedImageUrl = processImageUrl(imageUrl);
            CarouselImages entity = new CarouselImages();
            entity.setId(IdWorker.getId());  // 使用MyBatis Plus的IdWorker生成ID
            entity.setCarouselImages(processedImageUrl);

            log.info("imageUrl: {}", processedImageUrl);
            log.info("保存前实体: {}", entity);

            // 插入数据
            boolean result = carouselImagesService.save(entity);
            if (result) {
                log.info("保存后实体: {}", entity);
                insertCount++;
            } else {
                log.error("Failed to insert entity: {}", entity);
            }
        }

        return ResultUtils.success(insertCount);
    }

    /**
     * 处理路径
     * @param imageUrl
     * @return
     */
    private String processImageUrl(String imageUrl) {
        // 只保留public后的部分，并将反斜杠替换为正斜杠
        int index = imageUrl.indexOf("public");
        if (index != -1) {
            imageUrl = imageUrl.substring(index + "public".length());
        }
        return imageUrl.replace("\\", "/");
    }

    @PostMapping("/list/page")
    public BaseResponse<List<CarouselImages>> listAllCarouselImages(HttpServletRequest httpServletRequest) {

        // 登录才能操作
        final User loginUser = userService.getLoginUser(httpServletRequest);

        // 查询条件
        QueryWrapper<CarouselImages> queryWrapper = new QueryWrapper<>();
        // 如果需要可以添加具体查询条件
        // queryWrapper.eq("someColumn", carouselImagesQueryRequest.getSomeValue());

        // 查询所有数据
        List<CarouselImages> resultList = carouselImagesService.list(queryWrapper);

        return ResultUtils.success(resultList);
    }
}
