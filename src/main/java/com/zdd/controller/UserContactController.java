package com.zdd.controller;

import com.zdd.aop.annotation.GlobalInterceptor;
import com.zdd.config.TokenInterceptor;
import com.zdd.entry.domain.UserContact;
import com.zdd.entry.domain.UserContactApply;
import com.zdd.entry.domain.UserInfo;
import com.zdd.entry.dto.UserTokenDTO;
import com.zdd.entry.service.UserContactApplyService;
import com.zdd.entry.service.UserContactService;
import com.zdd.entry.service.UserInfoService;
import com.zdd.entry.vo.ResponseVO;
import com.zdd.entry.vo.UserInfoVO4Search;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.List;

@RestController
@Validated
@RequestMapping("/userContact")
@Slf4j
public class UserContactController {
    @Autowired
    private UserContactService userContactService;
    @Autowired
    private UserContactApplyService userContactApplyService;
    @Autowired
    private UserInfoService userInfoService;


    @PostMapping("/loadContactApplyDealWithCount")
    @GlobalInterceptor()
    public ResponseVO loadContactApplyDealWithCount() {
        UserTokenDTO userTokenDTO = TokenInterceptor.getUserTokenDTO();
        Long count = userContactApplyService.loadContactApplyDealWithCount(userTokenDTO.getUserId());
        return ResponseVO.success(count);
    }


    @PostMapping("/searchContact")
    @GlobalInterceptor()
    public ResponseVO searchContact(@NotEmpty String contactId) {
        UserTokenDTO userTokenDTO = TokenInterceptor.getUserTokenDTO();
        UserInfoVO4Search userInfoVO4Search = userContactService.searchContact(contactId, userTokenDTO.getUserId());
        return ResponseVO.success(userInfoVO4Search);
    }


    @PostMapping("/contactApply")
    @GlobalInterceptor()
    public ResponseVO contactApply(@NotEmpty String contactId) {
        UserTokenDTO userTokenDTO = TokenInterceptor.getUserTokenDTO();
        UserContactApply userContactApply = new UserContactApply();
        userContactApply.setApplyUserId(userTokenDTO.getUserId());
        userContactApply.setReceiveUserId(contactId);
        Integer result = userContactApplyService.saveUserContactApply(userContactApply);
        return ResponseVO.success(result);
    }


    @PostMapping("/dealWithApply")
    @GlobalInterceptor()
    public ResponseVO dealWithApply(@NotEmpty String applyUserId, @NotNull Integer status) {
        UserTokenDTO userTokenDTO = TokenInterceptor.getUserTokenDTO();
        userContactApplyService.dealWithApply(applyUserId, userTokenDTO.getUserId(), userTokenDTO.getNickName(), status);
        return ResponseVO.success();
    }

    @PostMapping("/loadContactUser")
    @GlobalInterceptor()
    public ResponseVO loadContactUser() {
        UserTokenDTO userTokenDTO = TokenInterceptor.getUserTokenDTO();
        List<UserContact> userContactList = userContactService.loadContactUser(userTokenDTO.getUserId());
        return ResponseVO.success(userContactList);
    }


    @PostMapping("/loadContactApply")
    @GlobalInterceptor()
    public ResponseVO loadContactApply() {
        UserTokenDTO userTokenDTO = TokenInterceptor.getUserTokenDTO();
        List<UserContactApply> userContactList = userContactApplyService.loadContactApply(userTokenDTO.getUserId());
        return ResponseVO.success(userContactList);
    }

    @PostMapping("/delContact")
    @GlobalInterceptor()
    public ResponseVO delContact(@NotEmpty String contactId, @NotNull Integer status) {
        UserTokenDTO userTokenDTO = TokenInterceptor.getUserTokenDTO();
        userContactService.delContact(userTokenDTO.getUserId(), contactId, status);
        return ResponseVO.success();
    }

    @PostMapping("/updateUserInfo")
    @GlobalInterceptor()

    public ResponseVO updateUserInfo(MultipartFile multipartFile,
                                     @NotEmpty String nickName,
                                     @NotNull Integer sex) throws IOException {
        UserTokenDTO userTokenDTO = TokenInterceptor.getUserTokenDTO();
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(userTokenDTO.getUserId());
        userInfo.setNickName(nickName);
        userInfo.setSex(sex);
        userInfoService.updateUserInfo(userInfo, multipartFile);


        return ResponseVO.success();

    }

    @PostMapping("/updatePassword")
    @GlobalInterceptor()
    public ResponseVO updatePassword(@NotEmpty String oldPassword,@NotEmpty String rePassword) {
        UserTokenDTO userTokenDTO = TokenInterceptor.getUserTokenDTO();
        userInfoService.updatePassword(userTokenDTO,oldPassword, rePassword);
        return ResponseVO.success();

    }

}
