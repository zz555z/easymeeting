package com.zdd.entry.eum;


public enum AppStatusEnum {

    INIT(0, "未发布"),
    GRAYSCALE(1, "灰度发布"),
    ALL(2, "全网发布");

    private int code;
    private String desc;

    AppStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }


    public static AppStatusEnum getByCode(Integer code) {
         for (AppStatusEnum value : AppStatusEnum.values()) {
            if (value.getCode() == code) {
                return value;
            }
        }
        return null;
    }




}
