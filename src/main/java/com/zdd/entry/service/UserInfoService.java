package com.zdd.entry.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zdd.entry.domain.UserInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zdd.entry.dto.UserTokenDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
* @author zdd
* @description 针对表【user_info】的数据库操作Service
* @createDate 2025-06-26 15:20:27
*/
public interface UserInfoService extends IService<UserInfo> {

    void register(String email, String password);

    UserTokenDTO login(String email, String password);

    void updateLastLoginTime(String userId);

    void updateUserInfo(UserInfo userInfo, MultipartFile multipartFile) throws IOException;

    void updatePassword(UserTokenDTO userTokenDTO,String oldPassword, String newPassword);

    List<UserInfo> loadUserList(String nickName, Page<UserInfo> userInfoIPage);

    void updateUserStatus(UserInfo userInfo);

    void forceLine(String userId);
}
