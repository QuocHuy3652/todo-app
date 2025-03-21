package com.huydevcorn.todo_app.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuration class for WebSocket message broker.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    /**
     * Registers STOMP endpoints for WebSocket connections.
     *
     * @param registry the registry to add endpoints to
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*");
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS();
    }

    /**
     * Configures the message broker for handling messages.
     *
     * @param config the message broker registry to configure
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/notification");
        config.setApplicationDestinationPrefixes("/socket");
    }
}
