package com.bloodlink.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * WebSocketMessage - DTO for WebSocket/STOMP message protocol
 * 
 * Used for:
 * - Chat messages between donors and patients
 * - Typing indicators
 * - Online status updates
 * - System notifications
 * 
 * Message Types:
 * - CHAT: Regular chat message
 * - TYPING: User is typing
 * - STATUS: Online/offline status
 * - NOTIFICATION: System notification
 * - NOTIFICATION_READ: Acknowledgement of notification
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketMessage {

    /**
     * Message type enumeration
     */
    public enum MessageType {
        CHAT,               // Chat message
        TYPING,             // Typing indicator
        STATUS,             // Online/offline status
        NOTIFICATION,       // System notification
        NOTIFICATION_READ,  // Notification read acknowledgement
        DELIVERY_CONFIRM    // Message delivered acknowledgement
    }

    /**
     * Message ID (UUID) - uniquely identifies message for acknowledgement
     */
    private String messageId;

    /**
     * Type of message
     */
    private MessageType type;

    /**
     * Chat ID - which chat this message belongs to
     */
    @JsonProperty("chatId")
    private Long chatId;

    /**
     * Sender ID - who sent the message
     */
    @JsonProperty("senderId")
    private Long senderId;

    /**
     * Sender name - display name of sender
     */
    @JsonProperty("senderName")
    private String senderName;

    /**
     * Recipient ID - who receives the message (for direct messages)
     */
    @JsonProperty("recipientId")
    private Long recipientId;

    /**
     * Recipient name - display name of recipient
     */
    @JsonProperty("recipientName")
    private String recipientName;

    /**
     * Message content - actual message text or data
     */
    private String content;

    /**
     * Is user typing (for TYPING message type)
     */
    @JsonProperty("isTyping")
    private Boolean isTyping;

    /**
     * User status (for STATUS message type): "ONLINE" or "OFFLINE"
     */
    private String status;

    /**
     * Timestamp when message was created
     */
    private LocalDateTime timestamp;

    /**
     * Timestamp when message was delivered to recipient
     */
    private LocalDateTime deliveredAt;

    /**
     * Timestamp when message was read by recipient
     */
    private LocalDateTime readAt;

    /**
     * Additional data payload (JSON)
     */
    private Object payload;

    /**
     * Error message if any error occurred
     */
    private String error;

    /**
     * Acknowledgement flag
     */
    @JsonProperty("isAck")
    private Boolean isAck;

    // ==================== Builder Methods ====================

    /**
     * Create a chat message
     */
    public static WebSocketMessage createChatMessage(Long chatId, Long senderId, String senderName,
                                                     Long recipientId, String content) {
        WebSocketMessage msg = new WebSocketMessage();
        msg.setMessageId(java.util.UUID.randomUUID().toString());
        msg.setType(MessageType.CHAT);
        msg.setChatId(chatId);
        msg.setSenderId(senderId);
        msg.setSenderName(senderName);
        msg.setRecipientId(recipientId);
        msg.setContent(content);
        msg.setTimestamp(LocalDateTime.now());
        return msg;
    }

    /**
     * Create a typing indicator
     */
    public static WebSocketMessage createTypingIndicator(Long chatId, Long userId, 
                                                         String userName, Boolean isTyping) {
        WebSocketMessage msg = new WebSocketMessage();
        msg.setType(MessageType.TYPING);
        msg.setChatId(chatId);
        msg.setSenderId(userId);
        msg.setSenderName(userName);
        msg.setIsTyping(isTyping);
        msg.setTimestamp(LocalDateTime.now());
        return msg;
    }

    /**
     * Create status update (online/offline)
     */
    public static WebSocketMessage createStatusUpdate(Long userId, String userName, String status) {
        WebSocketMessage msg = new WebSocketMessage();
        msg.setType(MessageType.STATUS);
        msg.setSenderId(userId);
        msg.setSenderName(userName);
        msg.setStatus(status); // "ONLINE" or "OFFLINE"
        msg.setTimestamp(LocalDateTime.now());
        return msg;
    }

    /**
     * Create notification
     */
    public static WebSocketMessage createNotification(Long userId, String title, String content,
                                                      Object payload) {
        WebSocketMessage msg = new WebSocketMessage();
        msg.setMessageId(java.util.UUID.randomUUID().toString());
        msg.setType(MessageType.NOTIFICATION);
        msg.setRecipientId(userId);
        msg.setContent(content);
        msg.setSenderName(title);
        msg.setPayload(payload);
        msg.setTimestamp(LocalDateTime.now());
        return msg;
    }

    /**
     * Create delivery confirmation
     */
    public static WebSocketMessage createDeliveryConfirm(String messageId, Long chatId, Long recipientId) {
        WebSocketMessage msg = new WebSocketMessage();
        msg.setType(MessageType.DELIVERY_CONFIRM);
        msg.setMessageId(messageId);
        msg.setChatId(chatId);
        msg.setRecipientId(recipientId);
        msg.setIsAck(true);
        msg.setDeliveredAt(LocalDateTime.now());
        return msg;
    }

    /**
     * Create read confirmation
     */
    public static WebSocketMessage createReadConfirm(String messageId, Long chatId, Long recipientId) {
        WebSocketMessage msg = new WebSocketMessage();
        msg.setType(MessageType.NOTIFICATION_READ);
        msg.setMessageId(messageId);
        msg.setChatId(chatId);
        msg.setRecipientId(recipientId);
        msg.setIsAck(true);
        msg.setReadAt(LocalDateTime.now());
        return msg;
    }

    /**
     * Create error message
     */
    public static WebSocketMessage createError(String messageId, String error) {
        WebSocketMessage msg = new WebSocketMessage();
        msg.setMessageId(messageId);
        msg.setError(error);
        msg.setTimestamp(LocalDateTime.now());
        return msg;
    }

    // Getters & Setters
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public MessageType getType() { return type; }
    public void setType(MessageType type) { this.type = type; }

    public Long getChatId() { return chatId; }
    public void setChatId(Long chatId) { this.chatId = chatId; }

    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public Long getRecipientId() { return recipientId; }
    public void setRecipientId(Long recipientId) { this.recipientId = recipientId; }

    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Boolean getIsTyping() { return isTyping; }
    public void setIsTyping(Boolean isTyping) { this.isTyping = isTyping; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public LocalDateTime getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(LocalDateTime deliveredAt) { this.deliveredAt = deliveredAt; }

    public LocalDateTime getReadAt() { return readAt; }
    public void setReadAt(LocalDateTime readAt) { this.readAt = readAt; }

    public Object getPayload() { return payload; }
    public void setPayload(Object payload) { this.payload = payload; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public Boolean getIsAck() { return isAck; }
    public void setIsAck(Boolean isAck) { this.isAck = isAck; }
}
