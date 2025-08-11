package com.zdd.entry.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zdd.entry.domain.UserContact;
import com.zdd.entry.domain.UserContactApply;
import com.zdd.entry.domain.UserInfo;
import com.zdd.entry.dto.MessageSendDto;
import com.zdd.entry.eum.*;
import com.zdd.entry.mapper.UserContactMapper;
import com.zdd.entry.mapper.UserInfoMapper;
import com.zdd.entry.service.UserContactApplyService;
import com.zdd.entry.mapper.UserContactApplyMapper;
import com.zdd.exception.BusinessException;
import com.zdd.websocket.message.MessageHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Objects;

/**
 * @author zdd
 * @description 针对表【user_contact_apply(用户联系申请记录表)】的数据库操作Service实现
 * @createDate 2025-07-15 14:38:37
 */
@Service
@Slf4j
public class UserContactApplyServiceImpl extends ServiceImpl<UserContactApplyMapper, UserContactApply>
        implements UserContactApplyService {

    @Autowired
    private UserContactMapper userContactMapper;
    @Autowired
    private UserContactApplyMapper userContactApplyMapper;
    @Autowired
    private MessageHandler messageHandler;
    @Autowired
    private UserInfoMapper userInfoMapper;


    @Override
    @Transactional
    public Integer  saveUserContactApply(UserContactApply userContactApply) {
        UserContact contact = getUserContact(userContactApply.getReceiveUserId(), userContactApply.getApplyUserId());
        if (contact != null && Objects.equals(contact.getStatus(), UserContactStatusEnum.BLACKLIST.getStatus())) {
            log.info("对方用户已经把你拉黑 ");
            throw new BusinessException("对方用户已经把你拉黑");
        }
        if (contact != null && Objects.equals(contact.getStatus(), UserContactStatusEnum.FRIEND.getStatus())) {
            contact.setStatus(UserContactStatusEnum.FRIEND.getStatus());
            userContactMapper.updateById(contact);

        }
        UserContactApply userContactApplydb = getUserContactApply(userContactApply.getApplyUserId(), userContactApply.getReceiveUserId());

        if (userContactApplydb == null) {
            userContactApply.setStatus(UserContactApplyStatusEnum.INIT.getStatus());
            userContactApplyMapper.insert(userContactApply);
        } else {
            userContactApply.setStatus(UserContactApplyStatusEnum.INIT.getStatus());
            userContactApplyMapper.updateById(userContactApply);
        }

        MessageSendDto<Object> messageSendDto = new MessageSendDto<>();
        messageSendDto.setMessageSend2Type(MessageSendTypeEnum.USER.getCode());
        messageSendDto.setMessageType(MessageTypeEnum.USER_CONTACT_APPLY.getType());
        messageSendDto.setSendUserId(userContactApply.getApplyUserId());
        messageSendDto.setReceiveUserId(userContactApply.getReceiveUserId());

        messageHandler.sendMessage(messageSendDto);

        return userContactApplydb == null?userContactApply.getStatus():userContactApplydb.getStatus();
    }

    /**
     * 处理用户联系申请
     *
     * @param applyUserId 申请用户的ID
     * @param userId      当前操作用户的ID
     * @param name        当前操作用户的昵称
     * @param status      申请的状态
     *                    <p>
     *                    此方法用于处理用户之间的联系申请，根据申请的状态进行相应的处理
     *                    如果申请状态无效或为初始状态，则抛出异常
     *                    如果申请不存在，则抛出异常
     *                    如果申请状态为已通过，则更新双方的用户联系状态为好友
     *                    最后，更新申请状态并发送消息通知申请用户
     */
    @Override
    @Transactional
    public void dealWithApply(String applyUserId, String userId, String name, Integer status) {
        // 根据状态获取申请状态枚举
        UserContactApplyStatusEnum statusEnum = UserContactApplyStatusEnum.getByStatus(status);
        // 检查申请状态是否有效
        if (statusEnum == null || statusEnum == UserContactApplyStatusEnum.INIT) {
            log.info("申请状态错误 applyUserId {} status {}", applyUserId, status);
            throw new BusinessException(ResponseCodeEnum.RESPONSE_CODE_903);
        }

        // 获取用户联系申请对象
        UserContactApply userContactApply = getUserContactApply(applyUserId, userId);
        // 检查申请是否存在
        if (userContactApply == null) {
            log.info("申请不存在 applyUserId {}", applyUserId);
            throw new BusinessException(ResponseCodeEnum.RESPONSE_CODE_903);
        }

        // 如果申请状态为已通过，则更新双方的用户联系状态为好友
        if (UserContactApplyStatusEnum.PASS.getStatus().equals(status)) {
            saveUserContactOrUpdate(applyUserId, userId, UserContactStatusEnum.FRIEND.getStatus());
            saveUserContactOrUpdate(userId, applyUserId, UserContactStatusEnum.FRIEND.getStatus());
        }

        // 更新申请状态
        userContactApply.setStatus(status);
        userContactApplyMapper.updateById(userContactApply);

        // 创建消息发送对象并设置消息属性
        MessageSendDto<Object> messageSendDto = new MessageSendDto<>();
        messageSendDto.setMessageSend2Type(MessageSendTypeEnum.USER.getCode());
        messageSendDto.setMessageType(MessageTypeEnum.DEAL_CONTACT_APPLY.getType());
        messageSendDto.setSendUserId(userId);
        messageSendDto.setSendUserNickName(name);
        messageSendDto.setMessageContent(status);
        messageSendDto.setReceiveUserId(applyUserId);

        // 发送消息
        messageHandler.sendMessage(messageSendDto);
    }

    @Override
    public List<UserContactApply> loadContactApply(String userId) {
        QueryWrapper<UserContactApply> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("receive_user_id", userId)
//                .eq("status", UserContactApplyStatusEnum.INIT.getStatus())
                .orderByDesc("last_apply_time");
        List<UserContactApply> userContactApplies = userContactApplyMapper.selectList(queryWrapper);
        userContactApplies.forEach(userContactApply -> {
            UserInfo userInfo = userInfoMapper.selectById(userContactApply.getApplyUserId());
            userContactApply.setNickName(userInfo.getNickName());

        });
        return userContactApplies;
    }

    @Override
    public Long loadContactApplyDealWithCount(String userId) {
        QueryWrapper<UserContactApply> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("receive_user_id", userId)
                .eq("status", UserContactApplyStatusEnum.INIT.getStatus());
        return userContactApplyMapper.selectCount(queryWrapper);
    }


    private UserContact createUserContact(String useridA, String userIdB, Integer status) {
        UserContact userContact = new UserContact();
        userContact.setUserId(useridA);
        userContact.setContactId(userIdB);
        userContact.setStatus(status);
        return userContact;
    }


    /**
     * 获取指定用户A与用户B之间的联系人状态
     */
    public UserContact getUserContact(String userA, String userB) {
        UserContact contact = userContactMapper.selectOne(new QueryWrapper<UserContact>()
                .eq("user_id", userA)
                .eq("contact_id", userB));
        return contact;
    }

    /**
     * 更新或保存联系人状态
     *
     * @param userIdA
     * @param userIdB
     * @param status
     */
    public void saveUserContactOrUpdate(String userIdA, String userIdB, Integer status) {
        UserContact userContact = getUserContact(userIdA, userIdB);
        if (userContact != null) {
            userContact.setStatus(status);
            userContactMapper.updateById(userContact);
        } else {
            userContactMapper.insert(createUserContact(userIdA, userIdB, status));
        }

    }


    /**
     * 查询用户a和用户b之间有没有申请关系
     */
    public UserContactApply getUserContactApply(String userA, String userB) {
        return userContactApplyMapper.selectOne(new QueryWrapper<UserContactApply>()
                .eq("apply_user_id", userA)
                .eq("receive_user_id", userB));
    }

}




