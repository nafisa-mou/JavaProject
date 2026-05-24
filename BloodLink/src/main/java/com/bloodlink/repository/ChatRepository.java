package com.bloodlink.repository;

import com.bloodlink.entity.Chat;
import com.bloodlink.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository Interface for Chat Entity
 */
@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {

    // ============ BASIC QUERIES ============
    Optional<Chat> findByChatId(Long chatId);

    // ============ CHAT SEARCHES ============
    /**
     * Find chat between two users
     */
    @Query("SELECT c FROM Chat c WHERE (c.initiator = :user1 AND c.recipient = :user2) " +
           "OR (c.initiator = :user2 AND c.recipient = :user1)")
    Optional<Chat> findChatBetweenUsers(@Param("user1") User user1, @Param("user2") User user2);

    /**
     * Find all chats for a user (as initiator or recipient)
     */
    @Query("SELECT c FROM Chat c WHERE c.initiator = :user OR c.recipient = :user")
    List<Chat> findChatsByUser(@Param("user") User user);

    /**
     * Find active chats for a user
     */
    @Query("SELECT c FROM Chat c WHERE (c.initiator = :user OR c.recipient = :user) " +
           "AND c.status = 'ACTIVE' ORDER BY c.lastMessageAt DESC")
    List<Chat> findActiveChatsForUser(@Param("user") User user);

    /**
     * Find archived chats
     */
    List<Chat> findByStatusOrderByLastMessageAtDesc(Chat.ChatStatus status);

    // ============ RECENT CHATS ============
    /**
     * Find recent chats (last 30 days)
     */
    @Query("SELECT c FROM Chat c WHERE c.lastMessageAt >= DATE_SUB(NOW(), INTERVAL 30 DAY) " +
           "ORDER BY c.lastMessageAt DESC")
    List<Chat> findRecentChats();

    /**
     * Find chats created between dates
     */
    @Query("SELECT c FROM Chat c WHERE c.createdAt BETWEEN :startDate AND :endDate")
    List<Chat> findChatsBetweenDates(@Param("startDate") LocalDateTime startDate, 
                                     @Param("endDate") LocalDateTime endDate);

    // ============ STATISTICS ============
    /**
     * Count total chats for user
     */
    @Query("SELECT COUNT(c) FROM Chat c WHERE c.initiator = :user OR c.recipient = :user")
    long countChatsByUser(@Param("user") User user);

    /**
     * Count active chats
     */
    long countByStatus(Chat.ChatStatus status);
}
