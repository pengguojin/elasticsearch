package com.es.data;

import java.io.Serializable;

import io.searchbox.annotations.JestId;
import lombok.Data;

@Data
public class AppTargetDataChildNewest implements Serializable {
	private static final long serialVersionUID = 1L;
	@JestId
	private String record_id;
	private String data_id;
	private String child_name;
	private String target_value;
	private Long create_time;
	private Integer orderno;
}
