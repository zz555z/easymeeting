package com.zdd.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "admin")
@Data// 绑定 app.* 的配置
public class AppConfig {
    private String email;
    private int wsPort;
    private String folder;
}
