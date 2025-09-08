package com.zdd.controller;

import com.wf.captcha.ArithmeticCaptcha;
import com.zdd.aop.annotation.GlobalInterceptor;
import com.zdd.component.RedisComponent;
import com.zdd.config.TokenInterceptor;
import com.zdd.entry.constants.CommonConstant;
import com.zdd.entry.domain.UserInfo;
import com.zdd.entry.dto.SysSettingDto;
import com.zdd.entry.dto.UserTokenDTO;
import com.zdd.entry.eum.ResponseCodeEnum;
import com.zdd.entry.service.UserInfoService;
import com.zdd.entry.vo.ResponseVO;
import com.zdd.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@Validated
@RequestMapping("/login")
@Slf4j
public class LoginController {

    @Autowired
    private RedisComponent redisComponent;

    @Autowired
    private UserInfoService userInfoService;


    /**
     * 获取验证码
     * <p>
     * 本方法用于生成并返回一个验证码及其对应的键值对
     * 验证码用于用户身份验证，以增强系统安全性
     *
     * @return ResponseVO<Map> 包含验证码及其键值对的响应对象
     */
    @PostMapping("/checkCode")
    public ResponseVO<Map> checkCode() {
        // 创建一个算术验证码对象，指定验证码图片的宽度和高度
        ArithmeticCaptcha arithmeticCaptcha = new ArithmeticCaptcha(100, 42);

        // 生成验证码文本
        String code = arithmeticCaptcha.text();


        // 使用Redis组件将验证码存入缓存，并获取其对应的键
        String codeKey = redisComponent.setCheckCode(code);

        // 创建一个HashMap对象来存储验证码及其键
        Map<String, Object> resp = new HashMap<>();

        // 将验证码键放入响应对象中
        resp.put("checkCodeKey", codeKey);

        // 将验证码图片转换为Base64编码，并放入响应对象中
        resp.put("checkCode", arithmeticCaptcha.toBase64());

        log.info("验证码：checkCodeKey :{} checkCode :{}", codeKey, code);

        // 返回包含验证码及其键值对的响应对象
        return ResponseVO.success(resp);
    }

    /**
     * 用户注册接口
     * <p>
     * 该接口用于处理用户注册请求，接收用户邮箱、密码和验证码信息，验证通过后完成用户注册
     * 验证码通过Redis进行验证，确保安全性
     *
     * @param email        用户邮箱，必须是有效的邮箱格式，长度不超过150
     * @param password     用户密码，必须至少8位，包含字母、数字和特殊字符
     * @param checkCodeKey 验证码key，用于从Redis中获取验证码
     * @param checkCode    用户输入的验证码
     * @return 返回注册结果，成功或错误信息
     */
    @PostMapping("/register")
    public ResponseVO register(@NotEmpty @Email @Size(max = 150) String email,
                               @NotEmpty String password,
                               @NotEmpty String checkCodeKey,
                               @NotEmpty String checkCode,
                               String nickName) {
        try {
            // 验证验证码是否匹配
            if (!checkCode.equals(redisComponent.getCheckCode(checkCodeKey))) {
                throw new BusinessException(ResponseCodeEnum.RESPONSE_CODE_903);
            }
            // 验证码匹配，调用服务层方法进行用户注册
            userInfoService.register(email, password,nickName);
            // 注册成功，返回成功信息
            return ResponseVO.success();
        } finally {
            // 无论成功与否，最后删除验证码，确保安全性
            redisComponent.deleteCheckCode(checkCodeKey);
        }

    }


    /**
     * 用户登录接口
     *
     * @param email        用户邮箱，需要是有效格式的邮箱地址，长度不超过150
     * @param password     用户密码，必须至少8位，包含字母、数字和特殊字符
     * @param checkCodeKey 验证码key，用于从Redis中获取对应的验证码值
     * @param checkCode    用户输入的验证码值
     * @param response     HTTP响应对象，用于设置响应头和状态码
     * @return 返回登录结果，包括用户信息和令牌等
     * <p>
     * 此方法负责处理用户登录请求，验证用户提供的邮箱、密码和验证码信息
     * 如果验证码不匹配，则抛出业务异常如果验证码匹配，则调用用户服务进行登录，并在成功后设置用户的Token cookie
     * 最后，无论登录是否成功，都会删除Redis中的验证码，以保证验证码的一次性使用
     */
    @PostMapping("/login")
    public ResponseVO login(@NotEmpty @Email @Size(max = 150) String email,
                            @NotEmpty String password,
                            @NotEmpty String checkCodeKey,
                            @NotEmpty String checkCode,
                            HttpServletResponse response) {
        try {
            // 验证码验证
            if (!checkCode.equals(redisComponent.getCheckCode(checkCodeKey))) {
                throw new BusinessException(ResponseCodeEnum.RESPONSE_CODE_909);
            }
            // 用户登录逻辑处理
            UserTokenDTO userTokenDTO = userInfoService.login(email, password);
            // 登录成功，返回成功响应
            return ResponseVO.success(userTokenDTO);
        } finally {
            // 删除Redis中的验证码，确保验证码只能使用一次
            redisComponent.deleteCheckCode(checkCodeKey);
        }

    }

    @GlobalInterceptor()
    @PostMapping("/logout")
    public ResponseVO loginOut(HttpServletResponse response) {
//        clearCookie(response);
        UserTokenDTO userTokenDTO = TokenInterceptor.getUserTokenDTO();
        userInfoService.loginOut(userTokenDTO);
        return ResponseVO.success();

    }











}
