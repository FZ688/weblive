package com.fz.entity.query;


import lombok.Getter;
import lombok.Setter;

/**
 * 视频播放历史参数
 * @author fz
 */
@Setter
@Getter
public class VideoPlayHistoryQuery extends BaseParam {


	/**
	 * 用户ID
	 */
	private String userId;

	private String userIdFuzzy;

	/**
	 * 视频ID
	 */
	private String videoId;

	private String videoIdFuzzy;

	/**
	 * 文件索引
	 */
	private Integer fileIndex;

	/**
	 * 最后更新时间
	 */
	private String lastUpdateTime;

	private String lastUpdateTimeStart;

	private String lastUpdateTimeEnd;

	// 是否联查
	private Boolean queryVideoDetail;

}
