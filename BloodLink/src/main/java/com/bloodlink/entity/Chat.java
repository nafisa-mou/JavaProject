package com.bloodlink.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Chat Entity
 * Represents a conversation between a patient and a donor
 * Demonstrates:
 * - One-to-Many relationship with Message
 * - Status tracking (Active, Archived, etc.)
 * - Timestamp management
 */
@Entity
@Table(name = "chats", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"initiator_id", "recipient_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long chatId;

    /**
     * Many-to-One: Many chats initiated by one user
     * Inverting side: User has @OneToMany initiatedChats
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "initiator_id", nullable = false)
    private User initiator;

    /**
     * Many-to-One: Many chats with one recipient user
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    /**
     * One-to-Many: One chat has many messages
     * Inverting side: Message has @ManyToOne chat
     */
    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<Message> messages = new HashSet<>();

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ChatStatus status = ChatStatus.ACTIVE;

    public enum ChatStatus {
        ACTIVE,
        ARCHIVED,
        BLOCKED
    }

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime lastMessageAt;

    @Column
    private LocalDateTime archivedAt;

    // ============ BUSINESS LOGIC METHODS ============
    public void archiveChat() {
        this.status = ChatStatus.ARCHIVED;
        this.archivedAt = LocalDateTime.now();
    }

    public void blockChat() {
        this.status = ChatStatus.BLOCKED;
    }

    public boolean isActive() {
        return status == ChatStatus.ACTIVE;
    }

    public int getMessageCount() {
        return messages != null ? messages.size() : 0;
    }

    public Message getLastMessage() {
        return messages.stream()
            .max((m1, m2) -> m1.getCreatedAt().compareTo(m2.getCreatedAt()))
            .orElse(null);
    }
}
