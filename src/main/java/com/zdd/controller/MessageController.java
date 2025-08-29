package com.zdd.controller;


import com.zdd.component.RedisComponent;
import com.zdd.config.TokenInterceptor;
import com.zdd.entry.domain.MeetingInfo;
import com.zdd.entry.dto.MeetingJoinDto;
import com.zdd.entry.dto.MessageSendDto;
import com.zdd.entry.dto.UserTokenDTO;
import com.zdd.entry.eum.MessageSendTypeEnum;
import com.zdd.entry.eum.MessageTypeEnum;
import com.zdd.entry.mapper.MeetingInfoMapper;
import com.zdd.entry.mapper.MeetingMemberMapper;
import com.zdd.websocket.message.MessageHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/message")
@Slf4j
public class MessageController {

    @Autowired
    private RedisComponent redisComponent;

    @Autowired
    private MessageHandler messageHandler;

    @Autowired
    private MeetingInfoMapper mapper;


    @GetMapping("/sendMessage")
    public void sendMessage(){
        UserTokenDTO userTokenDTO = TokenInterceptor.getUserTokenDTO();
        MeetingInfo meetingInfo = mapper.selectById(userTokenDTO.getCurrentMeetingId());

        //发送加入会议的消息
        sendMsg(userTokenDTO, meetingInfo);
    }


    private void sendMsg(UserTokenDTO userTokenDTO, MeetingInfo meetingInfo) {
        MeetingJoinDto meetingJoinDto = new MeetingJoinDto();
        meetingJoinDto.setMeetingMemberDto(redisComponent.getMeetingMember(meetingInfo.getMeetingId(), userTokenDTO.getUserId()));
        meetingJoinDto.setMeetingMemberDtoList(redisComponent.getMeetingList(meetingInfo.getMeetingId()));

        MessageSendDto<MeetingJoinDto> messageSendDto = new MessageSendDto<>();
        messageSendDto.setMessageType(MessageTypeEnum.ADD_MEETING_ROOM.getType());
        messageSendDto.setMessageContent(meetingJoinDto);
        messageSendDto.setMeetingId(meetingInfo.getMeetingId());
        messageSendDto.setMessageSend2Type(MessageSendTypeEnum.GROUP.getCode());

        messageHandler.sendMessage(messageSendDto);
    }



}
