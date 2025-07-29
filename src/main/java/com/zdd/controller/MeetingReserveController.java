package com.zdd.controller;

import com.zdd.aop.annotation.GlobalInterceptor;
import com.zdd.config.TokenInterceptor;
import com.zdd.entry.domain.MeetingInfo;
import com.zdd.entry.dto.UserTokenDTO;
import com.zdd.entry.service.MeetingInfoService;
import com.zdd.entry.vo.ResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@Validated
@RequestMapping("/meetingReserve")
@Slf4j
public class MeetingReserveController {

    @Autowired
    private MeetingInfoService meetingInfoService;


    @PostMapping("/loadMeetingReserve")
    @GlobalInterceptor()
    public ResponseVO loadMeetingReserve() {
        UserTokenDTO userTokenDTO = TokenInterceptor.getUserTokenDTO();
        List<MeetingInfo> meetingInfos = meetingInfoService.loadMeetingReserve(userTokenDTO.getUserId());
        return ResponseVO.success(meetingInfos);
    }


    @PostMapping("/createMeetingReserve")
    @GlobalInterceptor()
    public ResponseVO createMeetingReserve(@NotEmpty String startTime,
                                           @NotEmpty String meetingName,
                                           @NotNull Integer duration,
                                           @NotNull Integer joinType,
                                           String password,
                                           String userIds) {
        UserTokenDTO userTokenDTO = TokenInterceptor.getUserTokenDTO();
        meetingInfoService.createMeetingReserve(startTime,meetingName, duration, joinType, password, userIds, userTokenDTO);
        return ResponseVO.success(null);
    }

    @PostMapping("/delMeetingReserve")
    @GlobalInterceptor()
    public ResponseVO delMeetingReserve(@NotEmpty String meetingId) {
        UserTokenDTO userTokenDTO = TokenInterceptor.getUserTokenDTO();
        meetingInfoService.delMeetingReserve(meetingId, userTokenDTO.getUserId());
        return ResponseVO.success(null);
    }

    @PostMapping("/delMeetingReserveByUser")
    @GlobalInterceptor()
    public ResponseVO delMeetingReserveByUser(@NotEmpty String meetingId) {
        UserTokenDTO userTokenDTO = TokenInterceptor.getUserTokenDTO();
        meetingInfoService.delMeetingReserveByUser(meetingId, userTokenDTO.getUserId());
        return ResponseVO.success(null);
    }

    @PostMapping("/loadTodayMeeting")
    @GlobalInterceptor()
    public ResponseVO loadTodayMeeting() {
        UserTokenDTO userTokenDTO = TokenInterceptor.getUserTokenDTO();

        List<MeetingInfo> meetingInfos=meetingInfoService.loadTodayMeeting(userTokenDTO);

        return ResponseVO.success(meetingInfos);
    }

}
