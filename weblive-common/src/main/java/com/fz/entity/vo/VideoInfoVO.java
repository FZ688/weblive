package com.fz.entity.vo;

import com.fz.entity.po.VideoInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * @Author: fz
 * @Date: 2024/12/9 23:26
 * @Description:
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class VideoInfoVO {
    private VideoInfo videoInfo;
    private List userActionList;
}
