package com.fz.entity.query;


import lombok.Data;

/**
 * @author fz
 */
@Data
public class BaseParam {
	private SimplePage simplePage;
	private Integer pageNo;
	private Integer pageSize;
	private String orderBy;

}
