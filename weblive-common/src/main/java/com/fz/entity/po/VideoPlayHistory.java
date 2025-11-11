package com.fz.entity.po;

import java.util.Date;
import com.fz.entity.enums.DateTimePatternEnum;
import com.fz.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * 视频播放历史
 * @author fz
 */
@Data
public class VideoPlayHistory implements Serializable {


	/**
	 * 用户ID
	 */
	private String userId;

	/**
	 * 视频ID
	 */
	private String videoId;

	/**
	 * 文件索引
	 */
	private Integer fileIndex;

	/**
	 * 最后更新时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date lastUpdateTime;


	private String videoName;
	private String videoCover;

    @Override
	public String toString (){
		return "用户ID:"+(userId == null ? "空" : userId)+"，视频ID:"+(videoId == null ? "空" : videoId)+"，文件索引:"+(fileIndex == null ? "空" : fileIndex)+"，最后更新时间:"+(lastUpdateTime == null ? "空" : DateUtil.format(lastUpdateTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()));
	}
}
