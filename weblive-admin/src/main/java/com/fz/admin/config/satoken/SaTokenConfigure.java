package com.fz.admin.config.satoken;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import com.fz.admin.config.properties.AuthProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @Auther: fz
 * @Date: 2025/1/12-14:58
 * @Description: com.fz.admin.config.satoken
 */
@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {

    private final AuthProperties authProperties;

    public SaTokenConfigure(AuthProperties authProperties) {
        this.authProperties = authProperties;
    }

    // 注册拦截器
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 Sa-Token 拦截器，定义详细的拦截路由
        registry.addInterceptor(new SaInterceptor(handle -> StpUtil.checkLogin()))
                .addPathPatterns("/**")
                .excludePathPatterns(
                        authProperties.getNoAuthUrls()
                );
    }
}

