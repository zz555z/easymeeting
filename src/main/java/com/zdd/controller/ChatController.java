package com.zdd.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zdd.aop.annotation.GlobalInterceptor;
import com.zdd.config.TokenInterceptor;
import com.zdd.entry.domain.MeetingChatMessage;
import com.zdd.entry.domain.MeetingMember;
import com.zdd.entry.dto.UserTokenDTO;
import com.zdd.entry.eum.ReceiveTypeEnum;
import com.zdd.entry.eum.ResponseCodeEnum;
import com.zdd.entry.service.MeetingChatMessageService;
import com.zdd.entry.service.MeetingMemberService;
import com.zdd.entry.vo.ResponseVO;
import com.zdd.exception.BusinessException;
import com.zdd.utils.CommonUtils;
import org.apache.commons.lang.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private MeetingChatMessageService meetingChatMessageService;

    @Autowired
    private MeetingMemberService meetingMemberService;


    @PostMapping("/loadMessage")
    @GlobalInterceptor()
    public ResponseVO<List<MeetingChatMessage>> loadMessage(Long maxMessageId, Integer pageNo) {
        // 获取用户令牌信息，用于识别和验证用户身份
        UserTokenDTO userTokenDTO = TokenInterceptor.getUserTokenDTO();
        List<MeetingChatMessage> messageList = meetingChatMessageService.loadMessage(userTokenDTO, maxMessageId, pageNo);
        return ResponseVO.success(messageList);
    }


    @PostMapping("/sendMessage")
    @GlobalInterceptor()
    public ResponseVO<MeetingChatMessage> sendMessage(String message, @NotNull Integer messageType,
                                                      @NotEmpty String receiveUserId, String fileName,
                                                      Long fileSize, Integer fileType) {
        // 获取用户令牌信息，用于识别和验证用户身份
        UserTokenDTO userTokenDTO = TokenInterceptor.getUserTokenDTO();

        MeetingChatMessage meetingChatMessage = new MeetingChatMessage();
        meetingChatMessage.setMeetingId(userTokenDTO.getCurrentMeetingId());
        meetingChatMessage.setSendUserId(userTokenDTO.getUserId());
        meetingChatMessage.setSendUserNickName(userTokenDTO.getNickName());
        meetingChatMessage.setSendTime(System.currentTimeMillis());
        meetingChatMessage.setMessageType(messageType);
        meetingChatMessage.setFileName(fileName);
        meetingChatMessage.setFileSize(fileSize);
        meetingChatMessage.setFileType(fileType);
        meetingChatMessage.setMessageContent(message);
        if (ObjectUtils.equals("0", receiveUserId)) {
            meetingChatMessage.setReceiveType(ReceiveTypeEnum.ALL.getStatus());
        } else {
            meetingChatMessage.setReceiveType(ReceiveTypeEnum.USER.getStatus());
        }
        meetingChatMessage.setReceiveUserId(receiveUserId);

        meetingChatMessageService.saveChatMessage(meetingChatMessage);

        return ResponseVO.success(meetingChatMessage);
    }


    @PostMapping("/uploadFile")
    @GlobalInterceptor()
    public ResponseVO uploadFile(@NotNull MultipartFile multipartFile,
                                 @NotNull Long messageId, @NotNull Long sendTime) throws IOException {
        // 获取用户令牌信息，用于识别和验证用户身份
        UserTokenDTO userTokenDTO = TokenInterceptor.getUserTokenDTO();
        List<MeetingChatMessage> messageList = meetingChatMessageService.uploadFile(multipartFile, messageId, sendTime, userTokenDTO.getCurrentMeetingId());
        return ResponseVO.success(messageList);
    }

    @PostMapping("/loadHistoryMessage")
    @GlobalInterceptor()
    public ResponseVO<List<MeetingChatMessage>> loadHistoryMessage(@NotEmpty String meetingId,Long maxMessageId, Integer pageNo) {
        // 获取用户令牌信息，用于识别和验证用户身份
        UserTokenDTO userTokenDTO = TokenInterceptor.getUserTokenDTO();
        if (checkMeetingMember(meetingId, userTokenDTO.getUserId())) {
            throw new BusinessException(ResponseCodeEnum.RESPONSE_CODE_900);
        }
        List<MeetingChatMessage> messageList = meetingChatMessageService.loadHistoryMessage(meetingId,maxMessageId,pageNo);
        return ResponseVO.success(messageList);
    }


    public Boolean checkMeetingMember(String meetingId, String userId) {
        return meetingMemberService.count(new QueryWrapper<MeetingMember>().eq("meeting_id", meetingId).eq("user_id", userId)) > 0;
    }


}
