package com.bloodlink.service;

import com.bloodlink.entity.*;
import com.bloodlink.exception.BloodLinkExceptions.*;
import com.bloodlink.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ChatService - Handles chat creation and management
 * 
 * Responsibilities:
 * - Create chats between users
 * - Manage chat status (ACTIVE, ARCHIVED, BLOCKED)
 * - Retrieve chat history
 * - Handle chat archival and blocking
 * 
 * OOP Principles Applied:
 * - Encapsulation: Hides chat management logic
 * - Abstraction: Provides high-level chat operations
 * - Single Responsibility: Only handles chat operations
 * 
 * SOLID Principles Applied:
 * - S: Single Responsibility - Only chat logic
 * - D: Dependency Inversion - Depends on repository abstractions
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ChatService {

    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;

    /**
     * Start or get existing chat between two users
     * Business Logic: Finds existing chat or creates new one
     * Encapsulation: Hides chat lookup logic
     * 
     * @param userId1 First user ID
     * @param userId2 Second user ID
     * @return Chat entity
     */
    public Chat startChat(Long userId1, Long userId2) {
        log.info("Starting chat between users: {} and {}", userId1, userId2);
        
        if (userId1.equals(userId2)) {
            throw new ValidationException("Cannot start chat with yourself");
        }
        
        User user1 = userRepository.findById(userId1)
            .orElseThrow(() -> BloodLinkExceptions.userNotFound());
        
        User user2 = userRepository.findById(userId2)
            .orElseThrow(() -> BloodLinkExceptions.userNotFound());
        
        // Check if chat already exists (bidirectional search)
        Optional<Chat> existingChat = chatRepository.findChatBetweenUsers(userId1, userId2);
        
        if (existingChat.isPresent()) {
            log.debug("Chat already exists between users: {} and {}", userId1, userId2);
            Chat chat = existingChat.get();
            
            // If archived or blocked, reactivate it
            if (!chat.getStatus().equals(Chat.ChatStatus.ACTIVE)) {
                chat.setStatus(Chat.ChatStatus.ACTIVE);
                chatRepository.save(chat);
            }
            
            return chat;
        }
        
        // Create new chat
        try {
            Chat chat = new Chat();
            chat.setInitiator(user1);
            chat.setRecipient(user2);
            chat.setStatus(Chat.ChatStatus.ACTIVE);
            chat.setCreatedAt(LocalDateTime.now());
            
            Chat saved = chatRepository.save(chat);
            log.info("Chat created: {}", saved.getChatId());
            
            return saved;
            
        } catch (Exception e) {
            log.error("Error creating chat", e);
            throw new InvalidOperationException("Failed to create chat: " + e.getMessage());
        }
    }

    /**
     * Get chat by ID
     * 
     * @param chatId Chat ID
     * @return Chat entity
     * @throws ResourceNotFoundException if not found
     */
    public Chat getChatById(Long chatId) {
        log.debug("Fetching chat: {}", chatId);
        
        return chatRepository.findById(chatId)
            .orElseThrow(() -> new ResourceNotFoundException("Chat not found"));
    }

    /**
     * Get all active chats for a user
     * 
     * @param userId User ID
     * @return List of active chats
     */
    public List<Chat> getActiveChatsForUser(Long userId) {
        log.debug("Fetching active chats for user: {}", userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> BloodLinkExceptions.userNotFound());
        
        return chatRepository.findActiveChatsForUser(userId);
    }

    /**
     * Get all chats for a user (including archived)
     * 
     * @param userId User ID
     * @return List of all chats
     */
    public List<Chat> getAllChatsForUser(Long userId) {
        log.debug("Fetching all chats for user: {}", userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> BloodLinkExceptions.userNotFound());
        
        List<Chat> chats = new ArrayList<>();
        chats.addAll(chatRepository.findByInitiator(user));
        chats.addAll(chatRepository.findByRecipient(user));
        
        return chats.stream()
            .distinct()
            .sorted(Comparator.comparing(Chat::getUpdatedAt).reversed())
            .collect(Collectors.toList());
    }

    /**
     * Archive chat
     * Business Logic: Keeps chat but marks as ARCHIVED
     * 
     * @param chatId Chat ID
     * @param userId User requesting archive (for validation)
     */
    public void archiveChat(Long chatId, Long userId) {
        log.info("Archiving chat: {}", chatId);
        
        Chat chat = getChatById(chatId);
        
        // Validate user is part of this chat
        if (!chat.getInitiator().getUserId().equals(userId) && 
            !chat.getRecipient().getUserId().equals(userId)) {
            throw new UnauthorizedException("You are not part of this chat");
        }
        
        try {
            chat.setStatus(Chat.ChatStatus.ARCHIVED);
            chat.setUpdatedAt(LocalDateTime.now());
            chatRepository.save(chat);
            
            log.info("Chat archived: {}", chatId);
            
        } catch (Exception e) {
            log.error("Error archiving chat", e);
            throw new InvalidOperationException("Failed to archive chat: " + e.getMessage());
        }
    }

    /**
     * Block user in chat
     * Business Logic: Prevents further messages in this chat
     * 
     * @param chatId Chat ID
     * @param userId User ID doing the blocking
     */
    public void blockChat(Long chatId, Long userId) {
        log.info("Blocking chat: {}", chatId);
        
        Chat chat = getChatById(chatId);
        
        // Validate user is part of this chat
        if (!chat.getInitiator().getUserId().equals(userId) && 
            !chat.getRecipient().getUserId().equals(userId)) {
            throw new UnauthorizedException("You are not part of this chat");
        }
        
        try {
            chat.setStatus(Chat.ChatStatus.BLOCKED);
            chat.setUpdatedAt(LocalDateTime.now());
            chatRepository.save(chat);
            
            log.info("Chat blocked: {}", chatId);
            
        } catch (Exception e) {
            log.error("Error blocking chat", e);
            throw new InvalidOperationException("Failed to block chat: " + e.getMessage());
        }
    }

    /**
     * Get chat summary (last message, message count, etc.)
     * 
     * @param chatId Chat ID
     * @return Map with chat summary
     */
    public Map<String, Object> getChatSummary(Long chatId) {
        log.debug("Getting chat summary for: {}", chatId);
        
        Chat chat = getChatById(chatId);
        Message lastMessage = messageRepository.findLastMessageInChat(chat.getChatId());
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("chatId", chat.getChatId());
        summary.put("initiator", chat.getInitiator().getFullName());
        summary.put("recipient", chat.getRecipient().getFullName());
        summary.put("status", chat.getStatus());
        summary.put("createdAt", chat.getCreatedAt());
        summary.put("updatedAt", chat.getUpdatedAt());
        summary.put("messageCount", messageRepository.countByChatId(chat.getChatId()));
        summary.put("unreadCount", messageRepository.countUnseenMessages(chatId));
        
        if (lastMessage != null) {
            summary.put("lastMessage", lastMessage.getContent());
            summary.put("lastMessageTime", lastMessage.getCreatedAt());
            summary.put("lastMessageSender", lastMessage.getSender().getFullName());
        }
        
        return summary;
    }

    /**
     * Delete chat (soft delete - keep messages)
     * 
     * @param chatId Chat ID
     * @param userId User requesting deletion
     */
    public void deleteChat(Long chatId, Long userId) {
        log.info("Deleting chat: {}", chatId);
        
        Chat chat = getChatById(chatId);
        
        // Validate user is part of chat
        if (!chat.getInitiator().getUserId().equals(userId) && 
            !chat.getRecipient().getUserId().equals(userId)) {
            throw new UnauthorizedException("You are not part of this chat");
        }
        
        try {
            chatRepository.deleteById(chatId);
            log.info("Chat deleted: {}", chatId);
            
        } catch (Exception e) {
            log.error("Error deleting chat", e);
            throw new InvalidOperationException("Failed to delete chat: " + e.getMessage());
        }
    }

    /**
     * Get unread chat count for user
     * 
     * @param userId User ID
     * @return Number of chats with unread messages
     */
    public int getUnreadChatCount(Long userId) {
        log.debug("Getting unread chat count for user: {}", userId);
        
        List<Chat> chats = getActiveChatsForUser(userId);
        
        return (int) chats.stream()
            .filter(chat -> messageRepository.countUnseenMessages(chat.getChatId()) > 0)
            .count();
    }

    /**
     * Check if users can chat
     * Business Logic: Validates both users exist and can communicate
     * 
     * @param userId1 First user ID
     * @param userId2 Second user ID
     * @return true if users can chat
     */
    public boolean canUsersChat(Long userId1, Long userId2) {
        log.debug("Checking if users can chat: {} and {}", userId1, userId2);
        
        if (userId1.equals(userId2)) {
            return false;
        }
        
        Optional<User> user1 = userRepository.findById(userId1);
        Optional<User> user2 = userRepository.findById(userId2);
        
        if (user1.isEmpty() || user2.isEmpty()) {
            return false;
        }
        
        // Check if either user has blocked the other
        Optional<Chat> chat = chatRepository.findChatBetweenUsers(userId1, userId2);
        if (chat.isPresent() && chat.get().getStatus().equals(Chat.ChatStatus.BLOCKED)) {
            return false;
        }
        
        return true;
    }

    /**
     * Get chat between two users
     * 
     * @param userId1 First user ID
     * @param userId2 Second user ID
     * @return Optional containing chat if exists
     */
    public Optional<Chat> getChatBetweenUsers(Long userId1, Long userId2) {
        log.debug("Getting chat between users: {} and {}", userId1, userId2);
        return chatRepository.findChatBetweenUsers(userId1, userId2);
    }

    /**
     * Get chat statistics for user
     * 
     * @param userId User ID
     * @return Map with chat statistics
     */
    public Map<String, Object> getChatStatistics(Long userId) {
        log.debug("Getting chat statistics for user: {}", userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> BloodLinkExceptions.userNotFound());
        
        List<Chat> allChats = getAllChatsForUser(userId);
        List<Chat> activeChats = getActiveChatsForUser(userId);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalChats", allChats.size());
        stats.put("activeChats", activeChats.size());
        stats.put("archivedChats", allChats.stream().filter(c -> c.getStatus().equals(Chat.ChatStatus.ARCHIVED)).count());
        stats.put("blockedChats", allChats.stream().filter(c -> c.getStatus().equals(Chat.ChatStatus.BLOCKED)).count());
        stats.put("unreadChats", getUnreadChatCount(userId));
        stats.put("totalMessages", messageRepository.countByUserId(userId));
        stats.put("unreadMessages", messageRepository.countUnreadMessagesByUser(userId));
        
        return stats;
    }
}
