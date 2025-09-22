package com.zdd.entry.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zdd.config.AppConfig;

import com.zdd.entry.constants.CommonConstant;
import com.zdd.entry.domain.AppUpdate;
import com.zdd.entry.domain.UserInfo;
import com.zdd.entry.dto.AppUpdateDto;
import com.zdd.entry.eum.AppStatusEnum;
import com.zdd.entry.eum.LinkTypeEnum;
import com.zdd.entry.mapper.AppUpdateMapper;
import com.zdd.entry.service.AppUpdateService;
import com.zdd.exception.BusinessException;

import com.zdd.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * app发布 服务实现类
 * </p>
 *
 * @author zdd
 * @since 2025-03-05
 */
@Slf4j
@Service
public class AppUpdateServiceImpl extends ServiceImpl<AppUpdateMapper, AppUpdate> implements AppUpdateService {

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private AppUpdateMapper appUpdateMapper;


    /**
     * 根据页面参数查询应用更新记录
     *
     * @param appUpdateVO 包含页面参数和查询条件的应用更新视图对象
     * @return 返回查询到的应用更新记录列表
     */
    @Override
    public List<AppUpdate> findByPage(AppUpdateDto appUpdateVO) {
        // 初始化分页对象
        Page<AppUpdate> page = new Page<>(appUpdateVO.getPageNum(), appUpdateVO.getPageSize());
        // 初始化查询条件封装对象
        QueryWrapper<AppUpdate> appUpdateQueryWrapper = new QueryWrapper<>();

        // 如果版本号不为空，则添加版本号查询条件
        if (StringUtils.isNotEmpty(appUpdateVO.getVersion())) {
            appUpdateQueryWrapper.eq("version", appUpdateVO.getVersion());
        }
        // 如果状态不为空，则添加状态查询条件
        if (appUpdateVO.getStatus() != null) {
            appUpdateQueryWrapper.eq("status", appUpdateVO.getStatus());
        }

        // 执行分页查询
        Page<AppUpdate> appUpdatePage = baseMapper.selectPage(page, appUpdateQueryWrapper);
        // 返回查询结果中的记录列表
        return appUpdatePage.getRecords();
    }

    /**
     * 保存或更新应用版本信息
     *
     * @param id            应用版本记录的ID，用于标识数据库中的特定记录如果为null，则表示创建新记录
     * @param version       版本号，用于标识应用的特定版本
     * @param updateDesc    更新描述，说明此次更新的内容
     * @param outerLink     外部链接，用于提供更新的外部资源链接
     * @param multipartFile 多部分文件，包含应用更新的文件数据如果为null，则表示没有文件上传
     * @throws Exception 当文件上传失败或版本号校验失败时抛出
     *                   <p>
     *                   该方法主要用于处理应用版本的保存或更新操作，根据提供的参数判断是创建新版本记录还是更新现有记录
     *                   如果提供了ID，则尝试更新现有记录；否则，创建新记录此外，该方法还会检查数据库中现有的版本号，
     *                   确保新提供的版本号大于任何已存在的版本号
     */
    @Override
    @Transactional
    public void saveOrUpdateMethd(Integer id, String version, String updateDesc, String outerLink, MultipartFile multipartFile) throws Exception {
        // 检查数据库中是否存在版本记录，如果存在，则确保提供的版本号大于任何已存在的版本号
        AppUpdate appUpdate = appUpdateMapper.findOrderByCreateTime();
        if (ObjectUtil.isNotEmpty(appUpdate)) {
            String dbversion = appUpdate.getVersion().replaceAll("\\.", "");
            String newversion = version.replaceAll("\\.", "");
            if (Long.parseLong(dbversion) > Long.parseLong(newversion)) {
                throw new BusinessException("当前版本必须大于历史版本");
            }
        }

        // 如果提供了ID，检查当前版本的状态是否允许修改
        if (id != null) {
            AppUpdate db = baseMapper.selectById(id);
            if (db.getStatus() != AppStatusEnum.INIT.getCode()) {
                throw new BusinessException("当前版本状态不为初始化，无法修改");
            }
        }

        // 创建一个新的AppUpdate对象，并根据提供的参数设置其属性
        AppUpdate po = new AppUpdate();
        if (multipartFile == null) {
            po.setFileType(LinkTypeEnum.EXTERNAL_LINK.getCode());
        } else {
            po.setFileType(LinkTypeEnum.LOCAL_FILE.getCode());
        }

        // 根据是否提供了ID，决定是更新现有记录还是创建新记录
        if (id != null) {
            po.setVersion(version);
            po.setUpdateDesc(updateDesc);
            po.setId(id);
            po.setOuterLink(outerLink);
            baseMapper.updateById(po);
        } else {
            po.setVersion(version);
            po.setUpdateDesc(updateDesc);
            po.setStatus(AppStatusEnum.INIT.getCode());
            po.setCreateTime(new Date());
            po.setOuterLink(outerLink);
            baseMapper.insert(po);
        }

        // 上传文件，如果提供了的话
        uploadExeFile(po, multipartFile);
    }

    /**
     * 更新应用的灰度发布状态
     *
     * @param id           应用的ID
     * @param status       应用的状态码
     * @param grayscaleUid 灰度发布用户号
     * @throws BusinessException 当状态码未知或灰度发布用户号为空时抛出
     */
    @Override
    public void grayscaleStatus(Integer id, Integer status, String grayscaleUid) {
        // 根据状态码获取应用状态枚举
        AppStatusEnum appStatusEnum = AppStatusEnum.getByCode(status);
        // 如果状态码对应的应用状态不存在，则抛出异常
        if (null == appStatusEnum) {
            throw new BusinessException("未知状态码");
        }
        // 初始化应用更新对象
        AppUpdate appUpdate = new AppUpdate();
        // 如果状态为灰度发布，但灰度发布用户号为空，则抛出异常
        if (AppStatusEnum.GRAYSCALE.getCode() == status && StringUtils.isEmpty(grayscaleUid)) {
            throw new BusinessException("灰度发布用户号不能为空");
        }

        // 如果状态不是灰度发布，则清空灰度发布用户号
        if (AppStatusEnum.GRAYSCALE.getCode() != status) {
            grayscaleUid = null;
        }
        // 设置灰度发布用户号
        appUpdate.setGrayscaleUid(grayscaleUid);
        // 设置应用状态
        appUpdate.setStatus(status);
        // 设置应用ID
        appUpdate.setId(id);
        // 调用底层接口，根据ID更新应用信息
        this.baseMapper.updateById(appUpdate);
    }

    /**
     * 检查应用版本是否需要更新
     * <p>
     * 本方法通过比较数据库中的应用更新信息与客户端当前的版本号，来判断客户端应用是否需要更新
     * 如果数据库中的版本号大于客户端版本号，并且版本状态允许更新（全部用户或灰度发布且用户匹配），
     * 则返回数据库中的更新信息，否则返回null
     *
     * @param appVsersion 客户端当前的应用版本号
     * @param uid         用户ID，用于灰度发布的用户匹配
     * @return 如果需要更新，则返回AppUpdate对象，包含更新信息；否则返回null
     */
    @Override
    public AppUpdate checkVersion(String appVsersion, String uid) {
        // 从数据库中获取最新的应用更新记录
        AppUpdate db = appUpdateMapper.findOrderByCreateTime();

        // 将数据库中的版本号去点后转换为Long类型，以便比较
        Long dbversion = Long.parseLong(db.getVersion().replaceAll("\\.", ""));
        // 将客户端的版本号去点后转换为Long类型，以便比较
        Long appversion = Long.parseLong(appVsersion.replaceAll("\\.", ""));

        // 判断当前版本是否为灰度发布状态，且数据库版本号大于客户端版本号，同时用户ID匹配灰度发布的用户ID
        if (AppStatusEnum.GRAYSCALE.getCode() == db.getStatus() && dbversion > appversion && db.getGrayscaleUid().contains(uid)) {
            // 满足条件，则返回数据库中的更新信息
            return db;
        }

        // 判断当前版本是否为全部用户发布状态，且数据库版本号大于客户端版本号
        if (AppStatusEnum.ALL.getCode() == db.getStatus() && dbversion > appversion) {
            // 满足条件，则返回数据库中的更新信息
            return db;
        }

        // 不满足更新条件，则返回null
        return null;
    }

    @Override
    public List<AppUpdate> findByPage(Page<AppUpdate> appUpdatePage, String createTimeStart, String createTimeEnd) {
        QueryWrapper<AppUpdate> wrapper = new QueryWrapper<AppUpdate>();
        if (StringUtils.isNotEmpty(createTimeStart)&& StringUtils.isNotEmpty(createTimeEnd)){
            wrapper.between("create_time", CommonUtils.stringToDateYYYY_MM_DD(createTimeStart), CommonUtils.stringToDateYYYY_MM_DD(createTimeEnd));
        }
        return this.baseMapper.selectList(appUpdatePage,wrapper);


    }

    /**
     * 上传可执行文件服务方法
     * 该方法负责将给定的可执行文件上传到指定的目录结构中如果文件为空或不存在，则不会进行任何操作
     *
     * @param appUpdate     包含应用更新信息的对象，用于获取应用版本等信息
     * @param multipartFile 代表要上传的文件，通常来源于HTTP请求中的文件上传部分
     * @throws IOException 当文件上传过程中发生I/O错误时抛出
     */
    public void uploadExeFile(AppUpdate appUpdate, MultipartFile multipartFile) throws IOException {
        // 检查文件是否为空，如果为空则直接返回，不执行上传操作
        if (ObjectUtil.isEmpty(multipartFile)) {
            return;
        }

        // 构造文件上传的目标路径，结合应用配置信息和常量定义
        String Path = appConfig.getFolder() + CommonConstant.FIlE + CommonConstant.EXE_FILE_PATH;

        // 创建文件对象，代表上传目录与可执行文件路径的组合
        File uoloadFile = new File(Path);

        // 检查上传目录是否存在，如果不存在则创建目录结构
        if (!uoloadFile.exists()) {
            uoloadFile.mkdirs();
        } else {
            // 如果目录已存在，构造完整的文件路径，包括应用名称、版本号和可执行文件后缀
            String exePath = uoloadFile.getPath() + File.separator + appUpdate.getId() + appUpdate.getVersion();

            // 将上传的文件转移到指定路径下，并添加可执行文件后缀
            multipartFile.transferTo(new File(exePath + CommonConstant.EXE_SUFFIX));

            // 记录日志信息，指示文件上传成功及上传路径
            log.info("上传成功exePath：{}", exePath);
        }
    }
}
