package com.fz.entity.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author fz
 */
@Getter
public enum ResponseCodeEnum {
    CODE_200(200, "请求成功"),
    CODE_404(404, "请求地址不存在"),
    CODE_600(600, "请求参数错误"),
    CODE_601(601, "信息已经存在"),
    CODE_500(500, "服务器返回错误，请联系管理员"),

    CODE_901(901,"未登录或登录超时,请重新登录"),
    CODE_902(902, "权限不足，无法执行该操作"),
    CODE_903(903, "角色权限不足，无法执行该操作"),
    // 系统级别错误码
    VISIT_LIMIT_ERROR(-6,"亲~,您太热情了,请稍后再试噢~");
    private final Integer code;

    private final String msg;

    ResponseCodeEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

}
