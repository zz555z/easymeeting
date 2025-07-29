package com.zdd.utils;

/**
 * 自定义雪花算法
 */
public class SnowflakeIdUtil {
    // 起始时间戳（2020-01-01）
    private static final long START_TIMESTAMP = 1577808000000L;
    private static final long SEQUENCE_BITS = 12L;
    private static final long MACHINE_BITS = 5L;
    private static final long DATACENTER_BITS = 5L;

    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);
    private static final long MAX_MACHINE = ~(-1L << MACHINE_BITS);
    private static final long MAX_DATACENTER = ~(-1L << DATACENTER_BITS);

    private static final long MACHINE_SHIFT = SEQUENCE_BITS;
    private static final long DATACENTER_SHIFT = SEQUENCE_BITS + MACHINE_BITS;
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + MACHINE_BITS + DATACENTER_BITS;

    private long datacenterId; // 数据中心ID
    private long machineId;    // 机器ID
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    public SnowflakeIdUtil(long datacenterId, long machineId) {
        if (datacenterId > MAX_DATACENTER || datacenterId < 0) {
            throw new IllegalArgumentException("datacenterId out of range");
        }
        if (machineId > MAX_MACHINE || machineId < 0) {
            throw new IllegalArgumentException("machineId out of range");
        }
        this.datacenterId = datacenterId;
        this.machineId = machineId;
    }

    public synchronized long nextId() {
        long now = System.currentTimeMillis();
        if (now == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0) {
                // 当前毫秒序列用完，等待下一毫秒
                while ((now = System.currentTimeMillis()) <= lastTimestamp) {}
            }
        } else {
            sequence = 0L;
        }
        lastTimestamp = now;
        return ((now - START_TIMESTAMP) << TIMESTAMP_SHIFT)
                | (datacenterId << DATACENTER_SHIFT)
                | (machineId << MACHINE_SHIFT)
                | sequence;
    }

    // 测试
    public static void main(String[] args) {
        SnowflakeIdUtil idWorker = new SnowflakeIdUtil(1, 1);
        for (int i = 0; i < 10; i++) {
            System.out.println(idWorker.nextId());
        }
    }
}