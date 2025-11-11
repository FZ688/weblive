package com.fz.web.controller;

import com.fz.entity.constants.Constants;
import com.fz.entity.dto.TokenUserInfoDto;
import com.fz.entity.po.StatisticsInfo;
import com.fz.entity.query.StatisticsInfoQuery;
import com.fz.entity.vo.ResponseVO;
import com.fz.service.StatisticsInfoService;
import com.fz.utils.DateUtil;
import com.fz.web.annotation.GlobalInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/ucenter")
@Validated
@Slf4j
public class UCenterStatisticsController extends ABaseController{
    @Resource
    private StatisticsInfoService statisticsInfoService;

    /**
     * @description: 获取实时的用户数据
     * @param request
     * @return com.fz.entity.vo.ResponseVO
     * @author fz
     * 2025/1/15 22:15
     */
    @RequestMapping("/getActualTimeStatisticsInfo")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO getActualTimeStatisticsInfo(HttpServletRequest request){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);
        String preDate = DateUtil.getBeforeDayDate(Constants.ONE);

        StatisticsInfoQuery statisticsInfoQuery = new StatisticsInfoQuery();
        statisticsInfoQuery.setUserId(tokenUserInfoDto.getUserId());
        statisticsInfoQuery.setStatisticsDate(preDate);

        // 查出该用户前一天的所有数据
        List<StatisticsInfo> preDayData = statisticsInfoService.findListByParam(statisticsInfoQuery);
        // 根据类型分开
        Map<Integer,Integer> preDayDataMap = preDayData.stream().collect(Collectors.toMap(StatisticsInfo::getDataType,StatisticsInfo::getStatisticsCount));
        // 查用户实时数据（查实时数据与查最近的统计数据涉及的表不一样，因为用户可能会做删除视频之类的操作）
        Map<String, Integer> totalCountInfo = statisticsInfoService.getStatisticsInfoActualTime(tokenUserInfoDto.getUserId());

        Map<String,Object> resultVO = new HashMap<>();
        resultVO.put("preDayData",preDayDataMap);
        resultVO.put("totalCountInfo",totalCountInfo);

        return getSuccessResponseVO(resultVO);
    }

    /**
     * @description: 获取用户一周内的某种数据
     * @param request
     * @param dataType
     * @return com.fz.entity.vo.ResponseVO
     * @author fz
     * 2025/1/15 22:22
     */
    @RequestMapping("/getWeekStatisticsInfo")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO getWeekStatisticsInfo(HttpServletRequest request,Integer dataType){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(request);

        // 获取一周内的数据
        List<String> dataList = DateUtil.getBeforeDates(7);
        StatisticsInfoQuery statisticsInfoQuery = new StatisticsInfoQuery();
        statisticsInfoQuery.setUserId(tokenUserInfoDto.getUserId());
        statisticsInfoQuery.setDataType(dataType);
        statisticsInfoQuery.setStatisticsDateStart(dataList.get(0));
        statisticsInfoQuery.setStatisticsDateEnd(dataList.get(dataList.size() - 1));
        statisticsInfoQuery.setOrderBy("statistics_date asc");
        List<StatisticsInfo> statisticsInfoList = statisticsInfoService.findListByParam(statisticsInfoQuery);

        // 以天为单位分开
        Map<String, StatisticsInfo> dataMap = statisticsInfoList.stream().collect(Collectors.toMap(item -> item.getStatisticsDate(), Function.identity(),
                (d1, d2) -> d2));

        List<StatisticsInfo> resultDataList  = new ArrayList<>();
        for (String date : dataList) {
            StatisticsInfo statisticsInfo = dataMap.get(date);
            if (statisticsInfo == null){
                statisticsInfo = new StatisticsInfo();
                statisticsInfo.setStatisticsCount(0);
                statisticsInfo.setStatisticsDate(date);
                statisticsInfo.setUserId(tokenUserInfoDto.getUserId());
                statisticsInfo.setDataType(dataType);
            }
            resultDataList.add(statisticsInfo);
        }
        return getSuccessResponseVO(resultDataList);
    }
}
