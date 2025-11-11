package com.fz.web.component;

import com.fz.component.RedisComponent;
import com.fz.entity.constants.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * @Author: fz
 * @Date: 2025/1/11 11:12
 * @Description: 监听视频在线播放数的过期事件，减少在线播放数
 */
@Component
@Slf4j
public class RedisKeyExpirationListener extends KeyExpirationEventMessageListener {
    @Resource
    private RedisComponent redisComponent;

    public RedisKeyExpirationListener(RedisMessageListenerContainer listenerContainer) {
        super(listenerContainer);
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String key = message.toString();
        if (!key.startsWith(Constants.REDIS_KEY_VIDEO_PLAY_COUNT_ONLINE_PREIFX + Constants.REDIS_KEY_VIDEO_PLAY_COUNT_USER_PREFIX)){
            return;
        }
        //得到user:后面的文件ID索引
        int userKeyIndex = key.indexOf(Constants.REDIS_KEY_VIDEO_PLAY_COUNT_USER_PREFIX) + Constants.REDIS_KEY_VIDEO_PLAY_COUNT_USER_PREFIX.length();
        //文件ID在user:后面，长度为20个字符
        String fileId = key.substring(userKeyIndex,userKeyIndex + Constants.LENGTH_10 * 2);

        redisComponent.decrementPlayOnlineCount(String.format(Constants.REDIS_KEY_VIDEO_PLAY_COUNT_ONLINE,fileId));
    }
}
