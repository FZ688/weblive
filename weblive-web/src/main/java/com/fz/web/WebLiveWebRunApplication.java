package com.fz.web;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @Author: fz
 * @Date: 2024/12/4 15:12
 * @Description:
 */
@SpringBootApplication(scanBasePackages = {"com.fz"})
@MapperScan(basePackages = {"com.fz.mappers"},annotationClass = Mapper.class)
@EnableTransactionManagement
@EnableScheduling
public class WebLiveWebRunApplication {
    public static void main(String[] args) {
        SpringApplication.run(WebLiveWebRunApplication.class,args);
    }
}
