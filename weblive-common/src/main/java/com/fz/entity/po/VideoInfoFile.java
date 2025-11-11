package com.fz.entity.po;

import lombok.Data;

import java.io.Serializable;


/**
 * 视频文件信息
 * @author fz
 */
@Data
public class VideoInfoFile implements Serializable {


	/**
	 * 唯一ID
	 */
	private String fileId;

	/**
	 * 用户ID
	 */
	private String userId;

	/**
	 * 视频ID
	 */
	private String videoId;

	/**
	 * 文件名
	 */
	private String fileName;

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

	/**
	 * 持续时间（秒）
	 */
	private Integer duration;


    @Override
	public String toString (){
		return "唯一ID:"+(fileId == null ? "空" : fileId)+"，用户ID:"+(userId == null ? "空" : userId)+"，视频ID:"+(videoId == null ? "空" : videoId)+"，文件名:"+(fileName == null ? "空" : fileName)+"，文件索引:"+(fileIndex == null ? "空" : fileIndex)+"，文件大小:"+(fileSize == null ? "空" : fileSize)+"，文件路径:"+(filePath == null ? "空" : filePath)+"，持续时间（秒）:"+(duration == null ? "空" : duration);
	}
}
