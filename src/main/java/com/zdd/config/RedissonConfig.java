package com.zdd.config;

import com.zdd.entry.constants.CommonConstant;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
//消息通道是redis的时候才启用
@ConditionalOnProperty(name = CommonConstant.MESSAGING_HANDLE_CHANNEL, havingValue = CommonConstant.MESSAGING_HANDLE_CHANNEL_REDIS)
public class RedissonConfig {
    @Value("${spring.redis.host}")
    private String redisHost;
    @Value("${spring.redis.port}")
    private String redisPort;
    @Value("${spring.redis.password}")
    private String redisPassword;

    @Bean
    public RedissonClient redissonClient() {
        try {
            Config config = new Config();
            config.useSingleServer().setAddress("redis://" + redisHost + ":" + redisPort).setPassword(redisPassword);
            RedissonClient redissonClient = Redisson.create(config);
            log.info("初始化RedissonConfig成功 ");
            return redissonClient;
        } catch (Exception e) {
            log.info("初始化RedissonConfig失败 e:{}", e);
        }

        return null;


    }


}
