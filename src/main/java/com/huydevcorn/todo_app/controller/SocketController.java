package com.huydevcorn.todo_app.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class SocketController {
    @MessageMapping("/sendMessage")
    @SendTo("/notification/messages")
    public String receiveMessage(String message) {
        System.out.println("Received message: " + message);
        return "Server received: " + message;
    }
}
