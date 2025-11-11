package com.fz.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @Author: fz
 * @Date: 2024/12/4 15:13
 * @Description:
 */
@SpringBootApplication(scanBasePackages = {"com.fz"})
@MapperScan(basePackages = {"com.fz.mappers"})
@EnableTransactionManagement
@EnableScheduling
public class WebLiveAdminRunApplication {
    public static void main(String[] args) {
        SpringApplication.run(WebLiveAdminRunApplication.class,args);
    }
}
