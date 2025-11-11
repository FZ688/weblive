package com.fz.entity.vo;

import com.fz.entity.po.UserAction;
import com.fz.entity.po.VideoComment;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author fz
 */
@Setter
@Getter
public class VideoCommentResultVO {
    private PaginationResultVO<VideoComment> commentData;
    private List<UserAction> userActionList;

}
