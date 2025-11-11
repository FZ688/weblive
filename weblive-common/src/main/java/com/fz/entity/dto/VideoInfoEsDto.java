package com.fz.entity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @author fz
 */
@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class VideoInfoEsDto {


    /**
     * 视频ID
     */
    private String videoId;

    /**
     * 视频封面
     */
    private String videoCover;

    /**
     * 视频名称
     */
    private String videoName;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 创建时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 标签
     */
    private String tags;

    /**
     * 播放数量
     */
    private Integer playCount;

    /**
     * 弹幕数量
     */
    private Integer danmuCount;

    /**
     * 收藏数量
     */
    private Integer collectCount;


}
