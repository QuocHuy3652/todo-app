package com.huydevcorn.todo_app.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

/**
 * Controller for handling WebSocket messages.
 */
@Controller
public class SocketController {
    /**
     * Endpoint to receive messages from clients and broadcast them to subscribers.
     *
     * @param message the message received from the client
     * @return the message to be broadcasted to subscribers
     */
    @MessageMapping("/sendMessage")
    @SendTo("/notification/messages")
    public String receiveMessage(String message) {
        System.out.println("Received message: " + message);
        return "Server received: " + message;
    }
}
