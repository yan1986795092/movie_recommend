package com.movie_recommend.controller;

import com.movie_recommend.model.dto.chatmessage.ChatMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.stereotype.Controller;
/**
 * @Author yqq
 * @Date 2024/7/4 14:56
 * @Description chat
 * @Version 1.0
 */
@RestController
@Slf4j
public class ChatController {
    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(ChatMessage chatMessage) {
        return chatMessage;
    }
}
