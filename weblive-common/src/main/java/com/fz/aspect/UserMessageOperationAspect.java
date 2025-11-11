package com.fz.aspect;

import com.fz.annotation.RecordUserMessage;
import com.fz.component.RedisComponent;
import com.fz.entity.constants.Constants;
import com.fz.entity.dto.TokenUserInfoDto;
import com.fz.entity.enums.MessageTypeEnum;
import com.fz.entity.enums.ResponseCodeEnum;
import com.fz.entity.enums.UserActionTypeEnum;
import com.fz.entity.vo.ResponseVO;
import com.fz.exception.BusinessException;
import com.fz.service.UserMessageService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * @Author: fz
 * @Date: 2025/1/14 16:28
 * @Description: 发布信息切面
 */
@Aspect
@Component
@Slf4j
public class UserMessageOperationAspect {
    @Resource
    private RedisComponent redisComponent;
    @Resource
    private UserMessageService userMessageService;

    private static final String PARAMETERS_VIDEO_ID = "videoId";
    private static final String PARAMETERS_ACTION_TYPE = "actionType";
    private static final String PARAMETERS_REPLY_COMMENTID = "replyCommentId";
    private static final String PARAMETERS_AUDIT_REJECT_REASON = "reason";
    private static final String PARAMETERS_CONTENT = "content";


    /**
     * @description: 给用户发送消息
     * @param point 切点
     * @return com.fz.entity.vo.ResponseVO
     * @author fz
     * 2025/1/14 16:46
     */
    @Around("@annotation(com.fz.annotation.RecordUserMessage)")
    public ResponseVO interceptorDo(ProceedingJoinPoint point) throws Throwable {
        try {
            // 执行方法
            ResponseVO responseVO = (ResponseVO) point.proceed();
            // 获取方法
            Method method = ((MethodSignature) point.getSignature()).getMethod();
            // 获取注解
            RecordUserMessage recordUserMessage = method.getAnnotation(RecordUserMessage.class);
            // 保存信息
            if (recordUserMessage != null) {
                saveMessage(recordUserMessage,point.getArgs(),method.getParameters());
            }
            return responseVO;
        } catch (Exception e) {
            log.error("全局拦截器异常", e);
            throw e;
        } catch (Throwable e) {
            log.error("全局拦截器异常", e);
            throw new BusinessException(ResponseCodeEnum.CODE_500);
        }
    }

    /**
     * @description: 将消息存到消息表中(点赞,收藏,评论,系统消息)
     * 不同的方法参数是不一样的
     * @param recordUserMessage 注解
     * @param args 代理的方法的参数的值
     * @param parameters 代理的方法的参数
     * @author fz
     * 2025/1/14 16:47
     */
    private void saveMessage(RecordUserMessage recordUserMessage, Object[] args, Parameter[] parameters) {
        String videoId = null;
        Integer actionType = null;
        Integer replyCommentId =null;
        String content = null;

        // 1) 解析参数
        for (int i = 0; i < parameters.length; i++) {
            String name = parameters[i].getName();
            if (PARAMETERS_VIDEO_ID.equals(name)) {
                videoId = (String) args[i];
            } else if (PARAMETERS_ACTION_TYPE.equals(name)) {
                actionType = (Integer) args[i];
            } else if (PARAMETERS_REPLY_COMMENTID.equals(name)) {
                replyCommentId = (Integer) args[i];
            } else if (PARAMETERS_CONTENT.equals(name)) {
                content = (String) args[i];
            } else if (PARAMETERS_AUDIT_REJECT_REASON.equals(name)) {
                content = (String) args[i];
            }
        }
        MessageTypeEnum messageTypeEnum = recordUserMessage.messageType();
        // 对于收藏与点赞,使用的是同一个接口，我们加的是@recordUserMessage(messageType = like)
        // 因此有必要判断一下当前到底是哪一种信息
        if (UserActionTypeEnum.VIDEO_COLLECT.getType().equals(actionType)){
            messageTypeEnum = MessageTypeEnum.COLLECTION;
        }
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto();
        // 保存用户消息,管理端获取不到用户信息，系统发送，不需要用户id
        userMessageService.saveUserMessage(videoId,tokenUserInfoDto == null ? null : tokenUserInfoDto.getUserId(),messageTypeEnum,content,replyCommentId);
    }
    public TokenUserInfoDto getTokenUserInfoDto(){
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String token = request.getHeader(Constants.TOKEN_WEB);
        return redisComponent.getTokenInfo(token);
    }
}
