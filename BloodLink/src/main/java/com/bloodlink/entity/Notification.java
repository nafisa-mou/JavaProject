package com.bloodlink.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

/**
 * Notification Entity
 * Represents notifications sent to users
 * Demonstrates:
 * - Many-to-One with User
 * - Different notification types (enum)
 * - Read/Unread status
 */
@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;

    /**
     * Many-to-One: Many notifications belong to one user
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationType type = NotificationType.GENERAL;

    @Column(nullable = false)
    private Boolean isRead = false;

    @Column
    private Long relatedEntityId; // ID of related request, message, etc.

    @Column
    private String relatedEntityType; // Type of related entity

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime readAt;

    /**
     * Enum for Notification Types
     */
    public enum NotificationType {
        BLOOD_REQUEST_RECEIVED("New blood request received"),
        REQUEST_ACCEPTED("Your blood request was accepted"),
        REQUEST_DECLINED("Your blood request was declined"),
        NEW_MESSAGE("You have a new message"),
        DONOR_AVAILABLE("Preferred donor is available"),
        REMINDER("Donation reminder"),
        GENERAL("General notification"),
        ALERT("Important alert"),
        PROFILE_UPDATE("Profile update alert");

        private final String description;

        NotificationType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // ============ BUSINESS LOGIC METHODS ============
    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }

    public long getHoursAgo() {
        return java.time.temporal.ChronoUnit.HOURS.between(createdAt, LocalDateTime.now());
    }

    public boolean isRecent() {
        return getHoursAgo() <= 24;
    }
}
