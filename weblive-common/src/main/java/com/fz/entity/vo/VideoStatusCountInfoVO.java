package com.fz.entity.vo;

import lombok.Getter;
import lombok.Setter;

/**
 * @author fz
 */
@Setter
@Getter
public class VideoStatusCountInfoVO {
    private Integer auditPassCount;
    private Integer auditFailCount;
    private Integer inProgress;

}
