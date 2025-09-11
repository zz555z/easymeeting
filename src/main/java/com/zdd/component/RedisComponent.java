package com.zdd.component;


import com.zdd.entry.dto.SysSettingDto;
import com.zdd.utils.RedisUtils;
import com.zdd.entry.constants.CommonConstant;
import com.zdd.entry.dto.MeetingMemberDto;
import com.zdd.entry.dto.UserTokenDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class RedisComponent {

    @Autowired
    private RedisUtils redisUtils;


    public String setCheckCode(Object value) {
        String codeKey = UUID.randomUUID().toString();
        redisUtils.setex(CommonConstant.REDIS_KEY_CHECKCODE + codeKey, value, CommonConstant.REDIS_KEY_TIME_ONE_MIN * 10);
        return codeKey;
    }

    public String getCheckCode(String key) {
        return redisUtils.get(CommonConstant.REDIS_KEY_CHECKCODE + key).toString();
    }

    public void deleteCheckCode(String key) {
        redisUtils.delete(CommonConstant.REDIS_KEY_CHECKCODE + key);
    }

    public UserTokenDTO setUserTokenDTO(UserTokenDTO userTokenDTO) {
        if (userTokenDTO.getToken() == null) {
            String token = UUID.randomUUID().toString();
            userTokenDTO.setToken(token);
            userTokenDTO.setExpireAt(System.currentTimeMillis() + CommonConstant.REDIS_KEY_TIME_ONE_DAY * 7);
        }

        redisUtils.setex(CommonConstant.REDIS_KEY_USER_TOKEN + userTokenDTO.getToken(), userTokenDTO, CommonConstant.REDIS_KEY_TIME_ONE_DAY * 7);
        redisUtils.setex(CommonConstant.REDIS_KEY_USER_TOKEN + userTokenDTO.getUserId(), userTokenDTO, CommonConstant.REDIS_KEY_TIME_ONE_DAY * 7);
//        redisUtils.set(CommonConstant.REDIS_KEY_USER_TOKEN + userTokenDTO.getToken(), userTokenDTO);
//        redisUtils.set(CommonConstant.REDIS_KEY_USER_TOKEN + userTokenDTO.getUserId(), userTokenDTO);

        return userTokenDTO;
    }


    public UserTokenDTO getUserTokenDTO(String token) {
        return (UserTokenDTO) redisUtils.get(CommonConstant.REDIS_KEY_USER_TOKEN + token);
    }

    public void deleteUserTokenDTO(UserTokenDTO userTokenDTO) {
        redisUtils.delete(CommonConstant.REDIS_KEY_USER_TOKEN + userTokenDTO.getUserId());
        redisUtils.delete(CommonConstant.REDIS_KEY_USER_TOKEN + userTokenDTO.getToken());
    }

    public void addMeeting(String meetingId, MeetingMemberDto meetingMemberDto) {

        redisUtils.hset(CommonConstant.REDIS_KEY_MEETING_ROOM + meetingId, meetingMemberDto.getUserId(), meetingMemberDto);
    }

    public List<MeetingMemberDto> getMeetingList(String meetingId) {
        List<MeetingMemberDto> memberDtoList = redisUtils.hgetAll(CommonConstant.REDIS_KEY_MEETING_ROOM + meetingId);
        return memberDtoList.stream().sorted((o1, o2) -> o1.getJoinTime().compareTo(o2.getJoinTime())).collect(Collectors.toList());
    }


    public MeetingMemberDto getMeetingMember(String meetingId, String userId) {
        return (MeetingMemberDto) redisUtils.hget(CommonConstant.REDIS_KEY_MEETING_ROOM + meetingId, userId);
    }

    public void deleteMeetingMember(String meetingId) {
        redisUtils.hdel(CommonConstant.REDIS_KEY_MEETING_ROOM + meetingId);
    }


    public Boolean exitMeeting(String currenMeetingId, String userId, Integer status) {
        MeetingMemberDto meetingMember = getMeetingMember(currenMeetingId, userId);
        if (meetingMember == null) {
            return false;
        }
        meetingMember.setStatus(status);
        addMeeting(currenMeetingId, meetingMember);

        return true;

    }

    public void addInvite(String meetingId, String contactId) {
        redisUtils.setex(CommonConstant.REDIS_KEY_INVITE_KEY + contactId + meetingId, meetingId, CommonConstant.REDIS_KEY_TIME_ONE_MIN * 5);
    }

    public String getInvite(String meetingId, String contactId) {
        return (String) redisUtils.get(CommonConstant.REDIS_KEY_INVITE_KEY + contactId + meetingId);
    }

    public SysSettingDto getSysSetting() {
        return (SysSettingDto) redisUtils.get(CommonConstant.REDIS_KEY_SYSSETTING_KEY);
    }

    public void setSysSetting(SysSettingDto sysSettingDto) {
        redisUtils.setex(CommonConstant.REDIS_KEY_SYSSETTING_KEY, sysSettingDto, CommonConstant.REDIS_KEY_TIME_ONE_DAY);
    }
}
