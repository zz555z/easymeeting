package com.zdd.entry.eum;

public enum ReceiveTypeEnum {


    ALL(0, "全员"),
    USER(1, "个人"),

    ;
    private Integer status;
    private String desc;

    ReceiveTypeEnum(Integer status, String desc) {
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