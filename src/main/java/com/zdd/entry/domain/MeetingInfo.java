package com.zdd.entry.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * @TableName meeting_info
 */
@TableName(value = "meeting_info")
@Data
public class MeetingInfo {
    /**
     *
     */
    @TableId
    private String meetingId;

    /**
     *
     */
    private String meetingNo;

    /**
     *
     */
    private String meetingName;

    /**
     *
     */
    @JsonFormat(pattern = "yyyy-mm-dd hh:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-mm-dd hh:mm:ss")
    private Date createTime;

    /**
     *
     */
    private String createUserId;

    /**
     *
     */
    private Integer joinType;

    /**
     *
     */
    private String joinPassword;

    /**
     *
     */
    @JsonFormat(pattern = "yyyy-mm-dd hh:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-mm-dd hh:mm:ss")
    private Date startTime;

    /**
     *
     */
    @JsonFormat(pattern = "yyyy-mm-dd hh:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-mm-dd hh:mm:ss")
    private Date endTime;

    /**
     *
     */
    private Integer status;


    private Integer duration;

    @TableField(exist = false)
    private Long memberCount;

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
        MeetingInfo other = (MeetingInfo) that;
        return (this.getMeetingId() == null ? other.getMeetingId() == null : this.getMeetingId().equals(other.getMeetingId()))
                && (this.getMeetingNo() == null ? other.getMeetingNo() == null : this.getMeetingNo().equals(other.getMeetingNo()))
                && (this.getMeetingName() == null ? other.getMeetingName() == null : this.getMeetingName().equals(other.getMeetingName()))
                && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
                && (this.getCreateUserId() == null ? other.getCreateUserId() == null : this.getCreateUserId().equals(other.getCreateUserId()))
                && (this.getJoinType() == null ? other.getJoinType() == null : this.getJoinType().equals(other.getJoinType()))
                && (this.getJoinPassword() == null ? other.getJoinPassword() == null : this.getJoinPassword().equals(other.getJoinPassword()))
                && (this.getStartTime() == null ? other.getStartTime() == null : this.getStartTime().equals(other.getStartTime()))
                && (this.getEndTime() == null ? other.getEndTime() == null : this.getEndTime().equals(other.getEndTime()))
                && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getMeetingId() == null) ? 0 : getMeetingId().hashCode());
        result = prime * result + ((getMeetingNo() == null) ? 0 : getMeetingNo().hashCode());
        result = prime * result + ((getMeetingName() == null) ? 0 : getMeetingName().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getCreateUserId() == null) ? 0 : getCreateUserId().hashCode());
        result = prime * result + ((getJoinType() == null) ? 0 : getJoinType().hashCode());
        result = prime * result + ((getJoinPassword() == null) ? 0 : getJoinPassword().hashCode());
        result = prime * result + ((getStartTime() == null) ? 0 : getStartTime().hashCode());
        result = prime * result + ((getEndTime() == null) ? 0 : getEndTime().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", meetingId=").append(meetingId);
        sb.append(", meetingNo=").append(meetingNo);
        sb.append(", meetingName=").append(meetingName);
        sb.append(", createTime=").append(createTime);
        sb.append(", createUserId=").append(createUserId);
        sb.append(", joinType=").append(joinType);
        sb.append(", joinPassword=").append(joinPassword);
        sb.append(", startTime=").append(startTime);
        sb.append(", endTime=").append(endTime);
        sb.append(", status=").append(status);
        sb.append("]");
        return sb.toString();
    }
}