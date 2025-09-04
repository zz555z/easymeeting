package com.zdd.controller;

import com.zdd.aop.annotation.GlobalInterceptor;
import com.zdd.component.RedisComponent;
import com.zdd.config.AppConfig;
import com.zdd.entry.constants.CommonConstant;
import com.zdd.entry.dto.UserTokenDTO;
import com.zdd.entry.eum.FileTypeEnum;
import com.zdd.entry.eum.ResponseCodeEnum;
import com.zdd.exception.BusinessException;
import com.zdd.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.*;
import java.util.Date;

@RestController
@Slf4j
@RequestMapping("/file")
public class FileController {
    @Autowired
    private RedisComponent redisComponent;

    @Autowired
    private AppConfig appConfig;


    @PostMapping("/getResource")
    @GlobalInterceptor(checkLogin = false)
    public void getResource(HttpServletResponse response,
                            @RequestHeader(required = false, name = "range") String range,
                            @NotEmpty Long messageId,
                            @NotEmpty Long sendTime,
                            @NotNull Integer fileType,
                            @NotEmpty String token,
                            Boolean thumnail) {
        // 获取用户令牌信息，用于识别和验证用户身份
        UserTokenDTO userTokenDTO = redisComponent.getUserTokenDTO(token);
        if (userTokenDTO == null) {
            throw new BusinessException(ResponseCodeEnum.RESPONSE_CODE_900);
        }

        FileTypeEnum fileTypeEnum = FileTypeEnum.getByType(fileType);

        if (fileTypeEnum == null) {
            throw new BusinessException(ResponseCodeEnum.RESPONSE_CODE_900);
        }

        thumnail = thumnail == null ? false : thumnail;

        String folder = CommonUtils.getImageAndVideoPath(appConfig.getFolder(), sendTime);

        switch (fileTypeEnum) {
            case IMAGE:
                response.setHeader("Cache-Control", "max-age=" + 30 * 24 * 60 * 60);
                response.setContentType("image/jpg");
                break;
        }

        readFile(response, range, folder, thumnail);


    }

    /**
     * 从服务器读取文件并将其传输到客户端
     * 此方法支持处理HTTP范围请求，允许客户端请求文件的特定部分
     * 它还支持生成和读取缩略图
     *
     * @param response 用于向客户端发送响应的HttpServletResponse对象
     * @param range    客户端请求的文件范围，格式为"bytes=start-end"
     * @param folder   文件或缩略图所在的文件夹路径
     * @param thumnail 指示是否处理缩略图的布尔值
     */
    private void readFile(HttpServletResponse response, String range, String folder, Boolean thumnail) {
        // 根据thumnail参数决定是否获取缩略图路径
        folder = thumnail ? CommonUtils.getImageThumbnailSuffix(folder) : folder;

        // 创建File对象以访问文件系统中的文件
        File file = new File(folder);

        try (ServletOutputStream outputStream = response.getOutputStream()) {
            // 使用RandomAccessFile以随机访问方式打开文件进行读取
            RandomAccessFile read = new RandomAccessFile(file, "r");
            long count = read.length();
            int start = 0, end = 0;

            // 处理范围请求，解析客户端请求的文件范围
            if (range != null && range.startsWith("bytes=")) {
                String[] va = range.split("=")[1].split("-");
                start = Integer.parseInt(va[0]);
                if (va.length > 1) {
                    end = Integer.parseInt(va[1]);
                }
            }

            // 计算请求的文件大小
            int requestSize = 0;
            if (end != 0 && end > start) {
                requestSize = end - start + 1;
            } else {
                requestSize = Integer.MAX_VALUE;
            }

            // 创建缓冲区以读取文件数据
            byte[] buffer = new byte[4096];
            // 设置响应头，表明服务器支持字节范围请求
            response.setHeader("Accept-Ranges", "bytes");
            response.setHeader("Last-Modified", new Date().toString());

            // 根据是否是范围请求，设置响应头和状态码
            if (range == null) {
                response.setHeader("Content-Length", String.valueOf(count));
            } else {
                response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
                long requestStart = 0, requestEnd = 0;
                String[] ranges = range.split("=");
                if (ranges.length > 1) {
                    String[] rangeDatas = ranges[1].split("-");
                    requestStart = Long.parseLong(rangeDatas[0]);
                    if (rangeDatas.length > 1) {
                        requestEnd = Long.parseLong(rangeDatas[1]);
                    }
                }

                long length = 0;

                // 根据请求的范围计算内容长度和范围
                if (requestEnd > 0) {
                    length = requestEnd - requestStart + 1;
                    response.setHeader("Content-Length", length + "");
                    response.setHeader("Content-Range", "bytes " + requestStart + "-" + requestEnd + "/" + count);

                } else {
                    length = count - requestStart;
                    response.setHeader("Content-Length", length + "");
                    response.setHeader("Content-Range", "bytes " + requestStart + "-" + (count - 1) + "/" + count);

                }
            }

            // 读取并写入请求的文件部分到输出流
            int needSize = requestSize;
            read.seek(start);
            while (needSize > 0) {
                int len = read.read(buffer);
                if (needSize < buffer.length) {
                    outputStream.write(buffer, 0, needSize);
                } else {
                    outputStream.write(buffer, 0, len);
                    if (len < buffer.length) {
                        break;
                    }
                }
                needSize -= buffer.length;
            }

            // 关闭文件读取流
            read.close();


        } catch (Exception e) {
            // 如果发生异常，记录错误日志
            log.error("文件不存在");
        }
    }

    @RequestMapping("downloadFile")
    @GlobalInterceptor(checkLogin = false)
    public void downloadFile(HttpServletResponse response, @NotEmpty String token, @NotEmpty String messageId, @NotNull Long sendTime) throws IOException {
        UserTokenDTO userTokenDTO = redisComponent.getUserTokenDTO(token);
        if (null == userTokenDTO) {
            throw new BusinessException(ResponseCodeEnum.RESPONSE_CODE_900);
        }
        String filePath = CommonUtils.getImageAndVideoPath(appConfig.getFolder(), sendTime);
        File file = new File(filePath);
        response.setContentType("application/x-msdownload; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment;");
        response.setContentLengthLong(file.length());
        try (FileInputStream in = new FileInputStream(file); OutputStream out = response.getOutputStream()) {
            byte[] byteData = new byte[1024];
            int len = 0;
            while ((len = in.read(byteData)) != -1) {
                out.write(byteData, 0, len);
            }
            out.flush();
        }
    }


    @RequestMapping("/getAvatar")
    @GlobalInterceptor(checkLogin = false)
    public void getAvatar(HttpServletResponse response, @NotNull String userId, @NotEmpty String token) {
        UserTokenDTO userTokenDTO = redisComponent.getUserTokenDTO(token);
        if (null == userTokenDTO) {
            throw new BusinessException(ResponseCodeEnum.RESPONSE_CODE_900);
        }
        String filePath  =appConfig.getFolder()+File.separator + CommonConstant.FIlE + CommonConstant.FIlE_AVATAR;
        String fileName = filePath + userId + CommonConstant.IMAGE_SUFFIX;
        response.setContentType("image/jpg");
        File file = new File(fileName);
        if (!file.exists()) {
            fileName = filePath + CommonConstant.DEFAULT_AVATAR;
        }
        readFile(response, null, fileName, false);
    }

    private void readLocalFile(HttpServletResponse response,String filePath) {
        response.setHeader("Cache-Control", "max-age=" + 30 * 24 * 60 * 60);
        response.setContentType("image/jpg");
        // 读取文件
        ClassPathResource classPathResource = new ClassPathResource(CommonConstant.DEFAULT_AVATAR);
        try (OutputStream out = response.getOutputStream(); InputStream in = classPathResource.getInputStream()) {
            byte[] byteData = new byte[1024];
            int len = 0;
            while ((len = in.read(byteData)) != -1) {
                out.write(byteData, 0, len);
            }
            // out.flush();
        } catch (Exception e) {
            log.error("读取本地文件异常", e);
        }
    }


}
