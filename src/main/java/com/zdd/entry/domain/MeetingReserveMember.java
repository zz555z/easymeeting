package com.zdd.entry.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 
 * @TableName meeting_reserve_member
 */
@TableName(value ="meeting_reserve_member")
@Data
public class MeetingReserveMember {
    /**
     * 会议id
     */
    private String meetingId;

    /**
     * 邀请用户id
     */
    private String inviteUserId;

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
        MeetingReserveMember other = (MeetingReserveMember) that;
        return (this.getMeetingId() == null ? other.getMeetingId() == null : this.getMeetingId().equals(other.getMeetingId()))
            && (this.getInviteUserId() == null ? other.getInviteUserId() == null : this.getInviteUserId().equals(other.getInviteUserId()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getMeetingId() == null) ? 0 : getMeetingId().hashCode());
        result = prime * result + ((getInviteUserId() == null) ? 0 : getInviteUserId().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", meetingId=").append(meetingId);
        sb.append(", inviteUserId=").append(inviteUserId);
        sb.append("]");
        return sb.toString();
    }
}