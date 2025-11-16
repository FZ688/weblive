package com.fz.web.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.fz.entity.enums.ResponseCodeEnum;
import com.fz.entity.po.*;
import com.fz.entity.query.*;
import com.fz.entity.vo.ResponseVO;
import com.fz.entity.vo.UserVideoSeriesDetailVO;
import com.fz.exception.BusinessException;
import com.fz.service.*;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author: fz
 * @Date: 2025/1/11 13:24
 * @Description:
 */
@RestController
@RequestMapping("/uhome/series")
@Validated
@Slf4j
public class UHomeVideoSeriesController extends ABaseController {
    @Resource
    private VideoInfoService videoInfoService;

    @Resource
    private UserVideoSeriesService userVideoSeriesService;
    @Resource
    private  UserVideoSeriesVideoService userVideoSeriesVideoService;

    /**
     * @description: 加载视频合集
     * @param userId 用户id
     * @return com.fz.entity.vo.ResponseVO
     * @author fz
     * 2025/1/12 20:47
     */
    @RequestMapping("/loadVideoSeries")
    public ResponseVO loadVideoSeries(@NotEmpty String userId){
        List<UserVideoSeries> userAllSeries = userVideoSeriesService.getUserAllSeries(userId);
        return getSuccessResponseVO(userAllSeries);
    }

    /**
     * @description: 新增或修改视频列表(这里代指为视频分类)
     * @param seriesId 有值则为修改 否则是新增
     * @param seriesName 新增的分类名称
     * @param seriesDescription 分类描述
     * @param videoIds 新增的分类中的视频id
     * @return com.fz.entity.vo.ResponseVO
     * @author fz
     * 2025/1/12 20:49
     */
    @RequestMapping("/saveVideoSeries")
    @SaCheckLogin
    public ResponseVO saveVideoSeries(Integer seriesId,
                                      @NotEmpty @Size(max = 100) String seriesName,
                                      @Size(max = 200) String seriesDescription,
                                      String videoIds){
        //TokenUserInfoDto tokenUserInfoDto = getCurrentUser();
        UserVideoSeries videoSeries = new UserVideoSeries();
        videoSeries.setUserId(StpUtil.getLoginIdAsString());
        videoSeries.setSeriesId(seriesId);
        videoSeries.setSeriesName(seriesName);
        videoSeries.setSeriesDescription(seriesDescription);

        this.userVideoSeriesService.saveUserVideoSeries(videoSeries,videoIds);

        return getSuccessResponseVO(null);
    }


    /**
     * @description: 加载所有视频
     * @param seriesId 可选 有则是加载分类下的所有视频 否则就是加载自己的所有视频
     * @return com.fz.entity.vo.ResponseVO
     * @author fz
     * 2025/1/12 21:54
     */
    @RequestMapping("/loadAllVideo")
    @SaCheckLogin
    public ResponseVO loadAllVideo(Integer seriesId){
        //TokenUserInfoDto tokenUserInfoDto = getCurrentUser();
        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        if (seriesId != null){
            UserVideoSeriesVideoQuery videoSeriesVideoQuery = new UserVideoSeriesVideoQuery();
            // 以下的目的是排除当前分类的视频 因为你要添加的肯定是不是该分类的视频
            videoSeriesVideoQuery.setUserId(StpUtil.getLoginIdAsString());
            videoSeriesVideoQuery.setSeriesId(seriesId);
            List<UserVideoSeriesVideo> seriesVideoList = userVideoSeriesVideoService.findListByParam(videoSeriesVideoQuery);
            // 获取当前分类下的视频id集合
            // 这里的目的是为了排除当前分类下的视频
            List<String> seriesVideoIdList = seriesVideoList.stream()
                    .map(UserVideoSeriesVideo::getVideoId)
                    .toList();
            videoInfoQuery.setExcludeVideoIdArray(seriesVideoIdList.toArray(new String[seriesVideoIdList.size()]));
        }
        videoInfoQuery.setUserId(StpUtil.getLoginIdAsString());
        List<VideoInfo> videoInfoList = videoInfoService.findListByParam(videoInfoQuery);

        return getSuccessResponseVO(videoInfoList);
    }

    /**
     * @description: 获取分类中的视频（进入某一个分类中）
     * @param seriesId
     * @return com.fz.entity.vo.ResponseVO
     * @author fz
     * 2025/1/13 12:05
     */
    @RequestMapping("/getVideoSeriesDetail")
    public ResponseVO getVideoSeriesDetail(@NotNull Integer seriesId){
        UserVideoSeries videoSeries = userVideoSeriesService.getUserVideoSeriesBySeriesId(seriesId);
        if (videoSeries == null){
            throw new BusinessException(ResponseCodeEnum.CODE_404);
        }
        UserVideoSeriesVideoQuery seriesVideoQuery = new UserVideoSeriesVideoQuery();
        seriesVideoQuery.setOrderBy("sort asc");
        // 是否联查视频信息
        seriesVideoQuery.setQueryVideoInfo(true);
        seriesVideoQuery.setSeriesId(seriesId);

        List<UserVideoSeriesVideo> seriesVideoList = userVideoSeriesVideoService.findListByParam(seriesVideoQuery);

        UserVideoSeriesDetailVO resultVO = new UserVideoSeriesDetailVO(videoSeries,seriesVideoList);
        return getSuccessResponseVO(resultVO);
    }

    /**
     * @description: 新增分类中的视频
     * @param seriesId
     * @param videoIds
     * @return com.fz.entity.vo.ResponseVO
     * @author fz
     * 2025/1/13 13:09
     */
    @RequestMapping("/saveSeriesVideo")
    @SaCheckLogin
    public ResponseVO saveSeriesDetail(@NotNull Integer seriesId,@NotEmpty String videoIds){
        //TokenUserInfoDto tokenUserInfoDto = getCurrentUser();
        this.userVideoSeriesService.saveSeriesVideo(StpUtil.getLoginIdAsString(),seriesId,videoIds);

        return getSuccessResponseVO(null);
    }

    /**
     * @description: 删除分类中的视频
     * @param seriesId 分类id
     * @param videoId 视频id
     * @return com.fz.entity.vo.ResponseVO
     * @author fz
     * 2025/1/13 13:10
     */
    @RequestMapping("/delSeriesVideo")
    @SaCheckLogin
    public ResponseVO delSeriesVideo(@NotNull Integer seriesId,@NotEmpty String videoId){
        //TokenUserInfoDto tokenUserInfoDto = getCurrentUser();
        this.userVideoSeriesService.delSeriesVideo(StpUtil.getLoginIdAsString(),seriesId,videoId);
        return getSuccessResponseVO(null);
    }

    /**
     * @description: 删除视频集合
     * @param seriesId 集合id
     * @return com.fz.entity.vo.ResponseVO
     * @author fz
     * 2025/1/13 13:20
     */
    @RequestMapping("/delVideoSeries")
    @SaCheckLogin
    public ResponseVO delVideoSeries(@NotNull Integer seriesId){
        //TokenUserInfoDto tokenUserInfoDto = getCurrentUser();
        this.userVideoSeriesService.delVideoSeries(StpUtil.getLoginIdAsString(),seriesId);
        return getSuccessResponseVO(null);
    }

    /**
     * @description: 改变分类的顺序
     * @param seriesIds 目前的分类顺序
     * @return com.fz.entity.vo.ResponseVO
     * @author fz
     * 2025/1/13 13:53
     */
    @RequestMapping("/changeVideoSeriesSort")
    @SaCheckLogin
    public ResponseVO changeVideoSeriesSort(@NotEmpty String seriesIds){
        //TokenUserInfoDto tokenUserInfoDto = getCurrentUser();
        this.userVideoSeriesService.changeVideoSeriesSort(StpUtil.getLoginIdAsString(),seriesIds);
        return getSuccessResponseVO(null);
    }

    /**
     * @description: 加载分类的时候随便加载出该分类的前几个视频
     * @param userId
     * @return com.fz.entity.vo.ResponseVO
     * @author fz
     * 2025/1/13 13:58
     */
    @RequestMapping("/loadVideoSeriesWithVideo")
    public ResponseVO loadVideoSeriesWithVideo(@NotEmpty String userId){
        UserVideoSeriesQuery videoSeriesQuery = new UserVideoSeriesQuery();
        videoSeriesQuery.setUserId(userId);
        videoSeriesQuery.setOrderBy("sort asc");
        List<UserVideoSeries> videoSeriesList = userVideoSeriesService.findListWithVideoList(videoSeriesQuery);

        return getSuccessResponseVO(videoSeriesList);
    }
}
