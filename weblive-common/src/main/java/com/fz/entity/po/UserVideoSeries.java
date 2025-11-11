package com.fz.entity.po;

import java.util.Date;
import com.fz.entity.enums.DateTimePatternEnum;
import com.fz.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.List;


/**
 * 用户视频序列归档
 * @author fz
 */
@Data
public class UserVideoSeries implements Serializable {


	/**
	 * 列表ID
	 */
	private Integer seriesId;

	/**
	 * 列表名称
	 */
	private String seriesName;

	/**
	 * 描述
	 */
	private String seriesDescription;

	/**
	 * 用户ID
	 */
	private String userId;

	/**
	 * 排序
	 */
	private Integer sort;

	/**
	 * 更新时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date updateTime;

	// 合集封面
	private String cover;

	List<UserVideoSeriesVideo> videoInfoList;

    @Override
	public String toString (){
		return "列表ID:"+(seriesId == null ? "空" : seriesId)+"，列表名称:"+(seriesName == null ? "空" : seriesName)+"，描述:"+(seriesDescription == null ? "空" : seriesDescription)+"，用户ID:"+(userId == null ? "空" : userId)+"，排序:"+(sort == null ? "空" : sort)+"，更新时间:"+(updateTime == null ? "空" : DateUtil.format(updateTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()));
	}
}
