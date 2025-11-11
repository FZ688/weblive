package com.fz.entity.po;

import java.util.Date;
import com.fz.entity.enums.DateTimePatternEnum;
import com.fz.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * 用户行为 点赞、评论
 * @author fz
 */
@Data
public class UserAction implements Serializable {


	/**
	 * 自增ID
	 */
	private Integer actionId;

	/**
	 * 视频ID
	 */
	private String videoId;

	/**
	 * 视频用户ID
	 */
	private String videoUserId;

	/**
	 * 评论ID
	 */
	private Integer commentId;

	/**
	 * 0:评论喜欢点赞 1:讨厌评论 2:视频点赞 3:视频收藏 4:视频投币 
	 */
	private Integer actionType;

	/**
	 * 数量
	 */
	private Integer actionCount;

	/**
	 * 用户ID
	 */
	private String userId;

	/**
	 * 操作时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date actionTime;

	private String videoCover;
	private String videoName;

    @Override
	public String toString (){
		return "自增ID:"+(actionId == null ? "空" : actionId)+"，视频ID:"+(videoId == null ? "空" : videoId)+"，视频用户ID:"+(videoUserId == null ? "空" : videoUserId)+"，评论ID:"+(commentId == null ? "空" : commentId)+"，0:评论喜欢点赞 1:讨厌评论 2:视频点赞 3:视频收藏 4:视频投币 :"+(actionType == null ? "空" : actionType)+"，数量:"+(actionCount == null ? "空" : actionCount)+"，用户ID:"+(userId == null ? "空" : userId)+"，操作时间:"+(actionTime == null ? "空" : DateUtil.format(actionTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()));
	}
}
