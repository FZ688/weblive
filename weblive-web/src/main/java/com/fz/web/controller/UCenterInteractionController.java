package com.fz.web.controller;
import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.fz.entity.po.*;
import com.fz.entity.query.*;
import com.fz.entity.vo.PaginationResultVO;
import com.fz.entity.vo.ResponseVO;
import com.fz.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * @Author: fz
 * @Date: 2024/12/8 16:02
 * @Description: 视频发布
 */
@RestController
@RequestMapping("/ucenter")
@Validated
@Slf4j
@SaCheckLogin
public class UCenterInteractionController extends ABaseController{

    @Resource
    private VideoDanmuService videoDanmuService;
    @Resource
    private VideoCommentService videoCommentService;
    @Resource
    private VideoInfoService videoInfoService;

    /**
     * @description: 获取所有发布的视频
     * @return com.fz.entity.vo.ResponseVO
     * @author fz
     * 2025/1/13 17:19
     */
    @RequestMapping("/loadAllVideo")
    public ResponseVO saveVideoInteraction(){
        // 获取视频信息
        //TokenUserInfoDto tokenUserInfoDto = getCurrentUser();
        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        videoInfoQuery.setUserId(StpUtil.getLoginIdAsString());
        videoInfoQuery.setOrderBy("create_time desc");
        List<VideoInfo> videoInfoList = videoInfoService.findListByParam(videoInfoQuery);
        return getSuccessResponseVO(videoInfoList);
    }

    /**
     * @description: 加载评论（联表）
     * @param pageNo 页码
     * @param videoId 视频id
     * @return com.fz.entity.vo.ResponseVO
     * @author fz
     * 2025/1/13 17:49
     */
    @RequestMapping("/loadComment")
    public ResponseVO loadComment(Integer pageNo,String videoId){
        // 获取视频信息
        //TokenUserInfoDto tokenUserInfoDto = getCurrentUser();
        VideoCommentQuery videoCommentQuery = new VideoCommentQuery();
        videoCommentQuery.setVideoId(videoId);
        videoCommentQuery.setVideoUserId(StpUtil.getLoginIdAsString());
        videoCommentQuery.setOrderBy("comment_id desc");
        videoCommentQuery.setPageSize(pageNo);
        // 联查videoInfo
        videoCommentQuery.setQueryVideoInfo(true);

        PaginationResultVO<VideoComment> resultVO = videoCommentService.findListByPage(videoCommentQuery);
        return getSuccessResponseVO(resultVO);
    }


    @RequestMapping("/delComment")
    public ResponseVO delComment(@NotNull Integer commentId){
        // 获取视频信息
        //TokenUserInfoDto tokenUserInfoDto = getCurrentUser();
        videoCommentService.deleteComment(commentId,StpUtil.getLoginIdAsString());
        return getSuccessResponseVO(null);
    }

    /**
     * @description: 获取自己的视频的弹幕（联表）
     * @param pageNo 页码
     * @param videoId 视频id
     * @return com.fz.entity.vo.ResponseVO
     * @author fz
     * 2025/1/13 17:57
     */
    @RequestMapping("/loadDanmu")
    public ResponseVO loadDanmu(Integer pageNo,String videoId){
        // 获取视频弹幕信息
        //TokenUserInfoDto tokenUserInfoDto = getCurrentUser();
        VideoDanmuQuery videoDanmuQuery = new VideoDanmuQuery();
        videoDanmuQuery.setVideoId(videoId);
        videoDanmuQuery.setVideoUserId(StpUtil.getLoginIdAsString());
        videoDanmuQuery.setOrderBy("danmu_id desc");
        videoDanmuQuery.setPageNo(pageNo);
        // 是否联查视频表
        videoDanmuQuery.setQueryVideoInfo(true);

        PaginationResultVO<VideoDanmu> resultVO = videoDanmuService.findListByPage(videoDanmuQuery);
        return getSuccessResponseVO(resultVO);
    }


    @RequestMapping("/delDanmu")
    public ResponseVO delDanmu(@NotNull Integer danmuId){
        // 获取视频信息
        //TokenUserInfoDto tokenUserInfoDto = getCurrentUser();
        videoDanmuService.deleteDanmu(danmuId,StpUtil.getLoginIdAsString());
        return getSuccessResponseVO(null);
    }
}
