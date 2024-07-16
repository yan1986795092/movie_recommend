package com.movie_recommend.MovieEsRepository;

import com.movie_recommend.model.dto.movie.MovieEsDTO;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

/**
 * 电影 ES 操作
 */
public interface MovieEsRepository extends ElasticsearchRepository<MovieEsDTO, Long> {

}