package com.fz.entity.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author fz
 */
@Setter
@Getter
public class UserMessageCountDto {
    public Integer messageType;
    private Integer messageCount;

}
