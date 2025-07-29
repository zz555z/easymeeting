package com.zdd.utils;

import java.security.SecureRandom;

/**
 * 随机数工具类
 * 提供生成字母+数字随机字符串和纯数字随机字符串的方法
 */
public class RandomUtils {
    // 可选字符：字母（大小写）+ 数字
    private static final String LETTERS_AND_DIGITS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    // 仅数字字符
    private static final String DIGITS = "0123456789";
    // 使用安全的随机数生成器
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * 生成指定长度的字母+数字随机字符串
     * @param length 随机字符串长度
     * @return 随机字符串
     */
    public static String generateRandomString(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("长度必须大于0");
        }

        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomIndex = SECURE_RANDOM.nextInt(LETTERS_AND_DIGITS.length());
            sb.append(LETTERS_AND_DIGITS.charAt(randomIndex));
        }
        return sb.toString();
    }

    /**
     * 生成指定长度的纯数字随机字符串
     * @param length 随机字符串长度
     * @return 随机数字字符串
     */
    public static Long generateRandomNumber(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("长度必须大于0");
        }

        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomIndex = SECURE_RANDOM.nextInt(DIGITS.length());
            sb.append(DIGITS.charAt(randomIndex));
        }
        return Long.valueOf(sb.toString());
    }

    // 测试
    public static void main(String[] args) {
        System.out.println("随机字母+数字: " + generateRandomString(8)); // 示例: xY7n9p2Q
        System.out.println("随机纯数字: " + generateRandomNumber(6));    // 示例: 123456
    }
}