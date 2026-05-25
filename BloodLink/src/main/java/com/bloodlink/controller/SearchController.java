package com.bloodlink.controller;

import com.bloodlink.service.DonorMatchingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * SearchController - REST API for advanced search and matching operations
 * 
 * Endpoints:
 * - GET    /api/search/match                   - Find best matching donors
 * - GET    /api/search/match/{patientId}       - Find matches for patient
 * - GET    /api/search/emergency/{patientId}   - Emergency recommendations
 * - GET    /api/search/analytics/{patientId}   - Matching analytics
 * 
 * OOP Principle: Encapsulation - Complex matching logic delegated to DonorMatchingService
 * REST Principle: Resource-oriented - Search results are resources
 * SOLID: Single Responsibility - Only handles search-related endpoints
 */
@Slf4j
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080", "http://localhost:4200"})
public class SearchController {

    private final DonorMatchingService donorMatchingService;

    /**
     * Find best matching donors for a patient
     * 
     * @param patientId Patient ID
     * @return List of donors ranked by match score
     */
    @GetMapping("/match/{patientId}")
    @PreAuthorize("hasRole('PATIENT') || hasRole('ADMIN')")
    public ResponseEntity<?> findBestMatchingDonors(@PathVariable Long patientId) {
        log.info("Finding best matching donors for patient: {}", patientId);
        
        try {
            List<Map<String, Object>> matches = donorMatchingService.findBestMatchingDonors(patientId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "count", matches.size(),
                "patientId", patientId,
                "data", matches
            ));
            
        } catch (Exception e) {
            log.error("Error finding matching donors", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Get emergency donor recommendations for critical request
     * 
     * Query Parameters:
     * - maxDistance: Maximum distance in km (optional, default 50)
     * 
     * @param patientId Patient ID
     * @param maxDistance Maximum distance in km
     * @return Top emergency-recommended donors
     */
    @GetMapping("/emergency/{patientId}")
    @PreAuthorize("hasRole('PATIENT') || hasRole('ADMIN')")
    public ResponseEntity<?> getEmergencyRecommendations(@PathVariable Long patientId,
                                                        @RequestParam(defaultValue = "50") double maxDistance) {
        log.info("Getting emergency recommendations for patient: {}", patientId);
        
        try {
            List<Map<String, Object>> recommendations = donorMatchingService
                .getEmergencyRecommendations(patientId, maxDistance);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "count", recommendations.size(),
                "patientId", patientId,
                "maxDistance", maxDistance,
                "data", recommendations
            ));
            
        } catch (Exception e) {
            log.error("Error getting emergency recommendations", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Get detailed matching analytics for a patient
     * 
     * @param patientId Patient ID
     * @return Matching analytics with statistics
     */
    @GetMapping("/analytics/{patientId}")
    @PreAuthorize("hasRole('PATIENT') || hasRole('ADMIN')")
    public ResponseEntity<?> getMatchingAnalytics(@PathVariable Long patientId) {
        log.debug("Getting matching analytics for patient: {}", patientId);
        
        try {
            Map<String, Object> analytics = donorMatchingService.getMatchingAnalytics(patientId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "patientId", patientId,
                "data", analytics
            ));
            
        } catch (Exception e) {
            log.error("Error getting matching analytics", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Get detailed match score breakdown for donor-patient pair
     * 
     * Query Parameters:
     * - patientId: Patient ID
     * - donorId: Donor ID
     * 
     * @param patientId Patient ID
     * @param donorId Donor ID
     * @return Match score with breakdown
     */
    @GetMapping("/match-score")
    public ResponseEntity<?> getMatchScore(@RequestParam Long patientId,
                                          @RequestParam Long donorId) {
        log.debug("Getting match score for patient {} and donor {}", patientId, donorId);
        
        try {
            // This would require fetching patient and donor and calculating score
            // Placeholder for now
            return ResponseEntity.ok(Map.of(
                "success", true,
                "patientId", patientId,
                "donorId", donorId,
                "message", "Match score calculation endpoint"
            ));
            
        } catch (Exception e) {
            log.error("Error getting match score", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Get matching algorithm statistics
     * 
     * @return Algorithm statistics and info
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAlgorithmStatistics() {
        log.debug("Getting matching algorithm statistics");
        
        try {
            Map<String, Object> stats = Map.of(
                "success", true,
                "algorithm", "Multi-Factor Weighted Matching",
                "factors", Map.of(
                    "bloodGroup", "20%",
                    "location", "25%",
                    "reliability", "25%",
                    "availability", "15%",
                    "recency", "15%"
                ),
                "version", "1.0.0",
                "description", "AI-powered donor matching using multiple criteria"
            );
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            log.error("Error getting algorithm statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Health check for search service
     * 
     * @return Health status
     */
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        return ResponseEntity.ok(Map.of("status", "SEARCH_SERVICE_RUNNING"));
    }
}
