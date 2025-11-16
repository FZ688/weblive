package com.fz.controller;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.fz.entity.enums.ResponseCodeEnum;
import com.fz.entity.vo.ResponseVO;
import com.fz.exception.BusinessException;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;


/**
 * 全局异常处理器,捕获各种异常并处理
 * @author fz
 * 2024/12/5 1:02
 **/

@RestControllerAdvice
public class AGlobalExceptionHandlerController {
    private static final String STATUC_SUCCESS = "success";

    private static final String STATUC_ERROR = "error";
    private static final Logger logger = LoggerFactory.getLogger(AGlobalExceptionHandlerController.class);

    @ExceptionHandler(value = Exception.class)
    Object handleException(Exception e, HttpServletRequest request) {
        logger.error("请求错误，请求地址{},错误信息:", request.getRequestURL(), e);
        ResponseVO ajaxResponse = new ResponseVO();
        //404
        if (e instanceof NoHandlerFoundException) {
            ajaxResponse.setCode(ResponseCodeEnum.CODE_404.getCode());
            ajaxResponse.setInfo(ResponseCodeEnum.CODE_404.getMsg());
            ajaxResponse.setStatus(STATUC_ERROR);
        } else if (e instanceof BusinessException) {
            //业务错误
            BusinessException biz = (BusinessException) e;
            ajaxResponse.setCode(biz.getCode() == null ? ResponseCodeEnum.CODE_600.getCode() : biz.getCode());
            ajaxResponse.setInfo(biz.getMessage());
            ajaxResponse.setStatus(STATUC_ERROR);
        } else if (e instanceof BindException|| e instanceof MethodArgumentTypeMismatchException) {
            //参数类型错误
            ajaxResponse.setCode(ResponseCodeEnum.CODE_600.getCode());
            ajaxResponse.setInfo(ResponseCodeEnum.CODE_600.getMsg());
            ajaxResponse.setStatus(STATUC_ERROR);
        } else if (e instanceof DuplicateKeyException) {
            //主键冲突
            ajaxResponse.setCode(ResponseCodeEnum.CODE_601.getCode());
            ajaxResponse.setInfo(ResponseCodeEnum.CODE_601.getMsg());
            ajaxResponse.setStatus(STATUC_ERROR);
        } else if (e instanceof ConstraintViolationException || e instanceof BindException) {
            ConstraintViolationException ce = (ConstraintViolationException) e;
            ajaxResponse.setCode(ResponseCodeEnum.CODE_600.getCode());
            ajaxResponse.setInfo(ResponseCodeEnum.CODE_600.getMsg());
            ajaxResponse.setStatus(STATUC_ERROR);
        }else if (e instanceof NotLoginException) {
            NotLoginException notLoginException = (NotLoginException) e;
            ajaxResponse.setCode(ResponseCodeEnum.CODE_901.getCode());
            ajaxResponse.setInfo(buildNotLoginMessage(notLoginException));
            ajaxResponse.setStatus(STATUC_ERROR);
        } else if (e instanceof NotPermissionException) {
            ajaxResponse.setCode(ResponseCodeEnum.CODE_902.getCode());
            ajaxResponse.setInfo(ResponseCodeEnum.CODE_902.getMsg());
            ajaxResponse.setStatus(STATUC_ERROR);
        } else if (e instanceof NotRoleException) {
            ajaxResponse.setCode(ResponseCodeEnum.CODE_903.getCode());
            ajaxResponse.setInfo(ResponseCodeEnum.CODE_903.getMsg());
            ajaxResponse.setStatus(STATUC_ERROR);
        } else {
            ajaxResponse.setCode(ResponseCodeEnum.CODE_500.getCode());
            ajaxResponse.setInfo(ResponseCodeEnum.CODE_500.getMsg());
            ajaxResponse.setStatus(STATUC_ERROR);
        }
        return ajaxResponse;
    }

    private String buildNotLoginMessage(NotLoginException e) {
        if (NotLoginException.NOT_TOKEN.equals(e.getType())) {
            return "未能读取到有效的登录凭证";
        }
        if (NotLoginException.INVALID_TOKEN.equals(e.getType())) {
            return "登录凭证无效";
        }
        if (NotLoginException.TOKEN_TIMEOUT.equals(e.getType())) {
            return "登录凭证已过期";
        }
        if (NotLoginException.BE_REPLACED.equals(e.getType())) {
            return "当前账号已在其他设备登录";
        }
        if (NotLoginException.KICK_OUT.equals(e.getType())) {
            return "账号已被管理员强制下线";
        }
        if (NotLoginException.TOKEN_FREEZE.equals(e.getType())) {
            return "账号已被冻结，请联系管理员";
        }
        if (NotLoginException.NO_PREFIX.equals(e.getType())) {
            return "登录凭证缺少必要的前缀";
        }
        return ResponseCodeEnum.CODE_901.getMsg();
    }
}
