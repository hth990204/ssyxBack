package com.atguigu.ssyx.common.execption;

import com.atguigu.ssyx.common.result.Result;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result error(Exception e) {
        e.printStackTrace();
        return Result.fail(null);
    }

    @ExceptionHandler(SsyxException.class)
    @ResponseBody
    public Result error(SsyxException exception) {
        return Result.build(null, exception.getCode(), exception.getMessage());
    }

}
