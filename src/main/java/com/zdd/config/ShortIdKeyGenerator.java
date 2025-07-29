package com.zdd.config;

import com.zdd.utils.ShortIdUtil;
import com.zdd.utils.SnowflakeIdUtil;
import org.apache.shardingsphere.sharding.spi.KeyGenerateAlgorithm;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

public class ShortIdKeyGenerator implements KeyGenerateAlgorithm {


    private static final SnowflakeIdUtil snowflakeIdUtil = new SnowflakeIdUtil(1, 1);


    @Override
    public Comparable<?> generateKey() {
//        return ShortIdUtil.nextId(); // 每次加1，自增序列
        return snowflakeIdUtil.nextId();
    }

    @Override
    public String getType() {
        return "SHORT_ID";
    }

    @Override
    public void init(Properties properties) {
    }

    @Override
    public Properties getProps() {
        return new Properties();
    }
}
