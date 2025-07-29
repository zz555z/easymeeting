package com.zdd.entry.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 用户联系申请记录表
 * @TableName user_contact_apply
 */
@TableName(value ="user_contact_apply")
@Data
public class UserContactApply {
    /**
     * 申请记录ID
     */
    @TableId(value = "apply_id", type = IdType.AUTO)
    private Integer applyId;

    /**
     * 申请用户ID
     */
    private String applyUserId;

    /**
     * 接收用户ID
     */
    private String receiveUserId;

    /**
     * 最后申请时间
     */
    private Date lastApplyTime;

    /**
     * 状态
     */
    private Integer status;


    @TableField(exist = false)
    private String nickName;



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
        UserContactApply other = (UserContactApply) that;
        return (this.getApplyId() == null ? other.getApplyId() == null : this.getApplyId().equals(other.getApplyId()))
            && (this.getApplyUserId() == null ? other.getApplyUserId() == null : this.getApplyUserId().equals(other.getApplyUserId()))
            && (this.getReceiveUserId() == null ? other.getReceiveUserId() == null : this.getReceiveUserId().equals(other.getReceiveUserId()))
            && (this.getLastApplyTime() == null ? other.getLastApplyTime() == null : this.getLastApplyTime().equals(other.getLastApplyTime()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getApplyId() == null) ? 0 : getApplyId().hashCode());
        result = prime * result + ((getApplyUserId() == null) ? 0 : getApplyUserId().hashCode());
        result = prime * result + ((getReceiveUserId() == null) ? 0 : getReceiveUserId().hashCode());
        result = prime * result + ((getLastApplyTime() == null) ? 0 : getLastApplyTime().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", applyId=").append(applyId);
        sb.append(", applyUserId=").append(applyUserId);
        sb.append(", receiveUserId=").append(receiveUserId);
        sb.append(", lastApplyTime=").append(lastApplyTime);
        sb.append(", status=").append(status);
        sb.append("]");
        return sb.toString();
    }
}