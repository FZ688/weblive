package com.fz.service;

import java.util.List;

import com.fz.entity.po.VideoInfoFilePost;
import com.fz.entity.query.VideoInfoPostQuery;
import com.fz.entity.po.VideoInfoPost;
import com.fz.entity.vo.PaginationResultVO;


/**
 * 视频信息 业务接口
 * @author fz
 */
public interface VideoInfoPostService {

	/**
	 * 根据条件查询列表
	 */
	List<VideoInfoPost> findListByParam(VideoInfoPostQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(VideoInfoPostQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<VideoInfoPost> findListByPage(VideoInfoPostQuery param);

	/**
	 * 新增
	 */
	Integer add(VideoInfoPost bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<VideoInfoPost> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<VideoInfoPost> listBean);

	/**
	 * 多条件更新
	 */
	Integer updateByParam(VideoInfoPost bean,VideoInfoPostQuery param);

	/**
	 * 多条件删除
	 */
	Integer deleteByParam(VideoInfoPostQuery param);

	/**
	 * 根据VideoId查询对象
	 */
	VideoInfoPost getVideoInfoPostByVideoId(String videoId);


	/**
	 * 根据VideoId修改
	 */
	Integer updateVideoInfoPostByVideoId(VideoInfoPost bean,String videoId);


	/**
	 * 根据VideoId删除
	 */
	Integer deleteVideoInfoPostByVideoId(String videoId);

	/**
	 * @description: 保存视频信息(更新或插入)
	 * @param videoInfoPost
	 * @param uploadFileList
	 * @return
	 * @author fz
	 * 2024/12/10 14:10
	 */
	void saveVideoInfo(VideoInfoPost videoInfoPost, List<VideoInfoFilePost> uploadFileList);

	/**
	 * 文件转码（文件从temp移动到video）
	 * @param
	 * @return
	 * @author fz
	 * 2024/12/9 9:46
	 */
	void transferVideoFile(VideoInfoFilePost videoInfoFilePost);
	
	/**
	 * 审核视频
	 * @param status 是否通过 reason 不通过原因
	 * @return 
	 * @author fz
	 * 2024/12/9 19:56
	 */
    void auditVideo(String videoId, Integer status, String reason);
}