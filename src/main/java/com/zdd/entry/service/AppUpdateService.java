package com.zdd.entry.service;

import com.baomidou.mybatisplus.extension.service.IService;

import com.zdd.entry.domain.AppUpdate;
import com.zdd.entry.dto.AppUpdateDto;
import org.springframework.web.multipart.MultipartFile;

import java.net.BindException;
import java.util.List;

/**
 * <p>
 * app发布 服务类
 * </p>
 *
 * @author zdd
 * @since 2025-03-05
 */
public interface AppUpdateService extends IService<AppUpdate> {

    List<AppUpdate> findByPage(AppUpdateDto appUpdateVO);

    void saveOrUpdateMethd(Integer id, String version, String updateDesc, String outerLink, MultipartFile multipartFile) throws BindException, Exception;

    void grayscaleStatus(Integer id, Integer status, String grayscaleUid);

    AppUpdate checkVersion(String appVsersion, String uid);
}
