package com.movie_recommend.controller;

import cn.hutool.core.io.FileUtil;
import com.movie_recommend.common.BaseResponse;
import com.movie_recommend.common.ErrorCode;
import com.movie_recommend.common.ResultUtils;
import com.movie_recommend.constant.FileConstant;
import com.movie_recommend.exception.BusinessException;
import com.movie_recommend.manager.CosManager;
import com.movie_recommend.model.dto.file.UploadFileRequest;
import com.movie_recommend.model.entity.User;
import com.movie_recommend.model.enums.FileUploadBizEnum;
import com.movie_recommend.service.UserService;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件接口
 */
@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {

    @Resource
    private UserService userService;

    @Resource
    private CosManager cosManager;

    /**
     * 文件上传
     *
     * @param multipartFile
     * @param uploadFileRequest
     * @param request
     * @return
     */
    @PostMapping("/upload")
    public BaseResponse<String> uploadFile(@RequestPart("file") MultipartFile multipartFile,
                                           UploadFileRequest uploadFileRequest, HttpServletRequest request) {

        // 获取上传业务类型
        String biz = uploadFileRequest.getBiz();
        FileUploadBizEnum fileUploadBizEnum = FileUploadBizEnum.getEnumByValue(biz);

        // 检查业务类型是否有效
        if (fileUploadBizEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 验证文件是否符合要求
        validFile(multipartFile, fileUploadBizEnum);

        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);

        // 生成唯一文件名
        String uuid = RandomStringUtils.randomAlphanumeric(8);
        String filename = uuid + "-" + multipartFile.getOriginalFilename();

        // 文件路径格式：/业务类型/用户ID/文件名
        String filepath = String.format("/%s/%s/%s", fileUploadBizEnum.getValue(), loginUser.getId(), filename);

        File file = new File("C:\\Users\\86178\\Desktop\\电影推荐项目\\movie_recommend_front\\public" + filepath);  // 指定本地目录

        try {
            // 确保父目录存在
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }

            // 将上传的文件保存到本地文件
            multipartFile.transferTo(file);

            // 返回文件的可访问地址
            return ResultUtils.success(file.getAbsolutePath());
        } catch (IOException e) {
            // 记录上传错误并抛出业务异常
            log.error("file upload error, filepath = " + filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        }
    }


    /**
     * 校验文件
     *
     * @param multipartFile
     * @param fileUploadBizEnum 业务类型
     */
    private void validFile(MultipartFile multipartFile, FileUploadBizEnum fileUploadBizEnum) {
        // 文件大小
        long fileSize = multipartFile.getSize();
        // 文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        final long ONE_M = 1024 * 1024L;
        if (FileUploadBizEnum.USER_AVATAR.equals(fileUploadBizEnum)) {
            if (fileSize > ONE_M) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小不能超过 1M");
            }
            if (!Arrays.asList("jpeg", "jpg", "svg", "png", "webp").contains(fileSuffix)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件类型错误");
            }
        }
    }
}
