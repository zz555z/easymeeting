package com.zdd.entry.eum;

public enum UserSexEnum {
    FEMALE(0, "女"),
    MALE(1, "男"),
    UNKNOWN(2, "未知");

    private int code;
    private String desc;

    UserSexEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
