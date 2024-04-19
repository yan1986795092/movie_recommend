package com.yupi.springbootinit;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yupi.springbootinit.config.WxOpenConfig;
import com.yupi.springbootinit.model.entity.Movie;
import com.yupi.springbootinit.service.MovieService;
import com.yupi.springbootinit.utils.HttpUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * 主类测试
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
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

    @Test
    public void contextLoads() throws URISyntaxException, IOException {
        //请求地址
        //https://movie.douban.com/j/search_subjects?type=movie&tag=%E7%83%AD%E9%97%A8&page_limit=50&page_start=0
        String url = "https://movie.douban.com/j/search_subjects";
        Map<String, String> map = new HashMap<>();
        Map<String, String> mapTitle = new HashMap<>();
        //设置请求参数
        map.put("type", "movie");
        map.put("tag", "热门");
        map.put("sort", "recommend");
        map.put("page_limit", "20");
        //设置请求头
        mapTitle.put("Accept", "*/*");
        mapTitle.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 Edg/123.0.0.0");
        mapTitle.put("Cookie", "bid=G1pcWQrJpYg; _pk_id.100001.4cf6=727f200318a7859e.1713174963.; __yadk_uid=OZyjR4dJSlBcTkJyLwjtBfhFbnasLStc; _vwo_uuid_v2=DBACDD4555EAAF89D5561DC48CE17CBDB|4d584d2dd850a199d43955daab29cf33; __utma=30149280.2064442863.1713174964.1713423666.1713506676.4; __utmc=30149280; __utmz=30149280.1713506676.4.3.utmcsr=cn.bing.com|utmccn=(referral)|utmcmd=referral|utmcct=/; __utmb=30149280.1.10.1713506676; _pk_ref.100001.4cf6=%5B%22%22%2C%22%22%2C1713506679%2C%22https%3A%2F%2Fwww.douban.com%2F%22%5D; _pk_ses.100001.4cf6=1; __utma=223695111.1797087261.1713174964.1713423666.1713506679.4; __utmb=223695111.0.10.1713506679; __utmc=223695111; __utmz=223695111.1713506679.4.3.utmcsr=douban.com|utmccn=(referral)|utmcmd=referral|utmcct=/; ap_v=0,6.0; __gads=ID=6e2348f87d357532:T=1713174964:RT=1713507496:S=ALNI_MZ271olrTvBjde2gyzWT9Hyd91SHg; __gpi=UID=00000ded6a185a71:T=1713174964:RT=1713507496:S=ALNI_MaBjeBeJSJxFYYFsvryVZlzaGaEjg; __eoi=ID=899ce58d0423512e:T=1713174964:RT=1713507496:S=AA-AfjbkbExeTBHcx1prfLxeYfRz");
        //获取前100条数据，可以自行更改

        for (int i = 0; i < 100; i += 10) {
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
                movie.setDirector(element.text());
                Elements elements = doc.select("div#info a[rel=v:starring]");
                //主演
                String Actors = "";
                for (Element e : elements) {
                    Actors += e.text() + "，";
                }
                if (!Actors.equals("")) {
                    Actors = Actors.substring(0, Actors.length() - 1);
                }
                movie.setActors(Actors);
                //获取电影时长
                element = doc.select("div#info span[property=v:runtime]").first();
                movie.setRuntime(element.text());
                //获取国家
                element = doc.select("div#info [property=v:initialReleaseDate]").first();
                movie.setCountry(element.text());
                //获取类型
                element = doc.select("div#info [property=v:genre]").first();
                movie.setType(element.text());
                //获取简介
                element = doc.select("div#link-report-intra [property=v:summary]").first();
                movie.setType(element.text());
                movieService.save(movie);
            }
        }
        System.out.println("数据获取完成。。。");
    }

}
