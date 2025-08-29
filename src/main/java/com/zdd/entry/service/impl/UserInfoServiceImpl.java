package com.zdd.entry.service.impl;

import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zdd.component.RedisComponent;
import com.zdd.config.AppConfig;
import com.zdd.entry.constants.CommonConstant;
import com.zdd.entry.domain.UserInfo;
import com.zdd.entry.dto.MessageSendDto;
import com.zdd.entry.dto.UserTokenDTO;
import com.zdd.entry.eum.MessageSendTypeEnum;
import com.zdd.entry.eum.MessageTypeEnum;
import com.zdd.entry.eum.UserSexEnum;
import com.zdd.entry.eum.UserStatusEnum;
import com.zdd.entry.service.UserInfoService;
import com.zdd.entry.mapper.UserInfoMapper;
import com.zdd.exception.BusinessException;
import com.zdd.utils.*;
import com.zdd.websocket.message.MessageHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * @author zdd
 * @description 针对表【user_info】的数据库操作Service实现
 * @createDate 2025-06-26 15:20:27
 */
@Slf4j
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo>
        implements UserInfoService {

    @Autowired
    private RedisComponent redisComponent;

    @Autowired
    private AppConfig appConfig;
    @Autowired
    private FFmpegUtils fFmpegUtils;

    @Autowired
    private MessageHandler messageHandler;

    /**
     * 注册新用户
     *
     * @param email    用户的邮箱地址，作为唯一标识
     * @param password 用户的密码，将被加密存储
     *                 <p>
     *                 此方法首先检查数据库中是否已存在相同邮箱的用户如果存在，则抛出业务异常，表明用户已注册
     *                 如果用户不存在，将创建一个新的UserInfo对象，设置用户ID、邮箱、密码和昵称，
     *                 并将其保存到数据库中
     *                 <p>
     *                 注意：此方法中的业务异常表明尝试注册一个已存在的用户邮箱
     *                 用户ID和昵称是随机生成的字符串，以确保唯一性
     * @param nickName
     */
    @Override
    public void register(String email, String password, String nickName) {
        // 检查数据库中是否已存在相同邮箱的用户
        UserInfo dbUserInfo = baseMapper.selectOne(new QueryWrapper<UserInfo>().eq("email", email));
        if (dbUserInfo != null) {
            // 如果用户已存在，抛出业务异常，表明用户已注册
            throw new BusinessException("邮箱已经存在");
        }

        // 创建一个新的UserInfo对象
        UserInfo userInfo = new UserInfo();
        // 设置用户ID为随机生成的数字字符串
        userInfo.setUserId("U" + RandomUtils.generateRandomNumber(CommonConstant.RODMIX_NUMBER));
        // 设置用户邮箱
        userInfo.setEmail(email);
        // 设置用户密码为加密后的字符串
        userInfo.setPassword(DigestUtil.md5Hex(password + CommonConstant.MD5_SALTING));
        // 设置用户昵称为随机生成的字符串
        userInfo.setNickName(StringUtils.isEmpty(nickName) ? RandomUtils.generateRandomString(CommonConstant.RODMIX_NUMBER) : nickName);
        userInfo.setStatus(UserStatusEnum.NORMAL.getCode());
        userInfo.setSex(UserSexEnum.UNKNOWN.getCode());
        userInfo.setMeetingNo(RandomUtils.generateRandomString(CommonConstant.RODMIX_NUMBER));
        userInfo.setCreateTime(new Date());
        // 保存用户信息到数据库
        this.save(userInfo);

        log.info("用户注册成功：{}", userInfo);
    }

    @Override
    public UserTokenDTO login(String email, String password) {
        UserInfo dbUserInfo = baseMapper.selectOne(new QueryWrapper<UserInfo>().eq("email", email));
        if (dbUserInfo == null || !dbUserInfo.getPassword().equals(password)) {
            // 如果用户已存在，抛出业务异常，表明用户已注册
            throw new BusinessException("账号或密码不存在");
        }
        if (UserStatusEnum.LOCK.getCode() == dbUserInfo.getStatus()) {
            throw new BusinessException("账号被禁用");
        }
        if (dbUserInfo.getOnlineType()==1) {
            throw new BusinessException("该账号已在别处登陆");
        }

        dbUserInfo.setLastLoginTime(System.currentTimeMillis());
        this.updateById(dbUserInfo);

        UserTokenDTO userTokenDTO = CopyTools.copy(dbUserInfo, UserTokenDTO.class);
        userTokenDTO.setAdmin(email.equals(appConfig.getEmail()));

        log.info("用户登录成功：{} ", userTokenDTO);

        UserTokenDTO userTokenDTOr = redisComponent.getUserTokenDTO(dbUserInfo.getUserId());
        if (userTokenDTOr != null){
            redisComponent.deleteUserTokenDTO(userTokenDTOr);
        }
        return redisComponent.setUserTokenDTO(userTokenDTO);

    }

    @Override
    public void updateLastLoginTime(String userId) {
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(userId);
        userInfo.setLastLoginTime(System.currentTimeMillis());
        baseMapper.updateById(userInfo);
    }

    @Override
    public void updateUserInfo(UserInfo userInfo, MultipartFile multipartFile) throws IOException {
        if (multipartFile != null) {
            String avatrPath = CommonUtils.getAvatrPath(appConfig.getFolder());
            File avatarFile = new File(avatrPath);
            if (!avatarFile.exists()) {
                avatarFile.mkdirs();
            }
            String fileName = avatrPath + userInfo.getUserId() + CommonConstant.IMAGE_SUFFIX;

            File tmpFile = new File(CommonUtils.getTmpPath(appConfig.getFolder()) + CommonUtils.getMeetingNo());
            multipartFile.transferTo(tmpFile);
            fFmpegUtils.createImageThumbnail(tmpFile, fileName);
        }

        this.baseMapper.updateById(userInfo);
        UserTokenDTO userTokenDTO = redisComponent.getUserTokenDTO(userInfo.getUserId());
        userTokenDTO.setSex(userInfo.getSex());
        userTokenDTO.setNickName(userInfo.getNickName());
        redisComponent.setUserTokenDTO(userTokenDTO);

    }

    @Override
    public void updatePassword(UserTokenDTO userTokenDTO, String oldPassword, String newPassword) {
        UserInfo userInfo = this.baseMapper.selectById(userTokenDTO.getUserId());

        if (!userInfo.getPassword().equals(DigestUtil.md5Hex(oldPassword + CommonConstant.MD5_SALTING))) {
            throw new BusinessException("与原密码不一致");
        }

        userInfo.setPassword(DigestUtil.md5Hex(newPassword + CommonConstant.MD5_SALTING));
        this.baseMapper.updateById(userInfo);
        redisComponent.deleteUserTokenDTO(userTokenDTO);


    }

    @Override
    public List<UserInfo> loadUserList(String nickName, Page<UserInfo> userInfoIPage) {
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        if (nickName != null) {
            queryWrapper.like("nick_name", nickName);
        }
        return this.baseMapper.selectList(userInfoIPage, queryWrapper);
    }

    @Override
    public void updateUserStatus(UserInfo userInfo) {
        baseMapper.updateById(userInfo);
        if (userInfo.getStatus() == UserStatusEnum.LOCK.getCode()) {
            forceLine(userInfo.getUserId());
        }
    }

    @Override
    public void forceLine(String userId) {
        UserInfo userInfo = baseMapper.selectById(userId);
        if (userInfo.getOnlineType() == 0) {
            return;
        }

        MessageSendDto messageSendDto = new MessageSendDto<>();
        messageSendDto.setMessageSend2Type(MessageSendTypeEnum.USER.getCode());
        messageSendDto.setMessageType(MessageTypeEnum.FORCE_OFF_LINE.getType());
        messageSendDto.setReceiveUserId(userId);
        messageHandler.sendMessage(messageSendDto);

        redisComponent.deleteUserTokenDTO(redisComponent.getUserTokenDTO(userId));

    }
}




