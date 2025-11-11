package com.fz.web.task;

import com.fz.component.EsSearchComponent;
import com.fz.component.RedisComponent;
import com.fz.entity.dto.VideoPlayInfoDto;
import com.fz.entity.enums.SearchOrderTypeEnum;
import com.fz.entity.po.VideoInfoFilePost;
import com.fz.exception.BusinessException;
import com.fz.service.VideoInfoPostService;
import com.fz.service.VideoInfoService;
import com.fz.service.VideoPlayHistoryService;
import com.fz.utils.StringTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.BatchListenerFailedException;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.time.Duration;
import java.util.List;

/**
 * @author fz
 * @Auther: fz
 * @Date: 2025/11/3 - 11 - 03 - 23:09
 * @Description: com.fz.web.task
 */
@Component
@Slf4j
public class EventConsumer {
    @Resource
    private VideoInfoPostService videoInfoPostService;
    @Resource
    private VideoInfoService videoInfoService;
    @Resource
    private VideoPlayHistoryService videoPlayHistoryService;
    @Resource
    private RedisComponent redisComponent;
    @Resource
    private EsSearchComponent esSearchComponent;

    /**
     * 消费视频转码队列
     *
     * @param filePost 上传视频文件信息
     * @param ack      手动确认对象
     */
    @KafkaListener(topics = {"${kafka.topic.transfer-file-queue}"},
            groupId = "${kafka.consumer.group.transfer}",
            containerFactory = "videoListenerContainerFactory")
    public void transfer(VideoInfoFilePost filePost, Acknowledgment ack) {
        if (filePost == null) {
            ack.acknowledge();
            return;
        }
        try {
            // 处理视频文件转码
            videoInfoPostService.transferVideoFile(filePost);
            ack.acknowledge();
            log.info("成功处理转码队列, record={}", filePost);
        } catch (IllegalArgumentException iae) {
            //记录日志并抛出异常以触发DLQ的不可重试异常发送到DLQ
            log.warn("非法参数导致跳过并发送DLQ, record={}", filePost, iae);
            throw iae;
        } catch (Exception e) {
            log.error("处理转码队列失败,record={},error={}", filePost, e.getMessage(), e);
            // 延迟1秒后重投该消息
            try {
                ack.nack(Duration.ofMillis(1000));
            } catch (UnsupportedOperationException uoe) {
                // 某些 ack 实现不支持 nack，则交给错误处理器
                throw new BusinessException("单条处理失败", e);
            }
        }
    }


    /**
     * 消费视频的播放信息队列
     *
     * @param dto 播放信息
     * @param ack 手动确认对象
     */
    @KafkaListener(topics = "${kafka.topic.video-play-queue}",
            groupId = "${kafka.consumer.group.play}",
            containerFactory = "videoListenerContainerFactory")
    public void play(VideoPlayInfoDto dto, Acknowledgment ack) {
        try {
            if (dto == null) {
                ack.acknowledge();
                return;
            }
            // 增加视频播放量
            videoInfoService.addReadCount(dto.getVideoId());
            // 记录用户历史播放记录
            if (!StringTools.isEmpty(dto.getUserId())) {
                videoPlayHistoryService.saveHistory(dto.getUserId(), dto.getVideoId(), dto.getFileIndex());
            }
            // 按天记录视频播放量
            redisComponent.recordVideoPlayCount(dto.getVideoId());
            // 更新ES播放数量
            esSearchComponent.updateDocCount(dto.getVideoId(), SearchOrderTypeEnum.VIDEO_PLAY.getField(), 1);
            // 提交偏移量，确认这些记录已经成功处理
            ack.acknowledge();
        } catch (IllegalArgumentException iae) {
            //记录日志并抛出异常以触发DLQ的不可重试异常发送到DLQ
            log.warn("非法参数导致跳过并发送DLQ, record={}", dto, iae);
            throw iae;
        } catch (Exception e) {
            log.error("处理视频播放队列失败,record={},error={}", dto, e.getMessage(), e);
            try {
                ack.nack(Duration.ofMillis(1000));
            } catch (UnsupportedOperationException uoe) {
                throw new BusinessException("单条处理失败", e);
            }
        }
    }

    /**
     * 处理转码队列DLQ中的消息（持久化/告警/人工介入）
     */
    @KafkaListener(topics = "weblive-transfer-file-queue-dlq",
            groupId = "${kafka.consumer.group.transfer}",
            containerFactory = "videoListenerContainerFactory")
    public void transferDlq(VideoInfoFilePost dlqRecord, Acknowledgment ack,
                            @Header(KafkaHeaders.ORIGINAL_TOPIC) String originalTopic,
                            @Header(KafkaHeaders.ORIGINAL_PARTITION) Integer originalPartition,
                            @Header(KafkaHeaders.ORIGINAL_OFFSET) Long originalOffset) {
        try {
            log.error("[DLQ][transfer] 原始topic={}, partition={}, offset={}, payload={}",
                    originalTopic, originalPartition, originalOffset, dlqRecord);
            // TODO: 可落库表、触发告警、或尝试修复后再投递到原始主题
            log.error("需要人工介入处理转码DLQ消息 payload={}", dlqRecord);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("处理transfer DLQ失败 payload={} error={}", dlqRecord, e.getMessage(), e);
            try {
                ack.nack(Duration.ofSeconds(1));
            } catch (UnsupportedOperationException uoe) {
                throw new BatchListenerFailedException("DLQ单条处理失败", e, 0);
            }
        }
    }

    /**
     * 处理播放队列DLQ中的消息（持久化/告警/人工介入）
     */
    @KafkaListener(topics = "weblive-video-play-queue-dlq",
            groupId = "${kafka.consumer.group.play}",
            containerFactory = "videoListenerContainerFactory")
    public void playDlq(VideoPlayInfoDto dlqRecord, Acknowledgment ack,
                        @Header(KafkaHeaders.ORIGINAL_TOPIC) String originalTopic,
                        @Header(KafkaHeaders.ORIGINAL_PARTITION) Integer originalPartition,
                        @Header(KafkaHeaders.ORIGINAL_OFFSET) Long originalOffset) {
        try {
            log.error("[DLQ][play] 原始topic={}, partition={}, offset={}, payload={}",
                    originalTopic, originalPartition, originalOffset, dlqRecord);
            // TODO: 同上，可落库/告警/人工介入
            log.error(
                    "需要人工介入处理播放DLQ消息 payload={}", dlqRecord
            );
            ack.acknowledge();
        } catch (Exception e) {
            log.error("处理play DLQ失败 payload={} error={}", dlqRecord, e.getMessage(), e);
            try {
                ack.nack(Duration.ofSeconds(1));
            } catch (UnsupportedOperationException uoe) {
                throw new BatchListenerFailedException("DLQ单条处理失败", e, 0);
            }
        }

    }
}