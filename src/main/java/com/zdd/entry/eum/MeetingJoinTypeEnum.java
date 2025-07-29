package com.zdd.entry.eum;

public enum MeetingJoinTypeEnum {

    NO_PASSWORD(0, "无需密码"),
    PASSWORD(1, "需要密码"),

    ;

    private Integer status;
    private String desc;

    MeetingJoinTypeEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public Integer getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }
}
