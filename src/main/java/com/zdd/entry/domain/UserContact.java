package com.zdd.entry.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

import lombok.Data;

/**
 * 用户联系人表
 *
 * @TableName user_contact
 */
@TableName(value = "user_contact")
@Data
public class UserContact {
    /**
     * 用户ID
     */
    @TableId
    private String userId;

    /**
     * 联系人ID
     */
    private String contactId;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 最后更新时间
     */
    private Date lastUpdateTime;

    @TableField(exist = false)
    private String nickName;

    @TableField(exist = false)
    private Long lastLoginTime;

    @TableField(exist = false)
    private Long lastOffTime;

    @TableField(exist = false)
    private Integer onlinetype;

    public Integer getOnlinetype() {
        if (this.lastLoginTime != null && this.lastOffTime != null && this.lastLoginTime >= lastOffTime) {
            return 1;
        } else {
            return 0;
        }

    }


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
        UserContact other = (UserContact) that;
        return (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
                && (this.getContactId() == null ? other.getContactId() == null : this.getContactId().equals(other.getContactId()))
                && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
                && (this.getLastUpdateTime() == null ? other.getLastUpdateTime() == null : this.getLastUpdateTime().equals(other.getLastUpdateTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getContactId() == null) ? 0 : getContactId().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getLastUpdateTime() == null) ? 0 : getLastUpdateTime().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", userId=").append(userId);
        sb.append(", contactId=").append(contactId);
        sb.append(", status=").append(status);
        sb.append(", lastUpdateTime=").append(lastUpdateTime);
        sb.append("]");
        return sb.toString();
    }
}