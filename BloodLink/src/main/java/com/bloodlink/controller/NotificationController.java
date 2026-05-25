package com.bloodlink.controller;

import com.bloodlink.entity.Notification;
import com.bloodlink.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * NotificationController - REST API for notification operations
 * 
 * Endpoints:
 * - GET    /api/notifications              - Get user notifications
 * - GET    /api/notifications/unread       - Get unread notifications
 * - GET    /api/notifications/recent       - Get recent notifications
 * - GET    /api/notifications/count        - Get unread count
 * - GET    /api/notifications/by-type      - Filter by type
 * - POST   /api/notifications/{id}/read    - Mark as read
 * - POST   /api/notifications/read-all     - Mark all as read
 * - DELETE /api/notifications/{id}         - Delete notification
 * 
 * OOP Principle: Encapsulation - Notification logic delegated to service
 * REST Principle: Resource-oriented - Each endpoint represents a notification resource
 * SOLID: Single Responsibility - Only handles HTTP concerns
 */
@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080", "http://localhost:4200"})
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Get all notifications for user
     * 
     * @param userId User ID
     * @return List of all notifications
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getUserNotifications(@RequestParam Long userId) {
        log.debug("Fetching notifications for user: {}", userId);
        
        try {
            List<Notification> notifications = notificationService.getUserNotifications(userId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "count", notifications.size(),
                "data", notifications
            ));
            
        } catch (Exception e) {
            log.error("Error fetching notifications", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Get unread notifications for user
     * 
     * @param userId User ID
     * @return List of unread notifications
     */
    @GetMapping("/unread")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getUnreadNotifications(@RequestParam Long userId) {
        log.debug("Fetching unread notifications for user: {}", userId);
        
        try {
            List<Notification> notifications = notificationService.getUnreadNotifications(userId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "count", notifications.size(),
                "data", notifications
            ));
            
        } catch (Exception e) {
            log.error("Error fetching unread notifications", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Get recent notifications (last 7 days)
     * 
     * @param userId User ID
     * @return List of recent notifications
     */
    @GetMapping("/recent")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getRecentNotifications(@RequestParam Long userId) {
        log.debug("Fetching recent notifications for user: {}", userId);
        
        try {
            List<Notification> notifications = notificationService.getRecentNotifications(userId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "count", notifications.size(),
                "data", notifications
            ));
            
        } catch (Exception e) {
            log.error("Error fetching recent notifications", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Get count of unread notifications for user
     * 
     * @param userId User ID
     * @return Unread notification count
     */
    @GetMapping("/count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getUnreadCount(@RequestParam Long userId) {
        log.debug("Getting unread notification count for user: {}", userId);
        
        try {
            int count = notificationService.countUnreadNotifications(userId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "unreadCount", count
            ));
            
        } catch (Exception e) {
            log.error("Error getting unread count", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Get notifications by type
     * 
     * Query Parameters:
     * - userId: User ID
     * - type: Notification type (WELCOME, BLOOD_REQUEST_RECEIVED, REQUEST_ACCEPTED, etc.)
     * 
     * @param userId User ID
     * @param type Notification type
     * @return List of notifications of that type
     */
    @GetMapping("/by-type")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getNotificationsByType(@RequestParam Long userId,
                                                   @RequestParam String type) {
        log.debug("Fetching notifications of type {} for user: {}", type, userId);
        
        try {
            Notification.NotificationType notificationType = 
                Notification.NotificationType.valueOf(type.toUpperCase());
            
            List<Notification> notifications = notificationService
                .getNotificationsByType(userId, notificationType);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "count", notifications.size(),
                "type", type,
                "data", notifications
            ));
            
        } catch (Exception e) {
            log.error("Error fetching notifications by type", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Mark notification as read
     * 
     * @param id Notification ID
     * @return Success response
     */
    @PostMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        log.debug("Marking notification as read: {}", id);
        
        try {
            notificationService.markAsRead(id);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Notification marked as read",
                "notificationId", id
            ));
            
        } catch (Exception e) {
            log.error("Error marking notification as read", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Mark all notifications as read for user
     * 
     * @param userId User ID
     * @return Success response
     */
    @PostMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> markAllAsRead(@RequestParam Long userId) {
        log.debug("Marking all notifications as read for user: {}", userId);
        
        try {
            notificationService.markAllAsRead(userId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "All notifications marked as read",
                "userId", userId
            ));
            
        } catch (Exception e) {
            log.error("Error marking all as read", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Delete notification
     * 
     * @param id Notification ID
     * @return Success response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotification(@PathVariable Long id) {
        log.debug("Deleting notification: {}", id);
        
        try {
            notificationService.deleteNotification(id);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Notification deleted successfully",
                "notificationId", id
            ));
            
        } catch (Exception e) {
            log.error("Error deleting notification", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Health check endpoint
     * 
     * @return Health status
     */
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        return ResponseEntity.ok(Map.of("status", "NOTIFICATION_SERVICE_RUNNING"));
    }
}
