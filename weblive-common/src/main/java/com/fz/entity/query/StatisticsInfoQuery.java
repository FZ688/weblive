package com.fz.entity.query;


import lombok.Getter;
import lombok.Setter;

/**
 * 参数
 * @author fz
 */
@Setter
@Getter
public class StatisticsInfoQuery extends BaseParam {


	/**
	 * 统计日期
	 */
	private String statisticsDate;

	private String statisticsDateFuzzy;

	/**
	 * 用户ID
	 */
	private String userId;

	private String userIdFuzzy;

	/**
	 * 数据统计类型
	 */
	private Integer dataType;

	/**
	 * 统计数量
	 */
	private Integer statisticsCount;

	private String statisticsDateStart;

	private String statisticsDateEnd;

}
