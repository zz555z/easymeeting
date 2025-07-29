package com.zdd.exception;

import com.zdd.entry.vo.ResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.validation.ConstraintViolationException;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理所有未捕获异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseVO handleException(Exception e) {
        log.error("系统异常: {}", e.getMessage(), e);
        return ResponseVO.error("系统异常");
    }

    /**
     * 处理 404 请求
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseVO handleNotFound(NoHandlerFoundException e) {
        log.error("页面不存在: {}", e.getMessage(), e);
        return ResponseVO.error("页面不存在");
    }


    /**
     * 处理 @Validated 校验失败异常（@RequestParam 或 @PathVariable 校验）
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseVO handleConstraintViolationException(ConstraintViolationException ex) {
        // 提取参数错误信息
        Map<String, String> errors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        violation -> violation.getMessage()
                ));
        log.error("参数异常: {}",errors);
        return ResponseVO.error("参数异常");
    }

    /**
     * 处理 BusinessException 异常
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseVO handleBusinessException(BusinessException e) {
        log.error("业务异常:code : {} msg:{} e:{}", e.getCode(), e.getMessage(),e);
        return ResponseVO.errorAndCode(e.getCode(), e.getMessage());
    }

    /**
     * 处理 ValidationException 异常
     */
    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseVO handleValidationException(ValidationException e) {
        log.error("验证异常: {}", e.getMessage(), e);
        return ResponseVO.error(e.getMessage());
    }


}
