package com.bloodlink.websocket;

import com.bloodlink.dto.WebSocketMessage;
import com.bloodlink.entity.Message;
import com.bloodlink.service.ChatService;
import com.bloodlink.service.MessageService;
import com.bloodlink.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

/**
 * ChatWebSocketController - Handle real-time WebSocket messages for chat
 * 
 * Endpoints (via @MessageMapping):
 * - /app/message/send - Send message to chat
 * - /app/chat/{chatId}/typing - Send typing indicator
 * - /app/user/{userId}/online - Mark user online
 * - /app/user/{userId}/offline - Mark user offline
 * - /app/message/{messageId}/delivered - Confirm message delivered
 * - /app/message/{messageId}/read - Confirm message read
 * 
 * Broadcasts (via @SendTo):
 * - /topic/chat/{chatId} - Broadcast to chat participants
 * - /topic/user/{userId}/status - Broadcast user status
 * - /user/{userId}/queue/messages - Private user messages
 * 
 * OOP Principle: Encapsulation - Complex logic delegated to services
 * Design Pattern: Observer pattern via STOMP message broker
 * SOLID: Single Responsibility - Only handles WebSocket protocol
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;
    private final MessageService messageService;
    private final UserStatusService userStatusService;

    /**
     * Send chat message to all participants in chat
     * 
     * Endpoint: POST /app/message/send
     * Payload Example:
     * {
     *   "messageId": "uuid-123",
     *   "chatId": 1,
     *   "senderId": 2,
     *   "senderName": "John Donor",
     *   "content": "Hello!",
     *   "type": "CHAT"
     * }
     * 
     * @param message WebSocket message
     * @return Broadcast to /topic/chat/{chatId}
     */
    @MessageMapping("/message/send")
    @SendTo("/topic/chat/{chatId}")
    public WebSocketMessage sendMessage(@Payload WebSocketMessage message) {
        log.info("Received chat message from user {} in chat {}", 
                 message.getSenderId(), message.getChatId());
        
        try {
            // Validate message
            if (message.getContent() == null || message.getContent().trim().isEmpty()) {
                log.warn("Empty message received");
                return WebSocketMessage.createError(message.getMessageId(), "Message content cannot be empty");
            }

            if (message.getContent().length() > 5000) {
                log.warn("Message too long: {}", message.getContent().length());
                return WebSocketMessage.createError(message.getMessageId(), "Message exceeds 5000 characters");
            }

            // Save message to database via service layer
            Message savedMessage = messageService.sendMessage(
                message.getChatId(),
                message.getSenderId(),
                message.getContent()
            );

            // Update message with saved data
            message.setMessageId(savedMessage.getMessageId().toString());
            message.setTimestamp(LocalDateTime.now());

            log.info("Message saved successfully: {}", message.getMessageId());

            // Broadcast to all users in chat topic
            return message;

        } catch (Exception e) {
            log.error("Error sending message", e);
            return WebSocketMessage.createError(
                message.getMessageId(), 
                "Failed to send message: " + e.getMessage()
            );
        }
    }

    /**
     * Send typing indicator to chat participants
     * 
     * Endpoint: POST /app/chat/{chatId}/typing
     * Payload Example:
     * {
     *   "chatId": 1,
     *   "senderId": 2,
     *   "senderName": "John",
     *   "isTyping": true,
     *   "type": "TYPING"
     * }
     * 
     * @param chatId Chat ID
     * @param message Typing indicator message
     */
    @MessageMapping("/chat/{chatId}/typing")
    @SendTo("/topic/chat/{chatId}")
    public WebSocketMessage handleTypingIndicator(@DestinationVariable Long chatId,
                                                 @Payload WebSocketMessage message) {
        log.debug("User {} typing status in chat {}: {}", 
                  message.getSenderId(), chatId, message.getIsTyping());
        
        message.setChatId(chatId);
        message.setTimestamp(LocalDateTime.now());
        
        // Don't save to database, just broadcast
        return message;
    }

    /**
     * Mark user as online
     * 
     * Endpoint: POST /app/user/{userId}/online
     * 
     * @param userId User ID
     * @param message Status message
     */
    @MessageMapping("/user/{userId}/online")
    public void handleUserOnline(@DestinationVariable Long userId,
                                 @Payload WebSocketMessage message) {
        log.info("User {} marked as online", userId);
        
        try {
            // Update user status in service
            userStatusService.setUserOnline(userId);
            
            // Broadcast online status to all users
            WebSocketMessage statusMsg = WebSocketMessage.createStatusUpdate(
                userId, 
                message.getSenderName(), 
                "ONLINE"
            );
            
            messagingTemplate.convertAndSend(
                "/topic/user/" + userId + "/status", 
                statusMsg
            );
            
        } catch (Exception e) {
            log.error("Error setting user online", e);
        }
    }

    /**
     * Mark user as offline
     * 
     * Endpoint: POST /app/user/{userId}/offline
     * 
     * @param userId User ID
     * @param message Status message
     */
    @MessageMapping("/user/{userId}/offline")
    public void handleUserOffline(@DestinationVariable Long userId,
                                  @Payload WebSocketMessage message) {
        log.info("User {} marked as offline", userId);
        
        try {
            // Update user status in service
            userStatusService.setUserOffline(userId);
            
            // Broadcast offline status to all users
            WebSocketMessage statusMsg = WebSocketMessage.createStatusUpdate(
                userId, 
                message.getSenderName(), 
                "OFFLINE"
            );
            
            messagingTemplate.convertAndSend(
                "/topic/user/" + userId + "/status", 
                statusMsg
            );
            
        } catch (Exception e) {
            log.error("Error setting user offline", e);
        }
    }

    /**
     * Confirm message delivered to recipient
     * 
     * Endpoint: POST /app/message/{messageId}/delivered
     * 
     * @param messageId Message ID
     * @param message Delivery confirmation
     */
    @MessageMapping("/message/{messageId}/delivered")
    public void handleMessageDelivered(@DestinationVariable String messageId,
                                      @Payload WebSocketMessage message) {
        log.debug("Message {} delivered to user {}", messageId, message.getRecipientId());
        
        try {
            // Send confirmation to sender
            WebSocketMessage confirmMsg = WebSocketMessage.createDeliveryConfirm(
                messageId, 
                message.getChatId(), 
                message.getRecipientId()
            );
            
            messagingTemplate.convertAndSendToUser(
                message.getSenderId().toString(),
                "/queue/delivery-confirm",
                confirmMsg
            );
            
        } catch (Exception e) {
            log.error("Error handling delivery confirmation", e);
        }
    }

    /**
     * Confirm message read by recipient
     * 
     * Endpoint: POST /app/message/{messageId}/read
     * 
     * @param messageId Message ID
     * @param message Read confirmation
     */
    @MessageMapping("/message/{messageId}/read")
    public void handleMessageRead(@DestinationVariable String messageId,
                                 @Payload WebSocketMessage message) {
        log.debug("Message {} read by user {}", messageId, message.getRecipientId());
        
        try {
            // Mark message as seen in database
            messageService.markAsSeen(Long.parseLong(messageId));
            
            // Send confirmation to sender
            WebSocketMessage confirmMsg = WebSocketMessage.createReadConfirm(
                messageId, 
                message.getChatId(), 
                message.getRecipientId()
            );
            
            messagingTemplate.convertAndSendToUser(
                message.getSenderId().toString(),
                "/queue/read-confirm",
                confirmMsg
            );
            
            // Broadcast in chat that message was read
            messagingTemplate.convertAndSend(
                "/topic/chat/" + message.getChatId(),
                confirmMsg
            );
            
        } catch (Exception e) {
            log.error("Error handling read confirmation", e);
        }
    }

    /**
     * Send notification to specific user (private message)
     * Used for system notifications, blood request alerts, etc.
     * 
     * Note: This is called programmatically, not by client
     * 
     * @param userId Target user ID
     * @param notification Notification message
     */
    public void sendNotificationToUser(Long userId, WebSocketMessage notification) {
        log.info("Sending notification to user: {}", userId);
        
        try {
            messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/notifications",
                notification
            );
            
        } catch (Exception e) {
            log.error("Error sending notification to user {}", userId, e);
        }
    }

    /**
     * Broadcast message to all connected users
     * 
     * @param message Message to broadcast
     */
    public void broadcastToAllUsers(WebSocketMessage message) {
        log.info("Broadcasting message to all users");
        
        try {
            messagingTemplate.convertAndSend(
                "/topic/broadcast",
                message
            );
            
        } catch (Exception e) {
            log.error("Error broadcasting message", e);
        }
    }

    /**
     * Send message to specific chat
     * 
     * @param chatId Chat ID
     * @param message Message to send
     */
    public void sendMessageToChat(Long chatId, WebSocketMessage message) {
        log.debug("Sending message to chat: {}", chatId);
        
        try {
            messagingTemplate.convertAndSend(
                "/topic/chat/" + chatId,
                message
            );
            
        } catch (Exception e) {
            log.error("Error sending message to chat {}", chatId, e);
        }
    }
}
