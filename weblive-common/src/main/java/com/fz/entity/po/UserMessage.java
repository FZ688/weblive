package com.fz.entity.po;

import java.util.Date;

import com.fz.entity.dto.UserMessageExtendDto;
import com.fz.entity.enums.DateTimePatternEnum;
import com.fz.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fz.utils.JsonUtils;
import com.fz.utils.StringTools;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * 用户消息表
 * @author fz
 */
@Data
public class UserMessage implements Serializable {


	/**
	 * 消息ID自增
	 */
	private Integer messageId;

	/**
	 * 用户ID
	 */
	private String userId;

	/**
	 * 主体ID
	 */
	private String videoId;

	/**
	 * 消息类型
	 */
	private Integer messageType;

	/**
	 * 发送人ID
	 */
	private String sendUserId;

	/**
	 * 0:未读 1:已读
	 */
	private Integer readType;

	/**
	 * 创建时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;

	/**
	 * 扩展信息
	 */
	private String extendJson;


	private String sendUserAvatar;
	private String sendUserName;
	private String videoName;
	private String videoCover;

	private UserMessageExtendDto extendDto;

	public UserMessageExtendDto getExtendDto() {
		return StringTools.isEmpty(extendJson) ? new UserMessageExtendDto() : JsonUtils.convertJson2Obj(extendJson, UserMessageExtendDto.class);
	}


    @Override
	public String toString (){
		return "消息ID自增:"+(messageId == null ? "空" : messageId)+"，用户ID:"+(userId == null ? "空" : userId)+"，主体ID:"+(videoId == null ? "空" : videoId)+"，消息类型:"+(messageType == null ? "空" : messageType)+"，发送人ID:"+(sendUserId == null ? "空" : sendUserId)+"，0:未读 1:已读:"+(readType == null ? "空" : readType)+"，创建时间:"+(createTime == null ? "空" : DateUtil.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()))+"，扩展信息:"+(extendJson == null ? "空" : extendJson);
	}
}
