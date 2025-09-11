package com.zdd.entry.service;

import com.zdd.entry.domain.MeetingChatMessage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zdd.entry.dto.UserTokenDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
* @author zdd
* @description 针对表【meeting_chat_message(会议聊天消息表)】的数据库操作Service
* @createDate 2025-07-18 14:18:38
*/
public interface MeetingChatMessageService extends IService<MeetingChatMessage> {

    List<MeetingChatMessage> loadMessage(UserTokenDTO userTokenDTO, Long maxMessageId, Integer pageNo);

    MeetingChatMessage saveChatMessage(MeetingChatMessage meetingChatMessage);

    List<MeetingChatMessage> uploadFile(MultipartFile multipartFile, Long messageId, Long sendTime, String currentMeetingId) throws IOException;

    List<MeetingChatMessage> loadHistoryMessage(String meetingId,Long maxMessageId, Integer pageNo);
}
