package com.elasticsearch;

import java.util.Date;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringRunner;

import com.elasticsearch.entity.UserEntity;
import com.elasticsearch.repository.UserRepository;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ApplicationTests {
	@Autowired
	private UserRepository rep;

	// 插入
	@Test
	public void saveUser() {
		UserEntity user = new UserEntity();
		UUID uuid = UUID.randomUUID();
		user.setId(uuid.toString().replace("-", ""));
		user.setAge(34);
		user.setCreate_time(new Date());
		user.setName("李四");
		user.setCourse(90);
		rep.save(user);
	}

	// 查询
	@Test
	public void getUserList() {
		UserEntity user = rep.queryUserById("fc4e218dda9d4c02bbfb1bcf97bb9e87");
		System.out.println(user.getAge());
		UserEntity username = rep.queryUserByName("李四");
		System.out.println(username.getName());
	}

	// 删除
	@Test
	public void deleteUser() {
		rep.deleteById("a48fb54ccb944cb48c6860cc1c7a1fee");
	}

	// 更新
	@Test
	public void updateUser() {
		UserEntity user = rep.queryUserById("fc4e218dda9d4c02bbfb1bcf97bb9e87");
		user.setAge(87);
		rep.save(user);
	}

	// 分页
	@Test
	public void pageUser() {
		Pageable pageable = PageRequest.of(0, 10);
		Page<UserEntity> user = rep.findAll(pageable);
		user.getContent();
	}

	// java8
	@Test
	public void java8() {
		// Stream<UserEntity> s = rep.findUserStream();
		// System.out.println(s);
		UserEntity e = rep.findByName("张三");
		System.out.println(e.getName());
	}
}
