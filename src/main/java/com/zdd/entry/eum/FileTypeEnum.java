package com.zdd.entry.eum;

import java.util.Objects;

public enum FileTypeEnum {
    IMAGE(0, new String[]{".jpeg", ".jpg", ".png", ".gif", ".bmp", ".webp"}, ".jpg", "图片"),
    VIDEO(1, new String[]{".mp4", ".avi", ".rmvb", ".mkv", ".mov"}, ".mp4", "视频");

    private Integer type;
    private String[] suffixArray;
    private String suffix;
    private String desc;

    FileTypeEnum(Integer type, String[] suffixArray, String suffix, String desc) {
        this.type = type;
        this.suffixArray = suffixArray;
        this.suffix = suffix;
        this.desc = desc;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String[] getSuffixArray() {
        return suffixArray;
    }

    public void setSuffixArray(String[] suffixArray) {
        this.suffixArray = suffixArray;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public static FileTypeEnum fromSuffix(String suffix) {
        for (FileTypeEnum typeEnum : FileTypeEnum.values()) {
            for (String s : typeEnum.getSuffixArray()) {
                if (s.equalsIgnoreCase(suffix)) {
                    return typeEnum;
                }
            }
        }
        return null; // 没有匹配的类型时返回null
    }

    public static FileTypeEnum getByType(Integer type) {
        for (FileTypeEnum typeEnum : FileTypeEnum.values()) {
           if (Objects.equals(typeEnum.getType(), type)){
               return typeEnum;
           }
        }
        return null; // 没有匹配的类型时返回null
    }
}