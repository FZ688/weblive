package com.fz.web.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.fz.annotation.RecordUserMessage;
import com.fz.entity.enums.MessageTypeEnum;
import com.fz.entity.constants.Constants;
import com.fz.entity.dto.TokenUserInfoDto;
import com.fz.entity.enums.CommentTopTypeEnum;
import com.fz.entity.enums.PageSize;
import com.fz.entity.enums.UserActionTypeEnum;
import com.fz.entity.po.UserAction;
import com.fz.entity.po.VideoComment;
import com.fz.entity.po.VideoInfo;
import com.fz.entity.query.UserActionQuery;
import com.fz.entity.query.VideoCommentQuery;
import com.fz.entity.vo.PaginationResultVO;
import com.fz.entity.vo.ResponseVO;
import com.fz.entity.vo.VideoCommentResultVO;
import com.fz.service.UserActionService;
import com.fz.service.VideoCommentService;
import com.fz.service.VideoInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: fz
 * @Date: 2025/1/8 13:55
 * @Description:
 */
@RestController
@RequestMapping("/comment")
@Validated
@Slf4j
public class VideoCommentController extends ABaseController{
    @Resource
    private VideoCommentService videoCommentService;
    @Resource
    private UserActionService userActionService;
    @Resource
    private VideoInfoService videoInfoService;

    /**
     * @description: 发表评论
     * @param videoId 哪个视频
     * @param content 评论内容
     * @param imgPath 评论可以是图像
     * @param replyCommentId 回复了谁
     * @return com.fz.entity.vo.ResponseVO
     * @author fz
     * 2025/1/8 14:32
     */
    @RequestMapping("/postComment")
    @SaCheckLogin
    @RecordUserMessage(messageType = MessageTypeEnum.COMMENT)
    public ResponseVO postComment(@NotEmpty String videoId,
                                  @NotEmpty @Size(max = 500) String content,
                                  @Size(max = 50) String imgPath,
                                  Integer replyCommentId){
        TokenUserInfoDto tokenUserInfoDto = getCurrentUser();
        VideoComment videoComment = new VideoComment();
        videoComment.setUserId(tokenUserInfoDto.getUserId());
        videoComment.setAvatar(tokenUserInfoDto.getAvatar());
        videoComment.setNickName(tokenUserInfoDto.getNickName());
        videoComment.setVideoId(videoId);
        videoComment.setContent(content);
        videoComment.setImgPath(imgPath);
        videoCommentService.postComment(videoComment,replyCommentId);

        // 发布评论无需再查 直接返回给前端
        return getSuccessResponseVO(videoComment);
    }

    /**
     * @description: 加载评论区
     * @param videoId 视频id
     * @param pageNo 页号
     * @param orderType 根据什么排序
     * @return com.fz.entity.vo.ResponseVO
     * @author fz
     * 2025/1/8 15:55
     */
    @RequestMapping("/loadComment")
    public ResponseVO loadComment(@NotEmpty String videoId,
                                  Integer pageNo,
                                  Integer orderType){

        VideoInfo videoInfo = videoInfoService.getVideoInfoByVideoId(videoId);
        // 如果关闭互动
        if (videoInfo.getInteraction() != null && videoInfo.getInteraction().contains(Constants.ONE.toString())){
            return getSuccessResponseVO(new ArrayList<>());
        }
        // 获取评论
        VideoCommentQuery commentQuery = new VideoCommentQuery();
        // 是否加载子评论
        commentQuery.setLoadChildren(true);
        commentQuery.setVideoId(videoId);
        commentQuery.setPageNo(pageNo);
        commentQuery.setPageSize(PageSize.SIZE15.getSize());
        // 查一级评论
        commentQuery.setpCommentId(0);
        //comment_id是自增的，默认是热门按点赞数排序
        String orderBy = orderType == null || orderType == 0 ? "like_count desc,comment_id desc" : "comment_id desc";
        commentQuery.setOrderBy(orderBy);

        PaginationResultVO<VideoComment> commentData = videoCommentService.findListByPage(commentQuery);

        //pageNo为null时，表示首次加载评论区,只需在第一页展示置顶评论
        if (pageNo == null){
            List<VideoComment> topCommentList = topComment(videoId);
            // 如果存在置顶评论
            if (!topCommentList.isEmpty()){
                // 获取除了置顶评论的所有评论（此时topType = 1的评论还没被真正置顶）
                List<VideoComment> commentList = commentData.getList().stream()
                        .filter(item -> !item.getCommentId().equals(topCommentList.get(0).getCommentId()))
                        .collect(Collectors.toList());
                // 然后将置顶评论放到最前面（置顶）
                commentList.addAll(0,topCommentList);
                commentData.setList(commentList);
            }
        }


        // 获取用户对评论的行为
        List<UserAction> userActionList = new ArrayList<>();
        // 如果登录了
        if (StpUtil.isLogin()) {
            // 取出来
            UserActionQuery userActionQuery = new UserActionQuery();
            userActionQuery.setUserId(StpUtil.getLoginIdAsString());
            userActionQuery.setVideoId(videoId);
            userActionQuery.setActionTypeArray(new Integer[]{
                    UserActionTypeEnum.COMMENT_LIKE.getType(),
                    UserActionTypeEnum.COMMENT_HATE.getType()
            });
            userActionList = userActionService.findListByParam(userActionQuery);
        }
        // 组装成VO
        VideoCommentResultVO resultVO = new VideoCommentResultVO();
        resultVO.setCommentData(commentData);
        resultVO.setUserActionList(userActionList);

        return getSuccessResponseVO(resultVO);
    }

    /**
     * @description: 查询置顶评论
     * @param videoId 视频id
     * @return java.util.List<com.fz.entity.po.VideoComment>
     * @author fz
     * 2025/1/10 20:37
     */
    private List<VideoComment> topComment(String videoId){
        VideoCommentQuery commentQuery = new VideoCommentQuery();
        commentQuery.setVideoId(videoId);
        // 查询置顶评论
        commentQuery.setTopType(CommentTopTypeEnum.TOP.getType());
        commentQuery.setLoadChildren(true);

        List<VideoComment> videoCommentList = videoCommentService.findListByParam(commentQuery);
        return videoCommentList;
    }

    /**
     * @description: 将评论置顶
     * @param commentId 评论id
     * @return com.fz.entity.vo.ResponseVO
     * @author fz
     * 2025/1/10 22:55
     */
    @RequestMapping("/topComment")
    @SaCheckLogin
    public ResponseVO topComment(@NotNull Integer commentId){
        //TokenUserInfoDto tokenUserInfoDto = getCurrentUser();
        videoCommentService.topComment(commentId, StpUtil.getLoginIdAsString());
        return getSuccessResponseVO(null);
    }

    /**
     * @description: 取消置顶评论
     * @param commentId 评论id
     * @return com.fz.entity.vo.ResponseVO
     * @author fz
     * 2025/1/10 22:56
     */
    @RequestMapping("/cancelTopComment")
    @SaCheckLogin
    public ResponseVO cancelTopComment(@NotNull Integer commentId){
        //TokenUserInfoDto tokenUserInfoDto = getCurrentUser();
        videoCommentService.cancelTopComment(commentId,StpUtil.getLoginIdAsString());
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/userDelComment")
    @SaCheckLogin
    public ResponseVO deleteComment(@NotNull Integer commentId){
        //TokenUserInfoDto tokenUserInfoDto = getCurrentUser();
        videoCommentService.deleteComment(commentId,StpUtil.getLoginIdAsString());
        return getSuccessResponseVO(null);
    }
}
