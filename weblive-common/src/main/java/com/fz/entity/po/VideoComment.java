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
 * 评论
 * @author fz
 */
@Data
public class VideoComment implements Serializable {


	/**
	 * 评论ID
	 */
	private Integer commentId;

	/**
	 * 父级评论ID
	 */
	private Integer pCommentId;

	/**
	 * 视频ID
	 */
	private String videoId;

	/**
	 * 视频用户ID
	 */
	private String videoUserId;

	/**
	 * 回复内容
	 */
	private String content;

	/**
	 * 图片
	 */
	private String imgPath;

	/**
	 * 用户ID
	 */
	private String userId;

	/**
	 * 回复人ID
	 */
	private String replyUserId;

	/**
	 * 0:未置顶  1:置顶
	 */
	private Integer topType;

	/**
	 * 发布时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date postTime;

	/**
	 * 喜欢数量
	 */
	private Integer likeCount;

	/**
	 * 讨厌数量
	 */
	private Integer hateCount;

	private String avatar;
	private String nickName;
	private String replyNickName;
	private List<VideoComment> children;

	private String videoCover;
	private String videoName;


    public void setpCommentId(Integer pCommentId){
		this.pCommentId = pCommentId;
	}

	public Integer getpCommentId(){
		return this.pCommentId;
	}

    @Override
	public String toString (){
		return "评论ID:"+(commentId == null ? "空" : commentId)+"，父级评论ID:"+(pCommentId == null ? "空" : pCommentId)+"，视频ID:"+(videoId == null ? "空" : videoId)+"，视频用户ID:"+(videoUserId == null ? "空" : videoUserId)+"，回复内容:"+(content == null ? "空" : content)+"，图片:"+(imgPath == null ? "空" : imgPath)+"，用户ID:"+(userId == null ? "空" : userId)+"，回复人ID:"+(replyUserId == null ? "空" : replyUserId)+"，0:未置顶  1:置顶:"+(topType == null ? "空" : topType)+"，发布时间:"+(postTime == null ? "空" : DateUtil.format(postTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()))+"，喜欢数量:"+(likeCount == null ? "空" : likeCount)+"，讨厌数量:"+(hateCount == null ? "空" : hateCount);
	}
}
