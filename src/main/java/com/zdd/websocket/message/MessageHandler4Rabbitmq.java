package com.zdd.websocket.message;

import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.*;
import com.zdd.entry.constants.CommonConstant;
import com.zdd.entry.dto.MessageSendDto;
import com.zdd.websocket.netty.ChannelContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Slf4j
@Component
@ConditionalOnProperty(name = CommonConstant.MESSAGING_HANDLE_CHANNEL, havingValue = CommonConstant.MESSAGING_HANDLE_CHANNEL_RABBITMQ)

public class MessageHandler4Rabbitmq implements MessageHandler {
    private final static Integer RETRY_COUNT = 3;
    private final static String RETRY_COUNT_KEY = "retryCount";
    public static final String EXCHANGE_NAME = "fanoutExchange";

    @Value("${rabbitmq.host}")
    private String rmqHost;
    @Value("${rabbitmq.port}")
    private Integer rmqPort;

    @Autowired
    private ChannelContext channelContext;

    private ConnectionFactory connectionFactory;
    private Channel channel;
    private Connection connection;


    @Override
    public void listenMessage() {
        connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(rmqHost);
        connectionFactory.setPort(rmqPort);
        try {
            connection = connectionFactory.newConnection();
            channel = connection.createChannel();

            channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
            String queue = channel.queueDeclare().getQueue();
            channel.queueBind(queue, EXCHANGE_NAME, "");

            Boolean autoAck = false;
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                try {

                    String message = new String(delivery.getBody(), "UTF-8");
                    log.info("RabbitMQ收到消息 message:{}", message);
                    channelContext.sendMsg(JSONObject.parseObject(message, MessageSendDto.class));
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);

                } catch (Exception e) {
                    log.error("RabbitMQ消息处理异常 e:{}", e);
                    retry(channel, delivery, queue);
                }

            };

            channel.basicConsume(queue, autoAck, deliverCallback, consumerTag -> {
            });


        } catch (Exception e) {
            log.error("RabbitMQ连接异常 e:{}", e);
        }


    }

    @Override
    public void sendMessage(MessageSendDto messageSendDto) {
        try {
            channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
            channel.basicPublish(EXCHANGE_NAME, "", null, JSONObject.toJSONString(messageSendDto).getBytes("UTF-8"));
        } catch (Exception e) {
            log.error("RabbitMQ消息发送异常 e:{}", e);
        }

    }


    private static void retry(Channel channel, Delivery delivery, String queueName) throws IOException {
        Map<String, Object> headers = delivery.getProperties().getHeaders();
        if (headers == null) {
            headers = new HashMap<>();
        }

        Integer retryCount = 0;

        if (headers.containsKey(RETRY_COUNT_KEY)) {
            retryCount = (Integer) headers.get(RETRY_COUNT_KEY);
        }

        if (retryCount < RETRY_COUNT - 1) {
            headers.put(RETRY_COUNT_KEY, retryCount + 1);
            AMQP.BasicProperties build = new AMQP.BasicProperties.Builder().headers(headers).build();
            channel.basicPublish("", queueName, build, delivery.getBody());
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        } else {
            log.error("RabbitMQ消息重试超最大次数 message:{}", new String(delivery.getBody(), "UTF-8"));
            channel.basicReject(delivery.getEnvelope().getDeliveryTag(), false);
        }


    }

    @PreDestroy
    public void destory() throws IOException, TimeoutException {
        if (channel != null && channel.isOpen()) {
            channel.close();
        }
        if (connection != null && connection.isOpen()){
            connection.close();
        }
    }

}
