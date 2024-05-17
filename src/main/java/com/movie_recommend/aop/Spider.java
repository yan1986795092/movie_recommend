package com.movie_recommend.aop;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.movie_recommend.model.entity.Movie;
import com.movie_recommend.service.UserService;
import com.movie_recommend.utils.HttpUtils;
import com.movie_recommend.service.MovieService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author yqq
 * @Date 2024/4/18 9:52
 * @Description 网络爬虫  参考网站http://webmagic.io/docs/
 * @Version 1.0
 */

@Slf4j
public class Spider {
    @Resource
    private MovieService movieService;

    @Resource
    private UserService userService;

    public void contextLoads(String url, Map<String, String> map, Map<String, String> mapTitle, int i) throws URISyntaxException, IOException {

        //获取前100条数据，可以自行更改
        for ( i = 0; i < 20; i += 10) {
            map.put("page_start", i + "");
            String html = HttpUtils.doGetHtml(url, map, mapTitle);
            JSONObject jsonObject = JSONObject.parseObject(html);
            JSONArray jsonArray = jsonObject.getJSONArray("subjects");
            for (int j = 0; j < jsonArray.size(); j++) {  //循环遍历每页数据
                Movie movie = new Movie();
                JSONObject json = (JSONObject) jsonArray.get(j);
                movie.setName(json.getString("title"));
                movie.setMovieUrl(json.getString("url"));
                movie.setLocal(json.getString("cover"));
                movie.setRatingNum(json.getString("rate"));

                //下载保存图片
                HttpUtils.doGetImage(json.getString("cover"));

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
                element = doc.select("div#link-report-intra [property=v:summary]").first();
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
    }
}
