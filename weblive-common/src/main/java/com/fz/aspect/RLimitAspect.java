package com.fz.aspect;

import com.fz.annotation.RLimit;
import com.fz.entity.enums.ResponseCodeEnum;
import com.fz.exception.BusinessException;
import com.fz.redis.RedisUtils;
import com.fz.utils.IPUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import static com.fz.entity.constants.Constants.RATE_LIMIT_KEY;

/**
 * @Auther: fz
 * @Date: 2025/1/14-23:39
 * @Description: 基于RateLimiter的限流 AOP
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RLimitAspect {

    private final RedisUtils redisUtils;

    @Before("@annotation(limit)")
    public void doBefore(JoinPoint joinPoint, RLimit limit) throws Throwable {
        int time = limit.time();

        HttpServletRequest request = IPUtils.getRequest();
        // 拼接redis key = IP + Api限流
        assert request != null;
        String key = RATE_LIMIT_KEY + IPUtils.getIp() + request.getRequestURI();
        // 获取redis的value
        Integer maxTimes = null;
        Object value = redisUtils.get(key);
        if (value != null) {
            maxTimes = (Integer) value;
        }
        if (maxTimes == null) {
            // 如果redis中没有该ip对应的时间则表示第一次调用，保存key到redis
            redisUtils.set(key, 1, time, TimeUnit.SECONDS);
        } else if (maxTimes < limit.count()) {
            // 如果redis中的时间比注解上的时间小则表示可以允许访问,这是修改redis的value时间
            redisUtils.set(key, maxTimes + 1, time,TimeUnit.SECONDS);
        } else {
            log.info("API请求过于频繁，key={}，maxTimes={}", key, maxTimes);
            // 请求过于频繁
            throw new BusinessException(ResponseCodeEnum.VISIT_LIMIT_ERROR.getCode(), limit.msg());
        }
    }
}

