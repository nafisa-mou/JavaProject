package com.bloodlink.controller;

import com.bloodlink.entity.*;
import com.bloodlink.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * BloodRequestController - REST API for blood request operations
 * 
 * Endpoints:
 * - GET    /api/requests                   - Get all pending requests
 * - GET    /api/requests/{id}              - Get request details
 * - GET    /api/requests/critical          - Get critical requests
 * - GET    /api/requests/expired           - Get expired requests
 * - GET    /api/requests/{id}/donors       - Find suitable donors
 * - GET    /api/requests/{id}/priority     - Get priority score
 * - GET    /api/requests/statistics        - Request statistics
 * - POST   /api/requests/{id}/accept       - Accept request (Donor)
 * - POST   /api/requests/{id}/decline      - Decline request (Donor)
 * - POST   /api/requests/{id}/complete     - Complete request (Admin)
 * - POST   /api/requests/{id}/notify       - Notify nearby donors
 * 
 * OOP Principle: Encapsulation - Complex logic delegated to service layer
 * REST Principle: Resource-oriented - Each endpoint represents a request resource
 * SOLID: Single Responsibility - Only handles HTTP concerns
 */
@Slf4j
@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080", "http://localhost:4200"})
public class BloodRequestController {

    private final BloodRequestService bloodRequestService;
    private final NotificationService notificationService;

    /**
     * Get all pending blood requests
     * 
     * @return List of pending requests
     */
    @GetMapping
    public ResponseEntity<?> getAllPendingRequests() {
        log.debug("Fetching all pending blood requests");
        
        try {
            List<BloodRequest> requests = bloodRequestService.getAllPendingRequests();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "count", requests.size(),
                "data", requests
            ));
            
        } catch (Exception e) {
            log.error("Error fetching pending requests", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Get blood request by ID
     * 
     * @param id Request ID
     * @return Request details
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getRequestById(@PathVariable Long id) {
        log.debug("Fetching blood request: {}", id);
        
        try {
            BloodRequest request = bloodRequestService.getRequestById(id);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", request
            ));
            
        } catch (Exception e) {
            log.error("Error fetching request", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Get critical pending requests
     * 
     * @return List of critical requests
     */
    @GetMapping("/critical")
    @PreAuthorize("hasAnyRole('DONOR', 'ADMIN')")
    public ResponseEntity<?> getCriticalPendingRequests() {
        log.debug("Fetching critical pending requests");
        
        try {
            List<BloodRequest> requests = bloodRequestService.getCriticalPendingRequests();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "count", requests.size(),
                "data", requests
            ));
            
        } catch (Exception e) {
            log.error("Error fetching critical requests", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Get expired requests
     * 
     * @return List of expired requests
     */
    @GetMapping("/expired")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getExpiredRequests() {
        log.debug("Fetching expired requests");
        
        try {
            List<BloodRequest> requests = bloodRequestService.getExpiredRequests();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "count", requests.size(),
                "data", requests
            ));
            
        } catch (Exception e) {
            log.error("Error fetching expired requests", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Find suitable donors for a blood request
     * 
     * @param id Request ID
     * @return List of suitable donors
     */
    @GetMapping("/{id}/donors")
    public ResponseEntity<?> findSuitableDonors(@PathVariable Long id) {
        log.debug("Finding suitable donors for request: {}", id);
        
        try {
            List<Donor> donors = bloodRequestService.findSuitableDonors(id);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "count", donors.size(),
                "data", donors
            ));
            
        } catch (Exception e) {
            log.error("Error finding suitable donors", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Get priority score for a request
     * 
     * @param id Request ID
     * @return Priority score (0-100)
     */
    @GetMapping("/{id}/priority")
    public ResponseEntity<?> getRequestPriority(@PathVariable Long id) {
        log.debug("Getting priority score for request: {}", id);
        
        try {
            double score = bloodRequestService.getRequestPriorityScore(id);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "requestId", id,
                "priorityScore", String.format("%.2f", score)
            ));
            
        } catch (Exception e) {
            log.error("Error getting priority score", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Get blood request statistics
     * 
     * @return Statistics map
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getStatistics() {
        log.debug("Fetching blood request statistics");
        
        try {
            Map<String, Object> stats = bloodRequestService.getRequestStatistics();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", stats
            ));
            
        } catch (Exception e) {
            log.error("Error fetching statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Accept blood request (Donor accepting)
     * 
     * Request Body:
     * {
     *   "donorId": 1
     * }
     * 
     * @param id Request ID
     * @param request Accept request DTO
     * @return Accepted request
     */
    @PostMapping("/{id}/accept")
    @PreAuthorize("hasRole('DONOR')")
    public ResponseEntity<?> acceptRequest(@PathVariable Long id,
                                          @RequestBody AcceptRequestDto request) {
        log.info("Donor {} accepting blood request: {}", request.getDonorId(), id);
        
        try {
            BloodRequest accepted = bloodRequestService.acceptRequest(id, request.getDonorId());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Request accepted successfully",
                "data", accepted
            ));
            
        } catch (Exception e) {
            log.error("Error accepting request", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Decline blood request (Donor declining)
     * 
     * Request Body:
     * {
     *   "donorId": 1,
     *   "reason": "Not eligible to donate"
     * }
     * 
     * @param id Request ID
     * @param request Decline request DTO
     * @return Declined request
     */
    @PostMapping("/{id}/decline")
    @PreAuthorize("hasRole('DONOR')")
    public ResponseEntity<?> declineRequest(@PathVariable Long id,
                                           @RequestBody DeclineRequestDto request) {
        log.info("Donor {} declining blood request: {}", request.getDonorId(), id);
        
        try {
            BloodRequest declined = bloodRequestService.declineRequest(
                id, 
                request.getDonorId(), 
                request.getReason()
            );
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Request declined",
                "data", declined
            ));
            
        } catch (Exception e) {
            log.error("Error declining request", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Complete blood request (Mark as completed after donation)
     * 
     * Request Body:
     * {
     *   "unitsCollected": 450,
     *   "donationDate": "2024-05-25"
     * }
     * 
     * @param id Request ID
     * @param details Donation details
     * @return Completed request
     */
    @PostMapping("/{id}/complete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> completeRequest(@PathVariable Long id,
                                            @RequestBody Map<String, Object> details) {
        log.info("Completing blood request: {}", id);
        
        try {
            BloodRequest completed = bloodRequestService.completeRequest(id, details);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Request completed successfully",
                "data", completed
            ));
            
        } catch (Exception e) {
            log.error("Error completing request", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Notify nearby donors about a blood request
     * 
     * @param id Request ID
     * @return Success response
     */
    @PostMapping("/{id}/notify")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> notifyNearbyDonors(@PathVariable Long id) {
        log.info("Notifying nearby donors for request: {}", id);
        
        try {
            bloodRequestService.notifyNearbyDonors(id);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Nearby donors notified",
                "requestId", id
            ));
            
        } catch (Exception e) {
            log.error("Error notifying donors", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Handle expired requests (scheduled task)
     * 
     * @return Success response
     */
    @PostMapping("/handle-expired")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> handleExpiredRequests() {
        log.info("Handling expired blood requests");
        
        try {
            bloodRequestService.handleExpiredRequests();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Expired requests processed"
            ));
            
        } catch (Exception e) {
            log.error("Error handling expired requests", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // ==================== Helper Classes ====================

    /**
     * Accept request DTO
     */
    @lombok.Data
    public static class AcceptRequestDto {
        private Long donorId;

        public Long getDonorId() { return donorId; }
        public void setDonorId(Long donorId) { this.donorId = donorId; }
    }

    /**
     * Decline request DTO
     */
    @lombok.Data
    public static class DeclineRequestDto {
        private Long donorId;
        private String reason;

        public Long getDonorId() { return donorId; }
        public String getReason() { return reason; }
        public void setDonorId(Long donorId) { this.donorId = donorId; }
        public void setReason(String reason) { this.reason = reason; }
    }
}
