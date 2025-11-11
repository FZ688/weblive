package com.fz.web.controller;

import com.fz.component.EsSearchComponent;
import com.fz.component.RedisComponent;
import com.fz.entity.constants.Constants;
import com.fz.entity.dto.TokenUserInfoDto;
import com.fz.entity.enums.*;
import com.fz.entity.po.UserAction;
import com.fz.entity.po.VideoInfo;
import com.fz.entity.po.VideoInfoFile;
import com.fz.entity.query.UserActionQuery;
import com.fz.entity.query.VideoInfoFileQuery;
import com.fz.entity.query.VideoInfoQuery;
import com.fz.entity.vo.PaginationResultVO;
import com.fz.entity.vo.ResponseVO;
import com.fz.entity.vo.VideoInfoVO;
import com.fz.exception.BusinessException;
import com.fz.service.UserActionService;
import com.fz.service.VideoInfoFileService;
import com.fz.service.VideoInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: fz
 * @Date: 2024/12/9 21:54
 * @Description:
 */
@Slf4j
@RestController
@RequestMapping("/video")
public class VideoController extends ABaseController {
    @Resource
    private VideoInfoService videoInfoService;
    @Resource
    private VideoInfoFileService videoInfoFileService;
    @Resource
    private UserActionService userActionService;
    @Resource
    private RedisComponent redisComponent;
    @Resource
    private EsSearchComponent esSearchComponent;



    /**
     * 加载已推荐视频
     * @return ResponseVO
     * @author fz
     * 2024/12/9 22:02
     */
    @RequestMapping("/loadRecommendVideo")
    public ResponseVO loadRecommendVideo() {
        // 构建查询条件
        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        videoInfoQuery.setOrderBy("create_time desc");
        // 是否添加这个参数(获取推荐视频列表，视频下方会显示发布视频的作者信息，所以这个查询需要关联用户信息表)
        videoInfoQuery.setQueryUserInfo(true);
        videoInfoQuery.setRecommendType(VideoRecommendTypeEnum.RECOMMEND.getType());
        List<VideoInfo> recommendVideoList = this.videoInfoService.findListByParam(videoInfoQuery);
        return getSuccessResponseVO(recommendVideoList);
    }


    /**
     * 加载未推荐的视频
     *
     * @param
     * @return
     * @author fz
     * 2024/12/9 22:48
     */
    @RequestMapping("/loadVideo")
    public ResponseVO loadVideo(Integer pCategoryId, Integer categoryId, Integer pageNo) {
        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        videoInfoQuery.setpCategoryId(pCategoryId);
        videoInfoQuery.setCategoryId(categoryId);
        videoInfoQuery.setPageNo(pageNo);
        videoInfoQuery.setQueryUserInfo(true);
        videoInfoQuery.setOrderBy("create_time desc");
        videoInfoQuery.setRecommendType(VideoRecommendTypeEnum.NO_RECOMMEND.getType());
        // 分页查询
        PaginationResultVO<VideoInfo> resultVO = this.videoInfoService.findListByPage(videoInfoQuery);
        return getSuccessResponseVO(resultVO);
    }


    /**
     * @description: 获取视频详细信息
     * @param videoId 视频id
     * @return com.fz.entity.vo.ResponseVO
     * @author fz
     * 2024/12/10 21:43
     */
    @RequestMapping("/getVideoInfo")
    public ResponseVO getVideoInfo(HttpServletRequest request,@NotEmpty String videoId) {
        // 查出视频详细信息
        VideoInfo videoInfo = this.videoInfoService.getVideoInfoByVideoId(videoId);
        if (videoInfo == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_404);
        }
        // 获取用户所有的行为:是否点赞 投币 收藏
        List<UserAction> userActionList = new ArrayList<>();
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);
        // 如果登录了
        if (tokenUserInfoDto != null){
            // 取出来
            UserActionQuery userActionQuery = new UserActionQuery();
            userActionQuery.setUserId(tokenUserInfoDto.getUserId());
            userActionQuery.setVideoId(videoId);
            userActionQuery.setActionTypeArray(new Integer[]{
                    UserActionTypeEnum.VIDEO_LIKE.getType(),
                    UserActionTypeEnum.VIDEO_COLLECT.getType(),
                    UserActionTypeEnum.VIDEO_COIN.getType()
            });
            userActionList = userActionService.findListByParam(userActionQuery);
        }
        VideoInfoVO videoInfoVO = new VideoInfoVO(videoInfo,userActionList);
        return getSuccessResponseVO(videoInfoVO);
    }

    /**
     * @description: 加载视频分P
     * @param videoId 视频ID
     * @return com.fz.entity.vo.ResponseVO
     * @author fz
     * 2024/12/10 17:29
     */
    @RequestMapping("/loadVideoPList")
    public ResponseVO loadVideoPList(@NotEmpty String videoId){
        VideoInfoFileQuery videoInfoFileQuery = new VideoInfoFileQuery();
        videoInfoFileQuery.setVideoId(videoId);
        videoInfoFileQuery.setOrderBy("file_index asc");
        List<VideoInfoFile> resultVO  = videoInfoFileService.findListByParam(videoInfoFileQuery);
        return getSuccessResponseVO(resultVO);
    }
    /**
     * @description:  轮询上报视频在线观看人数
     * @param fileId 分p的id
     * @param deviceId 设备id
     * @return com.fz.entity.vo.ResponseVO
     * @author fz
     * 2024/12/10 17:46
     */
    @RequestMapping("/reportVideoPlayOnline")
    public ResponseVO reportVideoPlayOnline(@NotEmpty String fileId,@NotEmpty String deviceId){
        return getSuccessResponseVO(redisComponent.reportVideoOnline(fileId,deviceId));
    }

    /**
     * @description: 根据关键字搜索视频
     * @param keyword
     * @param orderType 根据什么排序
     * @param pageNo
     * @return com.fz.entity.vo.ResponseVO
     * @author fz
     * 2025/1/13 22:11
     */
    @RequestMapping("/search")
    public ResponseVO search(@NotEmpty String keyword,Integer orderType,Integer pageNo){
        // 记录搜索热词
        redisComponent.addKeywordCount(keyword);
        // 用ES搜索
        PaginationResultVO<VideoInfo> resultVO = esSearchComponent.search(true, keyword, orderType, pageNo, PageSize.SIZE30.getSize());
        return getSuccessResponseVO(resultVO);
    }

    /**
     * @description: 获取推荐视频
     * @param keyword 关键词：前端传过来的一般为当前播放视频的标题
     * @param videoId 视频id
     * @return com.fz.entity.vo.ResponseVO
     * @author fz
     * 2025/1/14 21:59
     */
    @RequestMapping("/getVideoRecommend")
    public ResponseVO search(@NotEmpty String keyword,@NotEmpty String videoId){
        List<VideoInfo> videoInfoList = esSearchComponent.search(false, keyword, SearchOrderTypeEnum.VIDEO_PLAY.getType(), 1, PageSize.SIZE10.getSize()).getList();
        // 把当前视频过滤掉
        videoInfoList = videoInfoList.stream()
                .filter(i -> !i.getVideoId().equals(videoId))
                .collect(Collectors.toList());
        return getSuccessResponseVO(videoInfoList);
    }

    /**
     * @description: 获取搜索热词列表
     * @return com.fz.entity.vo.ResponseVO
     * @author fz
     * 2025/1/14 11:22
     */
    @RequestMapping("/getSearchKeywordTop")
    public ResponseVO getSearchKeywordTop(){
        List<String> keywordTop = redisComponent.getKeywordTop(Constants.LENGTH_10);
        return getSuccessResponseVO(keywordTop);
    }

    /**
     * @description: 加载播放量高的视频
     * @param  pageNo
     * @return com.fz.entity.vo.ResponseVO
     * @author fz
     * 2025/1/14 11:45
     */
    @RequestMapping("/loadHotVideoList")
    public ResponseVO loadHotVideoList(Integer pageNo){
        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        videoInfoQuery.setPageNo(pageNo);
        // 按播放量升序
        videoInfoQuery.setOrderBy("play_count asc");
        videoInfoQuery.setQueryUserInfo(true);
        // 只要最近24个小时的
        videoInfoQuery.setLastPlayHour(Constants.HOUR_24);
        PaginationResultVO<VideoInfo> resultVO = videoInfoService.findListByPage(videoInfoQuery);
        return getSuccessResponseVO(resultVO);
    }
}
