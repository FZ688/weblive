package com.fz.web;

import com.fz.component.EsSearchComponent;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * @Author: fz
 * @Date: 2025/1/13 20:20
 * @Description: SpringBoot启动时会执行这里的run方法
 */
@Component
public class InitRun implements ApplicationRunner {
    @Resource
    private EsSearchComponent esSearchComponent;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        esSearchComponent.createIndex();
    }
}

