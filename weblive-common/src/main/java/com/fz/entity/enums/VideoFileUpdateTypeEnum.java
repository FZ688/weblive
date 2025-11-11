package com.fz.entity.enums;

import lombok.Getter;

/**
 * @description: 视频文件更新类型枚举，0-无更新，1-有更新
 * @author fz
 */
@Getter
public enum VideoFileUpdateTypeEnum {
    NO_UPDATE(0, "无更新"),
    UPDATE(1, "有更新");
    private final Integer status;
    private final String desc;

    VideoFileUpdateTypeEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

}