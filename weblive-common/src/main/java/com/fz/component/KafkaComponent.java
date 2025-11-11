package com.fz.component;

import com.fz.entity.config.AppConfig;
import com.fz.entity.dto.VideoPlayInfoDto;
import com.fz.entity.po.VideoInfoFilePost;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author fz
 * @Date: 2024/11/3 - 11 - 03 - 22:51
 * @Description: com.fz.component
 */
@Component
public class KafkaComponent {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final AppConfig appConfig;

    public KafkaComponent(KafkaTemplate<String, Object> kafkaTemplate, AppConfig appConfig) {
        this.appConfig = appConfig;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishTransfer(List<VideoInfoFilePost> fileList) {
        if (fileList == null || fileList.isEmpty()) {
            return;
        }
        for (VideoInfoFilePost filePost : fileList) {
            kafkaTemplate.send(appConfig.getTransferFileQueueTopic(), filePost);
        }
    }

    public void publishVideoPlay(VideoPlayInfoDto dto) {
        if (dto == null) {
            return;
        }
        kafkaTemplate.send(appConfig.getVideoPlayQueueTopic(), dto);
    }
}
