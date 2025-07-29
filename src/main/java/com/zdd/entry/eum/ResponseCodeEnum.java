package com.zdd.entry.eum;

public enum ResponseCodeEnum {
    RESPONSE_CODE_900(900,"业务异常"),
    RESPONSE_CODE_901(901,"请重新登录"),
    RESPONSE_CODE_902(902,"账号或密码不存在"),
    RESPONSE_CODE_903(903,"参数异常"),
    RESPONSE_CODE_904(904,"权限不足"),
    RESPONSE_CODE_905(905,"请添加联系人为好友"),
    RESPONSE_CODE_906(906,"请重新加入群聊"),
    RESPONSE_CODE_907(907,"邮箱已存在"),
    RESPONSE_CODE_908(908,"存在未结束的会议")





    ;

    private int code;
    private String desc;

    ResponseCodeEnum(int code, String desc) {
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
