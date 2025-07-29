package com.zdd.entry.eum;

public enum MeetingMemberTypeEnum {

    NORMAL(0, "普通成员"),
    COMPERE(1, "主持人"),

    ;

    private Integer status;
    private String desc;

    MeetingMemberTypeEnum(Integer status, String desc) {
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
