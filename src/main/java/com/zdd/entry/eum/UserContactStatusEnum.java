package com.zdd.entry.eum;

import org.apache.commons.lang3.StringUtils;

public enum UserContactStatusEnum {
    FRIEND( 1, "好友"),
    DEL( 2, "删除好友"),
    BLACKLIST( 3,  "拉黑好友");

    private Integer status;
    private String desc;

    UserContactStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public Integer getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }

    public static UserContactStatusEnum getByStatus(String status) {
        try {
            if (StringUtils.isEmpty( status)) {
                return null;
            }
            return UserContactStatusEnum.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static UserContactStatusEnum getByStatus(Integer status) {
        for (UserContactStatusEnum item : UserContactStatusEnum.values()) {
            if (item.getStatus().equals(status)) {
                return item;
            }
        }
        return null;
    }
}