package com.fz.entity.vo;


import lombok.Getter;
import lombok.Setter;

/**
 * @author fz
 */
@Setter
@Getter
public class ResponseVO<T> {
    private String status;
    private Integer code;
    private String info;
    private T data;
}
