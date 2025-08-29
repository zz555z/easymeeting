package com.zdd.entry.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zdd.entry.domain.MeetingInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zdd.entry.dto.UserTokenDTO;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
* @author zdd
* @description 针对表【meeting_info】的数据库操作Service
* @createDate 2025-06-30 16:43:00
*/
public interface MeetingInfoService extends IService<MeetingInfo> {


    Page<MeetingInfo> getAllList(UserTokenDTO userTokenDTO);

    void joinMeeting(UserTokenDTO userTokenDTO, Boolean videoOpen);

    String preJoinMeeting(String meetingNo, UserTokenDTO userTokenDTO, String joinPassword);

    void exitMeeting(UserTokenDTO userTokenDTO, Integer status);

    void kickOutMeeting(UserTokenDTO userTokenDTO, Integer status, @NotEmpty String userid);

    void forceOutMeeting(UserTokenDTO userTokenDTO, Integer status);

    void exitOutMeeting(UserTokenDTO userTokenDTO, Integer status);


    void finishMeeting(String currentMeetingId, String userId);

    List<MeetingInfo> loadMeetingReserve(String userId);

    void createMeetingReserve(String startTime, String meetingName, Integer duration, Integer joinType, String password, String userIds, UserTokenDTO userTokenDTO);

    MeetingInfo quickMeeting(Integer meetingNoType, String meetingName, Integer joinType, String joinPassword, UserTokenDTO userId);

    void delMeetingReserve(String meetingId, String userId);
    void delMeetingReserveByUser(String meetingId, String userId);

    List<MeetingInfo> loadTodayMeeting(UserTokenDTO userTokenDTO);

    void reserveJoinMeeting(String meetingId, UserTokenDTO userTokenDTO, String joinPassWord);

    void inviteMember(UserTokenDTO userTokenDTO, String userIds);

    void acceptInvite(UserTokenDTO userTokenDTO, String meetingId);

    void updateMemberOpenVideo(String currentMeetingId, String userId, Boolean flag);


    List<MeetingInfo> loadMeetingList(String meetingName, Page<MeetingInfo> userInfoIPage);

    void adminFinishMeeting(String meetingId);
}
