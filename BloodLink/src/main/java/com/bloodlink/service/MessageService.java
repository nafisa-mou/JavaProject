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
 * MessageService - Handles message creation and management
 * 
 * Responsibilities:
 * - Send messages in chat
 * - Retrieve chat messages
 * - Mark messages as seen
 * - Message search and filtering
 * - Message deletion
 * 
 * OOP Principles Applied:
 * - Encapsulation: Hides message handling logic
 * - Abstraction: Provides high-level message operations
 * - Single Responsibility: Only handles message operations
 * 
 * SOLID Principles Applied:
 * - S: Single Responsibility - Only message logic
 * - D: Dependency Inversion - Depends on repository abstractions
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MessageService {

    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    /**
     * Send message in chat
     * Business Logic: Creates message, validates chat access, sends notifications
     * Encapsulation: Hides message creation logic
     * 
     * @param chatId Chat ID
     * @param senderId Sender user ID
     * @param content Message content
     * @return Created Message
     * @throws InvalidOperationException if message cannot be sent
     */
    public Message sendMessage(Long chatId, Long senderId, String content) {
        log.info("Sending message in chat {}: from user {}", chatId, senderId);
        
        // Validate content
        if (content == null || content.trim().isEmpty()) {
            throw new ValidationException("Message content is required");
        }
        
        if (content.length() > 5000) {
            throw new ValidationException("Message is too long (max 5000 characters)");
        }
        
        Chat chat = chatRepository.findById(chatId)
            .orElseThrow(() -> new ResourceNotFoundException("Chat not found"));
        
        User sender = userRepository.findById(senderId)
            .orElseThrow(() -> BloodLinkExceptions.userNotFound());
        
        // Validate sender is part of chat
        if (!chat.getInitiator().getUserId().equals(senderId) && 
            !chat.getRecipient().getUserId().equals(senderId)) {
            throw new UnauthorizedException("You are not part of this chat");
        }
        
        // Check chat status
        if (chat.getStatus().equals(Chat.ChatStatus.BLOCKED)) {
            throw new InvalidOperationException("This chat is blocked");
        }
        
        try {
            Message message = new Message();
            message.setChat(chat);
            message.setSender(sender);
            message.setContent(content);
            message.setIsSeen(false);
            message.setCreatedAt(LocalDateTime.now());
            
            Message saved = messageRepository.save(message);
            
            // Update chat timestamp
            chat.setUpdatedAt(LocalDateTime.now());
            chatRepository.save(chat);
            
            log.info("Message created: {}", saved.getMessageId());
            
            // Send notification to recipient
            User recipient = chat.getInitiator().getUserId().equals(senderId) ? 
                chat.getRecipient() : chat.getInitiator();
            
            try {
                if (recipient instanceof Donor) {
                    notificationService.notifyDonorOfNewMessage((Donor) recipient, sender, content);
                } else if (recipient instanceof Patient) {
                    notificationService.notifyPatientOfNewMessage((Patient) recipient, sender, content);
                }
            } catch (Exception e) {
                log.warn("Failed to send notification for message", e);
            }
            
            return saved;
            
        } catch (Exception e) {
            log.error("Error sending message", e);
            throw new InvalidOperationException("Failed to send message: " + e.getMessage());
        }
    }

    /**
     * Get message by ID
     * 
     * @param messageId Message ID
     * @return Message entity
     * @throws ResourceNotFoundException if not found
     */
    public Message getMessageById(Long messageId) {
        log.debug("Fetching message: {}", messageId);
        
        return messageRepository.findById(messageId)
            .orElseThrow(() -> new ResourceNotFoundException("Message not found"));
    }

    /**
     * Get all messages in a chat
     * 
     * @param chatId Chat ID
     * @return List of messages (oldest first)
     */
    public List<Message> getChatMessages(Long chatId) {
        log.debug("Fetching messages for chat: {}", chatId);
        
        Chat chat = chatRepository.findById(chatId)
            .orElseThrow(() -> new ResourceNotFoundException("Chat not found"));
        
        return messageRepository.findByChatIdOrderByCreatedAtAsc(chatId);
    }

    /**
     * Get paginated messages in a chat
     * 
     * @param chatId Chat ID
     * @param page Page number (0-indexed)
     * @param pageSize Messages per page
     * @return List of messages for the page
     */
    public List<Message> getChatMessagesPaginated(Long chatId, int page, int pageSize) {
        log.debug("Fetching messages for chat {} - page: {}, size: {}", chatId, page, pageSize);
        
        Chat chat = chatRepository.findById(chatId)
            .orElseThrow(() -> new ResourceNotFoundException("Chat not found"));
        
        int offset = page * pageSize;
        
        // Get total count
        long totalMessages = messageRepository.countByChatId(chatId);
        
        // Get paginated results (last messages first)
        // In production, use Spring Data Page interface
        List<Message> allMessages = messageRepository.findByChatIdOrderByCreatedAtDesc(chatId);
        
        return allMessages.stream()
            .skip(offset)
            .limit(pageSize)
            .sorted(Comparator.comparing(Message::getCreatedAt))
            .collect(Collectors.toList());
    }

    /**
     * Get unread messages for user
     * 
     * @param userId User ID
     * @return List of unread messages
     */
    public List<Message> getUnreadMessages(Long userId) {
        log.debug("Fetching unread messages for user: {}", userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> BloodLinkExceptions.userNotFound());
        
        return messageRepository.findUnseenMessages(userId);
    }

    /**
     * Mark message as seen
     * Business Logic: Updates seen status and timestamp
     * Encapsulation: Hides update logic
     * 
     * @param messageId Message ID
     * @throws ResourceNotFoundException if message not found
     */
    public void markAsSeen(Long messageId) {
        log.debug("Marking message as seen: {}", messageId);
        
        Message message = getMessageById(messageId);
        
        if (message.getIsSeen()) {
            return; // Already seen
        }
        
        try {
            message.setIsSeen(true);
            message.setSeenAt(LocalDateTime.now());
            messageRepository.save(message);
            
            log.debug("Message marked as seen: {}", messageId);
            
        } catch (Exception e) {
            log.error("Error marking message as seen", e);
            throw new InvalidOperationException("Failed to mark message as seen: " + e.getMessage());
        }
    }

    /**
     * Mark all messages in chat as seen
     * 
     * @param chatId Chat ID
     * @param userId User viewing the chat
     */
    public void markAllMessagesAsSeen(Long chatId, Long userId) {
        log.debug("Marking all messages as seen for chat: {} by user: {}", chatId, userId);
        
        try {
            List<Message> unseenMessages = messageRepository.findUnseenMessagesInChat(chatId);
            LocalDateTime now = LocalDateTime.now();
            
            unseenMessages.forEach(message -> {
                if (!message.getSender().getUserId().equals(userId)) {
                    message.setIsSeen(true);
                    message.setSeenAt(now);
                }
            });
            
            messageRepository.saveAll(unseenMessages);
            log.debug("All messages marked as seen for chat: {}", chatId);
            
        } catch (Exception e) {
            log.error("Error marking all messages as seen", e);
        }
    }

    /**
     * Delete message
     * Business Logic: Only allows sender to delete
     * 
     * @param messageId Message ID
     * @param userId User requesting deletion (must be sender)
     * @throws UnauthorizedException if not the sender
     */
    public void deleteMessage(Long messageId, Long userId) {
        log.info("Deleting message: {}", messageId);
        
        Message message = getMessageById(messageId);
        
        // Validate user is sender
        if (!message.getSender().getUserId().equals(userId)) {
            throw new UnauthorizedException("You can only delete your own messages");
        }
        
        // Validate message is recent (within 15 minutes)
        LocalDateTime fifteenMinutesAgo = LocalDateTime.now().minusMinutes(15);
        if (message.getCreatedAt().isBefore(fifteenMinutesAgo)) {
            throw new InvalidOperationException("Messages can only be deleted within 15 minutes of sending");
        }
        
        try {
            messageRepository.deleteById(messageId);
            log.info("Message deleted: {}", messageId);
            
        } catch (Exception e) {
            log.error("Error deleting message", e);
            throw new InvalidOperationException("Failed to delete message: " + e.getMessage());
        }
    }

    /**
     * Search messages in chat
     * Business Logic: Full-text search within chat
     * 
     * @param chatId Chat ID
     * @param keyword Search keyword
     * @return List of matching messages
     */
    public List<Message> searchMessages(Long chatId, String keyword) {
        log.debug("Searching messages in chat {} with keyword: {}", chatId, keyword);
        
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new ValidationException("Search keyword is required");
        }
        
        try {
            List<Message> allMessages = messageRepository.findByChatIdOrderByCreatedAtDesc(chatId);
            
            String searchTerm = keyword.toLowerCase();
            return allMessages.stream()
                .filter(msg -> msg.getContent().toLowerCase().contains(searchTerm))
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            log.error("Error searching messages", e);
            throw new InvalidOperationException("Failed to search messages: " + e.getMessage());
        }
    }

    /**
     * Get message count for chat
     * 
     * @param chatId Chat ID
     * @return Number of messages
     */
    public long getMessageCount(Long chatId) {
        log.debug("Getting message count for chat: {}", chatId);
        return messageRepository.countByChatId(chatId);
    }

    /**
     * Get unread message count for user
     * 
     * @param userId User ID
     * @return Number of unread messages
     */
    public int getUnreadMessageCount(Long userId) {
        log.debug("Getting unread message count for user: {}", userId);
        return messageRepository.countUnreadMessagesByUser(userId);
    }

    /**
     * Get unread message count for specific chat
     * 
     * @param chatId Chat ID
     * @return Number of unread messages in this chat
     */
    public long getUnreadMessageCountInChat(Long chatId) {
        log.debug("Getting unread message count for chat: {}", chatId);
        return messageRepository.countUnseenMessages(chatId);
    }

    /**
     * Get message statistics for user
     * 
     * @param userId User ID
     * @return Map with message statistics
     */
    public Map<String, Object> getMessageStatistics(Long userId) {
        log.debug("Getting message statistics for user: {}", userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> BloodLinkExceptions.userNotFound());
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalMessagesSent", messageRepository.countByUserId(userId));
        stats.put("totalMessagesReceived", messageRepository.countReceivedByUserId(userId));
        stats.put("unreadMessages", getUnreadMessageCount(userId));
        stats.put("lastMessageTime", messageRepository.getLastMessageTime(userId));
        
        return stats;
    }

    /**
     * Get last message in chat
     * 
     * @param chatId Chat ID
     * @return Last message or empty optional
     */
    public Message getLastMessageInChat(Long chatId) {
        log.debug("Getting last message in chat: {}", chatId);
        return messageRepository.findLastMessageInChat(chatId);
    }

    /**
     * Export chat messages
     * 
     * @param chatId Chat ID
     * @return List of all messages in chat (for export)
     */
    public List<Message> exportChatMessages(Long chatId) {
        log.info("Exporting messages for chat: {}", chatId);
        
        Chat chat = chatRepository.findById(chatId)
            .orElseThrow(() -> new ResourceNotFoundException("Chat not found"));
        
        return messageRepository.findByChatIdOrderByCreatedAtAsc(chatId);
    }
}
