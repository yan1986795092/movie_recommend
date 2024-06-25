package com.movie_recommend.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.PutObjectRequest;
import com.movie_recommend.service.OssService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URL;
import java.util.Date;
/**
 * @Author yqq
 * @Date 2024/6/25 11:56
 * @Description
 * @Version 1.0
 */
@Service
public class OssServiceImpl implements OssService {
    @Autowired
    private OSS ossClient;

    @Value("${aliyun.oss.bucketName}")
    private String bucketName;

    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    public String uploadFile(MultipartFile file, String filePath) {
        try {
            InputStream inputStream = file.getInputStream();
            String fileName = filePath + "/" + file.getOriginalFilename();
            ossClient.putObject(new PutObjectRequest(bucketName, fileName, inputStream));
            return fileName;
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file to OSS", e);
        }
    }

    public String getFileUrl(String fileName) {
        Date expiration = new Date(System.currentTimeMillis() + 3600L * 1000 * 24 * 365 * 10); // 设置URL过期时间为10年
        URL url = ossClient.generatePresignedUrl(bucketName, fileName, expiration);
        return url.toString();
    }
}
