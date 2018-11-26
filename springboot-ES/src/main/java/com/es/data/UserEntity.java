package com.es.data;

import java.io.Serializable;

import io.searchbox.annotations.JestId;

public class UserEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	@JestId
	private String id;
	private String name;
	private Integer age;
	private String sex;
	private Long createTime;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	public Long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Long createTime) {
		this.createTime = createTime;
	}

}
