package com.zdd.entry.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 会议聊天消息表
 * @TableName meeting_chat_message
 */
@TableName(value ="meeting_chat_message")
@Data
public class MeetingChatMessage {
    /**
     * 消息ID
     */
    @TableId( type = IdType.AUTO)
    private Long messageId;

    /**
     * 会议id
     */
    private String meetingId;

    /**
     * 消息类型
     */
    private Integer messageType;

    /**
     * 消息内容
     */
    private String messageContent;

    /**
     * 发送人ID
     */
    private String sendUserId;

    /**
     * 发送人昵称
     */
    private String sendUserNickName;

    /**
     * 发送时间
     */
    private Long sendTime;

    /**
     * 接收类型
     */
    private Integer receiveType;

    /**
     * 接收人ID
     */
    private String receiveUserId;

    /**
     * 文件大小
     */
    private Long fileSize;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件类型
     */
    private Integer fileType;

    /**
     * 文件后缀
     */
    private String fileSuffix;

    /**
     * 状态
     */
    private Integer status;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        MeetingChatMessage other = (MeetingChatMessage) that;
        return (this.getMessageId() == null ? other.getMessageId() == null : this.getMessageId().equals(other.getMessageId()))
            && (this.getMeetingId() == null ? other.getMeetingId() == null : this.getMeetingId().equals(other.getMeetingId()))
            && (this.getMessageType() == null ? other.getMessageType() == null : this.getMessageType().equals(other.getMessageType()))
            && (this.getMessageContent() == null ? other.getMessageContent() == null : this.getMessageContent().equals(other.getMessageContent()))
            && (this.getSendUserId() == null ? other.getSendUserId() == null : this.getSendUserId().equals(other.getSendUserId()))
            && (this.getSendUserNickName() == null ? other.getSendUserNickName() == null : this.getSendUserNickName().equals(other.getSendUserNickName()))
            && (this.getSendTime() == null ? other.getSendTime() == null : this.getSendTime().equals(other.getSendTime()))
            && (this.getReceiveType() == null ? other.getReceiveType() == null : this.getReceiveType().equals(other.getReceiveType()))
            && (this.getReceiveUserId() == null ? other.getReceiveUserId() == null : this.getReceiveUserId().equals(other.getReceiveUserId()))
            && (this.getFileSize() == null ? other.getFileSize() == null : this.getFileSize().equals(other.getFileSize()))
            && (this.getFileName() == null ? other.getFileName() == null : this.getFileName().equals(other.getFileName()))
            && (this.getFileType() == null ? other.getFileType() == null : this.getFileType().equals(other.getFileType()))
            && (this.getFileSuffix() == null ? other.getFileSuffix() == null : this.getFileSuffix().equals(other.getFileSuffix()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getMessageId() == null) ? 0 : getMessageId().hashCode());
        result = prime * result + ((getMeetingId() == null) ? 0 : getMeetingId().hashCode());
        result = prime * result + ((getMessageType() == null) ? 0 : getMessageType().hashCode());
        result = prime * result + ((getMessageContent() == null) ? 0 : getMessageContent().hashCode());
        result = prime * result + ((getSendUserId() == null) ? 0 : getSendUserId().hashCode());
        result = prime * result + ((getSendUserNickName() == null) ? 0 : getSendUserNickName().hashCode());
        result = prime * result + ((getSendTime() == null) ? 0 : getSendTime().hashCode());
        result = prime * result + ((getReceiveType() == null) ? 0 : getReceiveType().hashCode());
        result = prime * result + ((getReceiveUserId() == null) ? 0 : getReceiveUserId().hashCode());
        result = prime * result + ((getFileSize() == null) ? 0 : getFileSize().hashCode());
        result = prime * result + ((getFileName() == null) ? 0 : getFileName().hashCode());
        result = prime * result + ((getFileType() == null) ? 0 : getFileType().hashCode());
        result = prime * result + ((getFileSuffix() == null) ? 0 : getFileSuffix().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", messageId=").append(messageId);
        sb.append(", meetingId=").append(meetingId);
        sb.append(", messageType=").append(messageType);
        sb.append(", messageContent=").append(messageContent);
        sb.append(", sendUserId=").append(sendUserId);
        sb.append(", sendUserNickName=").append(sendUserNickName);
        sb.append(", sendTime=").append(sendTime);
        sb.append(", receiveType=").append(receiveType);
        sb.append(", receiveUserId=").append(receiveUserId);
        sb.append(", fileSize=").append(fileSize);
        sb.append(", fileName=").append(fileName);
        sb.append(", fileType=").append(fileType);
        sb.append(", fileSuffix=").append(fileSuffix);
        sb.append(", status=").append(status);
        sb.append("]");
        return sb.toString();
    }
}