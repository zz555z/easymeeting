package com.zdd.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zdd.aop.annotation.GlobalInterceptor;
import com.zdd.component.RedisComponent;
import com.zdd.config.TokenInterceptor;
import com.zdd.entry.constants.CommonConstant;
import com.zdd.entry.domain.MeetingInfo;
import com.zdd.entry.domain.MeetingMember;
import com.zdd.entry.dto.UserTokenDTO;
import com.zdd.entry.eum.*;
import com.zdd.entry.service.MeetingInfoService;
import com.zdd.entry.service.MeetingMemberService;
import com.zdd.entry.vo.ResponseVO;
import com.zdd.exception.BusinessException;
import com.zdd.utils.CommonUtils;
import com.zdd.utils.RandomUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@Validated
@RequestMapping("/meeting")
@Slf4j
public class MeetingController {
    @Autowired
    private MeetingInfoService meetingInfoService;
    @Autowired
    private RedisComponent redisComponent;

    @Autowired
    private MeetingMemberService meetingMemberService;


    /**
     * 加载会议信息
     * <p>
     * 该方法用于处理加载会议信息的请求它使用了@PostMapping注解来指定HTTP POST请求的处理，
     * 并通过@GlobalInterceptor注解来应用全局拦截器逻辑这个方法首先从TokenInterceptor中获取当前用户的令牌信息，
     * 然后调用meetingInfoService的getAllList方法来获取与该用户相关的所有会议信息列表，
     * 最后将这些信息封装在一个成功的ResponseVO对象中返回给客户端
     *
     * @return 包含会议信息列表的ResponseVO对象
     */
    @PostMapping("/loadMeeting")
    @GlobalInterceptor()
    public ResponseVO loadMeeting() {
        // 获取当前用户令牌信息
        UserTokenDTO userTokenDTO = TokenInterceptor.getUserTokenDTO();
        // 获取用户相关的所有会议信息列表
        Page<MeetingInfo> allList = meetingInfoService.getAllList(userTokenDTO);
        // 返回成功响应，包含会议信息列表
        return ResponseVO.success(allList);
    }

    /**
     * 快速创建会议接口
     * 此接口用于用户快速创建会议，根据不同的会议类型生成会议号，并保存会议信息
     *
     * @param meetingName  会议名称，不能为空且长度不超过50
     * @param joinType     加入会议的方式，不能为空
     * @param joinPassword 加入会议的密码，可为空
     * @return 返回会议ID
     */
    @PostMapping("/quickMeeting")
    @GlobalInterceptor()
    public ResponseVO quickMeeting(@NotNull Integer meetingNoType,
                                   @NotEmpty @Size(max = 50) String meetingName,
                                   @NotNull Integer joinType,
                                   String joinPassword) {
        // 获取用户令牌信息
        UserTokenDTO userTokenDTO = TokenInterceptor.getUserTokenDTO();
        // 检查用户是否已经在会议中
        if (userTokenDTO.getCurrentMeetingId() != null) {
            throw new BusinessException(ResponseCodeEnum.RESPONSE_CODE_908);
        }


        // 保存会议信息到数据库
        MeetingInfo meetingInfo =  meetingInfoService.quickMeeting(meetingNoType,meetingName,joinType,joinPassword,userTokenDTO);

        // 更新用户令牌信息，关联当前会议
        userTokenDTO.setCurrentMeetingId(meetingInfo.getMeetingId());
        userTokenDTO.setCurrentMeetingName(meetingInfo.getMeetingName());
        // 将更新后的用户令牌信息保存到Redis
        redisComponent.setUserTokenDTO(userTokenDTO);

        // 返回成功创建的会议ID
        return ResponseVO.success(meetingInfo.getMeetingId());
    }


    /**
     * 处理用户预加入会议的请求
     * 该方法用于验证用户加入会议前提供的信息是否有效，包括会议编号、昵称和加入密码
     *
     * @param meetingNo    会议编号，不能为空，用于识别特定的会议
     * @param nickName     用户昵称，不能为空且长度不超过50，用于在会议中标识用户
     * @param joinPassword 加入会议的密码，可选，根据会议设置可能需要提供
     * @return 返回一个包含会议ID的响应对象，表示用户可以加入的会议
     * <p>
     * 会校验会议相关信息，然后把会议id塞到redis中 然后从redis中获取会议信息来加入
     */
    @PostMapping("/preJoinMeeting")
    @GlobalInterceptor()
    public ResponseVO preJoinMeeting(@NotNull String meetingNo,
                                     @NotEmpty @Size(max = 50) String nickName,
                                     String joinPassword) {
        // 获取用户令牌信息，用于验证用户身份
        UserTokenDTO userTokenDTO = TokenInterceptor.getUserTokenDTO();

        // 移除会议编号中的空格，确保会议编号的一致性和有效性
        meetingNo = meetingNo.replaceAll(" ", "");

        // 设置用户当前加入会议时使用的昵称
        userTokenDTO.setCurrentMeetingName(nickName);

        // 调用服务层方法，处理用户预加入会议的请求，并获取会议ID
        String meetingId = meetingInfoService.preJoinMeeting(meetingNo, userTokenDTO, joinPassword);

        // 返回成功响应，包含会议ID
        return ResponseVO.success(meetingId);
    }


    /**
     * 用户加入会议的接口
     * 该方法允许用户通过POST请求加入会议，并可以选择是否开启视频
     *
     * @param videoOpen 一个布尔值，指示用户是否开启视频
     * @return 返回一个ResponseVO对象，表示用户成功加入会议
     */
    @PostMapping("/joinMeeting")
    @GlobalInterceptor()
    public ResponseVO joinMeeting(@NotNull Boolean videoOpen) {
        // 获取用户令牌信息，用于识别和验证用户身份
        UserTokenDTO userTokenDTO = TokenInterceptor.getUserTokenDTO();

        // 调用会议信息服务的加入会议方法，传入用户令牌和视频开启状态
        meetingInfoService.joinMeeting(userTokenDTO, videoOpen);

        // 返回成功响应，表示用户已成功加入会议
        return ResponseVO.success();
    }


    @PostMapping("/exitMeeting")
    @GlobalInterceptor()
    public ResponseVO exitMeeting() {
        // 获取用户令牌信息，用于识别和验证用户身份
        UserTokenDTO userTokenDTO = TokenInterceptor.getUserTokenDTO();

        meetingInfoService.exitMeeting(userTokenDTO, MeetingMemberStatusEnum.EXIT_MEETINGING.getStatus());

        return ResponseVO.success();
    }


    @PostMapping("/kickOutMeeting")
    @GlobalInterceptor()
    public ResponseVO kickOutMeeting(@NotEmpty String userid) {
        // 获取用户令牌信息，用于识别和验证用户身份
        UserTokenDTO userTokenDTO = TokenInterceptor.getUserTokenDTO();

        meetingInfoService.kickOutMeeting(userTokenDTO, MeetingMemberStatusEnum.KICK_OUT.getStatus(), userid);

        return ResponseVO.success();
    }


    @PostMapping("/blackMeeting")
    @GlobalInterceptor()
    public ResponseVO blackMeeting(@NotEmpty String userid) {
        // 获取用户令牌信息，用于识别和验证用户身份
        UserTokenDTO userTokenDTO = TokenInterceptor.getUserTokenDTO();

        meetingInfoService.kickOutMeeting(userTokenDTO, MeetingMemberStatusEnum.BLACKLIST.getStatus(), userid);

        return ResponseVO.success();
    }


    @PostMapping("/getCurrentMeeting")
    @GlobalInterceptor()
    public ResponseVO getCurrentMeeting() {
        // 获取用户令牌信息，用于识别和验证用户身份
        UserTokenDTO userTokenDTO = TokenInterceptor.getUserTokenDTO();
        if (userTokenDTO.getCurrentMeetingId() == null) {
            return ResponseVO.success();
        }
        MeetingInfo meetingInfo = meetingInfoService.getById(userTokenDTO.getCurrentMeetingId());
        if (MeetingStatusEnum.OVER.getStatus().equals(meetingInfo.getStatus())) {
            return ResponseVO.success();
        }
        return ResponseVO.success(meetingInfo);
    }

    @PostMapping("/finishMeeting")
    @GlobalInterceptor()
    public ResponseVO finishMeeting() {
        // 获取用户令牌信息，用于识别和验证用户身份
        UserTokenDTO userTokenDTO = TokenInterceptor.getUserTokenDTO();

        meetingInfoService.finishMeeting(userTokenDTO.getCurrentMeetingId(), userTokenDTO.getUserId());

        return ResponseVO.success();
    }


    @PostMapping("/delMeetingHistory")
    @GlobalInterceptor()
    public ResponseVO delMeetingHistory(@NotEmpty String meetingId) {
        // 获取用户令牌信息，用于识别和验证用户身份
        UserTokenDTO userTokenDTO = TokenInterceptor.getUserTokenDTO();

        meetingMemberService.updateStatus(userTokenDTO.getUserId(), meetingId, MeetingMemberStatusEnum.DEL_MEETINGING.getStatus());

        return ResponseVO.success();
    }


    @PostMapping("/loadMeetingMembers")
    @GlobalInterceptor()
    public ResponseVO loadMeetingMembers(@NotEmpty String meetingId) {
        // 获取用户令牌信息，用于识别和验证用户身份
        UserTokenDTO userTokenDTO = TokenInterceptor.getUserTokenDTO();
        List<MeetingMember> meetingMembers = meetingMemberService.list(new QueryWrapper<MeetingMember>().eq("meeting_id", meetingId));
        Optional<MeetingMember> first = meetingMembers.stream().filter(meetingMember -> meetingMember.getUserId().equals(userTokenDTO.getUserId())).findFirst();
        if (!first.isPresent()) {
            log.info("用户不属于当前会议");
            throw new BusinessException("该不属于当前会议");
        }
        return ResponseVO.success(meetingMembers);
    }


    @PostMapping("/reserveJoinMeeting")
    @GlobalInterceptor()
    public ResponseVO reserveJoinMeeting(@NotEmpty String meetingId,@NotEmpty String joinPassWord) {
        // 获取用户令牌信息，用于识别和验证用户身份
        UserTokenDTO userTokenDTO = TokenInterceptor.getUserTokenDTO();
        meetingInfoService.reserveJoinMeeting(meetingId, userTokenDTO, joinPassWord);
        return ResponseVO.success();
    }


    @PostMapping("/inviteMember")
    @GlobalInterceptor()
    public ResponseVO inviteMember(@NotEmpty String userIds) {
        // 获取用户令牌信息，用于识别和验证用户身份
        UserTokenDTO userTokenDTO = TokenInterceptor.getUserTokenDTO();
        meetingInfoService.inviteMember(userTokenDTO, userIds);
        return ResponseVO.success();
    }

    @PostMapping("/acceptInvite")
    @GlobalInterceptor()
    public ResponseVO acceptInvite(@NotEmpty String meetingId) {
        // 获取用户令牌信息，用于识别和验证用户身份
        UserTokenDTO userTokenDTO = TokenInterceptor.getUserTokenDTO();
        meetingInfoService.acceptInvite(userTokenDTO, meetingId);
        return ResponseVO.success();
    }


    @PostMapping("/sendOpenVideoChangeMessage")
    @GlobalInterceptor()
    public ResponseVO sendVideoChangeMessage(@NotNull  Boolean  flag) {
        // 获取用户令牌信息，用于识别和验证用户身份
        UserTokenDTO userTokenDTO = TokenInterceptor.getUserTokenDTO();
        meetingInfoService.updateMemberOpenVideo(userTokenDTO.getCurrentMeetingId(),userTokenDTO.getUserId(),flag);
        return ResponseVO.success();
    }
}
