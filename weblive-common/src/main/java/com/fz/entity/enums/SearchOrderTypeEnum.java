package com.fz.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author fz
 */

@Getter
public enum SearchOrderTypeEnum {
    VIDEO_PLAY(0, "playCount", "视频播放数"),
    VIDEO_TIME(1, "createTime", "视频时间"),
    VIDEO_DANMU(2, "danmuCount", "弹幕数"),
    VIDEO_COLLECT(3, "collectCount", "视频收藏");


    private final Integer type;
    private final String field;
    private final String desc;

    SearchOrderTypeEnum(Integer type, String field, String desc) {
        this.type = type;
        this.field = field;
        this.desc = desc;
    }

    public static SearchOrderTypeEnum getByType(Integer type) {
        for (SearchOrderTypeEnum item : SearchOrderTypeEnum.values()) {
            if (item.getType().equals(type)) {
                return item;
            }
        }
        return null;
    }

}
