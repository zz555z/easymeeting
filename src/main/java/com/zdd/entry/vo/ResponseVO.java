package com.zdd.entry.vo;

import lombok.Data;

@Data
public class ResponseVO<T> {
    private int code; // HTTP状态码
    private String message; // 错误信息或内容信息
    private T data; // 数据内容

    private static int SUCCESS_CODE = 200;
    private static String SUCCESS_MSG = "success";
    private static int ERRER_CODE = 500;
    private static String ERRER_MSG = "errer";

    // 私有构造函数，防止外部直接实例化
    private ResponseVO() {}

    /**
     * 成功响应，不带数据
     * @return 成功响应对象
     */
    public static <T> ResponseVO<T> success() {
        return new ResponseVO<T>() {{
            setCode(SUCCESS_CODE);
            setMessage(SUCCESS_MSG);
        }};
    }

    /**
     * 成功响应，带数据
     * @param data 数据内容
     * @return 成功响应对象
     */
    public static <T> ResponseVO<T> success(T data) {
        return new ResponseVO<T>() {{
            setCode(SUCCESS_CODE);
            setMessage(SUCCESS_MSG);
            setData(data);
        }};
    }

    /**
     * 失败响应，带错误信息
     * @return 失败响应对象
     */
    public static <T> ResponseVO<T> error() {
        return new ResponseVO<T>() {{
            setCode(ERRER_CODE);
            setMessage(ERRER_MSG);
        }};
    }

    /**
     * 失败响应，带错误信息
     * @return 失败响应对象
     */
    public static <T> ResponseVO<T> errorAndCode(Integer code,String msg) {
        return new ResponseVO<T>() {{
            setCode(code);
            setMessage(msg);
        }};
    }



    /**
     * 失败响应，带错误信息和数据
     * @return 失败响应对象
     */
    public static <T> ResponseVO<T> error(String msg) {
        return new ResponseVO<T>() {{
            setCode(ERRER_CODE);
            setMessage(msg);
        }};
    }
}
