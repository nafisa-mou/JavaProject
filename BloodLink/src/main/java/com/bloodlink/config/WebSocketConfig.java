package com.bloodlink.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocketConfig - Configure Spring WebSocket and STOMP message broker
 * 
 * Features:
 * - STOMP protocol for real-time messaging
 * - Message broker for routing
 * - Destination prefixes for organizing messages
 * - CORS configuration for frontend connections
 * - Connection handling and queuing
 * 
 * Endpoints:
 * - /ws/chat - WebSocket connection point for chat
 * 
 * Architecture:
 * Client connects via WebSocket → STOMP protocol → Message Broker → Route to recipients
 * 
 * OOP Principle: Configuration pattern using @Configuration
 * Spring Pattern: WebSocket integration with STOMP
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Configure STOMP endpoints and WebSocket connection
     * 
     * @param registry Endpoint registry for WebSocket
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register WebSocket endpoint at /ws/chat
        registry.addEndpoint("/ws/chat")
                // Enable CORS for frontend applications
                .setAllowedOrigins("http://localhost:3000", 
                                   "http://localhost:8080", 
                                   "http://localhost:4200")
                // Use SockJS fallback for browsers without WebSocket support
                .withSockJS();
        
        // Optional: Another endpoint for notifications
        registry.addEndpoint("/ws/notify")
                .setAllowedOrigins("http://localhost:3000", 
                                   "http://localhost:8080", 
                                   "http://localhost:4200")
                .withSockJS();
    }

    /**
     * Configure message broker for routing messages between clients
     * 
     * @param config Message broker configuration
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple in-memory message broker
        // In production, consider using RabbitMQ, Kafka, or ActiveMQ
        config.enableSimpleBroker("/topic", "/queue");
        
        // Set application destination prefix for client-to-server messages
        // Client sends to /app/message/send → routes to @MessageMapping
        config.setApplicationDestinationPrefixes("/app");
        
        // Optional: Set user destination prefix for one-to-one messaging
        // /user/{userId}/queue/messages → Private queue per user
        config.setUserDestinationPrefix("/user");
    }

    /**
     * Message Broker Destinations:
     * 
     * Topic Destinations (Broadcasting - one to many):
     * - /topic/chat/{chatId} - Broadcast to all chat participants
     * - /topic/donors/available - Available donors notifications
     * - /topic/requests/critical - Critical blood requests
     * - /topic/online/status - User online/offline status
     * 
     * Queue Destinations (Private - one to one):
     * - /user/{userId}/queue/messages - Private message queue
     * - /user/{userId}/queue/notifications - Private notifications
     * - /user/{userId}/queue/typing - Typing indicators
     * 
     * Application Destinations (Client → Server):
     * - /app/message/send - Send message to chat
     * - /app/user/online - Mark user as online
     * - /app/user/offline - Mark user as offline
     * - /app/typing/start - Start typing indicator
     * - /app/typing/stop - Stop typing indicator
     */
}
