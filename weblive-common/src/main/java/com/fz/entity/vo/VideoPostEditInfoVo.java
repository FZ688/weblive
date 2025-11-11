package com.fz.entity.vo;

import com.fz.entity.po.VideoInfoFilePost;
import com.fz.entity.po.VideoInfoPost;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author fz
 */
@Setter
@Getter
public class VideoPostEditInfoVo {
    private VideoInfoPost videoInfo;
    private List<VideoInfoFilePost> videoInfoFileList;

}
