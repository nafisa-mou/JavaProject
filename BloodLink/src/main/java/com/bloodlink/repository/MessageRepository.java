package com.bloodlink.repository;

import com.bloodlink.entity.Chat;
import com.bloodlink.entity.Message;
import com.bloodlink.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository Interface for Message Entity
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    // ============ BASIC QUERIES ============
    Optional<Message> findByMessageId(Long messageId);

    // ============ CHAT MESSAGES ============
    /**
     * Find all messages in a chat
     */
    List<Message> findByChat(Chat chat);

    /**
     * Find messages in chat ordered by creation date
     */
    @Query("SELECT m FROM Message m WHERE m.chat = :chat ORDER BY m.createdAt ASC")
    List<Message> findMessagesByChatOrdered(@Param("chat") Chat chat);

    /**
     * Get last N messages from a chat
     */
    @Query(value = "SELECT * FROM messages WHERE chat_id = :chatId ORDER BY created_at DESC LIMIT :limit", 
           nativeQuery = true)
    List<Message> findLastMessagesInChat(@Param("chatId") Long chatId, @Param("limit") int limit);

    // ============ SEEN STATUS ============
    /**
     * Find unseen messages in a chat
     */
    @Query("SELECT m FROM Message m WHERE m.chat = :chat AND m.isSeen = false")
    List<Message> findUnseenMessages(@Param("chat") Chat chat);

    /**
     * Find unseen messages from specific sender
     */
    @Query("SELECT m FROM Message m WHERE m.chat = :chat AND m.sender = :sender AND m.isSeen = false")
    List<Message> findUnseenMessagesFromSender(@Param("chat") Chat chat, @Param("sender") User sender);

    /**
     * Count unseen messages in a chat
     */
    long countByChatAndIsSeenFalse(Chat chat);

    /**
     * Count unseen messages for a user (as recipient)
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.chat.recipient = :user AND m.isSeen = false")
    long countUnseenMessagesForUser(@Param("user") User user);

    // ============ TIME-BASED QUERIES ============
    /**
     * Find messages created after a specific time
     */
    List<Message> findByCreatedAtAfter(LocalDateTime dateTime);

    /**
     * Find messages between dates
     */
    @Query("SELECT m FROM Message m WHERE m.createdAt BETWEEN :startDate AND :endDate")
    List<Message> findMessagesBetweenDates(@Param("startDate") LocalDateTime startDate, 
                                           @Param("endDate") LocalDateTime endDate);

    /**
     * Find messages from chat created in last N hours
     */
    @Query("SELECT m FROM Message m WHERE m.chat = :chat AND m.createdAt >= DATE_SUB(NOW(), INTERVAL :hours HOUR)")
    List<Message> findRecentMessagesInChat(@Param("chat") Chat chat, @Param("hours") int hours);

    // ============ SENDER QUERIES ============
    /**
     * Find messages sent by specific user
     */
    List<Message> findBySender(User sender);

    /**
     * Find messages sent by user in specific chat
     */
    @Query("SELECT m FROM Message m WHERE m.chat = :chat AND m.sender = :sender")
    List<Message> findMessagesBySenderInChat(@Param("chat") Chat chat, @Param("sender") User sender);

    // ============ STATISTICS ============
    /**
     * Count messages in a chat
     */
    long countByChat(Chat chat);

    /**
     * Count messages sent by user
     */
    long countBySender(User sender);

    /**
     * Get total messages sent by user in a chat
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.chat = :chat AND m.sender = :sender")
    long countMessagesBySenderInChat(@Param("chat") Chat chat, @Param("sender") User sender);
}
