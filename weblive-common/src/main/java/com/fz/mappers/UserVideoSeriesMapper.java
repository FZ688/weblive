package com.fz.mappers;

import com.fz.entity.po.UserVideoSeries;
import com.fz.entity.query.UserVideoSeriesQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户视频序列归档 数据库操作接口
 * @author fz
 */
@Mapper
public interface UserVideoSeriesMapper<T,P> extends BaseMapper<T,P> {

	/**
	 * 根据SeriesId更新
	 */
	 Integer updateBySeriesId(@Param("bean") T t,@Param("seriesId") Integer seriesId);


	/**
	 * 根据SeriesId删除
	 */
	 Integer deleteBySeriesId(@Param("seriesId") Integer seriesId);


	/**
	 * 根据SeriesId获取对象
	 */
	 T selectBySeriesId(@Param("seriesId") Integer seriesId);

	List<T> selectUserAllSeries(@Param("userId") String userId);

	Integer selectMaxSort(@Param("userId") String userId);
	void changeSort(@Param("videoSeriesList")List<UserVideoSeries> videoSeriesList);

	List<T> seriesListWithVideo(@Param("query")UserVideoSeriesQuery videoSeriesQuery);
}
