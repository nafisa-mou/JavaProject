package com.bloodlink.websocket;

import com.bloodlink.dto.WebSocketMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

/**
 * WebSocketUtil - Utility methods for WebSocket operations
 * 
 * Provides common operations:
 * - Sending messages to specific users/chats
 * - Broadcasting announcements
 * - Notification delivery
 * - Error handling
 * 
 * Centralized location for WebSocket utility functions
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketUtil {

    private final SimpMessagingTemplate messagingTemplate;
    private final WebSocketEventListener eventListener;

    /**
     * Send message to specific chat
     * 
     * @param chatId Chat ID
     * @param message Message to send
     */
    public void sendToChat(Long chatId, WebSocketMessage message) {
        try {
            messagingTemplate.convertAndSend(
                "/topic/chat/" + chatId,
                message
            );
            log.debug("Message sent to chat {}", chatId);
        } catch (Exception e) {
            log.error("Error sending to chat {}", chatId, e);
        }
    }

    /**
     * Send private message to user
     * 
     * @param userId User ID
     * @param message Message to send
     */
    public void sendToUser(Long userId, WebSocketMessage message) {
        try {
            messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/messages",
                message
            );
            log.debug("Private message sent to user {}", userId);
        } catch (Exception e) {
            log.error("Error sending to user {}", userId, e);
        }
    }

    /**
     * Broadcast message to all users
     * 
     * @param message Message to broadcast
     */
    public void broadcast(WebSocketMessage message) {
        try {
            messagingTemplate.convertAndSend(
                "/topic/broadcast",
                message
            );
            log.debug("Message broadcasted to all users");
        } catch (Exception e) {
            log.error("Error broadcasting message", e);
        }
    }

    /**
     * Send typing indicator
     * 
     * @param chatId Chat ID
     * @param userId User ID
     * @param isTyping Is typing flag
     */
    public void sendTypingIndicator(Long chatId, Long userId, String userName, boolean isTyping) {
        try {
            WebSocketMessage typing = WebSocketMessage.createTypingIndicator(
                chatId, userId, userName, isTyping
            );
            messagingTemplate.convertAndSend(
                "/topic/chat/" + chatId,
                typing
            );
        } catch (Exception e) {
            log.error("Error sending typing indicator", e);
        }
    }

    /**
     * Notify user of new message
     * 
     * @param recipientId Recipient user ID
     * @param senderName Sender name
     * @param content Message content
     * @param chatId Chat ID
     */
    public void notifyNewMessage(Long recipientId, String senderName, String content, Long chatId) {
        try {
            WebSocketMessage notification = WebSocketMessage.createNotification(
                recipientId,
                "New Message from " + senderName,
                content,
                Map.of("chatId", chatId)
            );
            messagingTemplate.convertAndSendToUser(
                recipientId.toString(),
                "/queue/messages",
                notification
            );
        } catch (Exception e) {
            log.error("Error notifying new message", e);
        }
    }

    /**
     * Notify user of blood request
     * 
     * @param donorId Donor user ID
     * @param patientName Patient name
     * @param bloodGroup Blood group needed
     * @param location Location
     * @param requestId Request ID
     */
    public void notifyBloodRequest(Long donorId, String patientName, String bloodGroup,
                                  String location, Long requestId) {
        try {
            WebSocketMessage notification = WebSocketMessage.createNotification(
                donorId,
                "New Blood Request",
                patientName + " needs " + bloodGroup + " blood in " + location,
                Map.of(
                    "requestId", requestId,
                    "bloodGroup", bloodGroup,
                    "location", location
                )
            );
            messagingTemplate.convertAndSendToUser(
                donorId.toString(),
                "/queue/messages",
                notification
            );
        } catch (Exception e) {
            log.error("Error notifying blood request", e);
        }
    }

    /**
     * Send error to user
     * 
     * @param userId User ID
     * @param errorMessage Error message
     */
    public void sendError(Long userId, String errorMessage) {
        try {
            Map<String, Object> error = Map.of(
                "type", "ERROR",
                "message", errorMessage,
                "timestamp", LocalDateTime.now()
            );
            messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/errors",
                error
            );
        } catch (Exception e) {
            log.error("Error sending error message to user {}", userId, e);
        }
    }

    /**
     * Send acknowledgement to user
     * 
     * @param userId User ID
     * @param messageId Message ID being acknowledged
     * @param ackType Type of acknowledgement (DELIVERED, READ)
     */
    public void sendAcknowledgement(Long userId, String messageId, String ackType) {
        try {
            Map<String, Object> ack = Map.of(
                "type", "ACK",
                "ackType", ackType,
                "messageId", messageId,
                "timestamp", LocalDateTime.now()
            );
            messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/ack",
                ack
            );
        } catch (Exception e) {
            log.error("Error sending acknowledgement", e);
        }
    }

    /**
     * Get destination for broadcasting availability
     * 
     * @return STOMP destination path
     */
    public String getAvailabilityDest() {
        return "/topic/donors/available";
    }

    /**
     * Get destination for critical requests
     * 
     * @return STOMP destination path
     */
    public String getCriticalRequestsDest() {
        return "/topic/requests/critical";
    }

    /**
     * Get destination for user online status
     * 
     * @param userId User ID
     * @return STOMP destination path
     */
    public String getStatusDest(Long userId) {
        return "/topic/user/" + userId + "/status";
    }

    /**
     * Get destination for user's private queue
     * 
     * @param userId User ID
     * @return STOMP destination path
     */
    public String getUserQueueDest(Long userId) {
        return "/user/" + userId + "/queue/messages";
    }

    /**
     * Get destination for chat topic
     * 
     * @param chatId Chat ID
     * @return STOMP destination path
     */
    public String getChatDest(Long chatId) {
        return "/topic/chat/" + chatId;
    }

    /**
     * Validate WebSocket message
     * 
     * @param message Message to validate
     * @return Validation result with error message if invalid
     */
    public Map<String, Object> validateMessage(WebSocketMessage message) {
        Map<String, Object> result = new HashMap<>();
        result.put("valid", true);

        // Validate message type
        if (message.getType() == null) {
            result.put("valid", false);
            result.put("error", "Message type is required");
            return result;
        }

        // Validate based on type
        switch (message.getType()) {
            case CHAT:
                if (message.getChatId() == null) {
                    result.put("valid", false);
                    result.put("error", "Chat ID is required for CHAT messages");
                }
                if (message.getSenderId() == null) {
                    result.put("valid", false);
                    result.put("error", "Sender ID is required");
                }
                if (message.getContent() == null || message.getContent().trim().isEmpty()) {
                    result.put("valid", false);
                    result.put("error", "Message content cannot be empty");
                }
                if (message.getContent() != null && message.getContent().length() > 5000) {
                    result.put("valid", false);
                    result.put("error", "Message content exceeds 5000 characters");
                }
                break;

            case TYPING:
                if (message.getChatId() == null) {
                    result.put("valid", false);
                    result.put("error", "Chat ID is required for TYPING messages");
                }
                if (message.getSenderId() == null) {
                    result.put("valid", false);
                    result.put("error", "Sender ID is required");
                }
                break;

            case STATUS:
                if (message.getSenderId() == null) {
                    result.put("valid", false);
                    result.put("error", "Sender ID is required");
                }
                if (message.getStatus() == null || 
                    (!message.getStatus().equals("ONLINE") && !message.getStatus().equals("OFFLINE"))) {
                    result.put("valid", false);
                    result.put("error", "Status must be ONLINE or OFFLINE");
                }
                break;

            default:
                break;
        }

        return result;
    }

    /**
     * Get WebSocket connection statistics
     * 
     * @return Statistics map
     */
    public Map<String, Object> getConnectionStats() {
        return Map.of(
            "timestamp", LocalDateTime.now(),
            "description", "WebSocket Connection Statistics"
        );
    }
}
