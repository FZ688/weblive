package com.fz.web.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.fz.entity.dto.TokenUserInfoDto;
import com.fz.entity.query.VideoPlayHistoryQuery;
import com.fz.entity.vo.ResponseVO;
import com.fz.service.VideoPlayHistoryService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;


@RestController
@RequestMapping("/history")
@SaCheckLogin
public class VideoPlayHistoryController extends ABaseController{

	@Resource
	private VideoPlayHistoryService videoPlayHistoryService;

	/**
	 * @description: 获取历史记录
	 * @param pageNo 页码
	 * @return com.fz.entity.vo.ResponseVO
	 * @author fz
	 * 2025/1/14 21:47
	 */
	@RequestMapping("/loadHistory")
	public ResponseVO loadHistory(Integer pageNo){
		TokenUserInfoDto tokenUserInfoDto = getCurrentUser();
		VideoPlayHistoryQuery historyQuery = new VideoPlayHistoryQuery();
		historyQuery.setUserId(StpUtil.getLoginIdAsString());
		historyQuery.setPageNo(pageNo);
		historyQuery.setOrderBy("last_update_time desc");
		historyQuery.setQueryVideoDetail(true);

		return getSuccessResponseVO(videoPlayHistoryService.findListByPage(historyQuery));
	}

	/**
	 * @description: 删除所有历史记录
	 * @return com.fz.entity.vo.ResponseVO
	 * @author fz
	 * 2025/1/14 22:03
	 */
	@RequestMapping("/cleanHistory")
	public ResponseVO cleanHistory(){
		VideoPlayHistoryQuery historyQuery = new VideoPlayHistoryQuery();
		historyQuery.setUserId(StpUtil.getLoginIdAsString());
		videoPlayHistoryService.deleteByParam(historyQuery);
		return getSuccessResponseVO(null);
	}

	/**
	 * @description: 删除单个记录
	 * @param videoId
	 * @return com.fz.entity.vo.ResponseVO
	 * @author fz
	 * 2025/1/14 22:05
	 */
	@RequestMapping("/delHistory")
	public ResponseVO cleanHistory(@NotEmpty String videoId){
		//TokenUserInfoDto tokenUserInfoDto = getCurrentUser();
		videoPlayHistoryService.deleteVideoPlayHistoryByUserIdAndVideoId(StpUtil.getLoginIdAsString(),videoId);
		return getSuccessResponseVO(null);
	}
}