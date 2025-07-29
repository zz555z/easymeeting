package com.zdd.entry.eum;

public enum MessageStatusEnum {


    SENDING(0, "正在发送"),
    SENDED(1, "发送完毕"),

    ;
    private Integer status;
    private String desc;

    MessageStatusEnum(Integer status, String desc) {
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