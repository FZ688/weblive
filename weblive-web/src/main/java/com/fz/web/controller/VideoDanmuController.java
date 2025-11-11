package com.fz.web.controller;

import java.util.ArrayList;
import java.util.Date;

import com.fz.web.annotation.GlobalInterceptor;
import com.fz.entity.constants.Constants;
import com.fz.entity.po.VideoInfo;
import com.fz.entity.query.VideoDanmuQuery;
import com.fz.entity.po.VideoDanmu;
import com.fz.entity.vo.ResponseVO;
import com.fz.service.VideoDanmuService;
import com.fz.service.VideoInfoService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 视频弹幕 Controller
 */
@RestController("videoDanmuController")
@RequestMapping("/danmu")
@Validated
public class VideoDanmuController extends ABaseController{

	@Resource
	private VideoDanmuService videoDanmuService;
	@Resource
	private VideoInfoService videoInfoService;

	/**
	 * @description: 发送弹幕
	 * @param request
	 * @param videoId
	 * @param fileId
	 * @param text
	 * @param mode 弹幕出现位置
	 * @param color
	 * @param time 弹幕发送时间
	 * @return com.fz.entity.vo.ResponseVO
	 * @author fz
	 * 2024/12/12 18:51
	 */
	@RequestMapping("/postDanmu")
	@GlobalInterceptor(checkLogin = true)
	public ResponseVO postDanmu(HttpServletRequest request,
								@NotEmpty String videoId, @NotEmpty String fileId,
								@NotEmpty @Size(max = 200) String text, @NotNull Integer mode,
								@NotEmpty String color, @NotNull Integer time){

		VideoDanmu videoDanmu = new VideoDanmu();
		videoDanmu.setVideoId(videoId);
		videoDanmu.setFileId(fileId);
		videoDanmu.setText(text);
		videoDanmu.setMode(mode);
		videoDanmu.setColor(color);
		videoDanmu.setTime(time);
		videoDanmu.setUserId(getTokenUserInfoDto(request).getUserId());
		videoDanmu.setPostTime(new Date());

		videoDanmuService.saveVideoDamu(videoDanmu);
		return getSuccessResponseVO(null);
	}

	/**
	 * @description: 加载分p弹幕
	 * @param videoId 视频 id
	 * @param fileId 分p id
	 * @return com.fz.entity.vo.ResponseVO
	 * @author fz
	 * 2024/12/12 21:18
	 */
	@RequestMapping("/loadDanmu")
	public ResponseVO loadDanmu(@NotEmpty String videoId, @NotEmpty String fileId){
		// 根据视频id获取视频信息
		VideoInfo videoInfo = this.videoInfoService.getVideoInfoByVideoId(videoId);
		// 判断是否关闭弹幕
		if (videoInfo.getInteraction() != null && videoInfo.getInteraction().contains(Constants.ZERO.toString())){
			return getSuccessResponseVO(new ArrayList<>());
		}
		VideoDanmuQuery videoDanmuQuery = new VideoDanmuQuery();
		videoDanmuQuery.setFileId(fileId);
		videoDanmuQuery.setOrderBy("danmu_id asc");
		return getSuccessResponseVO(videoDanmuService.findListByParam(videoDanmuQuery));
	}
}