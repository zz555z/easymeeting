package com.zdd.entry.eum;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.zdd.entry.domain.MeetingMember;

import java.util.Arrays;

public enum MeetingMemberStatusEnum {

    DEL_MEETINGING(0, "删除会议"),
    NORMAL(1, "正常"),
    EXIT_MEETINGING(2, "退出会议"),
    KICK_OUT(3, "被踢出会"),
    BLACKLIST(4, "被拉黑");

    private Integer status;
    private String desc;

    MeetingMemberStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public static boolean isKickedOrBlacklisted(Integer status) {
        if (Arrays.asList(MeetingMemberStatusEnum.KICK_OUT.getStatus(), MeetingMemberStatusEnum.BLACKLIST.getStatus())
                .contains(status)) {
            return true;
        }
        return false;
    }

    public Integer getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }
}