package com.zdd.entry.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 
 * @TableName meeting_member
 */
@TableName(value ="meeting_member")
@Data
public class MeetingMember {
    /**
     * 
     */
    @TableId
    private String meetingId;

    /**
     * 
     */

    private String userId;

    /**
     * 
     */
    private String nickName;

    /**
     * 
     */
    @JsonFormat(pattern = "yyyy-mm-dd hh:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-mm-dd hh:mm:ss")
    private Date lastJoinTime;

    /**
     * 
     */
    private Integer status;

    /**
     * 
     */
    private Integer memberType;

    /**
     * 
     */
    private Integer meetingStatus;

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
        MeetingMember other = (MeetingMember) that;
        return (this.getMeetingId() == null ? other.getMeetingId() == null : this.getMeetingId().equals(other.getMeetingId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getNickName() == null ? other.getNickName() == null : this.getNickName().equals(other.getNickName()))
            && (this.getLastJoinTime() == null ? other.getLastJoinTime() == null : this.getLastJoinTime().equals(other.getLastJoinTime()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getMemberType() == null ? other.getMemberType() == null : this.getMemberType().equals(other.getMemberType()))
            && (this.getMeetingStatus() == null ? other.getMeetingStatus() == null : this.getMeetingStatus().equals(other.getMeetingStatus()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getMeetingId() == null) ? 0 : getMeetingId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getNickName() == null) ? 0 : getNickName().hashCode());
        result = prime * result + ((getLastJoinTime() == null) ? 0 : getLastJoinTime().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getMemberType() == null) ? 0 : getMemberType().hashCode());
        result = prime * result + ((getMeetingStatus() == null) ? 0 : getMeetingStatus().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", meetingId=").append(meetingId);
        sb.append(", userId=").append(userId);
        sb.append(", nickName=").append(nickName);
        sb.append(", lastJoinTime=").append(lastJoinTime);
        sb.append(", status=").append(status);
        sb.append(", memberType=").append(memberType);
        sb.append(", meetingStatus=").append(meetingStatus);
        sb.append("]");
        return sb.toString();
    }
}