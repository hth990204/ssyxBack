package com.atguigu.ssyx.common.result;

import lombok.Data;

@Data
public class Result<T> {
    private Integer code;

    private String message;

    private T data;

    private Result() {}

    public static<T> Result<T> build(T data, ResultCodeEnum resultCodeEnum) {
        Result<T> result = new Result<>();
        if (data != null) {
            result.setData(data);
        }
        result.setCode(resultCodeEnum.getCode());
        result.setMessage(resultCodeEnum.getMessage());
        return result;
    }

    public static<T> Result<T> ok(T data) {
        return build(data, ResultCodeEnum.SUCCESS);
    }
    public static<T> Result<T> fail(T data) {
        return build(data, ResultCodeEnum.FAIL);
    }
}
