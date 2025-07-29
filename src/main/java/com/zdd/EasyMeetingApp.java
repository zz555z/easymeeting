package com.zdd;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@MapperScan("com.zdd.entry.mapper")
@EnableTransactionManagement
@EnableAsync
@EnableScheduling
@EnableAspectJAutoProxy
public class EasyMeetingApp {
    public static void main(String[] args) {
        SpringApplication.run(EasyMeetingApp.class, args);
    }
}