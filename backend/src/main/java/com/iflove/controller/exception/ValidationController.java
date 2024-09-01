package com.iflove.controller.exception;

import com.iflove.entity.RestBean;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author 苍镜月
 * @version 1.0
 * @implNote
 */
@RestControllerAdvice
@Slf4j
public class ValidationController {

    /**
     * 校验不通过打印警告信息
     * @param e 验证异常
     * @return 校验结果
     */
    @ExceptionHandler(ValidationException.class)
    public RestBean<Void> validateException(ValidationException e) {
        log.warn("Resolve [{}: {}]", e.getClass().getName(), e.getMessage());
        return RestBean.failure(400, "请求参数错误");
    }
}
