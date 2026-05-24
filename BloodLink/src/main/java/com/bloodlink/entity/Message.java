package com.bloodlink.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

/**
 * Message Entity
 * Represents individual messages in a chat conversation
 * Demonstrates:
 * - Many-to-One with Chat
 * - Seen/Unseen status tracking
 * - Timestamp management
 */
@Entity
@Table(name = "messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long messageId;

    /**
     * Many-to-One: Many messages belong to one chat
     * Inverting side: Chat has @OneToMany messages
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chat_id", nullable = false)
    private Chat chat;

    /**
     * Many-to-One: Message sent by one user
     * Inverting side: User has @OneToMany sentMessages
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String content;

    @Column(nullable = false)
    private Boolean isSeen = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime seenAt;

    // ============ BUSINESS LOGIC METHODS ============
    /**
     * Mark message as seen
     * ENCAPSULATION: Validates and updates atomically
     */
    public void markAsSeen() {
        this.isSeen = true;
        this.seenAt = LocalDateTime.now();
    }

    /**
     * Get time elapsed since message was sent
     */
    public long getMinutesAgo() {
        return java.time.temporal.ChronoUnit.MINUTES.between(createdAt, LocalDateTime.now());
    }

    /**
     * Get formatted message with sender info
     */
    public String getFormattedMessage() {
        return String.format("[%s] %s: %s", createdAt, sender.getFullName(), content);
    }
}
