package com.fz.entity.query;


import lombok.Getter;
import lombok.Setter;

/**
 * 视频文件信息参数
 * @author fz
 */
@Setter
@Getter
public class VideoInfoFilePostQuery extends BaseParam {


	/**
	 * 唯一ID
	 */
	private String fileId;

	private String fileIdFuzzy;

	/**
	 * 上传ID
	 */
	private String uploadId;

	private String uploadIdFuzzy;

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
	 * 文件索引(第几p)
	 */
	private Integer fileIndex;

	/**
	 * 文件名
	 */
	private String fileName;

	private String fileNameFuzzy;

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
	 * 0:无更新 1:有更新(只需要审核更新的)
	 */
	private Integer updateType;

	/**
	 * 0:转码中 1:转码成功 2:转码失败
	 */
	private Integer transferResult;

	/**
	 * 持续时间（秒）
	 */
	private Integer duration;


}
