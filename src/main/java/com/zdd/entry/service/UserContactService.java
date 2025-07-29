package com.zdd.entry.service;

import com.zdd.entry.domain.UserContact;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zdd.entry.vo.UserInfoVO4Search;

import java.util.List;

/**
* @author zdd
* @description 针对表【user_contact(用户联系人表)】的数据库操作Service
* @createDate 2025-07-15 14:38:37
*/
public interface UserContactService extends IService<UserContact> {

    UserInfoVO4Search searchContact(String contactId, String userId);

    List<UserContact> loadContactUser(String userId);

    void delContact(String userId, String contactId, Integer status);
}
