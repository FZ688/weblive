package com.fz.entity.vo;

import com.fz.entity.po.UserVideoSeries;
import com.fz.entity.po.UserVideoSeriesVideo;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author fz
 */
@Setter
@Getter
public class UserVideoSeriesDetailVO {
    private UserVideoSeries videoSeries;
    private List<UserVideoSeriesVideo> seriesVideoList;

    public UserVideoSeriesDetailVO() {

    }

    public UserVideoSeriesDetailVO(UserVideoSeries videoSeries, List<UserVideoSeriesVideo> seriesVideoList) {
        this.videoSeries = videoSeries;
        this.seriesVideoList = seriesVideoList;
    }

}
