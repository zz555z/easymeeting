package com.zdd.entry.service;

import com.zdd.entry.domain.UserContactApply;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author zdd
* @description 针对表【user_contact_apply(用户联系申请记录表)】的数据库操作Service
* @createDate 2025-07-15 14:38:37
*/
public interface UserContactApplyService extends IService<UserContactApply> {

    Integer saveUserContactApply(UserContactApply userContactApply);


    void dealWithApply(String applyUserId,String userId,String name,Integer status);

    List<UserContactApply> loadContactApply(String userId);

    Long loadContactApplyDealWithCount(String userId);
}
