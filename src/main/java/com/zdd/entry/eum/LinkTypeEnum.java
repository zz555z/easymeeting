package com.zdd.entry.eum;


public enum LinkTypeEnum {

    LOCAL_FILE(0, "本地文件"),
    EXTERNAL_LINK(1, "外链");

    private int code;
    private String desc;

    LinkTypeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }


    public static LinkTypeEnum getByCode(Integer code) {
         for (LinkTypeEnum value : LinkTypeEnum.values()) {
            if (value.getCode() == code) {
                return value;
            }
        }
        return null;
    }




}
