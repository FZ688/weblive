package com.fz.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @Author: fz
 * @Date: 2024/12/5 15:38
 * @Description: 存入redis中的token包含的信息
 */
@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TokenUserInfoDto implements Serializable {
    private static final long serialVersionUID = 9170480547933408849L;
    private String userId;
    private String nickName;
    private String avatar;
    private Long expireAt;
    private String token;

    private Integer fansCount;
    private Integer currentCoinCount;
    private Integer focusCount;

}
