package cn.imustacm.user.controller.advice;

import cn.imustacm.common.domain.Resp;
import cn.imustacm.common.enums.ErrorCodeEnum;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 统一异常处理类
 *
 * @author liandong
 * Date: 2019/08/18
 */
@RestControllerAdvice
public class GlobalExceptionAdvice {


    @ExceptionHandler(Exception.class)
    public Resp exceptionHandler(Exception e) {
        return new Resp(ErrorCodeEnum.SERVER_ERR);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Resp handleBindException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        return new Resp(ErrorCodeEnum.BIZ_PARAM_ERR, fieldError.getDefaultMessage());
    }

    @ExceptionHandler(BindException.class)
    public Resp handleBindException(BindException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        return new Resp(ErrorCodeEnum.BIZ_PARAM_ERR, fieldError.getDefaultMessage());
    }
}
