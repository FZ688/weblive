package com.fz.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * @Auther: fz
 * @Date: 2025/1/16-1:46
 * @Description: 功能描述: 通用状态
 */
@Getter
public enum EnableStatus {
    enable("1", "启用"),
    disable("0", "禁用"),
    ;

    EnableStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    @EnumValue
    @JsonValue // 标记响应json值
    private final String code;
    private final String desc;
}

