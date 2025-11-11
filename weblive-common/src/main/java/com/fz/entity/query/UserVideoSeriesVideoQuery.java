package com.fz.entity.query;


import lombok.Getter;
import lombok.Setter;

/**
 * 参数
 * @author fz
 */
@Setter
@Getter
public class UserVideoSeriesVideoQuery extends BaseParam {


	/**
	 * 列表ID
	 */
	private Integer seriesId;

	/**
	 * 视频ID
	 */
	private String videoId;

	private String videoIdFuzzy;

	/**
	 * 用户ID
	 */
	private String userId;

	private String userIdFuzzy;

	/**
	 * 排序
	 */
	private Integer sort;

	// 是否查询视频信息（联查）
	private Boolean queryVideoInfo;

}
