package com.zdd.websocket.netty;

import com.zdd.websocket.message.MessageHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InitNetty implements ApplicationRunner {

    @Autowired
    private NettyServer nettyServer;

    @Autowired
    private MessageHandler messageHandler;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        new Thread(nettyServer).start();

        new Thread(() -> {
            messageHandler.listenMessage();
            log.info("消息监听线程启动成功。。。");
        }).start();
    }
}
