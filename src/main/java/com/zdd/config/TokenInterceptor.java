package com.zdd.config;

import com.zdd.component.RedisComponent;
import com.zdd.entry.dto.UserTokenDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class TokenInterceptor implements HandlerInterceptor {

    private static final ThreadLocal<UserTokenDTO> tokenHolder = new ThreadLocal<>();

    @Autowired
    private RedisComponent redisComponent;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = request.getHeader("token");
        if (token != null) {
            tokenHolder.set(redisComponent.getUserTokenDTO(token));
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        tokenHolder.remove(); // 清理 ThreadLocal，避免内存泄漏
    }


    public static UserTokenDTO getUserTokenDTO() {
        return tokenHolder.get();
    }


}

