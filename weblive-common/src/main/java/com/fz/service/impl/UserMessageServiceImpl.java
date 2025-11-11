package com.fz.service.impl;

import java.util.Date;
import java.util.List;


import com.fz.entity.dto.UserMessageCountDto;
import com.fz.entity.dto.UserMessageExtendDto;
import com.fz.entity.enums.MessageReadTypeEnum;
import com.fz.entity.enums.MessageTypeEnum;
import com.fz.entity.po.VideoComment;
import com.fz.entity.po.VideoInfo;
import com.fz.entity.po.VideoInfoPost;
import com.fz.entity.query.*;
import com.fz.mappers.VideoCommentMapper;
import com.fz.mappers.VideoInfoMapper;
import com.fz.mappers.VideoInfoPostMapper;
import com.fz.utils.JsonUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.fz.entity.enums.PageSize;
import com.fz.entity.po.UserMessage;
import com.fz.entity.vo.PaginationResultVO;
import com.fz.mappers.UserMessageMapper;
import com.fz.service.UserMessageService;
import com.fz.utils.StringTools;


/**
 * 用户消息表 业务接口实现
 */
@Service("userMessageService")
public class UserMessageServiceImpl implements UserMessageService {

	@Resource
	private UserMessageMapper<UserMessage, UserMessageQuery> userMessageMapper;
	@Resource
	private VideoInfoMapper<VideoInfo, VideoInfoQuery> videoInfoMapper;
	@Resource
	private VideoCommentMapper<VideoComment, VideoCommentQuery> videoCommentMapper;
	@Resource
	private VideoInfoPostMapper<VideoInfoPost, VideoInfoPostQuery> videoInfoPostMapper;
	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<UserMessage> findListByParam(UserMessageQuery param) {
		return this.userMessageMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(UserMessageQuery param) {
		return this.userMessageMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<UserMessage> findListByPage(UserMessageQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<UserMessage> list = this.findListByParam(param);
		PaginationResultVO<UserMessage> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(UserMessage bean) {
		return this.userMessageMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<UserMessage> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.userMessageMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<UserMessage> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.userMessageMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(UserMessage bean, UserMessageQuery param) {
		StringTools.checkParam(param);
		return this.userMessageMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(UserMessageQuery param) {
		StringTools.checkParam(param);
		return this.userMessageMapper.deleteByParam(param);
	}

	/**
	 * 根据MessageId获取对象
	 */
	@Override
	public UserMessage getUserMessageByMessageId(Integer messageId) {
		return this.userMessageMapper.selectByMessageId(messageId);
	}

	/**
	 * 根据MessageId修改
	 */
	@Override
	public Integer updateUserMessageByMessageId(UserMessage bean, Integer messageId) {
		return this.userMessageMapper.updateByMessageId(bean, messageId);
	}

	/**
	 * 根据MessageId删除
	 */
	@Override
	public Integer deleteUserMessageByMessageId(Integer messageId) {
		return this.userMessageMapper.deleteByMessageId(messageId);
	}

	/**
	 * 给用户发送消息
	 * @param videoId 哪个视频
	 * @param sendUserId 发送消息的人
	 * @param messageTypeEnum 消息的类型
	 * @param content 消息内容
	 * @param replyCommentId 被回复的评论id,即自己的评论id
	 */
	@Override
	@Async
	public void saveUserMessage(String videoId, String sendUserId, MessageTypeEnum messageTypeEnum, String content, Integer replyCommentId) {
		VideoInfo videoInfo = videoInfoMapper.selectByVideoId(videoId);
		if (videoInfo == null){
			return;
		}
		// 设置扩展信息
		UserMessageExtendDto extendDto = new UserMessageExtendDto();
		extendDto.setMessageContent(content);

		// 视频的主人
		String userId = videoInfo.getUserId();

		// 收藏与点赞，已经记录的，不再记录
		if (ArrayUtils.contains(new Integer[]{MessageTypeEnum.LIKE.getType(),MessageTypeEnum.COLLECTION.getType()},messageTypeEnum.getType())){
			UserMessageQuery userMessageQuery = new UserMessageQuery();
			userMessageQuery.setMessageType(messageTypeEnum.getType());
			userMessageQuery.setUserId(userId);
			userMessageQuery.setSendUserId(sendUserId);
			Integer count = userMessageMapper.selectCount(userMessageQuery);
			if (count > 0){
				return;
			}
		}
		UserMessage userMessage = new UserMessage();
		userMessage.setVideoId(videoId);
		userMessage.setReadType(MessageReadTypeEnum.NO_READ.getType());
		userMessage.setCreateTime(new Date());
		userMessage.setMessageType(messageTypeEnum.getType());
		userMessage.setSendUserId(sendUserId);
		// 评论特殊处理 应该是发布评论的人接受消息
		if (replyCommentId != null){
			VideoComment comment = videoCommentMapper.selectByCommentId(replyCommentId);
			if (comment != null){
				userId = comment.getUserId();
				// 你发布的评论的内容
				extendDto.setMessageContentReply(comment.getContent());
			}
		}
		userMessage.setUserId(userId);
		// 不能给自己发消息
		if (userId.equals(sendUserId)){
			return;
		}
		// 系统消息
		if (MessageTypeEnum.SYS == messageTypeEnum){
			VideoInfoPost videoInfoPost = videoInfoPostMapper.selectByVideoId(videoId);
			// 设置状态 因为要通知用户视频状态
			extendDto.setAuditStatus(videoInfoPost.getStatus());
		}

		userMessage.setExtendJson(JsonUtils.convertObj2Json(extendDto));
		userMessageMapper.insert(userMessage);
	}

	@Override
	public List<UserMessageCountDto> getMessageTypeNoReadCount(String userId) {
		return userMessageMapper.getMessageTypeNoReadCount(userId);
	}
}