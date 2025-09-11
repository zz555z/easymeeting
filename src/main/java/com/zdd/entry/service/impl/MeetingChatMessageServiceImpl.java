package com.zdd.entry.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zdd.config.AppConfig;
import com.zdd.entry.constants.CommonConstant;
import com.zdd.entry.domain.MeetingChatMessage;
import com.zdd.entry.dto.MessageSendDto;
import com.zdd.entry.dto.UserTokenDTO;
import com.zdd.entry.eum.*;
import com.zdd.entry.service.MeetingChatMessageService;
import com.zdd.entry.mapper.MeetingChatMessageMapper;
import com.zdd.exception.BusinessException;
import com.zdd.utils.CommonUtils;
import com.zdd.utils.FFmpegUtils;
import com.zdd.websocket.message.MessageHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author zdd
 * @description 针对表【meeting_chat_message(会议聊天消息表)】的数据库操作Service实现
 * @createDate 2025-07-18 14:18:38
 */
@Slf4j
@Service
public class MeetingChatMessageServiceImpl extends ServiceImpl<MeetingChatMessageMapper, MeetingChatMessage>
        implements MeetingChatMessageService {

    @Autowired
    private MessageHandler messageHandler;
    @Autowired
    private AppConfig appConfig;
    @Autowired
    private FFmpegUtils fFmpegUtils;

    @Override
    public List<MeetingChatMessage> loadMessage(UserTokenDTO userTokenDTO, Long maxMessageId, Integer pageNo) {
        QueryWrapper<MeetingChatMessage> queryWrapper = new QueryWrapper<MeetingChatMessage>().eq("meeting_id", userTokenDTO.getCurrentMeetingId())
                .or(qw -> qw.eq("send_user_id", userTokenDTO.getUserId()).eq("receive_user_id", userTokenDTO.getUserId()))
                .orderByAsc("message_id");
        if (maxMessageId != null) {
            queryWrapper.le("message_id", maxMessageId);
        }
        Page<MeetingChatMessage> page = new Page<>(pageNo == null ? 1 : pageNo, 20);
        return this.baseMapper.selectList(page, queryWrapper);
    }

    @Override
    public MeetingChatMessage saveChatMessage(MeetingChatMessage meetingChatMessage) {
        List<Integer> stausList = Arrays.asList(MessageTypeEnum.CHAT_TEXT_MESSAGE.getType(), MessageTypeEnum.CHAT_MEDIA_MESSAGE.getType());
        if (!stausList.contains(meetingChatMessage.getMessageType())) {
            throw new BusinessException("不存在的消息类型");
        }

        //文本消息
        Integer messageType = meetingChatMessage.getMessageType();
        if (messageType.equals(MessageTypeEnum.CHAT_TEXT_MESSAGE.getType())) {
            if (meetingChatMessage.getMessageContent().length() > 500) {
                log.error("消息内容过长");
                throw new BusinessException("消息内容过长");
            }
            if (StringUtils.isEmpty(meetingChatMessage.getMessageContent())) {
                log.error("消息内容不能为空");
                throw new BusinessException("消息内容不能为空");
            }
            meetingChatMessage.setStatus(MessageStatusEnum.SENDED.getStatus());

        }

        //媒体消息
        if (messageType.equals(MessageTypeEnum.CHAT_MEDIA_MESSAGE.getType())) {
            if (meetingChatMessage.getFileName() == null ||
                    meetingChatMessage.getFileType() == null ||
                    meetingChatMessage.getFileSize() == null) {
                log.error("媒体消息参数错误");
                throw new BusinessException("媒体消息参数错误");
            }
            meetingChatMessage.setStatus(MessageStatusEnum.SENDING.getStatus());
            meetingChatMessage.setFileSuffix(CommonUtils.getFileSuffix(meetingChatMessage.getFileName()));
        }


        this.baseMapper.insert(meetingChatMessage);

        MessageSendDto<Object> messageSendDto = new MessageSendDto<>();
        BeanUtils.copyProperties(meetingChatMessage, messageSendDto);

        if (meetingChatMessage.getReceiveType().equals(ReceiveTypeEnum.USER.getStatus())) {
            messageSendDto.setMessageSend2Type(MessageSendTypeEnum.USER.getCode());
//            messageHandler.sendMessage(messageSendDto);
            messageSendDto.setReceiveUserId(meetingChatMessage.getReceiveUserId());
            messageHandler.sendMessage(messageSendDto);

        } else {
            messageSendDto.setMessageSend2Type(MessageSendTypeEnum.GROUP.getCode());
            messageHandler.sendMessage(messageSendDto);

        }

        return meetingChatMessage;

    }

    @Override
    public List<MeetingChatMessage> uploadFile(MultipartFile multipartFile, Long messageId, Long sendTime, String currentMeetingId) throws IOException {

        String fileSuffix = CommonUtils.getFileSuffix(Objects.requireNonNull(multipartFile.getOriginalFilename()));
        FileTypeEnum fileTypeEnum = FileTypeEnum.fromSuffix(fileSuffix);


        String folder = CommonUtils.getUploadFilePath(appConfig.getFolder(), sendTime,fileTypeEnum);


        String filePath = folder + File.separator + messageId;


        log.info("上传文件目录：{}", folder);
        File file = new File(folder);
        if (!file.exists()) {
            file.mkdirs();
        }

        if (fileTypeEnum == FileTypeEnum.IMAGE) {
            File tmpFile = transferToTmpFile(multipartFile);
            filePath = filePath + CommonConstant.IMAGE_SUFFIX;
            filePath = fFmpegUtils.transferImageType(tmpFile, filePath);
            fFmpegUtils.createImageThumbnail(filePath);
        } else if (fileTypeEnum == FileTypeEnum.VIDEO) {
            File tmpFile = transferToTmpFile(multipartFile);
            filePath = filePath + CommonConstant.VIDEO_SUFFIX;
            fFmpegUtils.transferVideoType(tmpFile, filePath, fileSuffix);
            fFmpegUtils.createImageThumbnail(filePath);
        } else {
            filePath = filePath + fileSuffix;
            multipartFile.transferTo(new File(filePath));

        }

        MeetingChatMessage meetingChatMessage = new MeetingChatMessage();
//        meetingChatMessage.setMeetingId(currentMeetingId);
        meetingChatMessage.setMessageId(messageId);
        meetingChatMessage.setStatus(MessageStatusEnum.SENDED.getStatus());
        this.baseMapper.updateById(meetingChatMessage);

        MessageSendDto<Object> messageSendDto = new MessageSendDto<>();
        messageSendDto.setMessageSend2Type(MessageSendTypeEnum.GROUP.getCode());
        messageSendDto.setMessageType(MessageTypeEnum.CHAT_MEDIA_MESSAGE_UPDATE.getType());
        messageSendDto.setMessageId(messageId);
        messageSendDto.setMeetingId(currentMeetingId);
        messageSendDto.setStatus(MessageStatusEnum.SENDED.getStatus());
        messageHandler.sendMessage(messageSendDto);


        return null;
    }

    @Override
    public List<MeetingChatMessage> loadHistoryMessage(String meetingId, Long maxMessageId, Integer pageNo) {
        QueryWrapper<MeetingChatMessage> queryWrapper = new QueryWrapper<MeetingChatMessage>().eq("meeting_id", meetingId)
                .orderByAsc("message_id");
        if (maxMessageId != null) {
            queryWrapper.le("message_id", maxMessageId);
        }
        Page<MeetingChatMessage> page = new Page<>(pageNo == null ? 1 : pageNo, 20);
        return this.baseMapper.selectList(page, queryWrapper);
    }


    /**
     * 保存临时文件
     *
     * @param multipartFile
     * @return
     * @throws IOException
     */
    private File transferToTmpFile(MultipartFile multipartFile) throws IOException {
        File tmpFile = new File(CommonUtils.getTmpPath(appConfig.getFolder()) + CommonUtils.getMeetingNo());
        if (!tmpFile.exists()) {
            tmpFile.mkdirs();
        }
        multipartFile.transferTo(tmpFile);
        return tmpFile;
    }
}




