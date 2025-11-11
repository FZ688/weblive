package com.fz.entity.query;


import lombok.Getter;
import lombok.Setter;

/**
 * 参数
 * @author fz
 */
@Setter
@Getter
public class UserFocusQuery extends BaseParam {


	/**
	 * 用户ID
	 */
	private String userId;

	private String userIdFuzzy;

	/**
	 * 用户ID
	 */
	private String focusUserId;

	private String focusUserIdFuzzy;

	/**
	 * 
	 */
	private String focusTime;

	private String focusTimeStart;

	private String focusTimeEnd;

	private Integer queryType;

}
