package com.fz.entity.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author fz
 */
@Setter
@Getter
public class UserMessageExtendDto {
    private String messageContent;

    private String messageContentReply;

    //审核状态
    private Integer auditStatus;

}
