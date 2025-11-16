package com.fz.admin.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.fz.annotation.RecordUserMessage;
import com.fz.entity.enums.MessageTypeEnum;
import com.fz.entity.po.VideoInfoFilePost;
import com.fz.entity.po.VideoInfoPost;
import com.fz.entity.query.VideoInfoFilePostQuery;
import com.fz.entity.query.VideoInfoPostQuery;
import com.fz.entity.vo.PaginationResultVO;
import com.fz.entity.vo.ResponseVO;
import com.fz.service.VideoInfoFilePostService;
import com.fz.service.VideoInfoPostService;
import com.fz.service.VideoInfoService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * @Author: fz
 * @Date: 2024/12/9 16:05
 * @Description:
 */
@RestController
@RequestMapping("/videoInfo")
@Validated
public class VideoInfoController extends ABaseController {
    @Resource
    private VideoInfoPostService videoInfoPostService;
    @Resource
    private VideoInfoFilePostService videoInfoFilePostService;
    @Resource
    private VideoInfoService videoInfoService;

    /**
     * 加载视频
     * @param 
     * @return 
     * @author fz
     * 2024/12/9 19:53
     */
    @RequestMapping("/loadVideoList")
    public ResponseVO loadVideoList(VideoInfoPostQuery videoInfoPostQuery){
        // 构建查询条件
        videoInfoPostQuery.setOrderBy("v.last_update_time desc");
        // 需要连表查询
        // 关于数量的需要到video_info表查询
        videoInfoPostQuery.setQueryCountInfo(true);
        // 需要查询用户信息
        videoInfoPostQuery.setQueryUserInfo(true);
        PaginationResultVO<VideoInfoPost> resultVO = videoInfoPostService.findListByPage(videoInfoPostQuery);
        return getSuccessResponseVO(resultVO);
    }
    
    /**
     * @description: 审核视频
     * @param videoId 视频id
     * @param status 审核通过 不通过
     * @param reason 原因
     * @return com.fz.entity.vo.ResponseVO
     * @author fz
     * 2024/12/10 17:00
     */
    @SaCheckRole("admin")
    @RequestMapping("/auditVideo")
    @RecordUserMessage(messageType = MessageTypeEnum.SYS)
    public ResponseVO auditVideo(@NotEmpty String videoId, @NotNull Integer status, String reason){
        videoInfoPostService.auditVideo(videoId,status,reason);
        return getSuccessResponseVO(null);
    }

    /**
     * @description: 推荐视频
     * @param videoId
     * @return com.fz.entity.vo.ResponseVO
     * @author fz
     * 2025/1/16 10:41
     */
    @SaCheckRole("admin")
    @RequestMapping("/recommendVideo")
    public ResponseVO recommendVideo(@NotEmpty String videoId){
        videoInfoService.recommendVideo(videoId);
        return getSuccessResponseVO(null);
    }

    /**
     * @description: 删除视频
     * @param videoId
     * @return com.fz.entity.vo.ResponseVO
     * @author fz
     * 2025/1/16 10:42
     */
    @SaCheckRole("admin")
    @RequestMapping("/deleteVideo")
    public ResponseVO deleteVideo(@NotEmpty String videoId){
        videoInfoService.deleteVideo(videoId,null);
        return getSuccessResponseVO(null);
    }

    /**
     * @description: 加载分P信息
     * @param videoId 视频id
     * @return com.fz.entity.vo.ResponseVO
     * @author fz
     * 2025/1/16 10:56
     */
    @RequestMapping("/loadVideoPList")
    public ResponseVO loadVideoPList(@NotEmpty String videoId){
        VideoInfoFilePostQuery postQuery = new VideoInfoFilePostQuery();
        postQuery.setVideoId(videoId);
        postQuery.setOrderBy("file_index asc");
        List<VideoInfoFilePost> videoInfoFilePostList = videoInfoFilePostService.findListByParam(postQuery);
        return getSuccessResponseVO(videoInfoFilePostList);
    }

}
