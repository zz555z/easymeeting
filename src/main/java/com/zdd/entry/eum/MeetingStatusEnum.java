package com.zdd.entry.eum;

public enum MeetingStatusEnum {


    RUN(0, "会议进行"),
    OVER(1, "会议结束"),
    NO_START(2, "会议未开始"),
    DEL(3, "会议被删除"),

    ;
    private Integer status;
    private String desc;

    MeetingStatusEnum(Integer status, String desc) {
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