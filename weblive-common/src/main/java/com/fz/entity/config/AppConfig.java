package com.fz.entity.config;

import lombok.Data;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

/**
 * @Author: fz
 * @Date: 2024/12/5 23:23
 * @Description: 读取配置类信息
 */
@Configuration
@Getter
public class AppConfig {
    @Value("${project.folder}")
    private String projectFolder;
    @Value("${admin.account:admin}")
    private String adminAccount;
    @Value("${admin.password:admin123}")
    private String adminPassword;
    @Value("${showFFmpeg:true}")
    private Boolean showFFmpegLog;
    @Value("${es.host.port:127.0.0.1:9200}")
    private String esHostPort;

    @Value("${es.index.video.name:weblive_video}")
    private String esIndexVideoName;

    @Value("${kafka.topic.transfer-file-queue:weblive-transfer-file-queue}")
    private String transferFileQueueTopic;

    @Value("${kafka.topic.video-play-queue:weblive-video-play-queue}")
    private String videoPlayQueueTopic;

    @Value("${es.username}")
    private String esUsername;

    @Value("${es.password}")
    private String esPassword;

    @Value("classpath:elasticsearch/weblive_video.json")
    private Resource esVideoIndexMapping;

    @Value("classpath:elasticsearch/http_ca.crt")
    private Resource elasticCert;
}
