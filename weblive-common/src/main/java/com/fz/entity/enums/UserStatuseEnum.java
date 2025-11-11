package com.fz.entity.enums;

import lombok.Getter;

/**
 * @author fz
 */
@Getter
public enum UserStatuseEnum {
    DISABLE(0,"禁用"), ENABLE(1,"启用");

    private final Integer status;
    private final String desc;

    UserStatuseEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }
}
