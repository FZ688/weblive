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
public class UploadingFileDto implements Serializable {
    private String uploadId;
    private String fileName;
    private Integer chunkIndex;
    private Integer chunks;
    private Long fileSize = 0L;
    private String filePath;

}
