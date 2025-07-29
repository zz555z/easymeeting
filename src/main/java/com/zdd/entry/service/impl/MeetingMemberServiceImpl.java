package com.zdd.entry.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zdd.entry.domain.MeetingMember;
import com.zdd.entry.eum.MeetingMemberStatusEnum;
import com.zdd.entry.service.MeetingMemberService;
import com.zdd.entry.mapper.MeetingMemberMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author zdd
 * @description 针对表【meeting_member】的数据库操作Service实现
 * @createDate 2025-06-30 16:42:14
 */
@Service
public class MeetingMemberServiceImpl extends ServiceImpl<MeetingMemberMapper, MeetingMember>
        implements MeetingMemberService {

    @Autowired
    private MeetingMemberMapper meetingMemberMapper;


    @Override
    public void updateStatus(String userId, String meetingId, Integer status) {

        MeetingMember meetingMember = new MeetingMember();
        meetingMember.setStatus(status);
        meetingMemberMapper.update(meetingMember, new QueryWrapper<MeetingMember>()
                .eq("meeting_id", meetingId)
                .eq("user_id", userId));
    }
}




