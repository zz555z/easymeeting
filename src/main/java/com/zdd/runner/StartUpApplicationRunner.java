package com.zdd.runner;

import com.zdd.component.RedisComponent;
import com.zdd.entry.dto.SysSettingDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StartUpApplicationRunner implements ApplicationRunner {
    @Autowired
    private RedisComponent redisComponent;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("项目启动----开始执行缓存加载操作。。。");
        redisComponent.setSysSetting(new SysSettingDto());
        log.info("项目启动----完成执行缓存加载操作。。。");

    }
}
