package com.zdd.websocket.message;

import com.alibaba.fastjson.JSONObject;
import com.zdd.entry.constants.CommonConstant;
import com.zdd.entry.dto.MessageSendDto;
import com.zdd.websocket.netty.ChannelContext;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

@Slf4j
@Component
@ConditionalOnProperty(name = CommonConstant.MESSAGING_HANDLE_CHANNEL, havingValue = CommonConstant.MESSAGING_HANDLE_CHANNEL_REDIS)
public class MessageHandler4Redis implements MessageHandler {

    private static final String MESSAGE_TOPIC = "message.topic";

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private ChannelContext channelContext;

    /**
     * 监听消息功能
     * 该方法用于监听特定主题的消息，并在接收到消息时执行处理逻辑
     * 主要利用Redisson的发布/订阅机制来实现消息监听
     */
    @Override
    public void listenMessage() {
        // 获取Redisson客户端中的指定主题
        RTopic topic = redissonClient.getTopic(MESSAGE_TOPIC);

        // 为该主题添加消息监听器，指定监听的消息类型为MessageSendDto
        topic.addListener(MessageSendDto.class, (MessageSendDto, sendDto) -> {
            // 当接收到消息时，记录日志信息
            log.info("接收到消息:{}", JSONObject.toJSON(sendDto));

            // 将接收到的消息发送给客户端上下文
            channelContext.sendMsg(sendDto);
        });
    }

    /**
     * 发送消息到指定的主题
     * 此方法使用Redisson客户端将消息发布到MESSAGE_TOPIC主题
     * 它允许系统中的其他部分订阅该主题并接收消息
     *
     * @param messageSendDto 消息数据传输对象包含消息的所有必要信息
     */
    @Override
    public void sendMessage(MessageSendDto messageSendDto) {
        redissonClient.getTopic(MESSAGE_TOPIC).publish(messageSendDto);
    }


    @PreDestroy
    public void destory() {
        redissonClient.shutdown();
    }
}
