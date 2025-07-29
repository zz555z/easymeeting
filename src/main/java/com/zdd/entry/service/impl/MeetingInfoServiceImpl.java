package com.zdd.entry.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zdd.component.RedisComponent;
import com.zdd.entry.domain.MeetingInfo;
import com.zdd.entry.domain.MeetingMember;
import com.zdd.entry.domain.MeetingReserveMember;
import com.zdd.entry.domain.UserContact;
import com.zdd.entry.dto.*;
import com.zdd.entry.eum.*;
import com.zdd.entry.mapper.MeetingMemberMapper;
import com.zdd.entry.mapper.UserContactMapper;
import com.zdd.entry.service.MeetingInfoService;
import com.zdd.entry.mapper.MeetingInfoMapper;
import com.zdd.entry.service.MeetingReserveMemberService;
import com.zdd.exception.BusinessException;
import com.zdd.utils.CommonUtils;
import com.zdd.websocket.message.MessageHandler;
import com.zdd.websocket.netty.ChannelContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zdd
 * @description 针对表【meeting_info】的数据库操作Service实现
 * @createDate 2025-06-30 16:43:00
 */
@Slf4j
@Service
public class MeetingInfoServiceImpl extends ServiceImpl<MeetingInfoMapper, MeetingInfo>
        implements MeetingInfoService {
    @Autowired
    private MeetingInfoMapper meetingInfoMapper;

    @Autowired
    private MeetingMemberMapper meetingMemberMapper;

    @Autowired
    private ChannelContext channelContext;

    @Autowired
    private RedisComponent redisComponent;

    @Autowired
    private MessageHandler messageHandler;

    @Autowired
    private MeetingReserveMemberService meetingReserveMemberService;

    @Autowired
    private UserContactMapper userContactMapper;


    @Override
    public Page<MeetingInfo> getAllList(UserTokenDTO userTokenDTO) {
        QueryWrapper<MeetingInfo> queryWrapper = new QueryWrapper<MeetingInfo>()
                .eq("create_user_id", userTokenDTO.getUserId())
                .eq("status", MeetingMemberStatusEnum.NORMAL.getStatus())
                .orderByDesc("create_time");
        Page<MeetingInfo> meetingInfoPage = meetingInfoMapper.selectPage(new Page<MeetingInfo>(1, 15), queryWrapper);

        meetingInfoPage.getRecords().forEach(meetingInfo -> {
            meetingInfo.setMemberCount(meetingMemberMapper.selectCount(new QueryWrapper<MeetingMember>().eq("meeting_id", meetingInfo.getMeetingId())));
        });

        return meetingInfoPage;

    }

    /**
     * 用户加入会议
     * <p>
     * 此方法允许用户加入一个已经存在的会议它首先检查用户是否关联到一个有效的会议ID，
     * 然后验证会议是否存在且未结束如果用户和会议都是有效且会议正在进行中，
     * 它会进行加入会议的必要操作包括检查用户是否是会议成员，如果不是则添加，
     * 然后执行加入会议的操作，并根据视频开启状态决定是否开启视频最后，它将用户连接到会议的WebSocket通道，
     * 并发送一条消息通知其他会议参与者
     *
     * @param userTokenDTO 包含用户信息和当前会议ID的DTO
     * @param videoOpen    指示用户加入会议时是否开启视频的布尔值
     * @throws BusinessException 如果会议ID无效或会议已结束，抛出此异常
     */
    @Override
    public void joinMeeting(UserTokenDTO userTokenDTO, Boolean videoOpen) {
        //检查用户是否关联到一个会议ID
        if (userTokenDTO.getCurrentMeetingId() == null) {
            throw new BusinessException(ResponseCodeEnum.RESPONSE_CODE_903);
        }
        //通过会议ID获取会议信息
        MeetingInfo meetingInfo = baseMapper.selectById(userTokenDTO.getCurrentMeetingId());
        //验证会议存在且未结束
        if (meetingInfo == null || meetingInfo.getStatus().equals(MeetingStatusEnum.OVER.getStatus())) {
            throw new BusinessException(ResponseCodeEnum.RESPONSE_CODE_903);
        }
        //校验用户
        checkMeetingMember(meetingInfo.getMeetingId(), userTokenDTO.getUserId());
        //加入成员
        addMeetingMember(userTokenDTO, meetingInfo);
        //加入会议
        addMeeting(userTokenDTO, meetingInfo, videoOpen);
        //连接ws
        channelContext.addChannelToMeetingRoom(meetingInfo.getMeetingId(), userTokenDTO.getUserId());

        //发送加入会议的消息
        sendMsg(userTokenDTO, meetingInfo);
    }

    /**
     * 预加入会议方法
     * 该方法用于在用户正式加入会议前进行一系列的检查和验证，确保用户可以加入指定的会议
     *
     * @param meetingNo    会议编号，用于识别特定的会议
     * @param userTokenDTO 用户令牌DTO，包含用户信息和当前会议ID等数据
     * @param joinPassword 加入会议的密码，如果会议设置为需要密码加入，则需要验证此密码
     * @return 返回会议ID，表示预加入会议成功
     * @throws BusinessException 如果会议不存在、已结束、用户已有正在进行的会议、或加入密码错误，将抛出此异常
     */
    @Override
    public String preJoinMeeting(String meetingNo, UserTokenDTO userTokenDTO, String joinPassword) {
        // 查询正在进行的会议信息，根据会议编号和会议状态排序
        QueryWrapper<MeetingInfo> queryWrapper = new QueryWrapper<MeetingInfo>().eq("meeting_no", meetingNo)
                .eq("status", MeetingStatusEnum.RUN.getStatus())
                .orderByDesc("create_time");
        List<MeetingInfo> meetingInfos = baseMapper.selectList(queryWrapper);

        // 检查会议是否存在，如果不存在则抛出异常
        if (CollectionUtils.isEmpty(meetingInfos)) {
            log.info("会议不存在。。。");
            throw new BusinessException("会议不存在");
        }
        MeetingInfo meetingInfo = meetingInfos.get(0);

        // 检查会议是否已经结束，如果已结束则抛出异常
        if (meetingInfo.getStatus().equals(MeetingStatusEnum.OVER.getStatus())) {
            log.info("会议已结束");
            throw new BusinessException("会议已结束");
        }

        // 检查用户是否已经有正在进行的会议，如果有则不允许加入新的会议
        if (userTokenDTO.getCurrentMeetingId() != null && !userTokenDTO.getCurrentMeetingId().equals(meetingInfo.getMeetingId())) {
            log.info("您有正在进行中的会议");
            throw new BusinessException("您有正在进行中的会议");
        }

        // 检查用户是否是会议成员，如果不是则不允许加入
        checkMeetingMember(meetingInfo.getMeetingId(), userTokenDTO.getUserId());

        // 如果会议设置为需要密码加入，验证用户提供的密码是否正确
        if (meetingInfo.getJoinType().equals(MeetingJoinTypeEnum.PASSWORD.getStatus()) && !joinPassword.equals(meetingInfo.getJoinPassword())) {
            log.info("密码错误");
            throw new BusinessException("密码错误");
        }

        // 更新用户令牌DTO中的当前会议ID，并保存到Redis中
        userTokenDTO.setCurrentMeetingId(meetingInfo.getMeetingId());
        redisComponent.setUserTokenDTO(userTokenDTO);

        // 返回会议ID，表示预加入会议成功
        return meetingInfo.getMeetingId();
    }


    /**
     * 用户退出会议
     *
     * @param userTokenDTO 用户令牌信息，包含用户ID和当前会议ID等信息
     * @param status       退出会议的状态码，表示退出会议的原因或方式
     */
    @Override
    public void exitMeeting(UserTokenDTO userTokenDTO, Integer status) {
        // 参数校验
        if (userTokenDTO == null || userTokenDTO.getCurrentMeetingId() == null) {
            return;
        }

        String currenMeetingId = userTokenDTO.getCurrentMeetingId();
        String userId = userTokenDTO.getUserId();

        // 清除当前token中的会议信息
        userTokenDTO.setCurrentMeetingName(null);
        userTokenDTO.setCurrentMeetingId(null);
        redisComponent.setUserTokenDTO(userTokenDTO);

        // 退出会议操作
        Boolean flag = redisComponent.exitMeeting(currenMeetingId, userId, status);
        if (Boolean.FALSE.equals(flag)) {
            log.info("用户退出会议失败：meetingId={}, userId={}", currenMeetingId, userId);
            return;
        }

        // 构建并发送退出消息
        MessageSendDto<String> messageSendDto = new MessageSendDto();
        messageSendDto.setMessageType(MessageTypeEnum.EXIT_MEETING_ROOM.getType());

        List<MeetingMemberDto> memberDtoList = redisComponent.getMeetingList(currenMeetingId);
        if (CollectionUtils.isEmpty(memberDtoList)) {
            return;
        }

        MeetingExitDto meetingExitDto = new MeetingExitDto();
        meetingExitDto.setExitUserId(userId);
        meetingExitDto.setExitStatus(status);
        meetingExitDto.setMeetingMemberDtoList(memberDtoList);
        messageSendDto.setMessageContent(JSONObject.toJSONString(meetingExitDto));
        messageSendDto.setMeetingId(currenMeetingId);
        messageSendDto.setMessageSend2Type(MessageSendTypeEnum.GROUP.getCode());
        messageHandler.sendMessage(messageSendDto);

        // 过滤正常成员
        List<MeetingMemberDto> normalMembers = memberDtoList.stream()
                .filter(m -> MeetingMemberStatusEnum.NORMAL.equals(m.getStatus()))
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(normalMembers)) {
            log.info("会议已无正常成员，准备结束会议：meetingId={}", currenMeetingId);

            MeetingInfo meetingInfo = meetingInfoMapper.selectById(currenMeetingId);
            if (meetingInfo.getEndTime().compareTo(new Date()) <= 0) {
                log.info("会议已超过预约结束时间：meetingId={} endtime={}", currenMeetingId, meetingInfo.getEndTime());
                finishMeeting(currenMeetingId, null);
            }
            if (meetingInfo.getDuration() == null) {
                log.info("会议不是预约会议，准备结束会议：meetingId={}", currenMeetingId);
                finishMeeting(currenMeetingId, null);
            }

            return;
        }

        updateMemberStatus(status, currenMeetingId, userId);


    }

    private void updateMemberStatus(Integer status, String currenMeetingId, String userId) {
        // 判断是否是踢出或黑名单状态，更新数据库
        if (MeetingMemberStatusEnum.isKickedOrBlacklisted(status)) {
            MeetingMember meetingMember = new MeetingMember();
            meetingMember.setStatus(status);
            meetingMemberMapper.update(meetingMember,
                    new UpdateWrapper<MeetingMember>()
                            .eq("meeting_id", currenMeetingId)
                            .eq("user_id", userId));
        }
    }

    @Override
    public void kickOutMeeting(UserTokenDTO userTokenDTO, Integer status, String userid) {
        MeetingInfo meetingInfo = this.meetingInfoMapper.selectById(userTokenDTO.getCurrentMeetingId());
        if (!meetingInfo.getCreateUserId().equals(userTokenDTO.getUserId())) {
            throw new BusinessException(ResponseCodeEnum.RESPONSE_CODE_903);
        }
        UserTokenDTO user = redisComponent.getUserTokenDTO(userid);
        exitMeeting(user, status);

    }

    @Override
    public void forceOutMeeting(UserTokenDTO userTokenDTO, Integer status) {

    }

    @Override
    public void exitOutMeeting(UserTokenDTO userTokenDTO, Integer status) {

    }

    @Override
    public void finishMeeting(String currentMeetingId, String userId) {
        MeetingInfo meetingInfo = baseMapper.selectById(currentMeetingId);
        if (meetingInfo == null) {
            log.info("当前会议不存在");
            throw new BusinessException("当前会议不存在");
        }

        if (meetingInfo.getStatus().equals(MeetingStatusEnum.OVER.getStatus())) {
            log.info("当前会议已结束");
            throw new BusinessException("当前会议已结束");
        }

        if (StringUtils.isNotEmpty(userId) && meetingInfo.getCreateUserId().equals(userId)) {
            log.info("该会议不是自己的会议，无权限解散");
            throw new BusinessException("该会议不是自己的会议，无权限解散");
        }
        MeetingInfo meetingInfodb = new MeetingInfo();
        meetingInfodb.setMeetingId(currentMeetingId);
        meetingInfodb.setStatus(MeetingStatusEnum.OVER.getStatus());
        meetingInfodb.setEndTime(new Date());
        this.meetingInfoMapper.updateById(meetingInfodb);

        MessageSendDto messageSendDto = new MessageSendDto();
        messageSendDto.setMessageSend2Type(MessageSendTypeEnum.GROUP.getCode());
        messageSendDto.setMessageType(MessageTypeEnum.FINIS_MEETING.getType());
        messageSendDto.setMeetingId(currentMeetingId);
        messageHandler.sendMessage(messageSendDto);

        MeetingMember meetingMemberdb = new MeetingMember();
        meetingMemberdb.setStatus(MeetingStatusEnum.OVER.getStatus());
        QueryWrapper<MeetingMember> queryWrapper = new QueryWrapper<MeetingMember>().eq("meeting_id", currentMeetingId);
        meetingMemberMapper.update(meetingMemberdb, queryWrapper);


        List<MeetingMemberDto> meetingList = redisComponent.getMeetingList(currentMeetingId);
        meetingList.forEach(m -> {
            UserTokenDTO userTokenDTO = redisComponent.getUserTokenDTO(m.getUserId());
            userTokenDTO.setCurrentMeetingId(null);
            redisComponent.setUserTokenDTO(userTokenDTO);
        });
        redisComponent.deleteMeetingMember(currentMeetingId);

    }

    @Override
    public List<MeetingInfo> loadMeetingReserve(String userId) {
        List<MeetingInfo> meetingInfos = new ArrayList<>();
        List<MeetingReserveMember> meetingReserveMembers = meetingReserveMemberService.list(new Page<>(1, 20)
                , new QueryWrapper<MeetingReserveMember>().eq("invite_user_id", userId).orderByDesc("start_time"));
        meetingReserveMembers.forEach(m -> {
            meetingInfos.add(baseMapper.selectById(m.getMeetingId()));
        });

        return meetingInfos;
    }

    @Override
    public void createMeetingReserve(String startTime, String meetingName, Integer duration, Integer joinType, String password, String userIds, UserTokenDTO userTokenDTO) {
        // 初始化会议信息对象
        MeetingInfo meetingInfo = new MeetingInfo();
        meetingInfo.setMeetingName(meetingName);
        meetingInfo.setJoinType(joinType);
        meetingInfo.setJoinPassword(password);
        // 根据会议类型生成会议号
        meetingInfo.setMeetingId(CommonUtils.getMeetingId());
        meetingInfo.setMeetingNo(CommonUtils.getMeetingNo());
        meetingInfo.setStatus(MeetingStatusEnum.NO_START.getStatus());
        meetingInfo.setCreateTime(new Date());
        meetingInfo.setCreateUserId(userTokenDTO.getUserId());
        meetingInfo.setDuration(duration);
        meetingInfo.setStartTime(CommonUtils.stringToDate(startTime));
        meetingInfo.setEndTime(CommonUtils.addMinutes(meetingInfo.getStartTime(), duration));
        baseMapper.insert(meetingInfo);


        if (StringUtils.isEmpty(userIds)) {
            log.info("没有需要邀请的用户");
            return;
        }

        List<MeetingReserveMember> meetingReserveMembers = new ArrayList<>();

        String[] users = userIds.split(",");
        for (String userid : users) {
            if (userid.equals(userTokenDTO.getUserId())) {
                log.info("不能邀请自己");
                continue;
            }
            MeetingReserveMember meetingReserveMember = new MeetingReserveMember();
            meetingReserveMember.setMeetingId(meetingInfo.getMeetingId());
            meetingReserveMember.setInviteUserId(userid);
            meetingReserveMembers.add(meetingReserveMember);
        }

        //自己也需要添加
        MeetingReserveMember meetingReserveMember = new MeetingReserveMember();
        meetingReserveMember.setMeetingId(meetingInfo.getMeetingId());
        meetingReserveMember.setInviteUserId(userTokenDTO.getUserId());
        meetingReserveMembers.add(meetingReserveMember);

        meetingReserveMemberService.insertBatch(meetingReserveMembers);


    }

    @Override
    public MeetingInfo quickMeeting(Integer meetingType, String meetingName, Integer joinType, String joinPassword, String userId) {
        // 初始化会议信息对象
        MeetingInfo meetingInfo = new MeetingInfo();
        meetingInfo.setMeetingName(meetingName);
        meetingInfo.setJoinType(joinType);
        meetingInfo.setJoinPassword(joinPassword);
        // 根据会议类型生成会议号
        meetingInfo.setMeetingNo(CommonUtils.getMeetingNo());
        meetingInfo.setStatus(MeetingStatusEnum.RUN.getStatus());
        meetingInfo.setCreateTime(new Date());
        meetingInfo.setCreateUserId(userId);
        meetingInfo.setMeetingId(CommonUtils.getMeetingId());
        baseMapper.insert(meetingInfo);
        return meetingInfo;

    }

    /**
     * 删除会议预约信息
     * 此方法首先尝试删除指定会议ID和创建者ID的会议信息如果会议信息删除成功，
     * 则进一步删除与该会议相关的所有预约成员信息
     *
     * @param meetingId 会议ID，用于标识特定的会议
     * @param userId    用户ID，表示会议的创建者的标识
     */
    @Override
    @Transactional
    public void delMeetingReserve(String meetingId, String userId) {
        // 尝试删除会议信息，只有当会议信息存在且删除成功时，才进一步删除预约成员信息
        int result = meetingInfoMapper.delete(new QueryWrapper<MeetingInfo>().eq("meeting_id", meetingId).eq("create_user_id", userId));
        // 如果会议信息删除成功，则删除与该会议相关的所有预约成员信息
        if (result > 0) {
            log.info("删除预约会议成功 meetingId:{} 开始删除成员信息", meetingId);
            meetingReserveMemberService.remove(new QueryWrapper<MeetingReserveMember>().eq("meeting_id", meetingId));
        }
    }


    @Override
    public void delMeetingReserveByUser(String meetingId, String userId) {
        MeetingInfo meetingInfo = meetingInfoMapper.selectById(meetingId);
        if (meetingInfo == null) {
            log.info("会议不存在 meetingId:{}", meetingId);
            throw new BusinessException("会议不存在");
        }

        if (!meetingInfo.getCreateUserId().equals(userId)) {
            meetingReserveMemberService.remove(new QueryWrapper<MeetingReserveMember>().eq("meeting_id", meetingId).eq("invite_user_id", userId));
        } else {
            delMeetingReserve(meetingId, userId);
        }
    }

    @Override
    public List<MeetingInfo> loadTodayMeeting(UserTokenDTO userTokenDTO) {
        Date date = new Date();
        String userId = userTokenDTO.getUserId();
        QueryWrapper<MeetingInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.ge("start_time", CommonUtils.getStartOfDay(date));
        queryWrapper.lt("end_time", CommonUtils.getEndOfDay(date));
        queryWrapper.eq("status", MeetingStatusEnum.NO_START.getStatus());
        queryWrapper.orderByAsc("start_time");
        List<MeetingInfo> meetingInfos = meetingInfoMapper.selectList(queryWrapper);

        return meetingInfos.stream().filter(m -> {
            return !ObjectUtils.isEmpty(meetingReserveMemberService.getMeetingReserveMember(m.getMeetingId(), userId));
        }).collect(Collectors.toList());
    }

    /**
     * 预约会议加入方法
     * 当前方法用于处理用户加入一个已经预约的会议的操作
     * 它首先检查用户是否已经在一个会议中，然后验证会议的存在性，
     * 用户是否被邀请参加会议，以及入会密码是否正确如果所有检查都通过，
     * 会议信息将被更新，用户将被加入会议
     *
     * @param meetingId    会议ID
     * @param userTokenDTO 用户令牌DTO，包含用户信息和当前会议ID
     * @param joinPassWord 加入会议的密码
     */
    @Override
    public void reserveJoinMeeting(String meetingId, UserTokenDTO userTokenDTO, String joinPassWord) {
        // 获取用户ID
        String userId = userTokenDTO.getUserId();
        // 检查用户是否已经在一个会议中
        if (StringUtils.isNotEmpty(userTokenDTO.getCurrentMeetingId())) {
            log.info("当前有正在进行的会议");
            throw new BusinessException("当前有正在进行的会议");
        }
        // 检查用户是否是会议成员
        checkMeetingMember(meetingId, userId);
        // 根据会议ID获取会议信息
        MeetingInfo meetingInfo = meetingInfoMapper.selectById(meetingId);
        // 检查会议是否存在
        if (ObjectUtils.isEmpty(meetingInfo)) {
            log.info("会议不存在");
            throw new BusinessException("会议不存在");
        }
        // 检查用户是否被邀请参加预约会议
        MeetingReserveMember meetingReserveMember = meetingReserveMemberService.getMeetingReserveMember(meetingId, userId);
        if (ObjectUtils.isEmpty(meetingReserveMember)) {
            log.info("当前用户没有被邀请");
            throw new BusinessException("当前用户没有被邀请");
        }
        // 如果会议加入类型是密码加入，检查入会密码是否正确
        if (MeetingJoinTypeEnum.PASSWORD.getStatus().equals(meetingInfo.getJoinType()) && !meetingInfo.getJoinPassword().equals(joinPassWord)) {
            log.info("入会密码错误");
            throw new BusinessException("入会密码错误");
        }

        // 更新会议信息状态为进行中
        MeetingInfo meetingInfodb = new MeetingInfo();
        meetingInfodb.setMeetingId(meetingId);
        meetingInfodb.setCreateTime(new Date());
        meetingInfodb.setStatus(MeetingStatusEnum.RUN.getStatus());
        meetingInfoMapper.updateById(meetingInfodb);

        // 更新用户令牌DTO中的当前会议信息
        userTokenDTO.setCurrentMeetingId(meetingId);
        userTokenDTO.setCurrentMeetingName(meetingInfo.getMeetingName());
        redisComponent.setUserTokenDTO(userTokenDTO);
    }

    @Override
    public void inviteMember(UserTokenDTO userTokenDTO, String userIds) {
        List<UserContact> userContactList = userContactMapper.selectList(new QueryWrapper<UserContact>().eq("user_id", userTokenDTO.getUserId()
        ).eq("status", UserContactStatusEnum.FRIEND.getStatus()));

        List<String> contactIds = Arrays.stream(userIds.split(","))
                .map(String::trim)
                .collect(Collectors.toList());

        List<String> ids = userContactList.stream().map(UserContact::getContactId).collect(Collectors.toList());
        if (!ids.containsAll(contactIds)) {
            log.info("部分用户没有添加该用户为好友");
            throw new BusinessException(ResponseCodeEnum.RESPONSE_CODE_903);
        }


        MeetingInfo meetingInfo = meetingInfoMapper.selectById(userTokenDTO.getCurrentMeetingId());

        String meetingId = meetingInfo.getMeetingId();
        for (String contactId : contactIds) {
            MeetingMemberDto meetingMember = redisComponent.getMeetingMember(meetingId, contactId);
            if (meetingMember != null && meetingMember.getStatus().equals(MeetingMemberStatusEnum.NORMAL.getStatus())) {
                continue;
            }

            redisComponent.addInvite(meetingId, contactId);

            MessageSendDto<Object> messageSendDto = new MessageSendDto<>();
            messageSendDto.setMessageSend2Type(MessageSendTypeEnum.USER.getCode());
            messageSendDto.setMessageType(MessageTypeEnum.INVITE_MEMBER_MEETING.getType());
            messageSendDto.setSendUserId(userTokenDTO.getUserId());
            messageSendDto.setReceiveUserId(contactId);


            MeetingInviteDto meetingInviteDto = new MeetingInviteDto();
            meetingInviteDto.setMeetingId(meetingId);
            meetingInviteDto.setMeetingName(meetingInfo.getMeetingName());
            meetingInviteDto.setInviteUserName(userTokenDTO.getNickName());

            messageSendDto.setMessageContent(meetingInviteDto);

            messageHandler.sendMessage(messageSendDto);

        }


    }

    @Override
    public void acceptInvite(UserTokenDTO userTokenDTO, String meetingId) {
        String invite = redisComponent.getInvite(meetingId, userTokenDTO.getUserId());
        if (StringUtils.isEmpty(invite)) {
            log.info("邀请信息已过期");
            throw new BusinessException(ResponseCodeEnum.RESPONSE_CODE_903);
        }
        userTokenDTO.setCurrentMeetingId(meetingId);
        userTokenDTO.setCurrentMeetingName(userTokenDTO.getNickName());
        redisComponent.setUserTokenDTO(userTokenDTO);

    }

    @Override
    public void updateMemberOpenVideo(String currentMeetingId, String userId, Boolean flag) {
        MeetingMemberDto meetingMember = redisComponent.getMeetingMember(currentMeetingId, userId);
        meetingMember.setVideoOpen(flag);
        redisComponent.addMeeting(currentMeetingId, meetingMember);

        MessageSendDto<Object> messageSendDto = new MessageSendDto<>();
        messageSendDto.setMessageType(MessageTypeEnum.MEETING_USER_VIDEO_CHANGE.getType());
        messageSendDto.setMessageContent(flag);
        messageSendDto.setSendUserId(userId);
        messageSendDto.setMessageSend2Type(MessageSendTypeEnum.GROUP.getCode());
        messageSendDto.setMeetingId(currentMeetingId);
        messageHandler.sendMessage(messageSendDto);
    }

    @Override
    public List<MeetingInfo> loadMeetingList(String meetingName, Page<MeetingInfo> userInfoIPage) {
        QueryWrapper<MeetingInfo> meetingInfoQueryWrapper = new QueryWrapper<>();
        if (meetingName != null) {
            meetingInfoQueryWrapper.like("meeting_name", meetingName);
        }
        return baseMapper.selectList(userInfoIPage, meetingInfoQueryWrapper);
    }

    @Override
    public void adminFinishMeeting(String meetingId) {

    }

    private void sendMsg(UserTokenDTO userTokenDTO, MeetingInfo meetingInfo) {
        MeetingJoinDto meetingJoinDto = new MeetingJoinDto();
        meetingJoinDto.setMeetingMemberDto(redisComponent.getMeetingMember(meetingInfo.getMeetingId(), userTokenDTO.getUserId()));
        meetingJoinDto.setMeetingMemberDtoList(redisComponent.getMeetingList(meetingInfo.getMeetingId()));

        MessageSendDto<MeetingJoinDto> messageSendDto = new MessageSendDto<>();
        messageSendDto.setMessageType(MessageTypeEnum.ADD_MEETING_ROOM.getType());
        messageSendDto.setMessageContent(meetingJoinDto);
        messageSendDto.setMeetingId(meetingInfo.getMeetingId());
        messageSendDto.setMessageSend2Type(MessageSendTypeEnum.GROUP.getCode());

        messageHandler.sendMessage(messageSendDto);
    }

    private void checkMeetingMember(String meetingId, String userId) {
        MeetingMemberDto meetingMember = redisComponent.getMeetingMember(meetingId, userId);
        if (meetingMember != null && meetingMember.getStatus().equals(MeetingMemberStatusEnum.BLACKLIST.getStatus())) {
            throw new BusinessException("已被拉黑无法加入会议");
        }
    }


    private void addMeetingMember(UserTokenDTO userTokenDTO, MeetingInfo meetingInfo) {
        MeetingMember meetingMember = new MeetingMember();
        meetingMember.setMeetingId(meetingInfo.getMeetingId());
        meetingMember.setUserId(userTokenDTO.getUserId());
        meetingMember.setNickName(userTokenDTO.getNickName());
        meetingMember.setLastJoinTime(new Date());
        meetingMember.setStatus(MeetingMemberStatusEnum.NORMAL.getStatus());
        meetingMember.setMemberType(userTokenDTO.getUserId().equals(meetingInfo.getCreateUserId()) ? MeetingMemberTypeEnum.COMPERE.getStatus() : MeetingMemberTypeEnum.NORMAL.getStatus());
        meetingMember.setMeetingStatus(MeetingStatusEnum.RUN.getStatus());
        meetingMemberMapper.insert(meetingMember);

    }

    private void addMeeting(UserTokenDTO userTokenDTO, MeetingInfo meetingInfo, Boolean videoOpen) {
        MeetingMemberDto meetingMemberDto = new MeetingMemberDto();
        meetingMemberDto.setUserId(userTokenDTO.getUserId());
        meetingMemberDto.setNickName(userTokenDTO.getNickName());
        meetingMemberDto.setJoinTime(System.currentTimeMillis());
        meetingMemberDto.setStatus(MeetingMemberStatusEnum.NORMAL.getStatus());
        meetingMemberDto.setMemberType(userTokenDTO.getUserId().equals(meetingInfo.getCreateUserId()) ? MeetingMemberTypeEnum.COMPERE.getStatus() : MeetingMemberTypeEnum.NORMAL.getStatus());
        meetingMemberDto.setVideoOpen(videoOpen);
        meetingMemberDto.setSex(userTokenDTO.getSex());
        redisComponent.addMeeting(meetingInfo.getMeetingId(), meetingMemberDto);

    }


}




