package com.bloodlink.service;

import com.bloodlink.entity.*;
import com.bloodlink.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * NotificationService - Handles all notification operations
 * 
 * Responsibilities:
 * - Create and send notifications
 * - Mark notifications as read
 * - Retrieve user notifications
 * - Handle different notification types
 * - Notify donors of blood requests
 * - Notify patients of donor acceptances
 * 
 * OOP Principles Applied:
 * - Encapsulation: Hides notification creation logic
 * - Abstraction: Provides high-level notification operations
 * - Single Responsibility: Only handles notifications
 * 
 * SOLID Principles Applied:
 * - S: Single Responsibility - Only notification logic
 * - D: Dependency Inversion - Depends on NotificationRepository interface
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;

    /**
     * Send welcome notification to new user
     * Encapsulation: Hides notification creation details
     * 
     * @param user User who registered
     */
    public void sendWelcomeNotification(User user) {
        try {
            Notification notification = new Notification();
            notification.setUser(user);
            notification.setType(Notification.NotificationType.WELCOME);
            notification.setTitle("Welcome to BloodLink!");
            notification.setMessage("Thank you for joining BloodLink. Help save lives by donating blood.");
            notification.setIsRead(false);
            notification.setCreatedAt(LocalDateTime.now());
            
            notificationRepository.save(notification);
            log.info("Welcome notification sent to user: {}", user.getUserId());
            
        } catch (Exception e) {
            log.error("Error sending welcome notification", e);
        }
    }

    /**
     * Notify donor of blood request
     * Polymorphism: Works with any User, but specifically targets Donors
     * 
     * @param donor Donor to notify
     * @param bloodRequest Blood request details
     */
    public void notifyDonorOfRequest(Donor donor, BloodRequest bloodRequest) {
        try {
            Notification notification = new Notification();
            notification.setUser(donor);
            notification.setType(Notification.NotificationType.BLOOD_REQUEST_RECEIVED);
            notification.setTitle("New Blood Request");
            notification.setMessage(String.format(
                "Patient in %s needs %s blood. They are %d km away.",
                bloodRequest.getPatient().getCity(),
                bloodRequest.getPatient().getRequiredBloodGroup(),
                calculateDistance(donor, bloodRequest.getPatient())
            ));
            notification.setRelatedEntityId(bloodRequest.getRequestId());
            notification.setIsRead(false);
            notification.setCreatedAt(LocalDateTime.now());
            
            notificationRepository.save(notification);
            log.info("Blood request notification sent to donor: {}", donor.getUserId());
            
        } catch (Exception e) {
            log.error("Error notifying donor of request", e);
        }
    }

    /**
     * Notify patient that donor accepted request
     * 
     * @param patient Patient to notify
     * @param donor Donor who accepted
     * @param bloodRequest Blood request
     */
    public void notifyPatientOfAcceptance(Patient patient, Donor donor, BloodRequest bloodRequest) {
        try {
            Notification notification = new Notification();
            notification.setUser(patient);
            notification.setType(Notification.NotificationType.REQUEST_ACCEPTED);
            notification.setTitle("Blood Request Accepted!");
            notification.setMessage(String.format(
                "%s has accepted your blood request. Contact them to arrange collection.",
                donor.getFullName()
            ));
            notification.setRelatedEntityId(bloodRequest.getRequestId());
            notification.setIsRead(false);
            notification.setCreatedAt(LocalDateTime.now());
            
            notificationRepository.save(notification);
            log.info("Acceptance notification sent to patient: {}", patient.getUserId());
            
        } catch (Exception e) {
            log.error("Error notifying patient of acceptance", e);
        }
    }

    /**
     * Notify patient that donor declined request
     * 
     * @param patient Patient to notify
     * @param donor Donor who declined
     * @param bloodRequest Blood request
     */
    public void notifyPatientOfDecline(Patient patient, Donor donor, BloodRequest bloodRequest) {
        try {
            Notification notification = new Notification();
            notification.setUser(patient);
            notification.setType(Notification.NotificationType.REQUEST_DECLINED);
            notification.setTitle("Blood Request Declined");
            notification.setMessage(String.format(
                "%s is unable to fulfill your blood request at this time.",
                donor.getFullName()
            ));
            notification.setRelatedEntityId(bloodRequest.getRequestId());
            notification.setIsRead(false);
            notification.setCreatedAt(LocalDateTime.now());
            
            notificationRepository.save(notification);
            log.info("Decline notification sent to patient: {}", patient.getUserId());
            
        } catch (Exception e) {
            log.error("Error notifying patient of decline", e);
        }
    }

    /**
     * Notify patient of new available donor
     * 
     * @param patient Patient to notify
     * @param donor Donor who became available
     */
    public void notifyPatientOfAvailableDonor(Patient patient, Donor donor) {
        try {
            Notification notification = new Notification();
            notification.setUser(patient);
            notification.setType(Notification.NotificationType.DONOR_AVAILABLE);
            notification.setTitle("Donor Available!");
            notification.setMessage(String.format(
                "A %s blood donor (%s) is available in your area.",
                donor.getBloodGroup(),
                donor.getCity()
            ));
            notification.setRelatedEntityId(donor.getUserId());
            notification.setIsRead(false);
            notification.setCreatedAt(LocalDateTime.now());
            
            notificationRepository.save(notification);
            log.info("Available donor notification sent to patient: {}", patient.getUserId());
            
        } catch (Exception e) {
            log.error("Error notifying patient of available donor", e);
        }
    }

    /**
     * Notify donor of new chat message
     * 
     * @param donor Donor to notify
     * @param sender User who sent the message
     * @param message Message content
     */
    public void notifyDonorOfNewMessage(Donor donor, User sender, String message) {
        try {
            Notification notification = new Notification();
            notification.setUser(donor);
            notification.setType(Notification.NotificationType.NEW_MESSAGE);
            notification.setTitle("New Message");
            notification.setMessage(String.format("Message from %s: %s", sender.getFullName(), truncateMessage(message)));
            notification.setRelatedEntityId(sender.getUserId());
            notification.setIsRead(false);
            notification.setCreatedAt(LocalDateTime.now());
            
            notificationRepository.save(notification);
            log.debug("Message notification sent to donor: {}", donor.getUserId());
            
        } catch (Exception e) {
            log.error("Error notifying donor of message", e);
        }
    }

    /**
     * Notify patient of new chat message
     * 
     * @param patient Patient to notify
     * @param sender User who sent the message
     * @param message Message content
     */
    public void notifyPatientOfNewMessage(Patient patient, User sender, String message) {
        try {
            Notification notification = new Notification();
            notification.setUser(patient);
            notification.setType(Notification.NotificationType.NEW_MESSAGE);
            notification.setTitle("New Message");
            notification.setMessage(String.format("Message from %s: %s", sender.getFullName(), truncateMessage(message)));
            notification.setRelatedEntityId(sender.getUserId());
            notification.setIsRead(false);
            notification.setCreatedAt(LocalDateTime.now());
            
            notificationRepository.save(notification);
            log.debug("Message notification sent to patient: {}", patient.getUserId());
            
        } catch (Exception e) {
            log.error("Error notifying patient of message", e);
        }
    }

    /**
     * Notify donor of request completion
     * 
     * @param donor Donor to notify
     * @param bloodRequest Completed request
     */
    public void notifyDonorOfCompletion(Donor donor, BloodRequest bloodRequest) {
        try {
            Notification notification = new Notification();
            notification.setUser(donor);
            notification.setType(Notification.NotificationType.REQUEST_COMPLETED);
            notification.setTitle("Blood Donation Completed");
            notification.setMessage(String.format(
                "Thank you for donating blood! Your contribution saved lives."
            ));
            notification.setRelatedEntityId(bloodRequest.getRequestId());
            notification.setIsRead(false);
            notification.setCreatedAt(LocalDateTime.now());
            
            notificationRepository.save(notification);
            log.info("Completion notification sent to donor: {}", donor.getUserId());
            
        } catch (Exception e) {
            log.error("Error notifying donor of completion", e);
        }
    }

    /**
     * Get all unread notifications for user
     * 
     * @param userId User ID
     * @return List of unread notifications
     */
    public List<Notification> getUnreadNotifications(Long userId) {
        log.debug("Fetching unread notifications for user: {}", userId);
        return notificationRepository.findUnreadNotificationsByUser(userId);
    }

    /**
     * Get all notifications for user
     * 
     * @param userId User ID
     * @return List of all notifications
     */
    public List<Notification> getUserNotifications(Long userId) {
        log.debug("Fetching all notifications for user: {}", userId);
        return notificationRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Get recent notifications (last 7 days)
     * 
     * @param userId User ID
     * @return List of recent notifications
     */
    public List<Notification> getRecentNotifications(Long userId) {
        log.debug("Fetching recent notifications for user: {}", userId);
        return notificationRepository.findUserRecentNotifications(userId);
    }

    /**
     * Mark notification as read
     * Encapsulation: Hides read status update logic
     * 
     * @param notificationId Notification ID
     */
    public void markAsRead(Long notificationId) {
        try {
            Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
            
            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
            
            log.debug("Notification marked as read: {}", notificationId);
            
        } catch (Exception e) {
            log.error("Error marking notification as read", e);
        }
    }

    /**
     * Mark all notifications as read for user
     * 
     * @param userId User ID
     */
    public void markAllAsRead(Long userId) {
        try {
            List<Notification> notifications = notificationRepository.findUnreadNotificationsByUser(userId);
            LocalDateTime now = LocalDateTime.now();
            
            notifications.forEach(notification -> {
                notification.setIsRead(true);
                notification.setReadAt(now);
            });
            
            notificationRepository.saveAll(notifications);
            log.debug("All notifications marked as read for user: {}", userId);
            
        } catch (Exception e) {
            log.error("Error marking all notifications as read", e);
        }
    }

    /**
     * Delete notification
     * 
     * @param notificationId Notification ID
     */
    public void deleteNotification(Long notificationId) {
        try {
            notificationRepository.deleteById(notificationId);
            log.debug("Notification deleted: {}", notificationId);
            
        } catch (Exception e) {
            log.error("Error deleting notification", e);
        }
    }

    /**
     * Count unread notifications for user
     * 
     * @param userId User ID
     * @return Count of unread notifications
     */
    public int countUnreadNotifications(Long userId) {
        return notificationRepository.countUnreadNotificationsByUser(userId);
    }

    /**
     * Get notifications by type
     * 
     * @param userId User ID
     * @param type Notification type
     * @return List of notifications of specified type
     */
    public List<Notification> getNotificationsByType(Long userId, Notification.NotificationType type) {
        log.debug("Fetching notifications by type for user: {} - type: {}", userId, type);
        return notificationRepository.findUnreadNotificationsByType(userId, type);
    }

    // ==================== Helper Methods ====================

    /**
     * Calculate distance between two users
     * Encapsulation: Hides distance calculation logic
     * 
     * @param user1 First user
     * @param user2 Second user
     * @return Distance in kilometers
     */
    private int calculateDistance(User user1, User user2) {
        // Simple distance approximation (should use Haversine formula in production)
        if (user1.getLatitude() == null || user1.getLongitude() == null ||
            user2.getLatitude() == null || user2.getLongitude() == null) {
            return -1;
        }
        
        double lat1 = user1.getLatitude();
        double lon1 = user1.getLongitude();
        double lat2 = user2.getLatitude();
        double lon2 = user2.getLongitude();
        
        return (int) Math.sqrt(Math.pow(lat2 - lat1, 2) + Math.pow(lon2 - lon1, 2)) * 111;
    }

    /**
     * Truncate message to maximum length
     * 
     * @param message Message to truncate
     * @return Truncated message
     */
    private String truncateMessage(String message) {
        if (message == null) {
            return "";
        }
        int maxLength = 50;
        return message.length() > maxLength ? message.substring(0, maxLength) + "..." : message;
    }
}
