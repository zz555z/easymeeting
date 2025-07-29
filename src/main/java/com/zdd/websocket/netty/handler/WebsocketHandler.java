package com.zdd.websocket.netty.handler;

import com.alibaba.fastjson.JSONObject;
import com.zdd.component.RedisComponent;
import com.zdd.entry.constants.CommonConstant;
import com.zdd.entry.dto.MessageSendDto;
import com.zdd.entry.dto.PeerConnectionDataDto;
import com.zdd.entry.dto.PeerMessageDto;
import com.zdd.entry.dto.UserTokenDTO;
import com.zdd.entry.eum.MessageSendTypeEnum;
import com.zdd.entry.eum.MessageTypeEnum;
import com.zdd.entry.service.UserInfoService;
import com.zdd.websocket.message.MessageHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@ChannelHandler.Sharable
@Component
@Slf4j
public class WebsocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private RedisComponent redisComponent;

    @Autowired
    private MessageHandler messageHandler;
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame textWebSocketFrame) throws Exception {
        String msg = textWebSocketFrame.text();
        if (msg.contains(CommonConstant.NETTY_PING)){
            return;
        }
        log.info("收到消息：{}", msg);
        PeerConnectionDataDto dataDto = JSONObject.parseObject(msg, PeerConnectionDataDto.class);
        UserTokenDTO userTokenDTO = redisComponent.getUserTokenDTO(dataDto.getToken());
        if (userTokenDTO == null){
            log.info("用户信息不存在");
            return;
        }

        MessageSendDto<Object> messageSendDto = new MessageSendDto<>();
        messageSendDto.setMessageType(MessageTypeEnum.PEER.getType());

        PeerMessageDto peerMessageDto = new PeerMessageDto();
        peerMessageDto.setSignalData(dataDto.getSignalData());
        peerMessageDto.setSignalType(dataDto.getSignalType());

        messageSendDto.setMessageContent(peerMessageDto);
        messageSendDto.setMeetingId(userTokenDTO.getCurrentMeetingId());
        messageSendDto.setSendUserId(userTokenDTO.getUserId());
        messageSendDto.setReceiveUserId(dataDto.getReceiveUserId());
        messageSendDto.setMessageSend2Type(MessageSendTypeEnum.USER.getCode());

        messageHandler.sendMessage(messageSendDto);

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("有连接加入。。。");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //todo 处理连接断开的逻辑
        Attribute<String> attr = ctx.channel().attr(AttributeKey.valueOf(ctx.channel().id().toString()));
        String userId = attr.get();
        userInfoService.updateLastLoginTime(userId);
        log.info("有连接断开。。。userId:{}", userId);

    }
}
