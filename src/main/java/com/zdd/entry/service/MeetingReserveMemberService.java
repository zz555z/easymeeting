package com.zdd.entry.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zdd.entry.domain.MeetingReserveMember;

import java.util.List;

/**
* @author zdd
* @description 针对表【meeting_reserve_member】的数据库操作Service
* @createDate 2025-07-11 16:18:41
*/
public interface MeetingReserveMemberService extends IService<MeetingReserveMember> {

    void insertBatch(List<MeetingReserveMember> meetingReserveMembers);

    MeetingReserveMember getMeetingReserveMember(String meetingId, String userId);
}
