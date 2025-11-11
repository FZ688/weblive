package com.fz.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fz.component.RedisComponent;
import com.fz.entity.constants.Constants;
import com.fz.entity.enums.StatisticsTypeEnum;
import com.fz.entity.enums.UserActionTypeEnum;
import com.fz.entity.po.UserFocus;
import com.fz.entity.po.UserInfo;
import com.fz.entity.po.VideoInfo;
import com.fz.entity.query.*;
import com.fz.mappers.UserFocusMapper;
import com.fz.mappers.UserInfoMapper;
import com.fz.mappers.VideoInfoMapper;
import com.fz.utils.DateUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import com.fz.entity.enums.PageSize;
import com.fz.entity.po.StatisticsInfo;
import com.fz.entity.vo.PaginationResultVO;
import com.fz.mappers.StatisticsInfoMapper;
import com.fz.service.StatisticsInfoService;
import com.fz.utils.StringTools;


/**
 * 业务接口实现
 */
@Service("statisticsInfoService")
public class StatisticsInfoServiceImpl implements StatisticsInfoService {

    @Resource
    private StatisticsInfoMapper<StatisticsInfo, StatisticsInfoQuery> statisticsInfoMapper;
    @Resource
    private RedisComponent redisComponent;
    @Resource
    private VideoInfoMapper<VideoInfo, VideoInfoQuery> videoInfoMapper;
    @Resource
    private UserFocusMapper<UserFocus, UserFocusQuery> userFocusMapper;
    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;


    /**
     * 根据条件查询列表
     */
    @Override
    public List<StatisticsInfo> findListByParam(StatisticsInfoQuery param) {
        return this.statisticsInfoMapper.selectList(param);
    }

    /**
     * 根据条件查询列表
     */
    @Override
    public Integer findCountByParam(StatisticsInfoQuery param) {
        return this.statisticsInfoMapper.selectCount(param);
    }

    /**
     * 分页查询方法
     */
    @Override
    public PaginationResultVO<StatisticsInfo> findListByPage(StatisticsInfoQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<StatisticsInfo> list = this.findListByParam(param);
        PaginationResultVO<StatisticsInfo> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(StatisticsInfo bean) {
        return this.statisticsInfoMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<StatisticsInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.statisticsInfoMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或者修改
     */
    @Override
    public Integer addOrUpdateBatch(List<StatisticsInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.statisticsInfoMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 多条件更新
     */
    @Override
    public Integer updateByParam(StatisticsInfo bean, StatisticsInfoQuery param) {
        StringTools.checkParam(param);
        return this.statisticsInfoMapper.updateByParam(bean, param);
    }

    /**
     * 多条件删除
     */
    @Override
    public Integer deleteByParam(StatisticsInfoQuery param) {
        StringTools.checkParam(param);
        return this.statisticsInfoMapper.deleteByParam(param);
    }

    /**
     * 根据StatisticsDateAndUserIdAndDataType获取对象
     */
    @Override
    public StatisticsInfo getStatisticsInfoByStatisticsDateAndUserIdAndDataType(String statisticsDate, String userId, Integer dataType) {
        return this.statisticsInfoMapper.selectByStatisticsDateAndUserIdAndDataType(statisticsDate, userId, dataType);
    }

    /**
     * 根据StatisticsDateAndUserIdAndDataType修改
     */
    @Override
    public Integer updateStatisticsInfoByStatisticsDateAndUserIdAndDataType(StatisticsInfo bean, String statisticsDate, String userId, Integer dataType) {
        return this.statisticsInfoMapper.updateByStatisticsDateAndUserIdAndDataType(bean, statisticsDate, userId, dataType);
    }

    /**
     * 根据StatisticsDateAndUserIdAndDataType删除
     */
    @Override
    public Integer deleteStatisticsInfoByStatisticsDateAndUserIdAndDataType(String statisticsDate, String userId, Integer dataType) {
        return this.statisticsInfoMapper.deleteByStatisticsDateAndUserIdAndDataType(statisticsDate, userId, dataType);
    }

    /**
     * 统计数据，每天执行一次，统计前一天的数据
     1. 播放量
     2. 评论数
     3. 弹幕数
     4. 点赞数
     5. 收藏数
     6. 投币数
     7. 粉丝数
     */
    @Override
    public void statisticsData() {
        List<StatisticsInfo> statisticsInfoList = new ArrayList<>();

        final String statisticDate = DateUtil.getBeforeDayDate(Constants.ONE);

        // 统计某一天所有视频的播放量
        // 格式为 redisKey:videoId:播放量
        Map<String, Integer> videoPlayCountMap = redisComponent.getVideoPlayCount(statisticDate);
        if (videoPlayCountMap != null && videoPlayCountMap.isEmpty()) {
            // 转成 videoId:playCount
            List<String> playVideoKeys = new ArrayList<>(videoPlayCountMap.keySet());
            playVideoKeys = playVideoKeys.stream()
                    .map(item -> item.substring(item.lastIndexOf(":") + 1))
                    .collect(Collectors.toList());
            if (!playVideoKeys.isEmpty()) {
                // 拿到所有的视频信息
                VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
                videoInfoQuery.setVideoIdArray(playVideoKeys.toArray(new String[0]));
                List<VideoInfo> videoInfoList = videoInfoMapper.selectList(videoInfoQuery);
                // 根据用户分组 分完组之后统计每一个组的视频总播放量
                // key是userId
                // value是每一个用户在一天中所有视频的播放量
                Map<String, Integer> videoCountMap = videoInfoList
                        .stream()
                        .collect(Collectors.groupingBy(
                                VideoInfo::getUserId,
                                Collectors.summingInt(item ->
                                        videoPlayCountMap.getOrDefault(
                                                Constants.REDIS_KEY_VIDEO_PLAY_COUNT + statisticDate + ":" + item.getVideoId(),
                                                0))));

                // 设置每一个用户的播放量的统计数据
                videoCountMap.forEach((k,v)->{
                    StatisticsInfo statisticsInfo = new StatisticsInfo();
                    statisticsInfo.setStatisticsDate(statisticDate);
                    statisticsInfo.setUserId(k);
                    statisticsInfo.setDataType(StatisticsTypeEnum.PLAY.getType());
                    statisticsInfo.setStatisticsCount(v);
                    statisticsInfoList.add(statisticsInfo);
                });
            }
        }

        // 统计所有用户的粉丝数
        List<StatisticsInfo> fanDataList = statisticsInfoMapper.selectStatisticsFan(statisticDate);
        for (StatisticsInfo statisticsInfo : fanDataList) {
            statisticsInfo.setStatisticsDate(statisticDate);
            statisticsInfo.setDataType(StatisticsTypeEnum.FANS.getType());
        }
        statisticsInfoList.addAll(fanDataList);
        // 统计评论数
        List<StatisticsInfo> commentDataList = statisticsInfoMapper.selectStatisticsComment(statisticDate);
        for (StatisticsInfo statisticsInfo : commentDataList) {
            statisticsInfo.setStatisticsDate(statisticDate);
            statisticsInfo.setDataType(StatisticsTypeEnum.COMMENT.getType());
        }
        statisticsInfoList.addAll(commentDataList);
        // 统计弹幕，点赞，收藏，投币
        List<StatisticsInfo> otherDataList = statisticsInfoMapper.selectStatisticsInfo(statisticDate,new Integer[]{
                UserActionTypeEnum.VIDEO_LIKE.getType(),UserActionTypeEnum.VIDEO_COIN.getType(),
                UserActionTypeEnum.VIDEO_COLLECT.getType(),UserActionTypeEnum.VIDEO_DANMU.getType()
        });
        for (StatisticsInfo statisticsInfo : otherDataList) {
            statisticsInfo.setStatisticsDate(statisticDate);
            if (UserActionTypeEnum.VIDEO_LIKE.getType().equals(statisticsInfo.getDataType())){
                statisticsInfo.setDataType(StatisticsTypeEnum.LIKE.getType());
            }else if(UserActionTypeEnum.VIDEO_COLLECT.getType().equals(statisticsInfo.getDataType())){
                statisticsInfo.setDataType(StatisticsTypeEnum.COLLECTION.getType());
            }else if (UserActionTypeEnum.VIDEO_DANMU.getType().equals(statisticsInfo.getDataType())){
                statisticsInfo.setDataType(StatisticsTypeEnum.DANMU.getType());
            }else if (UserActionTypeEnum.VIDEO_COIN.getType().equals(statisticsInfo.getDataType())){
                statisticsInfo.setDataType(StatisticsTypeEnum.COIN.getType());
            }
        }
        statisticsInfoList.addAll(otherDataList);

        // 如果没有需要写入的数据，直接返回，避免 MyBatis 生成空的 VALUES 导致 SQL 语法错误
        if (statisticsInfoList.isEmpty()) {
            return;
        }

        // 空检查并调用 mapper 执行批量插入或更新
        this.addOrUpdateBatch(statisticsInfoList);
    }

    @Override
    public Map<String, Integer> getStatisticsInfoActualTime(String userId) {
        Map<String, Integer> result = statisticsInfoMapper.selectTotalCountInfo(userId);

        // 查粉丝数
        if (!StringTools.isEmpty(userId)){
            result.put("fansCount",userFocusMapper.selectFansCount(userId));
        }else {
            // 说明后台来查 查所有的用户数
            result.put("userCount",userInfoMapper.selectCount(new UserInfoQuery()));
        }
        return result;
    }


    @Override
    public List<StatisticsInfo> findListTotalInfoByParam(StatisticsInfoQuery query) {
        return statisticsInfoMapper.selectListTotalInfoByParam(query);
    }

    @Override
    public List<StatisticsInfo> findUserCountTotalInfoByParam(StatisticsInfoQuery query) {
        return statisticsInfoMapper.selectUserCountTotalInfoByParam(query);
    }
}