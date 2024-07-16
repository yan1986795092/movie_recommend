package com.movie_recommend.manager;

import com.movie_recommend.model.entity.Movie;
import com.movie_recommend.service.MovieService;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Properties;

/**
 * @Author yqq
 * @Date 2024/7/15 12:11
 * @Description 拦截器
 * @Version 1.0
 */

@Intercepts({
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})
})
@Component
public class MyBatisInterceptor implements Interceptor {

    @Resource
    @Lazy
    private MovieService movieService;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        String sqlId = mappedStatement.getId();

        // 执行原有的操作
        Object result = invocation.proceed();

        // 在这里根据sqlId判断是否为插入、更新、删除操作，并进行Elasticsearch同步
        if (sqlId.contains("insert") || sqlId.contains("update") || sqlId.contains("delete")) {
            // 获取变更的数据
            Object parameter = invocation.getArgs()[1];
            if (parameter instanceof Movie) {
                Movie movie = (Movie) parameter;
                movieService.syncMovieToEs(movie);
            }
        }

        return result;
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
    }
}