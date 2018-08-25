package com.elasticsearch.repository;

import java.util.stream.Stream;

import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Component;

import com.elasticsearch.entity.UserEntity;

@Component
public interface UserRepository
		extends ElasticsearchRepository<UserEntity, String>, PagingAndSortingRepository<UserEntity, String> {

	UserEntity queryUserById(String id);

	UserEntity queryUserByName(String name);

	@Query("select u from user u")
	Stream<UserEntity> findUserStream();

	@Query("{\"bool\" : {\"must\" : {\"field\" : {\"name\" : \"?0\"}}}}")
	UserEntity findByName(String name);
}
