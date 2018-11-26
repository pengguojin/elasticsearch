package com.es.data;

import java.io.Serializable;

import io.searchbox.annotations.JestId;
import lombok.Data;

@Data
public class AppTargetDataParentNewest implements Serializable {
	private static final long serialVersionUID = 1L;
	@JestId
	private String data_id;
	private String unit_code;
	private String app_code;
	private String target_name;
	private String target_value;
	private Long create_time;
	private String data_type;
}
