package com.bloodlink.controller;

import com.bloodlink.entity.*;
import com.bloodlink.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * ChatController - REST API for chat operations
 * 
 * Endpoints:
 * - GET    /api/chats                      - Get user's chats
 * - GET    /api/chats/{id}                 - Get chat details
 * - GET    /api/chats/{id}/messages        - Get chat messages
 * - GET    /api/chats/between              - Get chat between two users
 * - GET    /api/chats/statistics           - Get chat statistics
 * - POST   /api/chats/start                - Start new chat
 * - PUT    /api/chats/{id}/archive         - Archive chat
 * - PUT    /api/chats/{id}/block           - Block chat
 * - DELETE /api/chats/{id}                 - Delete chat
 * 
 * WebSocket Endpoints:
 * - WS     /ws/chat/{chatId}               - Real-time chat
 * 
 * OOP Principle: Encapsulation - Chat logic delegated to service
 * REST Principle: Resource-oriented - Each endpoint represents a chat resource
 * SOLID: Single Responsibility - Only handles HTTP concerns
 */
@Slf4j
@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080", "http://localhost:4200"})
public class ChatController {

    private final ChatService chatService;
    private final MessageService messageService;

    /**
     * Get all active chats for authenticated user
     * 
     * @param userId Authenticated user's ID (from JWT token)
     * @return List of active chats
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getUserChats(@RequestParam Long userId) {
        log.debug("Fetching chats for user: {}", userId);
        
        try {
            List<Chat> chats = chatService.getActiveChatsForUser(userId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "count", chats.size(),
                "data", chats
            ));
            
        } catch (Exception e) {
            log.error("Error fetching user chats", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Get chat by ID
     * 
     * @param id Chat ID
     * @return Chat details
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getChatById(@PathVariable Long id) {
        log.debug("Fetching chat: {}", id);
        
        try {
            Chat chat = chatService.getChatById(id);
            Map<String, Object> summary = chatService.getChatSummary(id);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", Map.of(
                    "chat", chat,
                    "summary", summary
                )
            ));
            
        } catch (Exception e) {
            log.error("Error fetching chat", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Get messages in a chat
     * 
     * Query Parameters:
     * - page: Page number (optional, for pagination)
     * - size: Page size (optional, for pagination)
     * 
     * @param id Chat ID
     * @param page Page number (0-indexed)
     * @param size Page size
     * @return List of messages
     */
    @GetMapping("/{id}/messages")
    public ResponseEntity<?> getChatMessages(@PathVariable Long id,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "50") int size) {
        log.debug("Fetching messages for chat: {} (page: {}, size: {})", id, page, size);
        
        try {
            List<Message> messages = messageService.getChatMessagesPaginated(id, page, size);
            long totalMessages = messageService.getMessageCount(id);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "page", page,
                "pageSize", size,
                "total", totalMessages,
                "count", messages.size(),
                "data", messages
            ));
            
        } catch (Exception e) {
            log.error("Error fetching chat messages", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Get chat between two specific users
     * 
     * Query Parameters:
     * - user1: First user ID
     * - user2: Second user ID
     * 
     * @param user1 First user ID
     * @param user2 Second user ID
     * @return Chat if exists
     */
    @GetMapping("/between")
    public ResponseEntity<?> getChatBetweenUsers(@RequestParam Long user1,
                                                @RequestParam Long user2) {
        log.debug("Getting chat between users: {} and {}", user1, user2);
        
        try {
            Optional<Chat> chat = chatService.getChatBetweenUsers(user1, user2);
            
            if (chat.isPresent()) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "exists", true,
                    "data", chat.get()
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "exists", false,
                    "message", "No chat exists between these users"
                ));
            }
            
        } catch (Exception e) {
            log.error("Error getting chat between users", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Get chat statistics for user
     * 
     * @param userId User ID
     * @return Chat statistics
     */
    @GetMapping("/statistics")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getChatStatistics(@RequestParam Long userId) {
        log.debug("Getting chat statistics for user: {}", userId);
        
        try {
            Map<String, Object> stats = chatService.getChatStatistics(userId);
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
     * Start new chat between two users
     * 
     * Request Body:
     * {
     *   "userId1": 1,
     *   "userId2": 2
     * }
     * 
     * @param request Start chat request
     * @return Created/existing chat
     */
    @PostMapping("/start")
    public ResponseEntity<?> startChat(@RequestBody StartChatRequest request) {
        log.info("Starting chat between users: {} and {}", request.getUserId1(), request.getUserId2());
        
        try {
            Chat chat = chatService.startChat(request.getUserId1(), request.getUserId2());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "message", "Chat started successfully",
                "data", chat
            ));
            
        } catch (Exception e) {
            log.error("Error starting chat", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Archive chat
     * 
     * @param id Chat ID
     * @param userId User requesting archive
     * @return Success response
     */
    @PutMapping("/{id}/archive")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> archiveChat(@PathVariable Long id,
                                        @RequestParam Long userId) {
        log.info("Archiving chat: {} by user: {}", id, userId);
        
        try {
            chatService.archiveChat(id, userId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Chat archived successfully",
                "chatId", id
            ));
            
        } catch (Exception e) {
            log.error("Error archiving chat", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Block chat
     * 
     * @param id Chat ID
     * @param userId User requesting block
     * @return Success response
     */
    @PutMapping("/{id}/block")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> blockChat(@PathVariable Long id,
                                      @RequestParam Long userId) {
        log.info("Blocking chat: {} by user: {}", id, userId);
        
        try {
            chatService.blockChat(id, userId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Chat blocked successfully",
                "chatId", id
            ));
            
        } catch (Exception e) {
            log.error("Error blocking chat", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Delete chat
     * 
     * @param id Chat ID
     * @param userId User requesting deletion
     * @return Success response
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteChat(@PathVariable Long id,
                                       @RequestParam Long userId) {
        log.info("Deleting chat: {} by user: {}", id, userId);
        
        try {
            chatService.deleteChat(id, userId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Chat deleted successfully",
                "chatId", id
            ));
            
        } catch (Exception e) {
            log.error("Error deleting chat", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Check if two users can chat
     * 
     * Query Parameters:
     * - user1: First user ID
     * - user2: Second user ID
     * 
     * @param user1 First user ID
     * @param user2 Second user ID
     * @return Can chat status
     */
    @GetMapping("/can-chat")
    public ResponseEntity<?> canUsersChat(@RequestParam Long user1,
                                         @RequestParam Long user2) {
        log.debug("Checking if users can chat: {} and {}", user1, user2);
        
        try {
            boolean canChat = chatService.canUsersChat(user1, user2);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "canChat", canChat
            ));
            
        } catch (Exception e) {
            log.error("Error checking if users can chat", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // ==================== Helper Classes ====================

    /**
     * Start chat request DTO
     */
    @lombok.Data
    public static class StartChatRequest {
        private Long userId1;
        private Long userId2;

        public Long getUserId1() { return userId1; }
        public Long getUserId2() { return userId2; }
        public void setUserId1(Long userId1) { this.userId1 = userId1; }
        public void setUserId2(Long userId2) { this.userId2 = userId2; }
    }
}
