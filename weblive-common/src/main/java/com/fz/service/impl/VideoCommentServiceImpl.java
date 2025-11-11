package com.fz.service.impl;

import java.util.Date;
import java.util.List;

import com.fz.entity.constants.Constants;
import com.fz.entity.enums.CommentTopTypeEnum;
import com.fz.entity.enums.ResponseCodeEnum;
import com.fz.entity.enums.UserActionTypeEnum;
import com.fz.entity.po.UserInfo;
import com.fz.entity.po.VideoInfo;
import com.fz.entity.query.UserInfoQuery;
import com.fz.entity.query.VideoInfoQuery;
import com.fz.exception.BusinessException;
import com.fz.mappers.UserInfoMapper;
import com.fz.mappers.VideoInfoMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import com.fz.entity.enums.PageSize;
import com.fz.entity.query.VideoCommentQuery;
import com.fz.entity.po.VideoComment;
import com.fz.entity.vo.PaginationResultVO;
import com.fz.entity.query.SimplePage;
import com.fz.mappers.VideoCommentMapper;
import com.fz.service.VideoCommentService;
import com.fz.utils.StringTools;
import org.springframework.transaction.annotation.Transactional;


/**
 * 评论 业务接口实现
 */
@Service("videoCommentService")
public class VideoCommentServiceImpl implements VideoCommentService {

	@Resource
	private VideoCommentMapper<VideoComment, VideoCommentQuery> videoCommentMapper;
	@Resource
	private VideoInfoMapper<VideoInfo, VideoInfoQuery> videoInfoMapper;
	@Resource
	private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<VideoComment> findListByParam(VideoCommentQuery param) {
		if (param.getLoadChildren() != null && param.getLoadChildren()) {
			return this.videoCommentMapper.selectListWithChildren(param);
		}
		return this.videoCommentMapper.selectList(param);
	}


	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(VideoCommentQuery param) {
		return this.videoCommentMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<VideoComment> findListByPage(VideoCommentQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<VideoComment> list = this.findListByParam(param);
		PaginationResultVO<VideoComment> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(VideoComment bean) {
		return this.videoCommentMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<VideoComment> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.videoCommentMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<VideoComment> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.videoCommentMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(VideoComment bean, VideoCommentQuery param) {
		StringTools.checkParam(param);
		return this.videoCommentMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(VideoCommentQuery param) {
		StringTools.checkParam(param);
		return this.videoCommentMapper.deleteByParam(param);
	}

	/**
	 * 根据CommentId获取对象
	 */
	@Override
	public VideoComment getVideoCommentByCommentId(Integer commentId) {
		return this.videoCommentMapper.selectByCommentId(commentId);
	}

	/**
	 * 根据CommentId修改
	 */
	@Override
	public Integer updateVideoCommentByCommentId(VideoComment bean, Integer commentId) {
		return this.videoCommentMapper.updateByCommentId(bean, commentId);
	}

	/**
	 * 根据CommentId删除
	 */
	@Override
	public Integer deleteVideoCommentByCommentId(Integer commentId) {
		return this.videoCommentMapper.deleteByCommentId(commentId);
	}


	@Override
	public void postComment(VideoComment comment, Integer replyCommentId) {
		VideoInfo videoInfo = videoInfoMapper.selectByVideoId(comment.getVideoId());
		// 参数错误
		if (videoInfo == null){
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		// 关闭互动
		if (videoInfo.getInteraction() != null && videoInfo.getInteraction().contains(Constants.ZERO.toString())){
			throw new BusinessException("UP主已关闭评论区");
		}
		if (replyCommentId != null){
			// 拿到回复的评论对象
			VideoComment replyComment = getVideoCommentByCommentId(replyCommentId);
			if (replyComment == null || !replyComment.getVideoId().equals(comment.getVideoId())){
				throw new BusinessException(ResponseCodeEnum.CODE_600);
			}
			// 如果我们回复的评论是一级评论
			if (replyComment.getpCommentId() == 0){
				// 那么当前评论的父评论就是它
				comment.setpCommentId(replyComment.getCommentId());
			}else {
				// 如果回复评论不是一级评论 那必然回复的是二级评论
				 //那么父评论应该是这个二级评论上的一级评论
				/*
					区分父评论与回复评论
					二级评论的父评论是一级评论 回复评论可以是一级评论 也可以是一级评论下的二级评论
					一级评论的父评论id是0  没有回复评论
				 */
				comment.setpCommentId(replyComment.getpCommentId());
				// 作用是拿到回复对象的信息
				comment.setReplyUserId(replyComment.getUserId());
			}
			UserInfo userInfo = userInfoMapper.selectByUserId(replyComment.getUserId());
			// fz回复了xxx
			comment.setReplyNickName(userInfo.getNickName());
		}else {
			// 说明是一级评论
			comment.setpCommentId(0);
		}
		comment.setPostTime(new Date());
		comment.setVideoUserId(videoInfo.getUserId());
		this.videoCommentMapper.insert(comment);
		// 增加评论数量（只算一级）
		if (comment.getpCommentId() == 0){
			this.videoInfoMapper.updateCountInfo(comment.getVideoId(), UserActionTypeEnum.VIDEO_COMMENT.getField(), 1);
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void topComment(Integer commentId, String userId) {
		// 取消置顶的评论
		this.cancelTopComment(commentId,userId);
		// 将当前评论置顶
		VideoComment videoComment = new VideoComment();
		videoComment.setTopType(CommentTopTypeEnum.TOP.getType());
		videoCommentMapper.updateByCommentId(videoComment,commentId);
	}

	@Override
	public void cancelTopComment(Integer commentId, String userId) {
		VideoComment dbVideoComment = videoCommentMapper.selectByCommentId(commentId);
		if (dbVideoComment == null){
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		VideoInfo videoInfo = videoInfoMapper.selectByVideoId(dbVideoComment.getVideoId());
		if (videoInfo == null){
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		if (!videoInfo.getUserId().equals(userId)){
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		VideoComment videoComment = new VideoComment();
		videoComment.setTopType(CommentTopTypeEnum.NO_TOP.getType());

		VideoCommentQuery videoCommentQuery = new VideoCommentQuery();
		videoCommentQuery.setVideoId(dbVideoComment.getVideoId());
		videoCommentQuery.setTopType(CommentTopTypeEnum.TOP.getType());

		videoCommentMapper.updateByParam(videoComment,videoCommentQuery);
	}

	@Override
	public void deleteComment(Integer commentId, String userId) {
		VideoComment comment = videoCommentMapper.selectByCommentId(commentId);
		if (comment == null){
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		VideoInfo videoInfo = videoInfoMapper.selectByVideoId(comment.getVideoId());
		if (videoInfo == null){
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		// 既不是视频发布者 也不是评论的主人 也不是管理员
		if (!videoInfo.getUserId().equals(userId) && !comment.getUserId().equals(userId) && userId != null){
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		videoCommentMapper.deleteByCommentId(commentId);

		// 如果是一级评论
		if (comment.getpCommentId() == 0){
			// 评论数减一
			videoInfoMapper.updateCountInfo(videoInfo.getVideoId(), UserActionTypeEnum.VIDEO_COMMENT.getField(), -1);
			// 删除二级评论
			VideoCommentQuery videoCommentQuery = new VideoCommentQuery();
			videoCommentQuery.setpCommentId(commentId);
			videoCommentMapper.deleteByParam(videoCommentQuery);
		}
	}
}