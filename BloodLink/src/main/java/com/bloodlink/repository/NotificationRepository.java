package com.bloodlink.repository;

import com.bloodlink.entity.Notification;
import com.bloodlink.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository Interface for Notification Entity
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Optional<Notification> findByNotificationId(Long notificationId);

    // ============ USER NOTIFICATIONS ============
    /**
     * Find all notifications for a user
     */
    List<Notification> findByUser(User user);

    /**
     * Find unread notifications for a user
     */
    List<Notification> findByUserAndIsReadFalse(User user);

    /**
     * Find notifications for user ordered by creation date
     */
    @Query("SELECT n FROM Notification n WHERE n.user = :user ORDER BY n.createdAt DESC")
    List<Notification> findNotificationsByUserOrdered(@Param("user") User user);

    /**
     * Count unread notifications for user
     */
    long countByUserAndIsReadFalse(User user);

    // ============ NOTIFICATION TYPE QUERIES ============
    /**
     * Find notifications by type
     */
    List<Notification> findByType(Notification.NotificationType type);

    /**
     * Find unread notifications of specific type for user
     */
    @Query("SELECT n FROM Notification n WHERE n.user = :user AND n.type = :type AND n.isRead = false")
    List<Notification> findUnreadNotificationsByType(@Param("user") User user, 
                                                      @Param("type") Notification.NotificationType type);

    // ============ TIME-BASED QUERIES ============
    /**
     * Find recent notifications (last 7 days)
     */
    @Query("SELECT n FROM Notification n WHERE n.createdAt >= DATE_SUB(NOW(), INTERVAL 7 DAY) ORDER BY n.createdAt DESC")
    List<Notification> findRecentNotifications();

    /**
     * Find user's recent notifications
     */
    @Query("SELECT n FROM Notification n WHERE n.user = :user AND n.createdAt >= DATE_SUB(NOW(), INTERVAL 7 DAY) " +
           "ORDER BY n.createdAt DESC")
    List<Notification> findUserRecentNotifications(@Param("user") User user);

    /**
     * Find notifications between dates
     */
    @Query("SELECT n FROM Notification n WHERE n.createdAt BETWEEN :startDate AND :endDate")
    List<Notification> findNotificationsBetweenDates(@Param("startDate") LocalDateTime startDate, 
                                                      @Param("endDate") LocalDateTime endDate);

    // ============ RELATED ENTITY QUERIES ============
    /**
     * Find notifications for specific entity
     */
    List<Notification> findByRelatedEntityIdAndRelatedEntityType(Long relatedEntityId, String relatedEntityType);

    /**
     * Find notifications for specific blood request
     */
    @Query("SELECT n FROM Notification n WHERE n.relatedEntityId = :requestId AND n.relatedEntityType = 'BLOOD_REQUEST'")
    List<Notification> findNotificationsByBloodRequest(@Param("requestId") Long requestId);
}
