package com.bloodlink.controller;

import com.bloodlink.entity.Message;
import com.bloodlink.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * MessageController - REST API for message operations
 * 
 * Endpoints:
 * - GET    /api/messages/unread            - Get unread messages
 * - GET    /api/messages/unread/count      - Get unread count
 * - GET    /api/messages/{id}              - Get message details
 * - POST   /api/messages                   - Send message
 * - POST   /api/messages/{id}/seen         - Mark as seen
 * - POST   /api/messages/chat/{cId}/seen   - Mark all as seen
 * - DELETE /api/messages/{id}              - Delete message
 * - GET    /api/messages/search            - Search messages
 * - GET    /api/messages/statistics        - Get statistics
 * 
 * OOP Principle: Encapsulation - Message logic delegated to service
 * REST Principle: Resource-oriented - Each endpoint represents a message resource
 * SOLID: Single Responsibility - Only handles HTTP concerns
 */
@Slf4j
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080", "http://localhost:4200"})
public class MessageController {

    private final MessageService messageService;

    /**
     * Get all unread messages for user
     * 
     * @param userId User ID
     * @return List of unread messages
     */
    @GetMapping("/unread")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getUnreadMessages(@RequestParam Long userId) {
        log.debug("Fetching unread messages for user: {}", userId);
        
        try {
            List<Message> messages = messageService.getUnreadMessages(userId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "count", messages.size(),
                "data", messages
            ));
            
        } catch (Exception e) {
            log.error("Error fetching unread messages", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Get count of unread messages for user
     * 
     * @param userId User ID
     * @return Unread message count
     */
    @GetMapping("/unread/count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getUnreadMessageCount(@RequestParam Long userId) {
        log.debug("Getting unread message count for user: {}", userId);
        
        try {
            int count = messageService.getUnreadMessageCount(userId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "unreadCount", count
            ));
            
        } catch (Exception e) {
            log.error("Error getting unread message count", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Get message by ID
     * 
     * @param id Message ID
     * @return Message details
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getMessageById(@PathVariable Long id) {
        log.debug("Fetching message: {}", id);
        
        try {
            Message message = messageService.getMessageById(id);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", message
            ));
            
        } catch (Exception e) {
            log.error("Error fetching message", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Send new message
     * 
     * Request Body:
     * {
     *   "chatId": 1,
     *   "senderId": 2,
     *   "content": "Hello!"
     * }
     * 
     * @param request Send message request
     * @return Created message
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> sendMessage(@RequestBody SendMessageRequest request) {
        log.info("Sending message in chat {}: from user {}", request.getChatId(), request.getSenderId());
        
        try {
            Message message = messageService.sendMessage(
                request.getChatId(),
                request.getSenderId(),
                request.getContent()
            );
            
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "message", "Message sent successfully",
                "data", message
            ));
            
        } catch (Exception e) {
            log.error("Error sending message", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Mark message as seen
     * 
     * @param id Message ID
     * @return Success response
     */
    @PostMapping("/{id}/seen")
    public ResponseEntity<?> markAsSeen(@PathVariable Long id) {
        log.debug("Marking message as seen: {}", id);
        
        try {
            messageService.markAsSeen(id);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Message marked as seen",
                "messageId", id
            ));
            
        } catch (Exception e) {
            log.error("Error marking message as seen", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Mark all messages in chat as seen
     * 
     * Query Parameters:
     * - chatId: Chat ID
     * - userId: User viewing (to exclude sender)
     * 
     * @param chatId Chat ID
     * @param userId User ID
     * @return Success response
     */
    @PostMapping("/chat/{chatId}/seen")
    public ResponseEntity<?> markAllAsSeen(@PathVariable Long chatId,
                                          @RequestParam Long userId) {
        log.debug("Marking all messages as seen in chat: {}", chatId);
        
        try {
            messageService.markAllMessagesAsSeen(chatId, userId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "All messages marked as seen",
                "chatId", chatId
            ));
            
        } catch (Exception e) {
            log.error("Error marking all messages as seen", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Delete message
     * 
     * @param id Message ID
     * @param userId User requesting deletion (must be sender)
     * @return Success response
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteMessage(@PathVariable Long id,
                                          @RequestParam Long userId) {
        log.info("Deleting message: {}", id);
        
        try {
            messageService.deleteMessage(id, userId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Message deleted successfully",
                "messageId", id
            ));
            
        } catch (Exception e) {
            log.error("Error deleting message", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Search messages in a chat
     * 
     * Query Parameters:
     * - chatId: Chat ID
     * - keyword: Search keyword
     * 
     * @param chatId Chat ID
     * @param keyword Search keyword
     * @return Matching messages
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchMessages(@RequestParam Long chatId,
                                           @RequestParam String keyword) {
        log.debug("Searching messages in chat {} with keyword: {}", chatId, keyword);
        
        try {
            List<Message> messages = messageService.searchMessages(chatId, keyword);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "count", messages.size(),
                "keyword", keyword,
                "data", messages
            ));
            
        } catch (Exception e) {
            log.error("Error searching messages", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Get message statistics for user
     * 
     * @param userId User ID
     * @return Message statistics
     */
    @GetMapping("/statistics")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMessageStatistics(@RequestParam Long userId) {
        log.debug("Getting message statistics for user: {}", userId);
        
        try {
            Map<String, Object> stats = messageService.getMessageStatistics(userId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", stats
            ));
            
        } catch (Exception e) {
            log.error("Error getting statistics", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Get unread message count for a specific chat
     * 
     * @param chatId Chat ID
     * @return Unread count
     */
    @GetMapping("/chat/{chatId}/unread")
    public ResponseEntity<?> getUnreadCountInChat(@PathVariable Long chatId) {
        log.debug("Getting unread count for chat: {}", chatId);
        
        try {
            long count = messageService.getUnreadMessageCountInChat(chatId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "chatId", chatId,
                "unreadCount", count
            ));
            
        } catch (Exception e) {
            log.error("Error getting unread count", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Get last message in chat
     * 
     * @param chatId Chat ID
     * @return Last message or null
     */
    @GetMapping("/chat/{chatId}/last")
    public ResponseEntity<?> getLastMessage(@PathVariable Long chatId) {
        log.debug("Getting last message for chat: {}", chatId);
        
        try {
            Message lastMessage = messageService.getLastMessageInChat(chatId);
            
            if (lastMessage != null) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "hasMessage", true,
                    "data", lastMessage
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "hasMessage", false,
                    "message", "No messages in chat"
                ));
            }
            
        } catch (Exception e) {
            log.error("Error getting last message", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Export chat messages
     * 
     * @param chatId Chat ID
     * @return All messages in chat
     */
    @GetMapping("/chat/{chatId}/export")
    public ResponseEntity<?> exportMessages(@PathVariable Long chatId) {
        log.debug("Exporting messages for chat: {}", chatId);
        
        try {
            List<Message> messages = messageService.exportChatMessages(chatId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "count", messages.size(),
                "chatId", chatId,
                "data", messages
            ));
            
        } catch (Exception e) {
            log.error("Error exporting messages", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // ==================== Helper Classes ====================

    /**
     * Send message request DTO
     */
    @lombok.Data
    public static class SendMessageRequest {
        private Long chatId;
        private Long senderId;
        private String content;

        public Long getChatId() { return chatId; }
        public Long getSenderId() { return senderId; }
        public String getContent() { return content; }
        
        public void setChatId(Long chatId) { this.chatId = chatId; }
        public void setSenderId(Long senderId) { this.senderId = senderId; }
        public void setContent(String content) { this.content = content; }
    }
}
