package com.fz.entity.enums;

import lombok.Getter;

/**
 * @author fz
 */
@Getter
public enum UserSexEnum {
    WOMAN(0,"女"),MAN(1,"男"),SECRECY(2,"保密");

    private final Integer type;
    private final String desc;
    UserSexEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }
}
