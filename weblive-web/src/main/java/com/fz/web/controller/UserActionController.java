package com.fz.web.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.fz.annotation.RecordUserMessage;
import com.fz.entity.enums.MessageTypeEnum;
import com.fz.entity.constants.Constants;
import com.fz.entity.po.UserAction;
import com.fz.entity.vo.ResponseVO;
import com.fz.service.UserActionService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.validation.constraints.*;

/**
 * @Author: fz
 * @Date: 2024/12/12 21:50
 * @Description:
 */
@RestController
@RequestMapping("/userAction")
@Validated
@SaCheckLogin
public class UserActionController extends ABaseController{
    @Resource
    private UserActionService userActionService;

    /**
     * @description:
     * @param videoId 视频id
     * @param actionType 行为类型 点赞/收藏/投币/评论点赞/评论点踩
     * @param actionCount 行为数量
     * @param commentId 评论id
     * @return com.fz.entity.vo.ResponseVO
     * @author fz
     * 2024/12/12 21:56
     */
    @RequestMapping("/doAction")
    @RecordUserMessage(messageType = MessageTypeEnum.LIKE)
    public ResponseVO doAction(@NotEmpty String videoId,
                               @NotNull Integer actionType,
                               @Max(2) @Min(1) Integer actionCount,
                               Integer commentId){
        UserAction userAction = new UserAction();
        userAction.setUserId(StpUtil.getLoginIdAsString());
        userAction.setVideoId(videoId);
        userAction.setActionType(actionType);
        Integer count = actionCount == null ? Constants.ONE : actionCount;
        userAction.setActionCount(count);
        commentId = commentId == null ? 0 : commentId;
        userAction.setCommentId(commentId);
        userActionService.saveAction(userAction);
        return getSuccessResponseVO(null);
    }
}
