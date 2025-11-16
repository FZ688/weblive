package com.fz.web.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.fz.entity.dto.UserMessageCountDto;
import com.fz.entity.enums.MessageReadTypeEnum;
import com.fz.entity.po.UserMessage;
import com.fz.entity.query.UserMessageQuery;
import com.fz.entity.vo.PaginationResultVO;
import com.fz.entity.vo.ResponseVO;
import com.fz.service.UserMessageService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * @Author: fz
 * @Date: 2024/12/12 21:50
 * @Description:
 */
@RestController
@RequestMapping("/message")
@Validated
@SaCheckLogin
public class UserMessageController extends ABaseController{
    @Resource
    private UserMessageService userMessageService;

    /**
     * @description: 获取未读消息数量
     * @return com.fz.entity.vo.ResponseVO
     * @author fz
     * 2025/1/14 18:47
     */
    @RequestMapping("/getNoReadCount")
    public ResponseVO getNoReadCount(){
        //TokenUserInfoDto tokenUserInfoDto = getCurrentUser();

        UserMessageQuery userMessageQuery = new UserMessageQuery();
        userMessageQuery.setUserId(StpUtil.getLoginIdAsString());
        userMessageQuery.setReadType(MessageReadTypeEnum.NO_READ.getType());
        Integer count = userMessageService.findCountByParam(userMessageQuery);

        return getSuccessResponseVO(count);
    }

    /**
     * @description: 获取各种类型的未读消息
     * @return com.fz.entity.vo.ResponseVO
     * @author fz
     * 2025/1/14 20:40
     */
    @RequestMapping("/getNoReadCountGroup")
    public ResponseVO getNoReadCountGroup(){
        //TokenUserInfoDto tokenUserInfoDto = getCurrentUser();
        List<UserMessageCountDto> dtoList = userMessageService.getMessageTypeNoReadCount(StpUtil.getLoginIdAsString());
        return getSuccessResponseVO(dtoList);
    }


    /**
     * @description: 读取消息/标记消息为已读
     * @param messageType 消息类型
     * @return com.fz.entity.vo.ResponseVO
     * @author fz
     * 2025/1/14 20:48
     */
    @RequestMapping("/readAll")
    public ResponseVO readAll(@NotNull Integer messageType){
        //TokenUserInfoDto tokenUserInfoDto = getCurrentUser();
        UserMessageQuery userMessageQuery = new UserMessageQuery();
        userMessageQuery.setUserId(StpUtil.getLoginIdAsString());
        userMessageQuery.setMessageType(messageType);

        UserMessage userMessage = new UserMessage();
        userMessage.setReadType(MessageReadTypeEnum.READ.getType());

        userMessageService.updateByParam(userMessage,userMessageQuery);

        return getSuccessResponseVO(null);
    }

    /**
     * @description: 获取消息
     * @param pageNo 页码
     * @param messageType 消息的类型
     * @return com.fz.entity.vo.ResponseVO
     * @author fz
     * 2025/1/14 20:46
     */
    @RequestMapping("/loadMessage")
    public ResponseVO loadMessage(Integer pageNo,@NotNull Integer messageType){
        //TokenUserInfoDto tokenUserInfoDto = getCurrentUser();
        UserMessageQuery userMessageQuery = new UserMessageQuery();
        userMessageQuery.setUserId(StpUtil.getLoginIdAsString());
        userMessageQuery.setPageNo(pageNo);
        userMessageQuery.setMessageType(messageType);
        userMessageQuery.setOrderBy("message_id desc");
        PaginationResultVO<UserMessage> resultVO = userMessageService.findListByPage(userMessageQuery);
        return getSuccessResponseVO(resultVO);
    }


    @RequestMapping("/delMessage")
    public ResponseVO delMessage(@NotNull Integer messageId){
        //TokenUserInfoDto tokenUserInfoDto = getCurrentUser();
        UserMessageQuery userMessageQuery = new UserMessageQuery();
        userMessageQuery.setUserId(StpUtil.getLoginIdAsString());
        userMessageQuery.setMessageId(messageId);

        userMessageService.deleteByParam(userMessageQuery);
        return getSuccessResponseVO(null);
    }
}
