package com.fz.entity.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;


/**
 * @author fz
 */
@Data
public class UserVideoSeriesVideo implements Serializable {


	/**
	 * 列表ID
	 */
	private Integer seriesId;

	/**
	 * 视频ID
	 */
	private String videoId;

	/**
	 * 用户ID
	 */
	private String userId;

	/**
	 * 排序
	 */
	private Integer sort;

	// 以下是联查字段
	// 视频名称
	private String VideoName;

	// 视频封面
	private String videoCover;
	// 视频播放量
	private Integer playCount;
	// 上一次修改时间
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;


    @Override
	public String toString (){
		return "列表ID:"+(seriesId == null ? "空" : seriesId)+"，视频ID:"+(videoId == null ? "空" : videoId)+"，用户ID:"+(userId == null ? "空" : userId)+"，排序:"+(sort == null ? "空" : sort);
	}
}
