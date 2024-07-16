package com.movie_recommend.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hankcs.hanlp.HanLP;
import com.movie_recommend.common.ErrorCode;
import com.movie_recommend.constant.CommonConstant;
import com.movie_recommend.MovieEsRepository.MovieEsRepository;
import com.movie_recommend.exception.BusinessException;
import com.movie_recommend.exception.ThrowUtils;
import com.movie_recommend.mapper.MovieFavourMapper;
import com.movie_recommend.mapper.MovieMapper;
import com.movie_recommend.mapper.MovieScoreMapper;
import com.movie_recommend.mapper.MovieThumbMapper;
import com.movie_recommend.model.dto.movie.MovieEsDTO;
import com.movie_recommend.model.dto.movie.MovieQueryRequest;
import com.movie_recommend.model.entity.*;
import com.movie_recommend.model.vo.MovieVO;
import com.movie_recommend.model.vo.UserVO;
import com.movie_recommend.service.MovieService;
import com.movie_recommend.service.UserService;
import com.movie_recommend.utils.SqlUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Author yqq
 * @Date 2024/4/18 13:25
 * @Description 爬虫
 * @Version 1.0
 */
@Service
public class MovieServiceImpl extends ServiceImpl<MovieMapper, Movie>
        implements MovieService {

    @Resource
    private UserService userService;
    @Resource
    private MovieMapper movieMapper;
    @Resource
    private MovieThumbMapper movieThumbMapper;
    @Resource
    private MovieFavourMapper movieFavourMapper;
    @Resource
    private MovieScoreMapper movieScoreMapper;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private MovieEsRepository movieEsRepository;
    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;
    private static final String MOVIE_COUNT_KEY = "movie_count";

    private static final String RECOMMENDATION_KEY_PREFIX = "recommendation_";

    private static final long CACHE_EXPIRATION = 10; // 缓存过期时间设置为10
    @Override
    public void validMovie(Movie movie, boolean add) {
        if (movie == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String name = movie.getName();
        String director = movie.getDirector();
        String actors = movie.getActors();

        // 创建时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StringUtils.isAnyBlank(name, director, actors), ErrorCode.PARAMS_ERROR);
        }
        // 有参数则校验
        if (StringUtils.isNotBlank(name) && name.length() > 80) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "名字过长");
        }
    }

    /**
     * 获取查询包装类
     *
     * @param movieQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Movie> getQueryWrapper(MovieQueryRequest movieQueryRequest) {
        QueryWrapper<Movie> queryWrapper = new QueryWrapper<>();
        if (movieQueryRequest == null) {
            return queryWrapper;
        }
        String searchText = movieQueryRequest.getSearchText();
        String sortField = movieQueryRequest.getSortField();
        String sortOrder = movieQueryRequest.getSortOrder();

        Long userId = movieQueryRequest.getUserId();
        Long movieId = movieQueryRequest.getMovieId();
        String name = movieQueryRequest.getName();
        String director = movieQueryRequest.getDirector();
        List<String> actorsList = movieQueryRequest.getActors();


        // 拼接查询条件
        if (StringUtils.isNotBlank(searchText)) {
            queryWrapper.and(qw -> qw.like("name", searchText).or().like("actors", searchText));
        }
        queryWrapper.like(StringUtils.isNotBlank(name), "name", name);
        queryWrapper.like(StringUtils.isNotBlank(director), "content", director);

        if (CollUtil.isNotEmpty(actorsList)) {
            for (String tag : actorsList) {
                queryWrapper.like("type", "\"" + tag + "\"");
            }
        }
        queryWrapper.eq(ObjectUtils.isNotEmpty(movieId), "movieId", movieId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public void saveMovieToEs(MovieEsDTO movie) {
        movieEsRepository.save(movie);
    }

    @Override
    public List<MovieEsDTO> searchMovies(String keyword) {
        Query searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery(keyword, "name", "director"))
                .build();
        SearchHits<MovieEsDTO> searchHits = elasticsearchRestTemplate.search(searchQuery, MovieEsDTO.class);
        return searchHits.getSearchHits().stream().map(hit -> hit.getContent()).collect(Collectors.toList());
    }

    @Override
    public void syncMoviesToEs() {
        List<Movie> movies = movieMapper.selectList(null);
        for (Movie movie : movies) {
            MovieEsDTO movieEsDTO = new MovieEsDTO();
            movieEsDTO.setMovieId(movie.getMovieId());
            movieEsDTO.setName(movie.getName());
            movieEsDTO.setDirector(movie.getDirector());
            saveMovieToEs(movieEsDTO);
        }
    }

    @Override
    public void syncMovieToEs(Movie movie) {
        MovieEsDTO movieEsDTO = new MovieEsDTO();
        movieEsDTO.setMovieId(movie.getMovieId());
        movieEsDTO.setName(movie.getName());
        movieEsDTO.setDirector(movie.getDirector());
        saveMovieToEs(movieEsDTO);
    }

    @Override
    public MovieVO getMovieVO(Movie movie, HttpServletRequest request) {
        MovieVO movieVO = MovieVO.objToVo(movie);
        // 1. 关联查询用户信息
        long movieId = movie.getMovieId();
        Long userId = movie.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        movieVO.setUser(userVO);


        // 2. 已登录，获取用户点赞、收藏状态
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            // 获取点赞
            QueryWrapper<MovieThumb> movieThumbQueryWrapper = new QueryWrapper<>();
            movieThumbQueryWrapper.in("movieId", movieId);
            movieThumbQueryWrapper.eq("userId", loginUser.getId());
            MovieThumb movieThumb = movieThumbMapper.selectOne(movieThumbQueryWrapper);
            movieVO.setHasThumb(movieThumb != null);
            // 获取收藏
            QueryWrapper<MovieFavour> movieFavourQueryWrapper = new QueryWrapper<>();
            movieFavourQueryWrapper.in("movieId", movieId);
            movieFavourQueryWrapper.eq("userId", loginUser.getId());
            MovieFavour movieFavour = movieFavourMapper.selectOne(movieFavourQueryWrapper);
            movieVO.setHasFavour(movieFavour != null);
        }
        return movieVO;
    }

    @Override
    public Page<MovieVO> getMovieVOPage(Page<Movie> moviePage, HttpServletRequest request) {
        List<Movie> movieList = moviePage.getRecords();
        Page<MovieVO> movieVOPage = new Page<>(moviePage.getCurrent(), moviePage.getSize(), moviePage.getTotal());
        if (CollUtil.isEmpty(movieList)) {
            return movieVOPage;
        }
        // 1. 关联查询用户信息
        Set<Long> userIdSet = movieList.stream().map(Movie::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 已登录，获取用户点赞、收藏状态
        Map<Long, Boolean> movieIdHasThumbMap = new HashMap<>();
        Map<Long, Boolean> movieIdHasFavourMap = new HashMap<>();
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            Set<Long> movieIdSet = movieList.stream().map(Movie::getMovieId).collect(Collectors.toSet());
            loginUser = userService.getLoginUser(request);
            // 获取点赞
            QueryWrapper<MovieThumb> movieThumbQueryWrapper = new QueryWrapper<>();
            movieThumbQueryWrapper.in("movieId", movieIdSet);
            movieThumbQueryWrapper.eq("userId", loginUser.getId());
            List<MovieThumb> movieMovieThumbList = movieThumbMapper.selectList(movieThumbQueryWrapper);
            movieMovieThumbList.forEach(movieMovieThumb -> movieIdHasThumbMap.put(movieMovieThumb.getMovieId(), true));
            // 获取收藏
            QueryWrapper<MovieFavour> movieFavourQueryWrapper = new QueryWrapper<>();
            movieFavourQueryWrapper.in("movieId", movieIdSet);
            movieFavourQueryWrapper.eq("userId", loginUser.getId());
            List<MovieFavour> movieFavourList = movieFavourMapper.selectList(movieFavourQueryWrapper);
            movieFavourList.forEach(movieFavour -> movieIdHasFavourMap.put(movieFavour.getMovieId(), true));
        }
        // 填充信息
        List<MovieVO> movieVOList = movieList.stream().map(movie -> {
            MovieVO movieVO = MovieVO.objToVo(movie);
            Long userId = movie.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            movieVO.setUser(userService.getUserVO(user));
            movieVO.setHasThumb(movieIdHasThumbMap.getOrDefault(movie.getMovieId(), false));
            movieVO.setHasFavour(movieIdHasFavourMap.getOrDefault(movie.getMovieId(), false));
            return movieVO;
        }).collect(Collectors.toList());
        movieVOPage.setRecords(movieVOList);
        return movieVOPage;
    }

    /**
     * 获取评分最高的电影
     * @return
     */
    @Override
    public Movie getTopRatedMovie() {
        // 查询评分最高的一部电影
        return movieMapper.selectOne(new QueryWrapper<Movie>().orderByDesc("ratingNum").last("LIMIT 1"));
    }

    /**
     * 判断是否有数据
     * @param userId
     * @return
     */
    @Override
    public boolean userHasData(Long userId) {
        boolean hasRatings = movieScoreMapper.selectCount(new QueryWrapper<MovieScore>().eq("userId", userId)) > 0;
        boolean hasFavourites = movieFavourMapper.selectCount(new QueryWrapper<MovieFavour>().eq("userId", userId)) > 0;
        return hasRatings || hasFavourites;
    }
    /**
     * 基于内容的推荐算法
     *
     * @param targetMovie
     * @return
     */
    @Override
    public List<Movie> recommendMovies(Movie targetMovie) {
        String cacheKey = RECOMMENDATION_KEY_PREFIX + targetMovie.getMovieId();

        // 尝试从缓存中获取推荐结果
        ValueOperations<String, Object> valueOps = redisTemplate.opsForValue();
        List<Movie> cachedRecommendations = (List<Movie>) valueOps.get(cacheKey);
        if (cachedRecommendations != null) {
            System.out.println("Returning cached recommendations for movie: " + targetMovie.getName());
            return cachedRecommendations;
        }

        // 如果缓存中没有推荐结果，执行推荐算法
        List<Movie> allMovies = movieMapper.selectList(null);

        if (allMovies == null || allMovies.isEmpty()) {
            System.out.println("No movies found in the database.");
            return Collections.emptyList();
        }

        List<MovieSimilarity> sortedMovies = allMovies.stream()
                .filter(m -> m != null && !m.getMovieId().equals(targetMovie.getMovieId()))
                .map(m -> new MovieSimilarity(m, calculateCosineSimilarity(targetMovie, m)))
                .sorted((m1, m2) -> Double.compare(m2.similarity, m1.similarity))
                .limit(12)
                .collect(Collectors.toList());

        List<Movie> recommendations = sortedMovies.stream().map(ms -> ms.movie).collect(Collectors.toList());

        // 将推荐结果存入缓存，并设置失效时间
        valueOps.set(cacheKey, recommendations, CACHE_EXPIRATION, TimeUnit.HOURS);
        System.out.println("Cached recommendations for movie: " + targetMovie.getName());

        return recommendations;
    }

    /**
     * 基于用户的协同过滤算法
     *
     * @param userId 目标用户的ID
     * @return 推荐的电影列表
     */
    @Override
    public List<Movie> recommendMoviesUser(Long userId) {
        String cacheKey = "for user" + userId+ "recommendations:";
        System.out.println("当前用户："+userId);
        List<Movie> recommendedMovies = (List<Movie>) redisTemplate.opsForValue().get(cacheKey);

        if (recommendedMovies == null || isDatabaseUpdated(userId)) {
            // 获取所有用户的电影评分数据
            List<MovieScore> allRatings = movieScoreMapper.selectList(null);
            // 将评分数据按用户ID分组，便于后续按用户检索评分
            Map<Long, List<MovieScore>> userRatingsMap = allRatings.stream()
                    .collect(Collectors.groupingBy(MovieScore::getUserId));

            // 获取所有用户的电影收藏列表
            List<MovieFavour> allFavorites = movieFavourMapper.selectList(null);
            // 提取当前用户收藏的电影ID集合
            Set<Long> favoriteMovieIds = allFavorites.stream()
                    .filter(favorite -> favorite.getUserId().equals(userId))
                    .map(MovieFavour::getMovieId)
                    .collect(Collectors.toSet());

            // 存储其他用户与当前用户的评分相似度
            Map<Long, Double> similarityScores = new HashMap<>();
            // 获取目标用户的评分列表
            List<MovieScore> targetUserRatings = userRatingsMap.get(userId);
            // 遍历每个用户的评分数据，计算与目标用户的相似度
            userRatingsMap.forEach((otherUserId, otherRatings) -> {
                if (!otherUserId.equals(userId)) {
                    double similarity = calculatePearsonCorrelation(targetUserRatings, otherRatings);
                    similarityScores.put(otherUserId, similarity);
                }
            });

            // 从相似度高的用户中选取前5个
            List<Map.Entry<Long, Double>> topTwelveUsers = similarityScores.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .limit(5)
                    .collect(Collectors.toList());

            // 根据这些用户的评分推荐电影，过滤掉目标用户已收藏的电影
            recommendedMovies = topTwelveUsers.stream()
                    .flatMap(e -> userRatingsMap.get(e.getKey()).stream())
                    .map(MovieScore::getMovieId)
                    .distinct()
                    .filter(movieId -> !favoriteMovieIds.contains(movieId))
                    .map(movieId -> movieMapper.selectById(movieId))
                    .limit(12)  // 只推荐12部电影
                    .collect(Collectors.toList());

            // 更新Redis缓存，缓存时间为1小时
            redisTemplate.opsForValue().set(cacheKey, recommendedMovies, 10, TimeUnit.MINUTES);
        }

        return recommendedMovies;
    }


    /**
     * 判断是否数据库更新
     * @return
     */
    private boolean isDatabaseUpdated(Long userId) {
        // 为每个用户生成独特的缓存键
        String userMovieCountKey = MOVIE_COUNT_KEY + "_" + userId;

        Long currentMovieCount = count();
        Object cachedMovieCountObj = redisTemplate.opsForValue().get(userMovieCountKey);

        Long cachedMovieCount = null;
        if (cachedMovieCountObj != null) {
            if (cachedMovieCountObj instanceof Integer) {
                cachedMovieCount = ((Integer) cachedMovieCountObj).longValue();
            } else if (cachedMovieCountObj instanceof Long) {
                cachedMovieCount = (Long) cachedMovieCountObj;
            } else {
                throw new IllegalStateException("Unexpected value type in cache: " + cachedMovieCountObj.getClass());
            }
        }

        // 如果Redis中没有缓存电影数量，或者当前电影数量与缓存的电影数量不一致，说明数据库已更新
        if (cachedMovieCount == null || !cachedMovieCount.equals(currentMovieCount)) {
            // 更新Redis中的电影数量
            redisTemplate.opsForValue().set(userMovieCountKey, currentMovieCount);
            return true;
        }
        return false;
    }

    /**
     * 计算两部电影之间的余弦相似度。
     *
     * @param m1 第一部电影
     * @param m2 第二部电影
     * @return 两部电影之间的余弦相似度
     */
    @Override
    public double calculateCosineSimilarity(Movie m1, Movie m2) {
        // 提取两部电影的特征向量
        Map<String, Double> features1 = extractFeatures(m1);
        Map<String, Double> features2 = extractFeatures(m2);

        // 初始化点积和模的平方和
        double dotProduct = 0.0, norm1 = 0.0, norm2 = 0.0;

        // 合并两部电影的特征键
        Set<String> allFeatures = new HashSet<>(features1.keySet());
        allFeatures.addAll(features2.keySet());

        // 计算点积和模的平方和
        for (String feature : allFeatures) {
            double f1 = features1.getOrDefault(feature, 0.0);
            double f2 = features2.getOrDefault(feature, 0.0);
            dotProduct += f1 * f2;  // 累加点积
            norm1 += f1 * f1;       // 累加第一部电影的特征向量的模的平方
            norm2 += f2 * f2;       // 累加第二部电影的特征向量的模的平方
        }

        // 计算余弦相似度，防止除以零
        return norm1 == 0 || norm2 == 0 ? 0 : dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    /**
     * 从电影对象中提取特征向量。
     *
     * @param movie 电影对象
     * @return 电影的特征向量
     */
    public Map<String, Double> extractFeatures(Movie movie) {
        Map<String, Double> features = new HashMap<>();
        if (movie != null) {
            // 电影类型和导演作为重要特征
            features.put("type:" + movie.getType(), 3.0);
            features.put("director:" + movie.getDirector(), 2.0);

            // 处理电影的演员列表
            if (movie.getActors() != null) {
                Arrays.stream(movie.getActors().split(","))
                        .forEach(actor -> features.put("actor:" + actor.trim(), 1.0));
            }

            // 假设评分范围从1到10
            double normalizedRating = (Double.valueOf(movie.getRatingNum()) - 1) / 9;  // 归一化到0到1之间
            features.put("rating", normalizedRating * 1.5);  // 示例：提高评分的权重
            // 提取电影标签中的关键词
            features.putAll(extractKeywords(movie.getTags()));
        }
        return features;
    }

    /**
     * 从文本中提取关键词及其出现次数。
     *
     * @param text 待提取关键词的文本
     * @return 关键词及其频率的映射
     */
    public Map<String, Double> extractKeywords(String text) {
        Map<String, Double> keywords = new HashMap<>();
        if (text != null) {
            List<String> words = HanLP.segment(text).stream()
                    .map(term -> term.word)  // 获取分词结果
                    .filter(word -> word.length() > 1)  // 过滤单个字符
                    .collect(Collectors.toList());

            for (String word : words) {
                // 累计每个关键词的频率
                keywords.put("keyword:" + word, keywords.getOrDefault("keyword:" + word, 0.0) + 1.0);
            }
        }
        return keywords;
    }

    /**
     * *基于用户的协同过滤算法计算相似度
     *
     * @param ratings1
     * @param ratings2
     * @return
     */
    private double calculatePearsonCorrelation(List<MovieScore> ratings1, List<MovieScore> ratings2) {
        // 首先找出两组评分中共同的电影ID
        Set<Long> commonMovieIds = ratings1.stream()
                .map(MovieScore::getMovieId)
                .collect(Collectors.toSet());
        commonMovieIds.retainAll(ratings2.stream()
                .map(MovieScore::getMovieId)
                .collect(Collectors.toSet()));

        // 筛选出两位用户对共同电影的评分
        List<Double> scores1 = ratings1.stream()
                .filter(score -> commonMovieIds.contains(score.getMovieId()))
                .map(MovieScore::getRating)
                .map(Double::parseDouble) // 将 String 转换为 Double
                .collect(Collectors.toList());

        List<Double> scores2 = ratings2.stream()
                .filter(score -> commonMovieIds.contains(score.getMovieId()))
                .map(MovieScore::getRating)
                .map(Double::parseDouble) // 同样需要转换
                .collect(Collectors.toList());


        // 计算两组评分的平均值
        double avg1 = scores1.stream().mapToDouble(d -> d).average().orElse(0.0);
        double avg2 = scores2.stream().mapToDouble(d -> d).average().orElse(0.0);

        // 计算协方差和两个标准差的乘积
        double covariance = 0.0;
        for (int i = 0; i < scores1.size(); i++) {
            covariance += (scores1.get(i) - avg1) * (scores2.get(i) - avg2);
        }
        double standardDeviation1 = Math.sqrt(scores1.stream().mapToDouble(d -> Math.pow(d - avg1, 2)).sum());
        double standardDeviation2 = Math.sqrt(scores2.stream().mapToDouble(d -> Math.pow(d - avg2, 2)).sum());

        // 计算皮尔逊相关系数
        if (standardDeviation1 == 0 || standardDeviation2 == 0) return 0;  // 防止除以零
        return covariance / (standardDeviation1 * standardDeviation2);
    }

    /**
     * 用于存储电影及其相似度的辅助类。
     */
    static class MovieSimilarity {
        Movie movie;
        double similarity;

        /**
         * 构造函数。
         *
         * @param movie      电影对象
         * @param similarity 与另一部电影的相似度
         */
        MovieSimilarity(Movie movie, double similarity) {
            this.movie = movie;
            this.similarity = similarity;
        }
    }


}
