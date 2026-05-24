package com.bloodlink.controller;

import com.bloodlink.entity.Notification;
import com.bloodlink.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * NotificationController - REST API for notification operations
 * 
 * Endpoints:
 * - GET /api/notifications - Get user notifications
 * - GET /api/notifications/unread - Get unread notifications
 * - GET /api/notifications/recent - Get recent notifications
 * - GET /api/notifications/count - Get unread count
 * - PUT /api/notifications/{id}/read - Mark as read
 * - PUT /api/notifications/read-all - Mark all as read
 * - DELETE /api/notifications/{id} - Delete notification
 * - GET /api/notifications/by-type - Get by type
 * 
 * Security:
 * - Protected: All endpoints require authentication
 */
@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:4200", "http://localhost:8080"})
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Get all notifications for user
     * 
     * @return List of notifications
     * @status 200 OK
     */
    @GetMapping
    @PreAuthorize("hasRole('DONOR') or hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<List<Notification>>> getUserNotifications() {
        log.info("GET /api/notifications");
        
        try {
            // Would extract userId from JWT token in production
            Long userId = 1L; // Placeholder
            
            List<Notification> notifications = notificationService.getUserNotifications(userId);
            return ResponseEntity.ok(ApiResponse.success(notifications, "Notifications retrieved: " + notifications.size()));
            
        } catch (Exception e) {
            log.error("Error fetching notifications", e);
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Failed to fetch notifications"));
        }
    }

    /**
     * Get unread notifications for user
     * 
     * @return List of unread notifications
     * @status 200 OK
     */
    @GetMapping("/unread")
    @PreAuthorize("hasRole('DONOR') or hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<List<Notification>>> getUnreadNotifications() {
        log.info("GET /api/notifications/unread");
        
        try {
            Long userId = 1L; // Placeholder
            
            List<Notification> notifications = notificationService.getUnreadNotifications(userId);
            return ResponseEntity.ok(ApiResponse.success(notifications, "Unread notifications retrieved: " + notifications.size()));
            
        } catch (Exception e) {
            log.error("Error fetching unread notifications", e);
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Failed to fetch unread notifications"));
        }
    }

    /**
     * Get recent notifications (last 7 days)
     * 
     * @return List of recent notifications
     * @status 200 OK
     */
    @GetMapping("/recent")
    @PreAuthorize("hasRole('DONOR') or hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<List<Notification>>> getRecentNotifications() {
        log.info("GET /api/notifications/recent");
        
        try {
            Long userId = 1L; // Placeholder
            
            List<Notification> notifications = notificationService.getRecentNotifications(userId);
            return ResponseEntity.ok(ApiResponse.success(notifications, "Recent notifications retrieved: " + notifications.size()));
            
        } catch (Exception e) {
            log.error("Error fetching recent notifications", e);
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Failed to fetch recent notifications"));
        }
    }

    /**
     * Get count of unread notifications
     * 
     * @return Unread count
     * @status 200 OK
     */
    @GetMapping("/count")
    @PreAuthorize("hasRole('DONOR') or hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> getUnreadCount() {
        log.info("GET /api/notifications/count");
        
        try {
            Long userId = 1L; // Placeholder
            
            int count = notificationService.countUnreadNotifications(userId);
            Map<String, Integer> result = Map.of("unreadCount", count);
            
            return ResponseEntity.ok(ApiResponse.success(result, "Unread count retrieved"));
            
        } catch (Exception e) {
            log.error("Error fetching unread count", e);
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Failed to fetch count"));
        }
    }

    /**
     * Mark notification as read
     * 
     * @param notificationId Notification ID
     * @return ApiResponse
     * @status 200 OK
     */
    @PutMapping("/{notificationId}/read")
    @PreAuthorize("hasRole('DONOR') or hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<String>> markAsRead(@PathVariable Long notificationId) {
        log.info("PUT /api/notifications/{}/read", notificationId);
        
        try {
            notificationService.markAsRead(notificationId);
            return ResponseEntity.ok(ApiResponse.success(null, "Notification marked as read"));
            
        } catch (Exception e) {
            log.error("Error marking notification as read: {}", notificationId, e);
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Failed to mark notification as read"));
        }
    }

    /**
     * Mark all notifications as read
     * 
     * @return ApiResponse
     * @status 200 OK
     */
    @PutMapping("/read-all")
    @PreAuthorize("hasRole('DONOR') or hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<String>> markAllAsRead() {
        log.info("PUT /api/notifications/read-all");
        
        try {
            Long userId = 1L; // Placeholder
            notificationService.markAllAsRead(userId);
            
            return ResponseEntity.ok(ApiResponse.success(null, "All notifications marked as read"));
            
        } catch (Exception e) {
            log.error("Error marking all as read", e);
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Failed to mark notifications as read"));
        }
    }

    /**
     * Delete notification
     * 
     * @param notificationId Notification ID
     * @return ApiResponse
     * @status 200 OK
     */
    @DeleteMapping("/{notificationId}")
    @PreAuthorize("hasRole('DONOR') or hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<String>> deleteNotification(@PathVariable Long notificationId) {
        log.info("DELETE /api/notifications/{}", notificationId);
        
        try {
            notificationService.deleteNotification(notificationId);
            return ResponseEntity.ok(ApiResponse.success(null, "Notification deleted successfully"));
            
        } catch (Exception e) {
            log.error("Error deleting notification: {}", notificationId, e);
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Failed to delete notification"));
        }
    }

    /**
     * Get notifications by type
     * 
     * @param type Notification type
     * @return List of notifications
     * @status 200 OK
     */
    @GetMapping("/by-type/{type}")
    @PreAuthorize("hasRole('DONOR') or hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<List<Notification>>> getNotificationsByType(@PathVariable String type) {
        log.info("GET /api/notifications/by-type/{}", type);
        
        try {
            Long userId = 1L; // Placeholder
            
            // Convert string type to enum
            Notification.NotificationType notificationType = 
                Notification.NotificationType.valueOf(type.toUpperCase());
            
            List<Notification> notifications = 
                notificationService.getNotificationsByType(userId, notificationType);
            
            return ResponseEntity.ok(ApiResponse.success(notifications, "Notifications retrieved: " + notifications.size()));
            
        } catch (Exception e) {
            log.error("Error fetching notifications by type: {}", type, e);
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Failed to fetch notifications: " + e.getMessage()));
        }
    }

    // ==================== Helper Classes ====================

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class ApiResponse<T> {
        private boolean success;
        private T data;
        private String message;

        public static <T> ApiResponse<T> success(T data, String message) {
            return new ApiResponse<>(true, data, message);
        }

        public static <T> ApiResponse<T> error(String message) {
            return new ApiResponse<>(false, null, message);
        }
    }
}
