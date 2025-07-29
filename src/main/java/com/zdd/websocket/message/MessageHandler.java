package com.zdd.websocket.message;

import com.zdd.entry.dto.MessageSendDto;
import org.springframework.stereotype.Component;


public interface MessageHandler {

    void listenMessage();

    void sendMessage(MessageSendDto messageSendDto);
}

