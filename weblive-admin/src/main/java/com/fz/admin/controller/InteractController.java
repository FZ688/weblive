package com.fz.admin.controller;

import com.fz.entity.po.VideoComment;
import com.fz.entity.po.VideoDanmu;
import com.fz.entity.query.VideoCommentQuery;
import com.fz.entity.query.VideoDanmuQuery;
import com.fz.entity.vo.PaginationResultVO;
import com.fz.entity.vo.ResponseVO;
import com.fz.service.VideoCommentService;
import com.fz.service.VideoDanmuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;


/**
 * @Author: fz
 * @Date: 2025/1/8 13:55
 * @Description:
 */
@RestController
@RequestMapping("/interact")
@Validated
@Slf4j
public class InteractController extends ABaseController{
    @Resource
    private VideoCommentService videoCommentService;
    @Resource
    private VideoDanmuService videoDanmuService;

    /**
     * @description: 加载评论
     * @param pageNo
     * @param videNameFuzzy
     * @return com.fz.entity.vo.ResponseVO
     * @author fz
     * 2025/1/16 11:13
     */
    @RequestMapping("/loadComment")
    public ResponseVO loadComment(Integer pageNo,String videNameFuzzy){
        VideoCommentQuery videoCommentQuery = new VideoCommentQuery();
        videoCommentQuery.setOrderBy("comment_id");
        videoCommentQuery.setPageNo(pageNo);
        videoCommentQuery.setVideoNameFuzzy(videNameFuzzy);
        // 联查视频信息
        videoCommentQuery.setQueryVideoInfo(true);
        PaginationResultVO<VideoComment> resultVO = videoCommentService.findListByPage(videoCommentQuery);
        return getSuccessResponseVO(resultVO);
    }

    @RequestMapping("/delComment")
    public ResponseVO delComment(@NotNull Integer commentId){
        videoCommentService.deleteComment(commentId,null);
        return getSuccessResponseVO(null);
    }


    @RequestMapping("/loadDanmu")
    public ResponseVO loadDanmu(Integer pageNo,String videoNameFuzzy){
        VideoDanmuQuery videoDanmuQuery = new VideoDanmuQuery();
        videoDanmuQuery.setPageNo(pageNo);
        videoDanmuQuery.setVideoNameFuzzy(videoNameFuzzy);
        videoDanmuQuery.setOrderBy("danmu_id desc");
        videoDanmuQuery.setQueryVideoInfo(true);

        PaginationResultVO<VideoDanmu> resultVO = videoDanmuService.findListByPage(videoDanmuQuery);
        return getSuccessResponseVO(resultVO);
    }

    @RequestMapping("/delDanmu")
    public ResponseVO delDanmu(@NotNull Integer danmuId){
        videoDanmuService.deleteDanmu(danmuId,null);
        return getSuccessResponseVO(null);
    }
}
