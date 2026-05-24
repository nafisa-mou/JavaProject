package com.bloodlink.controller;

import com.bloodlink.entity.Chat;
import com.bloodlink.entity.Message;
import com.bloodlink.service.ChatService;
import com.bloodlink.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * ChatController - REST API for chat operations
 * 
 * Endpoints:
 * - POST /api/chats/start - Start new chat
 * - GET /api/chats - Get user's chats
 * - GET /api/chats/{id} - Get chat details
 * - GET /api/chats/{id}/messages - Get chat messages
 * - POST /api/chats/{id}/messages - Send message
 * - PUT /api/chats/{id}/archive - Archive chat
 * - PUT /api/chats/{id}/block - Block chat
 * - DELETE /api/chats/{id} - Delete chat
 * - GET /api/chats/stats - Get chat statistics
 * - PUT /api/chats/{id}/messages/{msgId}/seen - Mark message seen
 * 
 * Security:
 * - Protected: All endpoints require authentication
 */
@Slf4j
@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:4200", "http://localhost:8080"})
public class ChatController {

    private final ChatService chatService;
    private final MessageService messageService;

    /**
     * Start new chat between two users
     * 
     * @param otherUserId ID of other user
     * @return Chat entity
     * @status 201 CREATED
     * @status 400 BAD_REQUEST
     */
    @PostMapping("/start")
    @PreAuthorize("hasRole('DONOR') or hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<Chat>> startChat(@RequestParam Long otherUserId) {
        log.info("POST /api/chats/start?otherUserId={}", otherUserId);
        
        try {
            // Would extract userId from JWT token in production
            Long userId = 1L; // Placeholder
            
            Chat chat = chatService.startChat(userId, otherUserId);
            return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(chat, "Chat started successfully"));
            
        } catch (Exception e) {
            log.error("Error starting chat", e);
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Failed to start chat: " + e.getMessage()));
        }
    }

    /**
     * Get all chats for user
     * 
     * @return List of chats
     * @status 200 OK
     */
    @GetMapping
    @PreAuthorize("hasRole('DONOR') or hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<List<Chat>>> getUserChats() {
        log.info("GET /api/chats");
        
        try {
            // Would extract userId from JWT token in production
            Long userId = 1L; // Placeholder
            
            List<Chat> chats = chatService.getActiveChatsForUser(userId);
            return ResponseEntity.ok(ApiResponse.success(chats, "Chats retrieved: " + chats.size()));
            
        } catch (Exception e) {
            log.error("Error fetching chats", e);
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Failed to fetch chats"));
        }
    }

    /**
     * Get chat by ID
     * 
     * @param chatId Chat ID
     * @return Chat details
     * @status 200 OK
     * @status 404 NOT_FOUND
     */
    @GetMapping("/{chatId}")
    @PreAuthorize("hasRole('DONOR') or hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getChatDetails(@PathVariable Long chatId) {
        log.info("GET /api/chats/{}", chatId);
        
        try {
            Chat chat = chatService.getChatById(chatId);
            Map<String, Object> summary = chatService.getChatSummary(chatId);
            
            return ResponseEntity.ok(ApiResponse.success(summary, "Chat details retrieved"));
            
        } catch (Exception e) {
            log.error("Chat not found: {}", chatId, e);
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Chat not found"));
        }
    }

    /**
     * Get messages in chat
     * 
     * @param chatId Chat ID
     * @param page Page number (0-indexed)
     * @param pageSize Messages per page
     * @return List of messages
     * @status 200 OK
     * @status 404 NOT_FOUND
     */
    @GetMapping("/{chatId}/messages")
    @PreAuthorize("hasRole('DONOR') or hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<List<Message>>> getChatMessages(
            @PathVariable Long chatId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int pageSize) {
        log.info("GET /api/chats/{}/messages?page={}&pageSize={}", chatId, page, pageSize);
        
        try {
            List<Message> messages = messageService.getChatMessagesPaginated(chatId, page, pageSize);
            
            // Mark all messages as seen
            messageService.markAllMessagesAsSeen(chatId, 1L); // Would use actual userId
            
            return ResponseEntity.ok(ApiResponse.success(messages, "Messages retrieved: " + messages.size()));
            
        } catch (Exception e) {
            log.error("Error fetching messages: {}", chatId, e);
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Failed to fetch messages"));
        }
    }

    /**
     * Send message in chat
     * 
     * @param chatId Chat ID
     * @param request Message content
     * @return Sent Message
     * @status 201 CREATED
     * @status 400 BAD_REQUEST
     */
    @PostMapping("/{chatId}/messages")
    @PreAuthorize("hasRole('DONOR') or hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<Message>> sendMessage(
            @PathVariable Long chatId,
            @RequestBody Map<String, String> request) {
        log.info("POST /api/chats/{}/messages", chatId);
        
        try {
            String content = request.get("content");
            // Would extract userId from JWT token in production
            Long senderId = 1L; // Placeholder
            
            Message message = messageService.sendMessage(chatId, senderId, content);
            
            return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(message, "Message sent successfully"));
            
        } catch (Exception e) {
            log.error("Error sending message", e);
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Failed to send message: " + e.getMessage()));
        }
    }

    /**
     * Archive chat
     * 
     * @param chatId Chat ID
     * @return ApiResponse
     * @status 200 OK
     */
    @PutMapping("/{chatId}/archive")
    @PreAuthorize("hasRole('DONOR') or hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<String>> archiveChat(@PathVariable Long chatId) {
        log.info("PUT /api/chats/{}/archive", chatId);
        
        try {
            Long userId = 1L; // Placeholder - would extract from JWT
            chatService.archiveChat(chatId, userId);
            
            return ResponseEntity.ok(ApiResponse.success(null, "Chat archived successfully"));
            
        } catch (Exception e) {
            log.error("Error archiving chat: {}", chatId, e);
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Failed to archive chat"));
        }
    }

    /**
     * Block chat
     * 
     * @param chatId Chat ID
     * @return ApiResponse
     * @status 200 OK
     */
    @PutMapping("/{chatId}/block")
    @PreAuthorize("hasRole('DONOR') or hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<String>> blockChat(@PathVariable Long chatId) {
        log.info("PUT /api/chats/{}/block", chatId);
        
        try {
            Long userId = 1L; // Placeholder - would extract from JWT
            chatService.blockChat(chatId, userId);
            
            return ResponseEntity.ok(ApiResponse.success(null, "Chat blocked successfully"));
            
        } catch (Exception e) {
            log.error("Error blocking chat: {}", chatId, e);
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Failed to block chat"));
        }
    }

    /**
     * Delete chat
     * 
     * @param chatId Chat ID
     * @return ApiResponse
     * @status 200 OK
     */
    @DeleteMapping("/{chatId}")
    @PreAuthorize("hasRole('DONOR') or hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<String>> deleteChat(@PathVariable Long chatId) {
        log.info("DELETE /api/chats/{}", chatId);
        
        try {
            Long userId = 1L; // Placeholder - would extract from JWT
            chatService.deleteChat(chatId, userId);
            
            return ResponseEntity.ok(ApiResponse.success(null, "Chat deleted successfully"));
            
        } catch (Exception e) {
            log.error("Error deleting chat: {}", chatId, e);
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Failed to delete chat"));
        }
    }

    /**
     * Mark message as seen
     * 
     * @param chatId Chat ID
     * @param messageId Message ID
     * @return ApiResponse
     * @status 200 OK
     */
    @PutMapping("/{chatId}/messages/{messageId}/seen")
    @PreAuthorize("hasRole('DONOR') or hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<String>> markMessageAsSeen(
            @PathVariable Long chatId,
            @PathVariable Long messageId) {
        log.info("PUT /api/chats/{}/messages/{}/seen", chatId, messageId);
        
        try {
            messageService.markAsSeen(messageId);
            
            return ResponseEntity.ok(ApiResponse.success(null, "Message marked as seen"));
            
        } catch (Exception e) {
            log.error("Error marking message as seen: {}", messageId, e);
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Failed to mark message as seen"));
        }
    }

    /**
     * Delete message
     * 
     * @param chatId Chat ID
     * @param messageId Message ID
     * @return ApiResponse
     * @status 200 OK
     */
    @DeleteMapping("/{chatId}/messages/{messageId}")
    @PreAuthorize("hasRole('DONOR') or hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<String>> deleteMessage(
            @PathVariable Long chatId,
            @PathVariable Long messageId) {
        log.info("DELETE /api/chats/{}/messages/{}", chatId, messageId);
        
        try {
            Long userId = 1L; // Placeholder - would extract from JWT
            messageService.deleteMessage(messageId, userId);
            
            return ResponseEntity.ok(ApiResponse.success(null, "Message deleted successfully"));
            
        } catch (Exception e) {
            log.error("Error deleting message: {}", messageId, e);
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Failed to delete message: " + e.getMessage()));
        }
    }

    /**
     * Search messages in chat
     * 
     * @param chatId Chat ID
     * @param keyword Search keyword
     * @return List of matching messages
     * @status 200 OK
     */
    @GetMapping("/{chatId}/search")
    @PreAuthorize("hasRole('DONOR') or hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<List<Message>>> searchMessages(
            @PathVariable Long chatId,
            @RequestParam String keyword) {
        log.info("GET /api/chats/{}/search?keyword={}", chatId, keyword);
        
        try {
            List<Message> results = messageService.searchMessages(chatId, keyword);
            return ResponseEntity.ok(ApiResponse.success(results, "Search results: " + results.size()));
            
        } catch (Exception e) {
            log.error("Error searching messages", e);
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Search failed: " + e.getMessage()));
        }
    }

    /**
     * Get chat statistics for user
     * 
     * @return Chat statistics map
     * @status 200 OK
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('DONOR') or hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getChatStatistics() {
        log.info("GET /api/chats/stats");
        
        try {
            Long userId = 1L; // Placeholder - would extract from JWT
            Map<String, Object> stats = chatService.getChatStatistics(userId);
            
            return ResponseEntity.ok(ApiResponse.success(stats, "Chat statistics retrieved"));
            
        } catch (Exception e) {
            log.error("Error fetching statistics", e);
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Failed to fetch statistics"));
        }
    }

    // ==================== Helper Classes ====================

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class ApiResponse<T> {
        private boolean success;
        private T data;
        private String message;

        public static <T> ApiResponse<T> success(T data, String message) {
            return new ApiResponse<>(true, data, message);
        }

        public static <T> ApiResponse<T> error(String message) {
            return new ApiResponse<>(false, null, message);
        }
    }
}
