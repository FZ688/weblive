package com.fz.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author fz
 */
@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class VideoPlayInfoDto implements Serializable {
    private String videoId;
    private String userId;
    /**
     * 文件索引(0表示原始文件，1表示转码后的第一个文件，2表示转码后的第二个文件，依此类推)
     */
    private Integer fileIndex;

}
