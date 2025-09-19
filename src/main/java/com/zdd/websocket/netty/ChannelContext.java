package com.zdd.websocket.netty;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zdd.component.RedisComponent;
import com.zdd.entry.domain.UserInfo;
import com.zdd.entry.dto.MeetingExitDto;
import com.zdd.entry.dto.MeetingMemberDto;
import com.zdd.entry.dto.MessageSendDto;
import com.zdd.entry.dto.UserTokenDTO;
import com.zdd.entry.eum.MeetingMemberStatusEnum;
import com.zdd.entry.eum.MeetingMemberTypeEnum;
import com.zdd.entry.eum.MessageTypeEnum;
import com.zdd.entry.mapper.UserInfoMapper;
import com.zdd.entry.service.UserInfoService;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Case;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ChannelContext {
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private RedisComponent redisComponent;

    public static final ConcurrentHashMap<String, Channel> USER_CHANNEL_MAP = new ConcurrentHashMap<>();

    public static final ConcurrentHashMap<String, ChannelGroup> MEETING_ROOM_MAP = new ConcurrentHashMap<>();

    /**
     * 添加用户通道信息
     *
     * @param userId  用户ID，用于标识用户
     * @param channel 用户的网络通道，用于通信
     *                <p>
     *                此方法用于将用户与其网络通道关联起来，并更新用户的登录信息
     *                它还处理了用户令牌和当前会议编号的逻辑，如果用户正在参加会议，则需要自动加入会议
     */
    public void addChannel(String userId, Channel channel) {
        try {
            // 获取通道ID并转换为字符串形式
            String channelId = channel.id().toString();
            AttributeKey<String> attributeKey = null;
            // 检查是否存在具有该通道ID的属性键
            if (!AttributeKey.exists(channelId)) {
                // 如果不存在，创建一个新的属性键
                attributeKey = AttributeKey.newInstance(channelId);
            } else {
                // 如果存在，获取已有的属性键
                attributeKey = AttributeKey.valueOf(channelId);
            }
            // 在通道的属性中设置用户ID
            channel.attr(attributeKey).set(userId);

            // 将用户ID和其通道的映射关系添加到全局用户通道映射中
            USER_CHANNEL_MAP.put(userId, channel);

            // 更新用户的最后一次登录时间
            UserInfo userInfo = new UserInfo();
            userInfo.setUserId(userId);
            userInfo.setLastLoginTime(System.currentTimeMillis());
            userInfoMapper.updateById(userInfo);
//            userInfoService.updateLastLoginTime(userId);

            // 从Redis中获取用户的令牌信息
            UserTokenDTO userTokenDTO = redisComponent.getUserTokenDTO(userId);
            // 检查用户是否正在参加会议
            if (userTokenDTO.getCurrentMeetingId() == null) {
                // 如果没有参加会议，直接返回
                return;
            }
            // 如果正在会议需要自动加入
            addChannelToMeetingRoom(userTokenDTO.getCurrentMeetingId(), userId);
        } catch (Exception e) {
            // 记录添加用户连接时发生的错误
            log.error("添加用户连接失败", e);
        }
    }

    /**
     * 将用户频道添加到会议室
     * 此方法用于将一个用户频道关联到特定的会议室，以便用户可以加入并参与会议
     * 如果用户频道或会议室不存在，则不会执行添加操作
     *
     * @param meetingId 会议室编号，用于标识特定的会议室
     * @param userId    用户ID，用于获取用户频道
     */
    public void addChannelToMeetingRoom(String meetingId, String userId) {
        // 根据用户ID获取用户频道
        Channel userChannel = USER_CHANNEL_MAP.get(userId);
        // 如果用户频道不存在，则直接返回，不执行后续操作
        if (userChannel == null) {
            return;
        }
        // 根据会议室编号获取会议室对应的频道组
        ChannelGroup group = MEETING_ROOM_MAP.get(meetingId);
        // 如果频道组不存在，则创建新的频道组，并与会议室编号关联
        if (group == null) {
            group = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
            MEETING_ROOM_MAP.put(String.valueOf(meetingId), group);
        }
        // 在频道组中查找用户频道，避免重复添加
        Channel channel = group.find(userChannel.id());
        // 如果频道组中不存在该用户频道，则将用户频道添加到频道组中
        if (channel == null) {
            group.add(userChannel);
        }
    }


    public void sendMsg(MessageSendDto messageSendDto) {
        switch (messageSendDto.getMessageSend2Type()) {
            case 0:
                send2User(messageSendDto);
                break;
            case 1:
                send2Group(messageSendDto);
                break;
            default:
                break;
        }
    }

    /**
     * 将消息发送到指定的会议分组
     * 此方法首先检查消息的ID是否存在，然后查找对应的会议分组，
     * 如果会议分组存在，则将消息转换为文本帧并发送给该分组中的所有客户端
     *
     * @param messageSendDto 包含消息信息的数据传输对象
     */
    private void send2Group(MessageSendDto messageSendDto) {
        // 检查消息ID是否存在，如果不存在则直接返回
        if (messageSendDto.getMeetingId() == null) {
            return;
        }

        String meetingId = messageSendDto.getMeetingId();
        // 从全局会议房间映射中获取指定会议ID对应的频道分组
        ChannelGroup channels = MEETING_ROOM_MAP.get(meetingId);

        // 检查频道分组是否存在，如果不存在则直接返回
        if (channels == null) {
            log.info("会议id:{} 对应的群组不存在", meetingId);

            return;
        }
        String jsonString = JSON.toJSONString(messageSendDto);
        log.info("发送群组消息：{}", jsonString);

        // 将消息对象转换为JSON字符串，然后封装成文本帧，发送给频道分组中的所有客户端
        channels.writeAndFlush(new TextWebSocketFrame(jsonString));


        processingTasks(messageSendDto, meetingId);


    }

    private void processingTasks(MessageSendDto messageSendDto, String meetingId) {

        switch (messageSendDto.getMessageType()) {
            case 3:
                log.info("开始处理任务:{}", messageSendDto.getMessageType());

                MeetingExitDto meetingExitDto = JSONObject.parseObject(messageSendDto.getMessageContent().toString(), MeetingExitDto.class);
                removeGroupOne(meetingExitDto.getExitUserId(), meetingId);

                List<MeetingMemberDto> collect = redisComponent.getMeetingList(meetingId).stream().filter(m -> MeetingMemberStatusEnum.NORMAL.equals(m.getStatus())).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(collect)) {
                    removeGroupAll(meetingId);
                }
                break;
            case 4:
                log.info("开始处理任务:{}", messageSendDto.getMessageType());
                redisComponent.getMeetingList(meetingId).forEach(m -> removeGroupOne(m.getUserId(), meetingId));
                removeGroupAll(meetingId);
                break;
            default:
                break;
        }

    }


    public void removeGroupOne(String userId, String meetingId) {
        Channel channel = ChannelContext.USER_CHANNEL_MAP.get(userId);
        if (channel == null) {
            return;
        }

        ChannelGroup channelGroup = ChannelContext.MEETING_ROOM_MAP.get(meetingId);
        if (channelGroup != null) {
            channelGroup.remove(channel);
        }
    }

    public void removeGroupAll(String meetingId) {
        MEETING_ROOM_MAP.remove(meetingId);
    }


    /**
     * 向用户发送消息
     * 该方法通过WebSocket通道向指定用户发送消息，主要执行以下操作：
     * 1. 检查接收用户ID是否为空，为空则不执行发送操作
     * 2. 从用户通道映射中获取接收用户的WebSocket通道
     * 3. 检查通道是否为空，为空则不执行发送操作
     * 4. 将消息对象序列化为JSON字符串，并通过WebSocket通道发送给用户
     *
     * @param messageSendDto 消息发送数据传输对象，包含接收用户ID和消息内容
     */
    private void send2User(MessageSendDto messageSendDto) {
        // 检查接收用户ID是否为空，为空则不执行发送操作
        if (messageSendDto.getReceiveUserId() == null) {
            return;
        }

        // 从用户通道映射中获取接收用户的WebSocket通道
        Channel channel = USER_CHANNEL_MAP.get(messageSendDto.getReceiveUserId());
        // 检查通道是否为空，为空则不执行发送操作
        if (channel == null) {
            log.info("用户：{} 不在线", messageSendDto.getReceiveUserId());

            return;
        }
        String jsonString = JSON.toJSONString(messageSendDto);
        log.info("发送用户消息：{}", jsonString);
        // 将消息对象序列化为JSON字符串，并通过WebSocket通道发送给用户
        channel.writeAndFlush(new TextWebSocketFrame(jsonString));
    }


    public void closeContext(String userId) {
        if (userId == null) {
            return;
        }
        Channel channel = USER_CHANNEL_MAP.get(userId);
        if (channel != null) {
            channel.close();
        }

        USER_CHANNEL_MAP.remove(userId);
    }


}
