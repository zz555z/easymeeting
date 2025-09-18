package com.zdd.utils;

import cn.hutool.crypto.digest.DigestUtil;
import com.zdd.entry.constants.CommonConstant;
import com.zdd.entry.eum.FileTypeEnum;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@Slf4j
public class CommonUtils {
    private static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private static final String DEFAULT_DATE_TIME_FORMAT_YYYYMMDD = "yyyyMMdd";

    private static final String DEFAULT_DATE_YYYY_MM_DD_HH_MM = "yyyy-MM-dd HH:mm";


    public static String getMeetingId() {
        return RandomUtils.generateRandomNumber(CommonConstant.RODMIX_NUMBER).toString();
    }

    public static String getMeetingNo() {
        return RandomUtils.generateRandomString(CommonConstant.RODMIX_NUMBER);
    }


    /**
     * 将yyyy-MM-dd HH:mm:ss格式的字符串转换为Date类型
     *
     * @param dateTimeStr 日期时间字符串
     * @return 转换后的Date对象
     * @throws ParseException 如果字符串格式不正确
     */
    public static Date stringToDate(String dateTimeStr) {
        SimpleDateFormat sdf = new SimpleDateFormat(DEFAULT_DATE_TIME_FORMAT);
        try {
            return sdf.parse(dateTimeStr);
        } catch (ParseException e) {
            log.error("字符串转换日期错误");
            throw new RuntimeException(e);
        }

    }


    /**
     * 将yyyy-MM-dd HH:mm:ss格式的字符串转换为Date类型
     *
     * @param dateTimeStr 日期时间字符串
     * @return 转换后的Date对象
     * @throws ParseException 如果字符串格式不正确
     */
    public static Date stringToDateYMDHM(String dateTimeStr) {
        SimpleDateFormat sdf = new SimpleDateFormat(DEFAULT_DATE_YYYY_MM_DD_HH_MM);
        try {
            return sdf.parse(dateTimeStr);
        } catch (ParseException e) {
            log.error("字符串转换日期错误");
            throw new RuntimeException(e);
        }

    }


    /**
     * 将yyyy-MM-dd HH:mm:ss格式的字符串转换为Date类型
     *
     * @param dateTimeStr 日期时间字符串
     * @return 转换后的Date对象
     * @throws ParseException 如果字符串格式不正确
     */
    public static Date parseYYYYMMDD(String dateTimeStr) {
        SimpleDateFormat sdf = new SimpleDateFormat(DEFAULT_DATE_TIME_FORMAT_YYYYMMDD);
        try {
            return sdf.parse(dateTimeStr);
        } catch (ParseException e) {
            log.error("字符串转换日期错误");
            throw new RuntimeException(e);
        }

    }

    /**
     * 将Date对象格式化为yyyyMMdd字符串
     *
     * @param date Date对象
     * @return 格式化后的字符串
     */
    public static String dateToStringYYYYMMDD(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(DEFAULT_DATE_TIME_FORMAT_YYYYMMDD);
        return sdf.format(date);
    }


    /**
     * 获取指定日期的开始时间（00:00:00）
     *
     * @param date 指定的日期
     * @return 一天的开始时间
     */
    public static String getStartOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return dateToString(calendar.getTime());
    }

    /**
     * 获取指定日期的结束时间（23:59:59）
     *
     * @param date 指定的日期
     * @return 一天的结束时间
     */
    public static String getEndOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return dateToString(calendar.getTime());
    }

    /**
     * 计算指定分钟数后的时间
     *
     * @param minutes 要增加的分钟数
     * @return 增加分钟数后的Date对象
     */
    public static Date addMinutes(Date date, int minutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MINUTE, minutes);
        return calendar.getTime();
    }




    /**
     * 将Date对象格式化为yyyy-MM-dd HH:mm:ss字符串
     *
     * @param date Date对象
     * @return 格式化后的字符串
     */
    public static String dateToString(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(DEFAULT_DATE_TIME_FORMAT);
        return sdf.format(date);
    }


    /**
     * 获取文件后缀
     *
     * @param fileName
     * @return
     */
    public static String getFileSuffix(String fileName) {
        return fileName.substring(fileName.lastIndexOf("."));
    }


    public static String getImageThumbnailSuffix(String filePath) {
        return filePath + CommonConstant.IMAGETHUMBNAIL + CommonConstant.IMAGE_SUFFIX;
    }

    public static String getUploadFilePath(String filePath, Long sendTime, FileTypeEnum fileTypeEnum) {
        return filePath +File.separator+ CommonConstant.FIlE + CommonConstant.FILE_IMAGE + CommonUtils.dateToStringYYYYMMDD(new Date(sendTime))+File.separator+fileTypeEnum.getCode();

    }
    public static String getTmpPath(String filePath) {
        return filePath +File.separator+ CommonConstant.FIlE + CommonConstant.FIlE_TMP;
    }

    public static String getAvatrPath(String filePath) {
        return filePath +File.separator+ CommonConstant.FIlE + CommonConstant.FIlE_AVATAR ;

    }


}
