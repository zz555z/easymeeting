package com.zdd.entry.eum;

public enum MessageSendTypeEnum {
    USER(0, "用户消息"),
    GROUP(1, "群组消息");

    private int code;
    private String desc;

    MessageSendTypeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static MessageSendTypeEnum getByCode(int code) {
        for (MessageSendTypeEnum messageSendTypeEnum : values()) {
            if (messageSendTypeEnum.code == code) {
                return messageSendTypeEnum;
            }
        }
        return null;
    }

    public static MessageSendTypeEnum getByDesc(String desc) {
        for (MessageSendTypeEnum messageSendTypeEnum : values()) {
            if (messageSendTypeEnum.desc.equals(desc)) {
                return messageSendTypeEnum;
            }
        }
        return null;
    }
}
