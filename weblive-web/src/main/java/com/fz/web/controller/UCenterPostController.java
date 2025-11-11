package com.fz.web.controller;
import com.fz.web.annotation.GlobalInterceptor;
import com.fz.entity.dto.TokenUserInfoDto;
import com.fz.entity.enums.ResponseCodeEnum;
import com.fz.entity.enums.VideoStatusEnum;
import com.fz.entity.po.VideoInfoFilePost;
import com.fz.entity.po.VideoInfoPost;
import com.fz.entity.query.VideoInfoFilePostQuery;
import com.fz.entity.query.VideoInfoPostQuery;
import com.fz.entity.vo.PaginationResultVO;
import com.fz.entity.vo.ResponseVO;
import com.fz.entity.vo.VideoPostEditInfoVo;
import com.fz.entity.vo.VideoStatusCountInfoVO;
import com.fz.exception.BusinessException;
import com.fz.service.VideoInfoFilePostService;
import com.fz.service.VideoInfoPostService;
import com.fz.service.VideoInfoService;
import com.fz.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class UCenterPostController extends ABaseController{
    @Resource
    private VideoInfoPostService videoInfoPostService;
    @Resource
    private VideoInfoFilePostService videoInfoFilePostService;
    @Resource
    private VideoInfoService videoInfoService;

    /**
     * @description: 发布视频接口
     * @param request HttpServletRequest
     * @param videoId 视频id
     * @param videoCover 视频封面
     * @param videoName 视频标题
     * @param pCategoryId 父级分类
     * @param categoryId 子级分类
     * @param postType 发布类型（转载or自制）
     * @param tags 所属标签(多个逗号分隔)
     * @param introduction 介绍
     * @param interaction  互动设置(关闭弹幕/关闭评论）
     * @param uploadFileList 分p列表
     * @return com.fz.entity.vo.ResponseVO
     * @author fz
     * 2024/12/10 14:06
     */
    @RequestMapping("/postVideo")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO postVideo(HttpServletRequest request, String videoId,
                                @NotEmpty String videoCover,
                                @NotEmpty @Size(max = 100) String videoName,
                                @NotNull Integer pCategoryId,
                                Integer categoryId,
                                @NotNull Integer postType,
                                String originInfo,
                                @NotEmpty @Size(max = 300) String tags,
                                @Size(max = 2000) String introduction,
                                @Size(max = 3) String interaction,
                                @NotEmpty String uploadFileList){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);
        List<VideoInfoFilePost> uploadFileList1 = JsonUtils.convertJsonArray2List(uploadFileList, VideoInfoFilePost.class);

        VideoInfoPost videoInfoPost = new VideoInfoPost();
        videoInfoPost.setVideoId(videoId);
        videoInfoPost.setVideoName(videoName);
        videoInfoPost.setVideoCover(videoCover);
        videoInfoPost.setpCategoryId(pCategoryId);
        videoInfoPost.setCategoryId(categoryId);
        // 发布视频类型：自制/转载
        videoInfoPost.setPostType(postType);
        //转载视频才有的来源
        videoInfoPost.setOriginInfo(originInfo);
        videoInfoPost.setTags(tags);
        videoInfoPost.setIntroduction(introduction);
        videoInfoPost.setInteraction(interaction);

        videoInfoPost.setUserId(tokenUserInfoDto.getUserId());
        videoInfoPostService.saveVideoInfo(videoInfoPost,uploadFileList1);
        return getSuccessResponseVO(null);
    }

    /**
     * 加载已发布视频
     * @param
     * @return
     * @author fz
     * 2024/12/9 16:45
     */
    @RequestMapping("/loadVideoList")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO loadVideoPost(HttpServletRequest request,Integer status,Integer pageNo,String videoNameFuzzy){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);
        VideoInfoPostQuery videoInfoPostQuery = new VideoInfoPostQuery();
        videoInfoPostQuery.setUserId(tokenUserInfoDto.getUserId());
        videoInfoPostQuery.setPageNo(pageNo);
        videoInfoPostQuery.setOrderBy("v.create_time asc");
        if (status!=null){
            if (status == -1){ // 前端传来的status为进行中:-1
                // 排除审核成功与失败
                videoInfoPostQuery.setExcludeStatusArray(new Integer[]{VideoStatusEnum.STATUS3.getStatus(),VideoStatusEnum.STATUS4.getStatus()});
            }else {
                videoInfoPostQuery.setStatus(status);
            }
        }
        videoInfoPostQuery.setVideoNameFuzzy(videoNameFuzzy);
        videoInfoPostQuery.setQueryCountInfo(true);
        PaginationResultVO<VideoInfoPost> resultVO  = videoInfoPostService.findListByPage(videoInfoPostQuery);
        return getSuccessResponseVO(resultVO);
    }

    /**
     * 获取视频总数
     * @param 
     * @return 
     * @author fz
     * 2024/12/9 17:35
     */
    @RequestMapping("/getVideoCountInfo")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO getVideoCountInfo(HttpServletRequest request){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);
        VideoInfoPostQuery videoInfoPostQuery = new VideoInfoPostQuery();
        videoInfoPostQuery.setUserId(tokenUserInfoDto.getUserId());

        //查审核通过的数量
        videoInfoPostQuery.setStatus(VideoStatusEnum.STATUS3.getStatus());
        Integer auditPassCount = videoInfoPostService.findCountByParam(videoInfoPostQuery);

        //查审核失败的数量
        videoInfoPostQuery.setStatus(VideoStatusEnum.STATUS4.getStatus());
        Integer auditFailCount = videoInfoPostService.findCountByParam(videoInfoPostQuery);

        //查进行中的数量
        // 进行中的：前端传过来的状态为-1
        videoInfoPostQuery.setStatus(null);
        videoInfoPostQuery.setExcludeStatusArray(new Integer[]{VideoStatusEnum.STATUS3.getStatus(),VideoStatusEnum.STATUS4.getStatus()});
        Integer inProgress = videoInfoPostService.findCountByParam(videoInfoPostQuery);

        VideoStatusCountInfoVO countInfoVO = new VideoStatusCountInfoVO();
        countInfoVO.setAuditFailCount(auditFailCount);
        countInfoVO.setAuditPassCount(auditPassCount);
        countInfoVO.setInProgress(inProgress);
        return getSuccessResponseVO(countInfoVO);
    }

    /**
     * @description: 获取视频的信息
     * @param request
     * @param videoId
     * @return com.fz.entity.vo.ResponseVO
     * @author fz
     * 2025/1/13 16:04
     */
    @RequestMapping("/getVideoByVideoId")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO getVideoByVideoId(HttpServletRequest request,@NotEmpty String videoId){
        // 获取视频信息
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);
        VideoInfoPost videoInfoPost = videoInfoPostService.getVideoInfoPostByVideoId(videoId);
        if (videoInfoPost == null || !videoInfoPost.getUserId().equals(tokenUserInfoDto.getUserId())){
            throw new BusinessException(ResponseCodeEnum.CODE_404);
        }
        // 获取分p信息
        VideoInfoFilePostQuery videoInfoFilePostQuery = new VideoInfoFilePostQuery();
        videoInfoFilePostQuery.setVideoId(videoId);
        videoInfoFilePostQuery.setOrderBy("file_index asc");
        List<VideoInfoFilePost> videoInfoFilePostList = videoInfoFilePostService.findListByParam(videoInfoFilePostQuery);
        // 组装成vo返回
        VideoPostEditInfoVo vo = new VideoPostEditInfoVo();
        vo.setVideoInfo(videoInfoPost);
        vo.setVideoInfoFileList(videoInfoFilePostList);
        return getSuccessResponseVO(vo);
    }

    /**
     * @description: 保存视频的交互信息
     * @param request
     * @param videoId
     * @param interaction
     * @return com.fz.entity.vo.ResponseVO
     * @author fz
     * 2025/1/13 16:23
     */
    @RequestMapping("/saveVideoInteraction")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO saveVideoInteraction(HttpServletRequest request,@NotEmpty String videoId,String interaction){
        // 获取视频信息
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);
        videoInfoService.changeInteraction(videoId,tokenUserInfoDto.getUserId(),interaction);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/deleteVideo")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO saveVideoInteraction(HttpServletRequest request,@NotEmpty String videoId){
        // 获取视频信息
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);
        videoInfoService.deleteVideo(videoId,tokenUserInfoDto.getUserId());
        return getSuccessResponseVO(null);
    }

}
