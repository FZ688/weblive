package com.fz.entity.enums;


import lombok.Getter;

/**
 * @author fz
 */
@Getter
public enum VideoOrderTypeEnum {

    CREATE_TIME(0, "create_time", "最新发布"),
    PLAY_COUNT(1, "play_count", "最多播放"),
    COLLECT_COUNT(2, "collect_count", "最多收藏");


    private final Integer type;
    private final String field;
    private final String desc;

    VideoOrderTypeEnum(Integer type, String field, String desc) {
        this.type = type;
        this.field = field;
        this.desc = desc;
    }

    public static VideoOrderTypeEnum getByType(Integer type) {
        for (VideoOrderTypeEnum item : VideoOrderTypeEnum.values()) {
            if (item.getType().equals(type)) {
                return item;
            }
        }
        return null;
    }

}
