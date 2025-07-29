package com.zdd.entry.mapper;

import com.zdd.entry.domain.UserContact;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
* @author zdd
* @description 针对表【user_contact(用户联系人表)】的数据库操作Mapper
* @createDate 2025-07-15 14:38:37
* @Entity com.zdd.entry.domain.UserContact
*/
public interface UserContactMapper extends BaseMapper<UserContact> {

    void saveBatch(List<UserContact> userContactList);
}




