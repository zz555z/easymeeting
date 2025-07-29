package com.zdd.aop.aspect;

import com.google.protobuf.BoolValueOrBuilder;
import com.zdd.aop.annotation.GlobalInterceptor;
import com.zdd.config.TokenInterceptor;
import com.zdd.entry.dto.UserTokenDTO;
import com.zdd.entry.eum.ResponseCodeEnum;
import com.zdd.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component
@Aspect
@Slf4j
public class GlobalInterceptorAspect {


    @Before("@annotation(com.zdd.aop.annotation.GlobalInterceptor)")
    public void beforeAspect(JoinPoint joinPoint) {
        try {
            Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
            GlobalInterceptor annotation = method.getAnnotation(GlobalInterceptor.class);
            if (annotation == null) {
                return;
            }
            if (annotation.checkLogin() || annotation.checkAdmin()) {
                checkLogin(annotation.checkAdmin());
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("aop异常", e);
            throw new BusinessException(ResponseCodeEnum.RESPONSE_CODE_903);
        }


    }

    private void checkLogin(Boolean admin) {
        UserTokenDTO userTokenDTO = TokenInterceptor.getUserTokenDTO();
        if (userTokenDTO == null) {
            throw new BusinessException(ResponseCodeEnum.RESPONSE_CODE_901);
        }

        if (!userTokenDTO.getAdmin() && admin) {
            throw new BusinessException(ResponseCodeEnum.RESPONSE_CODE_904);
        }


    }


}
