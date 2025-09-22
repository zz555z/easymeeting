package com.zdd.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zdd.aop.annotation.GlobalInterceptor;
import com.zdd.component.RedisComponent;
import com.zdd.config.AppConfig;
import com.zdd.entry.constants.CommonConstant;
import com.zdd.entry.domain.AppUpdate;
import com.zdd.entry.domain.MeetingInfo;
import com.zdd.entry.domain.UserInfo;
import com.zdd.entry.dto.AppUpdateDto;
import com.zdd.entry.dto.SysSettingDto;
import com.zdd.entry.eum.ResponseCodeEnum;
import com.zdd.entry.eum.UserStatusEnum;
import com.zdd.entry.service.AppUpdateService;
import com.zdd.entry.service.MeetingInfoService;
import com.zdd.entry.service.UserInfoService;
import com.zdd.entry.vo.ResponseVO;
import com.zdd.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private RedisComponent redisComponent;
    @Autowired
    private MeetingInfoService meetingInfoService;
    @Autowired
    private AppUpdateService appUpdateService;
    @Autowired
    private AppConfig appConfig;

    @PostMapping("/loadUserList")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO loadUserList(String nickName, Integer pageNo, Integer pageSize) {
        Page<UserInfo> userInfoIPage = new Page<UserInfo>(pageNo, pageSize);
        List<UserInfo> userInfos = userInfoService.loadUserList(nickName, userInfoIPage);
        Map<String, Object> map = new HashMap<>();
        map.put("list", userInfos);
        return ResponseVO.success(map);
    }


    @PostMapping("/updateUserStatus")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO updateUserStatus(@NotNull Integer userStatus, @NotEmpty String userId) {
        UserStatusEnum userStatusEnum = UserStatusEnum.getByCode(userStatus);

        if (userStatusEnum == null) {
            log.info("用户状态错误");
            throw new BusinessException(ResponseCodeEnum.RESPONSE_CODE_900);
        }

        UserInfo userInfo = new UserInfo();
        userInfo.setStatus(userStatus);
        userInfo.setUserId(userId);
        userInfoService.updateUserStatus(userInfo);
        return ResponseVO.success();
    }


    @PostMapping("/forceLine")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO forceLine(@NotEmpty String userId) {
        userInfoService.forceLine(userId);
        return ResponseVO.success();
    }

    @PostMapping("/updateSysSetting")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO updateSysSetting(SysSettingDto sysSettingDto) {
        redisComponent.setSysSetting(sysSettingDto);
        return ResponseVO.success();
    }

    @PostMapping("/getSysSetting")
//    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO getSysSetting() {
        return ResponseVO.success(redisComponent.getSysSetting());
    }


    @PostMapping("/loadMeetingList")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO loadMeetingList(String meetingName, Integer pageNum, Integer pageSize) {
        Page<MeetingInfo> userInfoIPage = new Page<MeetingInfo>(pageNum == null ? 1 : pageNum, pageSize == null ? CommonConstant.PAGE_SIZE : pageSize);
        List<MeetingInfo> meetingInfos = meetingInfoService.loadMeetingList(meetingName, userInfoIPage);
        Map<String, Object> map = new HashMap<>();
        map.put("list", meetingInfos);
        return ResponseVO.success(map);
    }

    @PostMapping("/adminFinishMeeting")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO adminFinishMeeting(@NotEmpty String meetingId) {
        meetingInfoService.finishMeeting(meetingId, null);
        return ResponseVO.success();
    }


    @PostMapping("/loadUpLoadList")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO loadUpLoadList(String createTimeStart,String createTimeEnd, Integer pageNum, Integer pageSize) {
        Page<AppUpdate> appUpdatePage = new Page<AppUpdate>(pageNum, pageSize);

        // 通过分页查询获取应用更新列表
        List<AppUpdate> appUpdates = appUpdateService.findByPage(appUpdatePage, createTimeStart, createTimeEnd);
        // 将查询结果转换为视图对象列表并返回成功响应
        // 返回用户信息
        Map<String, Object> map = new HashMap<>();
        map.put("list", appUpdates);
        return ResponseVO.success(map);
    }

    /**
     * 修改或新增版本
     *
     * @param id            版本ID，用于标识特定的版本记录
     * @param version       版本号，表示软件的版本
     * @param updateDesc    更新描述，说明此版本的更新内容
     * @param outerLink     外部链接，可选参数，用于提供外部更新资源链接
     * @param multipartFile 文件，可选参数，用于上传更新文件
     * @return 返回操作结果的响应对象
     * @throws Exception 抛出异常，处理过程中可能遇到的通用异常
     */
    @PostMapping("/saveOrUpdateAppUpdate")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO saveOrUpdateAppUpdate(Integer id, @NotEmpty String version, @NotEmpty String updateDesc,
                                            String outerLink, MultipartFile multipartFile) throws Exception {
        // 调用服务层方法，保存或更新版本信息
        appUpdateService.saveOrUpdateMethd(id, version, updateDesc, outerLink, multipartFile);
        // 返回用户信息
        return ResponseVO.success();
    }

    /**
     * 删除版本
     * <p>
     * 此方法用于删除指定版本的应用更新信息它要求管理员权限，并且输入的ID不能为空
     * 使用PostMapping注解限定HTTP请求方法为POST，确保此操作通过POST请求触发
     * LoginCheck注解用于检查管理员权限，保证只有管理员可以执行此操作
     *
     * @param id 要删除的应用更新记录的ID，不能为空
     * @return 返回一个表示操作结果的ResponseVO对象，用于告知客户端操作是否成功
     * @throws Exception 如果操作失败，抛出异常
     */
    @PostMapping("/delupdate")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO delupdate(@NotEmpty Integer id) throws Exception {
        appUpdateService.removeById(id);
        // 返回用户信息
        return ResponseVO.success();
    }

    /**
     * 修改版本状态
     * 该接口用于修改版本的灰度发布状态
     *
     * @param id           版本ID，不能为空
     * @param status       灰度发布状态，如果为null，则不改变当前状态
     * @param grayscaleUid 灰度发布UID，用于指定特定的灰度发布版本
     * @return 返回操作结果的响应对象
     * @throws Exception 如果操作失败，抛出异常
     */
    @PostMapping("/grayscaleStatus")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO grayscaleStatus(@NotEmpty Integer id, Integer status, String grayscaleUid) throws Exception {
        appUpdateService.grayscaleStatus(id, status, grayscaleUid);
        // 返回用户信息
        return ResponseVO.success();
    }

    @PostMapping("/checkVersion")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO<AppUpdate> checkVersion(String appVsersion, String uid) {
        // 检查版本号或用户ID是否为空，如果任一为空，则直接返回成功响应，不进行版本检查
        if (StringUtils.isEmpty(appVsersion) || StringUtils.isEmpty(uid)) {
            return ResponseVO.success();
        }
        // 调用服务层方法检查版本，并获取更新信息
        AppUpdate appUpdate = appUpdateService.checkVersion(appVsersion, uid);
        // 将更新信息转换为视图对象，并封装到响应对象中返回
        return ResponseVO.success(appUpdate);
    }

    @RequestMapping("/download")
    @GlobalInterceptor
    public void download(HttpServletResponse response, @NotNull Integer id) {
        AppUpdate appUpdate = appUpdateService.getById(id);
        String path = appConfig.getFolder() + CommonConstant.FIlE + CommonConstant.EXE_FILE_PATH + appUpdate.getId() + appUpdate.getVersion() + CommonConstant.EXE_SUFFIX;
        log.info("下载文件路径：{}", path);
        File file = new File(path);
        if (!file.exists()) {
            return;
        }
        response.setContentType("application/x-msdownload; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment;");
        response.setContentLengthLong(file.length());
        try (OutputStream out = response.getOutputStream(); FileInputStream in = new FileInputStream(file)) {
            byte[] byteData = new byte[1024];
            int len = 0;
            while ((len = in.read(byteData)) != -1) {
                out.write(byteData, 0, len);
            }
            out.flush();
        } catch (Exception e) {
            log.error("读取文件异常", e);
        }
    }


}
