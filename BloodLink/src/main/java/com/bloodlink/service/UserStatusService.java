package com.bloodlink.service;

import com.bloodlink.entity.User;
import com.bloodlink.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * UserStatusService - Track and manage user online/offline status
 * 
 * Features:
 * - Real-time online/offline status tracking
 * - Last seen timestamp tracking
 * - Active session management
 * - Status queries and statistics
 * 
 * OOP Principle: Encapsulation - Status data kept private with accessor methods
 * Design Pattern: Singleton (Spring Service), Cache pattern for in-memory status
 * Performance: In-memory ConcurrentHashMap for fast lookups (O(1))
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserStatusService {

    private final UserRepository userRepository;
    
    // In-memory store for user online status
    // Key: userId, Value: UserStatus object
    private final ConcurrentHashMap<Long, UserStatus> userStatusMap = new ConcurrentHashMap<>();

    /**
     * UserStatus - Internal DTO for tracking user status
     */
    public static class UserStatus {
        public Long userId;
        public String userRole;
        public boolean isOnline;
        public LocalDateTime lastSeen;
        public LocalDateTime connectedAt;
        public String sessionId;

        public UserStatus(Long userId, String userRole) {
            this.userId = userId;
            this.userRole = userRole;
            this.isOnline = false;
            this.lastSeen = LocalDateTime.now();
            this.connectedAt = null;
            this.sessionId = null;
        }
    }

    /**
     * Set user as online
     * 
     * @param userId User ID to mark online
     */
    public void setUserOnline(Long userId) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                log.warn("User not found: {}", userId);
                return;
            }

            UserStatus status = userStatusMap.computeIfAbsent(
                userId, 
                k -> new UserStatus(userId, user.getUserRole())
            );

            status.isOnline = true;
            status.connectedAt = LocalDateTime.now();
            status.lastSeen = LocalDateTime.now();

            log.info("User {} marked ONLINE", userId);

        } catch (Exception e) {
            log.error("Error marking user online: {}", userId, e);
        }
    }

    /**
     * Set user as offline
     * 
     * @param userId User ID to mark offline
     */
    public void setUserOffline(Long userId) {
        try {
            UserStatus status = userStatusMap.get(userId);
            if (status != null) {
                status.isOnline = false;
                status.lastSeen = LocalDateTime.now();
                status.connectedAt = null;
            }

            log.info("User {} marked OFFLINE", userId);

        } catch (Exception e) {
            log.error("Error marking user offline: {}", userId, e);
        }
    }

    /**
     * Update last seen timestamp for user
     * 
     * @param userId User ID
     */
    public void updateLastSeen(Long userId) {
        UserStatus status = userStatusMap.get(userId);
        if (status != null) {
            status.lastSeen = LocalDateTime.now();
        }
    }

    /**
     * Check if user is currently online
     * 
     * @param userId User ID
     * @return true if user is online, false otherwise
     */
    public boolean isUserOnline(Long userId) {
        UserStatus status = userStatusMap.get(userId);
        return status != null && status.isOnline;
    }

    /**
     * Get user's online status
     * 
     * @param userId User ID
     * @return Status map with isOnline, lastSeen, connectedAt
     */
    public Map<String, Object> getUserStatus(Long userId) {
        UserStatus status = userStatusMap.get(userId);
        
        return Map.of(
            "userId", userId,
            "isOnline", status != null && status.isOnline,
            "lastSeen", status != null ? status.lastSeen : null,
            "connectedAt", status != null ? status.connectedAt : null
        );
    }

    /**
     * Get all online users
     * 
     * @return List of online user IDs
     */
    public List<Long> getOnlineUsers() {
        return userStatusMap.entrySet().stream()
            .filter(entry -> entry.getValue().isOnline)
            .map(entry -> entry.getKey())
            .toList();
    }

    /**
     * Get online donors
     * 
     * @return List of online donor IDs
     */
    public List<Long> getOnlineDonors() {
        return userStatusMap.entrySet().stream()
            .filter(entry -> entry.getValue().isOnline && "DONOR".equals(entry.getValue().userRole))
            .map(entry -> entry.getKey())
            .toList();
    }

    /**
     * Get online patients
     * 
     * @return List of online patient IDs
     */
    public List<Long> getOnlinePatients() {
        return userStatusMap.entrySet().stream()
            .filter(entry -> entry.getValue().isOnline && "PATIENT".equals(entry.getValue().userRole))
            .map(entry -> entry.getKey())
            .toList();
    }

    /**
     * Get count of online users
     * 
     * @return Number of currently online users
     */
    public int getOnlineUserCount() {
        return (int) userStatusMap.values().stream()
            .filter(status -> status.isOnline)
            .count();
    }

    /**
     * Get count of online donors
     * 
     * @return Number of currently online donors
     */
    public int getOnlineDonorCount() {
        return (int) userStatusMap.values().stream()
            .filter(status -> status.isOnline && "DONOR".equals(status.userRole))
            .count();
    }

    /**
     * Get count of online patients
     * 
     * @return Number of currently online patients
     */
    public int getOnlinePatientCount() {
        return (int) userStatusMap.values().stream()
            .filter(status -> status.isOnline && "PATIENT".equals(status.userRole))
            .count();
    }

    /**
     * Get how long user has been online
     * 
     * @param userId User ID
     * @return Duration in minutes, or -1 if offline
     */
    public long getOnlineDurationMinutes(Long userId) {
        UserStatus status = userStatusMap.get(userId);
        if (status == null || !status.isOnline || status.connectedAt == null) {
            return -1;
        }

        return java.time.temporal.ChronoUnit.MINUTES
            .between(status.connectedAt, LocalDateTime.now());
    }

    /**
     * Get user's last activity time
     * 
     * @param userId User ID
     * @return Last seen timestamp
     */
    public LocalDateTime getLastActivityTime(Long userId) {
        UserStatus status = userStatusMap.get(userId);
        return status != null ? status.lastSeen : null;
    }

    /**
     * Check if user was active in last N minutes
     * 
     * @param userId User ID
     * @param minutesAgo Minutes threshold
     * @return true if user was active in last N minutes
     */
    public boolean wasActiveInLastMinutes(Long userId, int minutesAgo) {
        UserStatus status = userStatusMap.get(userId);
        if (status == null || status.lastSeen == null) {
            return false;
        }

        LocalDateTime threshold = LocalDateTime.now().minusMinutes(minutesAgo);
        return status.lastSeen.isAfter(threshold);
    }

    /**
     * Get global statistics about user activity
     * 
     * @return Stats map
     */
    public Map<String, Object> getActivityStatistics() {
        int totalOnline = getOnlineUserCount();
        int onlineDonors = getOnlineDonorCount();
        int onlinePatients = getOnlinePatientCount();

        return Map.of(
            "totalOnlineUsers", totalOnline,
            "onlineDonors", onlineDonors,
            "onlinePatients", onlinePatients,
            "trackedUsers", userStatusMap.size(),
            "timestamp", LocalDateTime.now()
        );
    }

    /**
     * Clear user from tracking (on session timeout)
     * 
     * @param userId User ID
     */
    public void removeUser(Long userId) {
        userStatusMap.remove(userId);
        log.debug("User {} removed from status tracking", userId);
    }

    /**
     * Clear all offline users from tracking (cleanup)
     */
    public void cleanupOfflineUsers() {
        userStatusMap.values().removeIf(status -> !status.isOnline);
        log.info("Cleaned up offline users from status tracking");
    }

    /**
     * Clear all status data (full reset)
     */
    public void resetAllStatus() {
        userStatusMap.clear();
        log.info("All user status data cleared");
    }
}
