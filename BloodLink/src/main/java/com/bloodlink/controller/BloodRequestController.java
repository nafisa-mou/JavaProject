package com.bloodlink.controller;

import com.bloodlink.entity.BloodRequest;
import com.bloodlink.entity.Donor;
import com.bloodlink.service.BloodRequestService;
import com.bloodlink.service.DonorMatchingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * BloodRequestController - REST API for blood request operations
 * 
 * Endpoints:
 * - GET /api/requests - Get all pending requests
 * - GET /api/requests/{id} - Get request by ID
 * - GET /api/requests/critical - Get critical pending requests
 * - POST /api/requests/{id}/accept - Accept request
 * - POST /api/requests/{id}/decline - Decline request
 * - POST /api/requests/{id}/complete - Complete request
 * - GET /api/requests/{id}/donors - Find suitable donors
 * - GET /api/requests/{id}/priority - Get priority score
 * - POST /api/requests/{id}/notify-donors - Notify donors
 * - GET /api/requests/stats - Get request statistics
 * 
 * Security:
 * - Public: GET all, GET by ID
 * - Protected: POST operations (donor/admin)
 */
@Slf4j
@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:4200", "http://localhost:8080"})
public class BloodRequestController {

    private final BloodRequestService bloodRequestService;
    private final DonorMatchingService donorMatchingService;

    /**
     * Get all pending blood requests
     * 
     * @return List of pending requests
     * @status 200 OK
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<BloodRequest>>> getAllPendingRequests() {
        log.info("GET /api/requests");
        
        try {
            List<BloodRequest> requests = bloodRequestService.getAllPendingRequests();
            return ResponseEntity.ok(ApiResponse.success(requests, "Requests retrieved: " + requests.size()));
            
        } catch (Exception e) {
            log.error("Error fetching requests", e);
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Failed to fetch requests"));
        }
    }

    /**
     * Get blood request by ID
     * 
     * @param requestId Request ID
     * @return BloodRequest
     * @status 200 OK
     * @status 404 NOT_FOUND
     */
    @GetMapping("/{requestId}")
    public ResponseEntity<ApiResponse<BloodRequest>> getRequestById(@PathVariable Long requestId) {
        log.info("GET /api/requests/{}", requestId);
        
        try {
            BloodRequest request = bloodRequestService.getRequestById(requestId);
            return ResponseEntity.ok(ApiResponse.success(request, "Request retrieved successfully"));
            
        } catch (Exception e) {
            log.error("Request not found: {}", requestId, e);
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Request not found"));
        }
    }

    /**
     * Get critical pending requests (CRITICAL or LIFE_THREATENING)
     * Requires admin role
     * 
     * @return List of critical requests
     * @status 200 OK
     */
    @GetMapping("/critical")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<BloodRequest>>> getCriticalRequests() {
        log.info("GET /api/requests/critical");
        
        try {
            List<BloodRequest> requests = bloodRequestService.getCriticalPendingRequests();
            return ResponseEntity.ok(ApiResponse.success(requests, "Critical requests retrieved: " + requests.size()));
            
        } catch (Exception e) {
            log.error("Error fetching critical requests", e);
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Failed to fetch critical requests"));
        }
    }

    /**
     * Accept blood request (donor accepting)
     * 
     * @param requestId Request ID
     * @param donorId Donor ID
     * @return Accepted BloodRequest
     * @status 200 OK
     * @status 401 UNAUTHORIZED
     * @status 404 NOT_FOUND
     */
    @PostMapping("/{requestId}/accept")
    @PreAuthorize("hasRole('DONOR') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BloodRequest>> acceptRequest(
            @PathVariable Long requestId,
            @RequestParam Long donorId) {
        log.info("POST /api/requests/{}/accept?donorId={}", requestId, donorId);
        
        try {
            BloodRequest accepted = bloodRequestService.acceptRequest(requestId, donorId);
            return ResponseEntity.ok(ApiResponse.success(accepted, "Request accepted successfully"));
            
        } catch (Exception e) {
            log.error("Error accepting request: {}", requestId, e);
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Failed to accept request: " + e.getMessage()));
        }
    }

    /**
     * Decline blood request (donor declining)
     * 
     * @param requestId Request ID
     * @param donorId Donor ID
     * @param reason Reason for declining
     * @return Declined BloodRequest
     * @status 200 OK
     * @status 401 UNAUTHORIZED
     * @status 404 NOT_FOUND
     */
    @PostMapping("/{requestId}/decline")
    @PreAuthorize("hasRole('DONOR') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BloodRequest>> declineRequest(
            @PathVariable Long requestId,
            @RequestParam Long donorId,
            @RequestParam(required = false) String reason) {
        log.info("POST /api/requests/{}/decline?donorId={}", requestId, donorId);
        
        try {
            BloodRequest declined = bloodRequestService.declineRequest(requestId, donorId, reason);
            return ResponseEntity.ok(ApiResponse.success(declined, "Request declined successfully"));
            
        } catch (Exception e) {
            log.error("Error declining request: {}", requestId, e);
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Failed to decline request: " + e.getMessage()));
        }
    }

    /**
     * Complete blood request (after donation)
     * Requires admin role
     * 
     * @param requestId Request ID
     * @param details Donation details
     * @return Completed BloodRequest
     * @status 200 OK
     * @status 401 UNAUTHORIZED
     * @status 404 NOT_FOUND
     */
    @PostMapping("/{requestId}/complete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BloodRequest>> completeRequest(
            @PathVariable Long requestId,
            @RequestBody Map<String, Object> details) {
        log.info("POST /api/requests/{}/complete", requestId);
        
        try {
            BloodRequest completed = bloodRequestService.completeRequest(requestId, details);
            return ResponseEntity.ok(ApiResponse.success(completed, "Request completed successfully"));
            
        } catch (Exception e) {
            log.error("Error completing request: {}", requestId, e);
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Failed to complete request: " + e.getMessage()));
        }
    }

    /**
     * Find suitable donors for request
     * 
     * @param requestId Request ID
     * @return List of suitable donors
     * @status 200 OK
     * @status 404 NOT_FOUND
     */
    @GetMapping("/{requestId}/donors")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Donor>>> findSuitableDonors(@PathVariable Long requestId) {
        log.info("GET /api/requests/{}/donors", requestId);
        
        try {
            List<Donor> donors = bloodRequestService.findSuitableDonors(requestId);
            return ResponseEntity.ok(ApiResponse.success(donors, "Suitable donors found: " + donors.size()));
            
        } catch (Exception e) {
            log.error("Error finding suitable donors: {}", requestId, e);
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Failed to find donors"));
        }
    }

    /**
     * Get request priority score
     * 
     * @param requestId Request ID
     * @return Priority score (0-100)
     * @status 200 OK
     * @status 404 NOT_FOUND
     */
    @GetMapping("/{requestId}/priority")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRequestPriority(@PathVariable Long requestId) {
        log.info("GET /api/requests/{}/priority", requestId);
        
        try {
            double priority = bloodRequestService.getRequestPriorityScore(requestId);
            Map<String, Object> result = Map.of(
                "requestId", requestId,
                "priorityScore", priority,
                "urgency", priority > 75 ? "CRITICAL" : priority > 50 ? "URGENT" : "ROUTINE"
            );
            
            return ResponseEntity.ok(ApiResponse.success(result, "Priority score calculated"));
            
        } catch (Exception e) {
            log.error("Error calculating priority: {}", requestId, e);
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Request not found"));
        }
    }

    /**
     * Notify nearby donors of request
     * Requires admin role
     * 
     * @param requestId Request ID
     * @return ApiResponse
     * @status 200 OK
     */
    @PostMapping("/{requestId}/notify-donors")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> notifyNearbyDonors(@PathVariable Long requestId) {
        log.info("POST /api/requests/{}/notify-donors", requestId);
        
        try {
            bloodRequestService.notifyNearbyDonors(requestId);
            return ResponseEntity.ok(ApiResponse.success(null, "Donors notified successfully"));
            
        } catch (Exception e) {
            log.error("Error notifying donors: {}", requestId, e);
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Failed to notify donors"));
        }
    }

    /**
     * Get request statistics
     * Requires admin role
     * 
     * @return Request statistics map
     * @status 200 OK
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRequestStatistics() {
        log.info("GET /api/requests/stats");
        
        try {
            Map<String, Object> stats = bloodRequestService.getRequestStatistics();
            return ResponseEntity.ok(ApiResponse.success(stats, "Statistics retrieved"));
            
        } catch (Exception e) {
            log.error("Error fetching statistics", e);
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Failed to fetch statistics"));
        }
    }

    /**
     * Get matching recommendations for request
     * 
     * @param requestId Request ID
     * @return Recommended donors with scores
     * @status 200 OK
     */
    @GetMapping("/{requestId}/recommendations")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRecommendations(@PathVariable Long requestId) {
        log.info("GET /api/requests/{}/recommendations", requestId);
        
        try {
            BloodRequest request = bloodRequestService.getRequestById(requestId);
            
            // Get matching analytics for patient
            Map<String, Object> recommendations = donorMatchingService
                .getMatchingAnalytics(request.getPatient().getUserId());
            
            return ResponseEntity.ok(ApiResponse.success(recommendations, "Recommendations retrieved"));
            
        } catch (Exception e) {
            log.error("Error getting recommendations: {}", requestId, e);
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Failed to get recommendations"));
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
