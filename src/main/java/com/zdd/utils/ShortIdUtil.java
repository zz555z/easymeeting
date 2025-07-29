package com.zdd.utils;

public class ShortIdUtil {
    // 自增序列，防止同一毫秒内重复
    private static volatile long sequence = 0L;
    private static final long SEQUENCE_MASK = 4095; // 12位自增，0~4095
    private static long lastTimestamp = -1L;

    private static final char[] BASE62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();

    public static synchronized String nextId() {
        long timestamp = System.currentTimeMillis();
        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0) {
                // 如果同一毫秒内自增溢出，等待下一毫秒
                while ((timestamp = System.currentTimeMillis()) <= lastTimestamp) {}
            }
        } else {
            sequence = 0L;
        }
        lastTimestamp = timestamp;

        // 组合：时间戳左移12位 + 序列号
        long id = ((timestamp & 0x1FFFFFFFFFFL) << 12) | sequence; // 41位时间戳+12位序列
        return toBase62(id);
    }

    // 转为Base62字符串
    private static String toBase62(long num) {
        StringBuilder sb = new StringBuilder();
        do {
            sb.append(BASE62[(int) (num % 62)]);
            num /= 62;
        } while (num > 0);
        return sb.reverse().toString();
    }

    // 测试
    public static void main(String[] args) {
        for (int i = 0; i < 20; i++) {
            System.out.println(ShortIdUtil.nextId());
        }
    }
}