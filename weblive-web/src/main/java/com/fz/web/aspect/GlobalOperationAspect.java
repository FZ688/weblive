package com.fz.web.aspect;

import com.fz.web.annotation.GlobalInterceptor;
import com.fz.entity.constants.Constants;
import com.fz.entity.dto.TokenUserInfoDto;
import com.fz.entity.enums.ResponseCodeEnum;
import com.fz.exception.BusinessException;
import com.fz.redis.RedisUtils;
import com.fz.utils.StringTools;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * @Author: fz
 * @Date: 2025/1/14 14:16
 * @Description: 切面
 */
@Aspect
@Component
@Slf4j
public class GlobalOperationAspect {

    @Resource
    private RedisUtils redisUtils;
    /**
     * @description: 检验登录
     * @param point
     * @author fz
     * 2025/1/14 15:24
     */
    @Before("@annotation(com.fz.web.annotation.GlobalInterceptor)")
    public void interceptor(JoinPoint point){
        // 获取方法
        Method method =((MethodSignature) point.getSignature()).getMethod();
        // 拿到注解
        GlobalInterceptor interceptor = method.getAnnotation(GlobalInterceptor.class);
        // 如果是true,则校验登录
        if (interceptor.checkLogin()){
            checkLogin();
        }
    }

    /**
     * @description: 检验登录
     * @author fz
     * 2025/1/14 15:25
     */
    private void checkLogin(){
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String token = request.getHeader(Constants.TOKEN_WEB);
        // 如果没有token
        if (StringTools.isEmpty(token)){
            throw new BusinessException(ResponseCodeEnum.CODE_901);
        }
        // 如果在redis中没有这个token 说明过期了或者说是恶意token
        TokenUserInfoDto tokenUserInfoDto = (TokenUserInfoDto) redisUtils.get(Constants.REDIS_KEY_TOKEN_WEB + token);
        if (tokenUserInfoDto == null){
            throw new BusinessException(ResponseCodeEnum.CODE_901);
        }
    }
}

