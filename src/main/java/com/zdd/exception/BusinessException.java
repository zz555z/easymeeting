package com.zdd.exception;

import com.zdd.entry.eum.ResponseCodeEnum;
import lombok.Data;

@Data
public class BusinessException extends RuntimeException {

    private Integer code;
    private String msg;

    public BusinessException() {
        super();
    }

    public BusinessException(String message) {
        super(message);
        this.code= ResponseCodeEnum.RESPONSE_CODE_900.getCode();
        this.msg = message;
    }

    public BusinessException(ResponseCodeEnum responseCodeEnum) {
        super(responseCodeEnum.getDesc());
        this.code= responseCodeEnum.getCode();
        this.msg = responseCodeEnum.getDesc();
    }

    public BusinessException(Integer code , String message) {
        super(message);
        this.code=code;
        this.msg = message;
    }





}
