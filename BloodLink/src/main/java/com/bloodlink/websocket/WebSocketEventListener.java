package com.bloodlink.websocket;

import com.bloodlink.dto.WebSocketMessage;
import com.bloodlink.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * WebSocketEventListener - Handle WebSocket connection lifecycle events
 * 
 * Events:
 * - SessionConnectedEvent: User establishes WebSocket connection
 * - SessionDisconnectEvent: User closes WebSocket connection
 * - SessionSubscribeEvent: User subscribes to STOMP destination
 * - SessionUnsubscribeEvent: User unsubscribes from destination
 * 
 * Features:
 * - Track connected users in real-time
 * - Broadcast user online/offline events
 * - Cleanup resources on disconnect
 * - Session management and logging
 * 
 * OOP Principle: Encapsulation - Event handling logic isolated
 * Design Pattern: Observer/Listener pattern
 * Best Practice: Async event handling for non-blocking operations
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserStatusService userStatusService;

    /**
     * Handle user WebSocket connection
     * 
     * Triggered when client establishes WebSocket connection
     * 
     * @param event SessionConnectedEvent
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        log.info("WebSocket connected for session: {}", event.getMessage().getHeaders().get("simpSessionId"));
        
        try {
            // Get user from Principal
            String sessionId = (String) event.getMessage().getHeaders().get("simpSessionId");
            log.debug("New WebSocket session established: {}", sessionId);

            // Broadcast connection event to monitoring dashboard
            Map<String, Object> connectionEvent = new HashMap<>();
            connectionEvent.put("event", "USER_CONNECTED");
            connectionEvent.put("sessionId", sessionId);
            connectionEvent.put("timestamp", LocalDateTime.now());

            messagingTemplate.convertAndSend(
                "/topic/connections",
                connectionEvent
            );

        } catch (Exception e) {
            log.error("Error handling WebSocket connection", e);
        }
    }

    /**
     * Handle user WebSocket disconnection
     * 
     * Triggered when client closes WebSocket connection
     * Performs cleanup: removes user from online status, notifies others
     * 
     * @param event SessionDisconnectEvent
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        log.info("WebSocket disconnected for session: {}", event.getSessionId());
        
        try {
            String sessionId = event.getSessionId();
            log.debug("WebSocket session closed: {}", sessionId);

            // Extract userId from session attributes if available
            // In production, you'd maintain a mapping of sessionId -> userId
            // For now, log the disconnection

            // Cleanup: Remove user from status tracking
            // Note: In production, extract userId and call:
            // userStatusService.removeUser(userId);

            // Broadcast disconnection event
            Map<String, Object> disconnectionEvent = new HashMap<>();
            disconnectionEvent.put("event", "USER_DISCONNECTED");
            disconnectionEvent.put("sessionId", sessionId);
            disconnectionEvent.put("timestamp", LocalDateTime.now());

            messagingTemplate.convertAndSend(
                "/topic/connections",
                disconnectionEvent
            );

            log.info("User session cleanup completed: {}", sessionId);

        } catch (Exception e) {
            log.error("Error handling WebSocket disconnection", e);
        }
    }

    /**
     * Handle user subscribing to STOMP destination
     * 
     * @param event SessionSubscribeEvent
     */
    @EventListener
    public void handleSubscribeListener(SessionSubscribeEvent event) {
        String destination = (String) event.getMessage().getHeaders().get("simpDestination");
        String sessionId = (String) event.getMessage().getHeaders().get("simpSessionId");

        log.debug("User subscribed to destination {} (session: {})", destination, sessionId);

        try {
            // Log subscription for monitoring
            Map<String, Object> subscriptionEvent = new HashMap<>();
            subscriptionEvent.put("event", "USER_SUBSCRIBED");
            subscriptionEvent.put("destination", destination);
            subscriptionEvent.put("sessionId", sessionId);
            subscriptionEvent.put("timestamp", LocalDateTime.now());

            // Optionally broadcast subscription events for admin monitoring
            // messagingTemplate.convertAndSend("/topic/admin/subscriptions", subscriptionEvent);

        } catch (Exception e) {
            log.error("Error handling subscription", e);
        }
    }

    /**
     * Handle user unsubscribing from STOMP destination
     * 
     * @param event SessionUnsubscribeEvent
     */
    @EventListener
    public void handleUnsubscribeListener(SessionUnsubscribeEvent event) {
        String destination = (String) event.getMessage().getHeaders().get("simpDestination");
        String sessionId = (String) event.getMessage().getHeaders().get("simpSessionId");

        log.debug("User unsubscribed from destination {} (session: {})", destination, sessionId);

        try {
            // Log unsubscription for monitoring
            Map<String, Object> unsubscriptionEvent = new HashMap<>();
            unsubscriptionEvent.put("event", "USER_UNSUBSCRIBED");
            unsubscriptionEvent.put("destination", destination);
            unsubscriptionEvent.put("sessionId", sessionId);
            unsubscriptionEvent.put("timestamp", LocalDateTime.now());

            // Optionally broadcast unsubscription events for admin monitoring
            // messagingTemplate.convertAndSend("/topic/admin/subscriptions", unsubscriptionEvent);

        } catch (Exception e) {
            log.error("Error handling unsubscription", e);
        }
    }

    /**
     * Broadcast notification to all connected users
     * 
     * Called programmatically (not via event)
     * 
     * @param title Notification title
     * @param message Notification message
     * @param type Notification type (e.g., "ALERT", "INFO", "WARNING")
     */
    public void broadcastNotification(String title, String message, String type) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("title", title);
            notification.put("message", message);
            notification.put("type", type);
            notification.put("timestamp", LocalDateTime.now());

            messagingTemplate.convertAndSend(
                "/topic/notifications",
                notification
            );

            log.info("Broadcast notification sent: {}", title);

        } catch (Exception e) {
            log.error("Error broadcasting notification", e);
        }
    }

    /**
     * Broadcast system alert to all connected users
     * 
     * @param alert Alert message
     */
    public void broadcastSystemAlert(String alert) {
        try {
            Map<String, Object> alertMessage = new HashMap<>();
            alertMessage.put("type", "SYSTEM_ALERT");
            alertMessage.put("message", alert);
            alertMessage.put("timestamp", LocalDateTime.now());

            messagingTemplate.convertAndSend(
                "/topic/alerts",
                alertMessage
            );

            log.warn("System alert broadcast: {}", alert);

        } catch (Exception e) {
            log.error("Error broadcasting system alert", e);
        }
    }

    /**
     * Notify specific user of urgent event
     * 
     * @param userId User ID to notify
     * @param event Event details
     */
    public void notifyUserOfEvent(Long userId, String eventType, Object eventData) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", eventType);
            notification.put("data", eventData);
            notification.put("timestamp", LocalDateTime.now());

            messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/events",
                notification
            );

            log.debug("Event notification sent to user {}: {}", userId, eventType);

        } catch (Exception e) {
            log.error("Error notifying user {} of event", userId, e);
        }
    }

    /**
     * Broadcast donor availability update to all online patients
     * 
     * @param donorId Donor ID
     * @param donorName Donor name
     * @param bloodGroup Blood group
     * @param location Location
     */
    public void broadcastDonorAvailability(Long donorId, String donorName, 
                                          String bloodGroup, String location) {
        try {
            WebSocketMessage msg = WebSocketMessage.createNotification(
                null,
                "New Donor Available",
                donorName + " (" + bloodGroup + ") is now available in " + location,
                Map.of(
                    "donorId", donorId,
                    "donorName", donorName,
                    "bloodGroup", bloodGroup,
                    "location", location
                )
            );

            messagingTemplate.convertAndSend(
                "/topic/donors/available",
                msg
            );

            log.info("Donor availability broadcast: {} in {}", donorName, location);

        } catch (Exception e) {
            log.error("Error broadcasting donor availability", e);
        }
    }

    /**
     * Broadcast critical blood request to nearby donors
     * 
     * @param requestId Request ID
     * @param bloodGroup Required blood group
     * @param location Location
     * @param urgencyLevel Urgency level
     */
    public void broadcastCriticalRequest(Long requestId, String bloodGroup, 
                                        String location, String urgencyLevel) {
        try {
            WebSocketMessage msg = WebSocketMessage.createNotification(
                null,
                "CRITICAL Blood Request",
                "Urgent need for " + bloodGroup + " blood in " + location,
                Map.of(
                    "requestId", requestId,
                    "bloodGroup", bloodGroup,
                    "location", location,
                    "urgencyLevel", urgencyLevel
                )
            );

            messagingTemplate.convertAndSend(
                "/topic/requests/critical",
                msg
            );

            log.warn("Critical blood request broadcast: {} for {}", bloodGroup, location);

        } catch (Exception e) {
            log.error("Error broadcasting critical request", e);
        }
    }

    /**
     * Broadcast blood request acceptance to patient
     * 
     * @param patientId Patient ID
     * @param donorName Donor name
     * @param donorContact Donor contact
     */
    public void notifyPatientOfAcceptance(Long patientId, String donorName, String donorContact) {
        try {
            WebSocketMessage msg = WebSocketMessage.createNotification(
                patientId,
                "Request Accepted",
                "Your blood request has been accepted by " + donorName,
                Map.of(
                    "donorName", donorName,
                    "donorContact", donorContact
                )
            );

            messagingTemplate.convertAndSendToUser(
                patientId.toString(),
                "/queue/messages",
                msg
            );

            log.info("Patient {} notified of acceptance by {}", patientId, donorName);

        } catch (Exception e) {
            log.error("Error notifying patient of acceptance", e);
        }
    }

    /**
     * Get real-time user activity statistics
     * 
     * @return Activity stats
     */
    public Map<String, Object> getActivityStats() {
        return userStatusService.getActivityStatistics();
    }
}
