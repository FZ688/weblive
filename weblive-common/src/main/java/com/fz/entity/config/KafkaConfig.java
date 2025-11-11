package com.fz.entity.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.core.convert.ConversionException;
import org.springframework.messaging.handler.invocation.MethodArgumentResolutionException;

/**
 * Kafka related configuration: create topics and provide a DefaultErrorHandler bean.
 * This class intentionally does not programmatically configure the default listener container factory;
 * listener behavior is configured via `application.yml` and @KafkaListener attributes.
 * @author fz
 */
@Configuration
@Slf4j
@EnableKafka
public class KafkaConfig {

    /**
     * 在应用启动时创建主题--文件转码队列
     */
    @Bean
    public NewTopic transferTopic() {
        return TopicBuilder
                .name("weblive-transfer-file-queue")
                .partitions(3)
                .replicas(2)
                .build();
    }

    /**
     * 在应用启动时创建 transfer DLQ
     */
    @Bean
    public NewTopic transferDlqTopic() {
        return TopicBuilder
                .name("weblive-transfer-file-queue-dlq")
                .partitions(3)
                .replicas(2)
                .build();
    }

    /**
     * 在应用启动时创建主题--视频播放队列
     */
    @Bean
    public NewTopic videoPlayTopic() {
        return TopicBuilder
                .name("weblive-video-play-queue")
                .partitions(5)
                .replicas(2)
                .build();
    }

    /**
     * 在应用启动时创建 video-play DLQ
     */
    @Bean
    public NewTopic videoPlayDlqTopic() {
        return TopicBuilder.name("weblive-video-play-queue-dlq")
                .partitions(5)
                .replicas(2)
                .build();
    }

    /**
     * Kafka消费者错误处理器配置
     * 提供一个 DefaultErrorHandler 的 bean，供容器工厂作为 common error handler 使用。
     * 不要使用与 @KafkaListener(errorHandler=...) 相同的 bean 名称，以避免类型期待冲突。
     */
    @Bean
    public DefaultErrorHandler defaultErrorHandler(KafkaTemplate<String, Object> kafkaTemplate) {
        // 退避策略：初始间隔1秒，默认最大间隔30秒，最大重试3次
        FixedBackOff fixedBackOff = new FixedBackOff(1000L, 3L);

        // 死信记录发布器，将失败的消息发送到对应的DLQ主题
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
                (record, ex) -> new TopicPartition(record.topic() + "-dlq", record.partition()));

        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, fixedBackOff);

        handler.setRetryListeners((record, ex, deliveryAttempt) ->
                log.warn("重试第 {} 次，record：{}，异常：{}",
                        deliveryAttempt,
                        record,
                        ex.getMessage())
        );

        // 添加常见的不可重试异常，使其直接发送到DLQ
        handler.addNotRetryableExceptions(
                DeserializationException.class,
                MessageConversionException.class,
                ConversionException.class,
                MethodArgumentResolutionException.class,
                NoSuchMethodException.class,
                ClassCastException.class,
                IllegalArgumentException.class
        );

        // 注意：DefaultErrorHandler 已经将某些异常视为不可重试。
        // 对于较长的延迟或非阻塞重试，需要考虑 ContainerPausingBackOffHandler 或非阻塞重试。

        return handler;
    }

    /**
     * 为 @KafkaListener 创建一个带有自定义 DefaultErrorHandler 的 ConcurrentKafkaListenerContainerFactory。
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> videoListenerContainerFactory(
                    ConsumerFactory<String, Object> consumerFactory,
                    DefaultErrorHandler defaultErrorHandler) {
            ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
            // 设置并发消费者数量
            factory.setConcurrency(3);
            factory.setConsumerFactory(consumerFactory);
            // 改为 MANUAL_IMMEDIATE：单条成功立即提交；失败可 nack 进行延迟重试或进入 DLQ
            factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
            factory.setCommonErrorHandler(defaultErrorHandler);
            return factory;
    }
}
