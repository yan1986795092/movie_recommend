package com.yupi.springbootinit.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.springbootinit.annotation.AuthCheck;
import com.yupi.springbootinit.common.BaseResponse;
import com.yupi.springbootinit.common.DeleteRequest;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.common.ResultUtils;
import com.yupi.springbootinit.constant.UserConstant;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.exception.ThrowUtils;
import com.yupi.springbootinit.model.dto.movie.MovieAddRequest;
import com.yupi.springbootinit.model.dto.movie.MovieQueryRequest;
import com.yupi.springbootinit.model.dto.movie.MovieUpdateRequest;
import com.yupi.springbootinit.model.dto.spider.SpiderRequest;
import com.yupi.springbootinit.model.entity.Movie;
import com.yupi.springbootinit.model.entity.User;
import com.yupi.springbootinit.model.vo.MovieVO;
import com.yupi.springbootinit.service.MovieService;
import com.yupi.springbootinit.service.UserService;
import com.yupi.springbootinit.utils.HttpUtils;
import com.yupi.springbootinit.utils.Spider;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import static com.yupi.springbootinit.utils.HttpUtils.doGetImage;

/**
 * @Author yqq
 * @Date 2024/4/18 16:08
 * @Description 电影
 * @Version 1.0
 */
@RestController
@Slf4j
@RequestMapping("/movie")
public class MovieController {
    @Resource
    private MovieService movieService;

    @Resource
    private UserService userService;


    @PostMapping("/spider")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> runSpider(@RequestBody SpiderRequest spiderRequest, HttpServletRequest request) throws URISyntaxException, IOException {
        if (spiderRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Spider spider = new Spider();
        //请求地址
        //https://movie.douban.com/j/search_subjects?type=movie&tag=%E7%83%AD%E9%97%A8&page_limit=50&page_start=0
        String url = "https://movie.douban.com/j/search_subjects";
        Map<String, String> map = new HashMap<>();
        Map<String, String> mapTitle = new HashMap<>();
        User loginUser = userService.getLoginUser(request);
        //设置请求参数
        map.put("type", "movie");
        map.put("tag", "冷门佳片");
        map.put("sort", "recommend");
        map.put("page_limit", "10");
        //设置请求头
        mapTitle.put("Accept", "*/*");
        mapTitle.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 Edg/123.0.0.0");
        mapTitle.put("Cookie", "bid=G1pcWQrJpYg; _pk_id.100001.4cf6=727f200318a7859e.1713174963.; __yadk_uid=OZyjR4dJSlBcTkJyLwjtBfhFbnasLStc; _vwo_uuid_v2=DBACDD4555EAAF89D5561DC48CE17CBDB|4d584d2dd850a199d43955daab29cf33; __utma=30149280.2064442863.1713174964.1713423666.1713506676.4; __utmc=30149280; __utmz=30149280.1713506676.4.3.utmcsr=cn.bing.com|utmccn=(referral)|utmcmd=referral|utmcct=/; __utmb=30149280.1.10.1713506676; _pk_ref.100001.4cf6=%5B%22%22%2C%22%22%2C1713506679%2C%22https%3A%2F%2Fwww.douban.com%2F%22%5D; _pk_ses.100001.4cf6=1; __utma=223695111.1797087261.1713174964.1713423666.1713506679.4; __utmb=223695111.0.10.1713506679; __utmc=223695111; __utmz=223695111.1713506679.4.3.utmcsr=douban.com|utmccn=(referral)|utmcmd=referral|utmcct=/; ap_v=0,6.0; __gads=ID=6e2348f87d357532:T=1713174964:RT=1713507496:S=ALNI_MZ271olrTvBjde2gyzWT9Hyd91SHg; __gpi=UID=00000ded6a185a71:T=1713174964:RT=1713507496:S=ALNI_MaBjeBeJSJxFYYFsvryVZlzaGaEjg; __eoi=ID=899ce58d0423512e:T=1713174964:RT=1713507496:S=AA-AfjbkbExeTBHcx1prfLxeYfRz");
        //获取前100条数据，可以自行更改

        for (int i = 0; i < spiderRequest.getNum(); i += 10) {
            map.put("page_start", i + "");
            String html = HttpUtils.doGetHtml(url, map, mapTitle);
            JSONObject jsonObject = JSONObject.parseObject(html);
            JSONArray jsonArray = jsonObject.getJSONArray("subjects");
            for (int j = 0; j < jsonArray.size(); j++) {  //循环遍历每页数据
                Movie movie = new Movie();
                JSONObject json = (JSONObject) jsonArray.get(j);
                movie.setName(json.getString("title"));
                movie.setMovieUrl(json.getString("url"));
                movie.setRatingNum(json.getString("rate"));
                movie.setUserId(loginUser.getId());
                //下载保存图片
                movie.setLocal(doGetImage(json.getString("cover")));

                String url2 = json.getString("url");
                Map<String, String> map2 = new HashMap<>();
                String html2 = HttpUtils.doGetHtml(url2, map2, mapTitle);
                //解析HTML获取DOM对象
                Document doc = Jsoup.parse(html2);
                //获取导演名称
                Element element = doc.select("div#info a[rel=v:directedBy]").first();
                if (element != null) {
                    movie.setDirector(element.text());
                } else {
                    // 处理元素为空的情况
                    movie.setDirector(null);
                }
                //获取主演
                Elements elements = doc.select("div#info a[rel=v:starring]");
                if (element != null) {
                    String Actors = "";
                    for (Element e : elements) {
                        Actors += e.text() + "，";
                    }
                    if (!Actors.equals("")) {
                        Actors = Actors.substring(0, Actors.length() - 1);
                    }
                    movie.setActors(Actors);
                } else {
                    // 处理元素为空的情况
                    movie.setActors(null);
                }

                //获取电影时长
                element = doc.select("div#info span[property=v:runtime]").first();
                if (element != null) {
                    movie.setRuntime(element.text());
                } else {
                    // 处理元素为空的情况
                    movie.setRuntime(null);
                }

                //获取国家
                element = doc.select("div#info span.pl:contains(制片国家/地区)").first();
                if (element != null) {
                    movie.setCountry(element.text());
                } else {
                    // 处理元素为空的情况
                    movie.setCountry(null);
                }

                //获取上映时间
                element = doc.select("div#info [property=v:initialReleaseDate]").first();
                if (element != null) {
                    movie.setReleaseDate(element.text());
                } else {
                    // 处理元素为空的情况
                    movie.setReleaseDate(null);
                }
                //获取类型
                element = doc.select("div#info [property=v:genre]").first();
                if (element != null) {
                    movie.setType(element.text());
                } else {
                    // 处理元素为空的情况
                    movie.setType(null);
                }
                //获取简介
                element = doc.select("div#link-report-intra [property=v:summary]").first();
                if (element != null) {
                    movie.setTags(element.text());
                } else {
                    // 处理元素为空的情况
                    movie.setTags(null);
                }
                movieService.save(movie);
            }
        }
        System.out.println("数据获取完成。。。");
        return ResultUtils.success(loginUser.getId());
    }

    // region 增删改查

    /**
     * 创建
     *
     * @param movieAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addMovie(@RequestBody MovieAddRequest movieAddRequest, HttpServletRequest request) {
        if (movieAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Movie movie = new Movie();
        BeanUtils.copyProperties(movieAddRequest, movie);
        movieService.validMovie(movie, true);
        User loginUser = userService.getLoginUser(request);
        movie.setUserId(loginUser.getId());
        movie.setFavourNum(0);
        movie.setThumbNum(0);
        boolean result = movieService.save(movie);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newMovieId = movie.getMovieId();
        return ResultUtils.success(newMovieId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteMovie(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Movie oldMovie = movieService.getById(id);
        ThrowUtils.throwIf(oldMovie == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldMovie.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = movieService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param movieUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateMovie(@RequestBody MovieUpdateRequest movieUpdateRequest) {
        if (movieUpdateRequest == null || movieUpdateRequest.getMovieId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Movie movie = new Movie();
        BeanUtils.copyProperties(movieUpdateRequest, movie);
        // 参数校验
        movieService.validMovie(movie, false);
        long movieId = movieUpdateRequest.getMovieId();
        // 判断是否存在
        Movie oldMovie = movieService.getById(movieId);
        ThrowUtils.throwIf(oldMovie == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = movieService.updateById(movie);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<MovieVO> getMovieVOById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Movie movie = movieService.getById(id);
        if (movie == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(movieService.getMovieVO(movie, request));
    }

    /**
     * 分页获取列表（仅管理员）
     *
     * @param movieQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<Movie>> listMovieByPage(@RequestBody MovieQueryRequest movieQueryRequest) {
        long current = movieQueryRequest.getCurrent();
        long size = movieQueryRequest.getPageSize();
        Page<Movie> moviePage = movieService.page(new Page<>(current, size),
                movieService.getQueryWrapper(movieQueryRequest));
        return ResultUtils.success(moviePage);
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param movieQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<MovieVO>> listMovieVOByPage(@RequestBody MovieQueryRequest movieQueryRequest,
                                                         HttpServletRequest request) {
        long current = movieQueryRequest.getCurrent();
        long size = movieQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Movie> moviePage = movieService.page(new Page<>(current, size),
                movieService.getQueryWrapper(movieQueryRequest));
        return ResultUtils.success(movieService.getMovieVOPage(moviePage, request));
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param movieQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<MovieVO>> listMyMovieVOByPage(@RequestBody MovieQueryRequest movieQueryRequest,
                                                           HttpServletRequest request) {
        log.info("分页获取当前用户创建的资源列表:", movieQueryRequest);
        //判断前端请求
        if (movieQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //获取登录状态
        User loginUser = userService.getLoginUser(request);
        movieQueryRequest.setUserId(loginUser.getId());
        //判断当前页码和页面大小
        long current = movieQueryRequest.getCurrent();
        long size = movieQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        //分页查询
        Page<Movie> moviePage = movieService.page(new Page<>(current, size),
                movieService.getQueryWrapper(movieQueryRequest));
        return ResultUtils.success(movieService.getMovieVOPage(moviePage, request));
    }
}
