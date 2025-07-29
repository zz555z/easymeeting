package com.zdd.entry.service;

import com.zdd.entry.domain.MeetingMember;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author zdd
* @description 针对表【meeting_member】的数据库操作Service
* @createDate 2025-06-30 16:42:14
*/
public interface MeetingMemberService extends IService<MeetingMember> {

    void updateStatus(String userId , String meetingId , Integer status);

}
