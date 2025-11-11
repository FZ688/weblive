package com.fz.entity.enums;


import lombok.Getter;

/**
 * @author fz
 */
@Getter
public enum VideoRecommendTypeEnum {
    NO_RECOMMEND(0, "未推荐"),
    RECOMMEND(1, "已推荐");

    private final Integer type;
    private final String desc;

    VideoRecommendTypeEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public static VideoRecommendTypeEnum getByType(Integer type) {
        for (VideoRecommendTypeEnum typeEnum : VideoRecommendTypeEnum.values()) {
            if (typeEnum.getType().equals(type)) {
                return typeEnum;
            }
        }
        return null;
    }
}