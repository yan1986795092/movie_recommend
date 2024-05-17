package com.movie_recommend;

import com.movie_recommend.config.WxOpenConfig;
import com.movie_recommend.service.UserService;
import com.movie_recommend.service.MovieService;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * 主类测试
 */
@SpringBootTest
class MainApplicationTests {

    @Resource
    private WxOpenConfig wxOpenConfig;

    // @Test
    //void contextLoads() {
    //    System.out.println(wxOpenConfig);
    //}

    @Resource
    private MovieService movieService;

    @Resource
    private UserService userService;


}
