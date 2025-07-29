package com.zdd.entry.eum;

public enum UserStatusEnum {
    LOCK(0, "禁用"),
    NORMAL(1, "启用"),

    ;
    private int code;
    private String desc;

    public static UserStatusEnum getByCode(Integer userStatus) {
        for (UserStatusEnum value : UserStatusEnum.values()) {
            if (value.code == userStatus) {
                return value;
            }
        }
        return null;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    UserStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
