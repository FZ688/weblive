package com.fz.entity.query;


import lombok.Getter;
import lombok.Setter;

/**
 * 视频文件信息参数
 * @author fz
 */
@Setter
@Getter
public class VideoInfoFileQuery extends BaseParam {


	/**
	 * 唯一ID
	 */
	private String fileId;

	private String fileIdFuzzy;

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
	 * 文件名
	 */
	private String fileName;

	private String fileNameFuzzy;

	/**
	 * 文件索引
	 */
	private Integer fileIndex;

	/**
	 * 文件大小
	 */
	private Long fileSize;

	/**
	 * 文件路径
	 */
	private String filePath;

	private String filePathFuzzy;

	/**
	 * 持续时间（秒）
	 */
	private Integer duration;


}
