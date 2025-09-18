package com.zdd.entry.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zdd.entry.domain.MeetingReserveMember;
import com.zdd.entry.mapper.MeetingReserveMemberMapper;
import com.zdd.entry.service.MeetingReserveMemberService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author zdd
* @description 针对表【meeting_reserve_member】的数据库操作Service实现
* @createDate 2025-07-11 16:18:41
*/
@Service
public class MeetingReserveMemberServiceImpl extends ServiceImpl<MeetingReserveMemberMapper, MeetingReserveMember>
    implements MeetingReserveMemberService{

    @Override
    public void insertBatch(List<MeetingReserveMember> meetingReserveMembers) {

        this.saveBatch(meetingReserveMembers);
    }

    @Override
    public MeetingReserveMember getMeetingReserveMember(String meetingId, String userId) {
        return this.baseMapper.selectOne(new QueryWrapper<MeetingReserveMember>().eq("meeting_id", meetingId)
                .eq("invite_user_id", userId));
//                .or(qw ->
//                        qw.eq("invite_user_id", userId)
//                                .eq("create_user_id", userId)));

    }
}




