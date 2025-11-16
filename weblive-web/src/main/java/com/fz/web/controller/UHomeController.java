package com.fz.web.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.fz.entity.constants.Constants;
import com.fz.entity.dto.TokenUserInfoDto;
import com.fz.entity.enums.PageSize;
import com.fz.entity.enums.UserActionTypeEnum;
import com.fz.entity.enums.VideoOrderTypeEnum;
import com.fz.entity.po.UserAction;
import com.fz.entity.po.UserFocus;
import com.fz.entity.po.UserInfo;
import com.fz.entity.po.VideoInfo;
import com.fz.entity.query.UserActionQuery;
import com.fz.entity.query.UserFocusQuery;
import com.fz.entity.query.VideoInfoQuery;
import com.fz.entity.vo.PaginationResultVO;
import com.fz.entity.vo.ResponseVO;
import com.fz.entity.vo.UserInfoVO;
import com.fz.service.UserActionService;
import com.fz.service.UserFocusService;
import com.fz.service.UserInfoService;
import com.fz.service.VideoInfoService;
import com.fz.utils.CopyTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.validation.constraints.*;

/**
 * @Author: fz
 * @Date: 2025/1/11 13:24
 * @Description:
 */
@RestController
@RequestMapping("/uhome")
@Validated
@Slf4j
public class UHomeController extends ABaseController {
    @Resource
    private UserInfoService userInfoService;
    @Resource
    private VideoInfoService videoInfoService;
    @Resource
    private UserFocusService userFocusService;
    @Resource
    private UserActionService userActionService;

    /**
     * @description: 获取主页用户信息
     * @param userId 访问该用户
     * @return com.fz.entity.vo.ResponseVO
     * @author fz
     * 2025/1/11 13:34
     */
    @RequestMapping("/getUserInfo")
    public ResponseVO getUserInfo(@NotEmpty String userId){
        String currentUserId = null;
        if (StpUtil.isLogin()) {
            currentUserId = StpUtil.getLoginIdAsString();
        }
        UserInfo userInfo = userInfoService.getUserDetailInfo(currentUserId, userId);

        UserInfoVO userInfoVO = CopyTools.copy(userInfo, UserInfoVO.class);

        return getSuccessResponseVO(userInfoVO);
    }

    /**
     * @description: 更新主页信息
     * @param nickName 昵称
     * @param avatar 头像
     * @param sex 性别
     * @param birthday 生日
     * @param school 学校
     * @param personIntroduction 个人介绍
     * @param noticeInfo 公告
     * @return com.fz.entity.vo.ResponseVO
     * @author fz
     * 2025/1/11 14:00
     */
    @RequestMapping("/updateUserInfo")
    @SaCheckLogin
    public ResponseVO updateUserInfo(@NotEmpty @Size(max = 20) String nickName,
                                     @NotEmpty @Size(max = 100) String avatar,
                                     @NotNull Integer sex,
                                     @Size(max = 10) String birthday,
                                     @Size(max = 150) String school,
                                     @Size(max = 80) String personIntroduction,
                                     @Size(max = 300) String noticeInfo){
        TokenUserInfoDto tokenUserInfoDto = getCurrentUser();
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(tokenUserInfoDto.getUserId());
        userInfo.setNickName(nickName);
        userInfo.setAvatar(avatar);
        userInfo.setSex(sex);
        userInfo.setBirthday(birthday);
        userInfo.setSchool(school);
        userInfo.setPersonIntroduction(personIntroduction);
        userInfo.setNoticeInfo(noticeInfo);

        userInfoService.updateUserInfo(userInfo, tokenUserInfoDto);
        return getSuccessResponseVO(null);
    }

    /**
     * @description: 更换主题
     * @param theme 主题图片序号
     * @return com.fz.entity.vo.ResponseVO
     * @author fz
     * 2025/1/11 15:01
     */
    @RequestMapping("/saveTheme")
    @SaCheckLogin
    public ResponseVO saveTheme(@Min(1) @Max(10) @NotNull Integer theme){
        //TokenUserInfoDto tokenUserInfoDto = getCurrentUser();
        UserInfo userInfo = new UserInfo();
        userInfo.setTheme(theme);
        userInfoService.updateUserInfoByUserId(userInfo,StpUtil.getLoginIdAsString());
        return getSuccessResponseVO(null);
    }

    /**
     * @description: 关注
     * @param focusUserId 被关注用户id
     * @return com.fz.entity.vo.ResponseVO
     * @author fz
     * 2025/1/11 15:01
     */
    @RequestMapping("/focus")
    @SaCheckLogin
    public ResponseVO focus(String focusUserId){
        userFocusService.focusUser(StpUtil.getLoginIdAsString(),focusUserId);
        return getSuccessResponseVO(null);
    }

    /**
     * @description: 取消关注
     * @param focusUserId
     * @return com.fz.entity.vo.ResponseVO
     * @author fz
     * 2025/1/11 15:02
     */
    @RequestMapping("/cancelFocus")
    @SaCheckLogin
    public ResponseVO cancelFocus(String focusUserId){
        userFocusService.cancelFocus(StpUtil.getLoginIdAsString(),focusUserId);
        return getSuccessResponseVO(null);
    }

    /**
     * @description: 查询关注列表
     * @param pageNo
     * @return com.fz.entity.vo.ResponseVO
     * @author fz
     * 2025/1/11 15:33
     */
    @RequestMapping("/loadFocusList")
    @SaCheckLogin
    public ResponseVO loadFocusList(Integer pageNo){
        //TokenUserInfoDto tokenUserInfoDto = getCurrentUser();

        UserFocusQuery userFocusQuery = new UserFocusQuery();
        userFocusQuery.setUserId(StpUtil.getLoginIdAsString());
        userFocusQuery.setPageNo(pageNo);
        userFocusQuery.setOrderBy("focus_time desc");
        userFocusQuery.setQueryType(Constants.ZERO);
        PaginationResultVO<UserFocus> resultVO = userFocusService.findListByPage(userFocusQuery);
        return getSuccessResponseVO(resultVO);
    }

    /**
     * @description: 查询粉丝列表
     * @param pageNo 页号
     * @return com.fz.entity.vo.ResponseVO
     * @author fz
     * 2025/1/11 16:58
     */
    @RequestMapping("/loadFansList")
    @SaCheckLogin
    public ResponseVO loadFansList(Integer pageNo){
        //tgTokenUserInfoDto tokenUserInfoDto = getCurrentUser();

        UserFocusQuery userFocusQuery = new UserFocusQuery();
        userFocusQuery.setFocusUserId(StpUtil.getLoginIdAsString());
        userFocusQuery.setPageNo(pageNo);
        userFocusQuery.setOrderBy("focus_time desc");
        userFocusQuery.setQueryType(Constants.ONE);
        PaginationResultVO<UserFocus> resultVO = userFocusService.findListByPage(userFocusQuery);
        return getSuccessResponseVO(resultVO);
    }

    /**
     * @description: 加载视频列表
     * @param userId 谁的主页
     * @param pageNo 可选参数 页号
     * @param type
     * @param videoName 视频名称（可根据该字段搜索视频）
     * @param orderType 排序字段（最新发布，最多播放，最多收藏）
     * @return com.fz.entity.vo.ResponseVO
     * @author fz
     * 2025/1/12 19:40
     */
    @RequestMapping("/loadVideoList")
    public ResponseVO loadFansList(@NotEmpty String userId,
                                   Integer pageNo,
                                   Integer type,
                                   String videoName,
                                   Integer orderType){
        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        if (type != null){
            //主页默认展示10条视频，投稿视频列表的分页也是10条
            videoInfoQuery.setPageSize(PageSize.SIZE10.getSize());
        }
        VideoOrderTypeEnum videoOrderTypeEnum = VideoOrderTypeEnum.getByType(orderType);
        if (videoOrderTypeEnum == null){
            // 如果没有传入排序类型，则默认按创建时间倒序
            // 这样可以保证主页视频列表的最新视频在最前面
            videoOrderTypeEnum = VideoOrderTypeEnum.CREATE_TIME;
        }
        videoInfoQuery.setOrderBy(videoOrderTypeEnum.getField() + " desc");
        videoInfoQuery.setVideoNameFuzzy(videoName);
        videoInfoQuery.setPageNo(pageNo);
        videoInfoQuery.setUserId(userId);
        PaginationResultVO<VideoInfo> resultVO = videoInfoService.findListByPage(videoInfoQuery);
        return getSuccessResponseVO(resultVO);
    }

    /**
     * @description: 查询收藏列表
     * @param userId 谁的主页
     * @param pageNo 页号
     * @return com.fz.entity.vo.ResponseVO
     * @author fz
     * 2025/1/12 20:00
     */
    @RequestMapping("/loadUserCollection")
    public ResponseVO loadFansList(@NotEmpty String userId, Integer pageNo){
        UserActionQuery actionQuery = new UserActionQuery();
        actionQuery.setActionType(UserActionTypeEnum.VIDEO_COLLECT.getType());
        actionQuery.setUserId(userId);
        actionQuery.setPageNo(pageNo);
        actionQuery.setOrderBy("action_time desc");
        actionQuery.setQueryVideoInfo(true);
        PaginationResultVO<UserAction> resultVO = userActionService.findListByPage(actionQuery);

        return getSuccessResponseVO(resultVO);
    }
}
