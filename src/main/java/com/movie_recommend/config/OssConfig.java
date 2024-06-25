package com.movie_recommend.config;


import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OssConfig {

    @Value("${aliyun.oss.endpoint:}")
    private String endpoint;

    @Value("${aliyun.oss.accessKeyId:}")
    private String accessKeyId;

    @Value("${aliyun.oss.accessKeySecret:}")
    private String accessKeySecret;

    @Value("${aliyun.oss.bucketName:}")
    private String bucketName;

    @Bean
    @ConditionalOnProperty(name = "aliyun.oss.endpoint", havingValue = "")
    public OSS ossClient() {
        if (!endpoint.isEmpty() && !accessKeyId.isEmpty() && !accessKeySecret.isEmpty()) {
            return new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        }
        return null;
    }

    @Bean
    public String bucketName() {
        return bucketName;
    }
}