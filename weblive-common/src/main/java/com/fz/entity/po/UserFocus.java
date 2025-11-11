package com.fz.entity.po;

import java.util.Date;
import com.fz.entity.enums.DateTimePatternEnum;
import com.fz.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * @author fz
 */
@Data
public class UserFocus implements Serializable {


	/**
	 * 用户ID
	 */
	private String userId;

	/**
	 * 用户ID
	 */
	private String focusUserId;

	/**
	 * 
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date focusTime;

	private String otherNickName;

	private String otherUserId;

	private String otherPersonIntroduction;

	private String otherAvatar;

	private Integer focusType;

    @Override
	public String toString (){
		return "用户ID:"+(userId == null ? "空" : userId)+"，用户ID:"+(focusUserId == null ? "空" : focusUserId)+"，focusTime:"+(focusTime == null ? "空" : DateUtil.format(focusTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()));
	}
}
