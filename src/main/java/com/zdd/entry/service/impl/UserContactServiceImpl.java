package com.zdd.entry.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zdd.entry.domain.UserContact;
import com.zdd.entry.domain.UserContactApply;
import com.zdd.entry.domain.UserInfo;
import com.zdd.entry.eum.ResponseCodeEnum;
import com.zdd.entry.eum.UserContactApplyStatusEnum;
import com.zdd.entry.eum.UserContactStatusEnum;
import com.zdd.entry.mapper.UserContactApplyMapper;
import com.zdd.entry.mapper.UserInfoMapper;
import com.zdd.entry.service.UserContactService;
import com.zdd.entry.mapper.UserContactMapper;
import com.zdd.entry.vo.UserInfoVO4Search;
import com.zdd.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author zdd
 * @description 针对表【user_contact(用户联系人表)】的数据库操作Service实现
 * @createDate 2025-07-15 14:38:37
 */
@Slf4j
@Service
public class UserContactServiceImpl extends ServiceImpl<UserContactMapper, UserContact>
        implements UserContactService {
    @Autowired
    private UserContactMapper userContactMapper;
    @Autowired
    private UserContactApplyMapper userContactApplyMapper;
    @Autowired
    private UserInfoMapper userInfoMapper;


    @Override
    public UserInfoVO4Search searchContact(String contactId, String userId) {

        // 根据联系人ID查询用户信息
        UserInfo userInfo = userInfoMapper.selectById(contactId);
        // 如果用户信息为空，记录日志并抛出异常
        if (userInfo == null) {
            log.info("用户不存在 contactId:{}", contactId);
            throw new BusinessException("用户不存在");
        }

        // 初始化UserInfoVO4Search对象，用于返回搜索结果
        UserInfoVO4Search userInfoVO4Search = new UserInfoVO4Search();
        userInfoVO4Search.setUserId(contactId);
        userInfoVO4Search.setNickName(userInfo.getNickName());

        if (contactId.equals(userId)) {
            userInfoVO4Search.setStatus(-UserContactApplyStatusEnum.PASS.getStatus());
        }

        // 获取双方联系人关系状态
        UserContact myUserContact = getContactStatus(userId, contactId);
        UserContact youUserContact = getContactStatus(contactId, userId);

        // 判断双方是否为好友关系
        if ((myUserContact != null && Objects.equals(myUserContact.getStatus(), UserContactStatusEnum.FRIEND.getStatus())) ||
                (youUserContact != null && Objects.equals(youUserContact.getStatus(), UserContactStatusEnum.FRIEND.getStatus()))) {
            userInfoVO4Search.setStatus(UserContactApplyStatusEnum.PASS.getStatus());
            return userInfoVO4Search;
        }

        // 判断双方是否有将对方加入黑名单
        if ((myUserContact != null && Objects.equals(myUserContact.getStatus(), UserContactStatusEnum.BLACKLIST.getStatus())) ||
                (youUserContact != null && Objects.equals(youUserContact.getStatus(), UserContactStatusEnum.BLACKLIST.getStatus()))) {
            userInfoVO4Search.setStatus(UserContactApplyStatusEnum.BLACKLIST.getStatus());
            return userInfoVO4Search;
        }

        // 查询是否有待处理的联系人申请（双向）
        UserContactApply apply = userContactApplyMapper.selectOne(new QueryWrapper<UserContactApply>()
                .eq("apply_user_id", userId)
                .eq("receive_user_id", contactId));
        if (apply != null && Objects.equals(apply.getStatus(), UserContactApplyStatusEnum.INIT.getStatus())) {
            userInfoVO4Search.setStatus(UserContactApplyStatusEnum.INIT.getStatus());
            return userInfoVO4Search;
        }

        // 如果不符合上述任何条件，返回null
        return userInfoVO4Search;
    }

    @Override
    public List<UserContact> loadContactUser(String userId) {
        QueryWrapper<UserContact> userContactQueryWrapper = new QueryWrapper<>();
        userContactQueryWrapper.eq("user_id", userId)
                .eq("status", UserContactStatusEnum.FRIEND.getStatus());

        List<UserContact> userContactList = userContactMapper.selectList(userContactQueryWrapper);

        userContactList.forEach(userContactApply -> {
            UserInfo userInfo = userInfoMapper.selectById(userContactApply.getContactId());
            userContactApply.setNickName(userInfo.getNickName());
            userContactApply.setLastLoginTime(userInfo.getLastLoginTime());
            userContactApply.setLastOffTime(userInfo.getLastOffTime());
            userContactApply.setOnlinetype(userInfo.getOnlineType());
        });
        return userContactList;
    }

    @Override
    public void delContact(String userId, String contactId, Integer status) {
        if (!status.equals(UserContactStatusEnum.BLACKLIST.getStatus()) || !status.equals(UserContactStatusEnum.DEL.getStatus())) {
            log.info("删除好友状态错误 status:{}", status);
            throw new BusinessException(ResponseCodeEnum.RESPONSE_CODE_903);
        }
        UserContact userContact = new UserContact();
        userContact.setStatus(status);
        QueryWrapper<UserContact> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId).eq("contact_id", contactId);
        userContactMapper.update(userContact, queryWrapper);


    }

    /**
     * 获取指定用户A与用户B之间的联系人状态
     */
    public UserContact getContactStatus(String userA, String userB) {
        UserContact contact = userContactMapper.selectOne(new QueryWrapper<UserContact>()
                .eq("user_id", userA)
                .eq("contact_id", userB));
        return contact;
    }


}




