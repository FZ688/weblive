package com.fz.web.config.satoken;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import com.fz.web.config.properties.AuthProperties;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author fz
 * @Date: 2025/11/14-1:30
 * @Description: com.fz.web.config.satoken
 */
@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {

    @Resource
    private AuthProperties authProperties;

    // 注册 Sa-Token 拦截器，打开注解式鉴权功能
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 Sa-Token 拦截器，定义详细的拦截路由
        registry.addInterceptor(new SaInterceptor())
                .addPathPatterns("/**");
    }
}
