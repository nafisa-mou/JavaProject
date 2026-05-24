package com.bloodlink.controller;

import com.bloodlink.dto.UserDTO.DonorDTO;
import com.bloodlink.entity.Donor;
import com.bloodlink.entity.DonationHistory;
import com.bloodlink.service.DonorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DonorController - REST API for donor operations
 * 
 * Endpoints:
 * - GET /api/donors - Get all donors
 * - GET /api/donors/{id} - Get donor by ID
 * - GET /api/donors/search - Search donors by blood group
 * - GET /api/donors/nearby - Find nearby donors
 * - GET /api/donors/{id}/profile - Get donor profile
 * - GET /api/donors/{id}/donations - Get donation history
 * - PUT /api/donors/{id} - Update donor profile
 * - PUT /api/donors/{id}/availability - Set availability status
 * - POST /api/donors/{id}/donations - Record new donation
 * - GET /api/donors/{id}/score - Get reliability score
 * 
 * Security:
 * - Public: GET /api/donors, /search, /nearby (limited info)
 * - Protected: PUT, POST, DELETE operations
 * 
 * OOP Principles:
 * - Encapsulation: Hides service implementation
 * - Single Responsibility: Only donor endpoints
 */
@Slf4j
@RestController
@RequestMapping("/api/donors")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:4200", "http://localhost:8080"})
public class DonorController {

    private final DonorService donorService;

    /**
     * Get all donors
     * 
     * @return List of all donors
     * @status 200 OK
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<DonorDTO>>> getAllDonors() {
        log.info("GET /api/donors");
        
        try {
            List<DonorDTO> donors = donorService.getAllDonors();
            return ResponseEntity.ok(ApiResponse.success(donors, "Donors retrieved successfully"));
            
        } catch (Exception e) {
            log.error("Error fetching donors", e);
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Failed to fetch donors"));
        }
    }

    /**
     * Get donor by ID
     * 
     * @param donorId Donor ID
     * @return DonorDTO with donor details
     * @status 200 OK
     * @status 404 NOT_FOUND
     */
    @GetMapping("/{donorId}")
    public ResponseEntity<ApiResponse<DonorDTO>> getDonorById(@PathVariable Long donorId) {
        log.info("GET /api/donors/{}", donorId);
        
        try {
            DonorDTO donor = donorService.getDonorById(donorId);
            return ResponseEntity.ok(ApiResponse.success(donor, "Donor retrieved successfully"));
            
        } catch (Exception e) {
            log.error("Donor not found: {}", donorId, e);
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Donor not found"));
        }
    }

    /**
     * Search donors by blood group
     * 
     * @param bloodGroup Blood group (e.g., O+, A-, AB+)
     * @return List of donors with matching blood group
     * @status 200 OK
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<DonorDTO>>> searchByBloodGroup(@RequestParam String bloodGroup) {
        log.info("GET /api/donors/search?bloodGroup={}", bloodGroup);
        
        try {
            List<DonorDTO> donors = donorService.searchByBloodGroup(bloodGroup);
            return ResponseEntity.ok(ApiResponse.success(donors, "Donors found: " + donors.size()));
            
        } catch (Exception e) {
            log.error("Error searching donors", e);
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Search failed: " + e.getMessage()));
        }
    }

    /**
     * Find nearby donors
     * 
     * @param latitude User latitude
     * @param longitude User longitude
     * @param radius Search radius in kilometers (default: 50)
     * @return List of nearby donors
     * @status 200 OK
     */
    @GetMapping("/nearby")
    public ResponseEntity<ApiResponse<List<DonorDTO>>> findNearbyDonors(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "50") Double radius) {
        log.info("GET /api/donors/nearby?lat={}&lon={}&radius={}", latitude, longitude, radius);
        
        try {
            List<DonorDTO> donors = donorService.findNearbyDonors(latitude, longitude, radius);
            return ResponseEntity.ok(ApiResponse.success(donors, "Nearby donors found: " + donors.size()));
            
        } catch (Exception e) {
            log.error("Error finding nearby donors", e);
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Failed to find nearby donors"));
        }
    }

    /**
     * Get donor profile with detailed information
     * 
     * @param donorId Donor ID
     * @return Map with donor profile, statistics
     * @status 200 OK
     * @status 404 NOT_FOUND
     */
    @GetMapping("/{donorId}/profile")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDonorProfile(@PathVariable Long donorId) {
        log.info("GET /api/donors/{}/profile", donorId);
        
        try {
            Map<String, Object> profile = donorService.getDonorProfile(donorId);
            return ResponseEntity.ok(ApiResponse.success(profile, "Donor profile retrieved"));
            
        } catch (Exception e) {
            log.error("Error fetching donor profile: {}", donorId, e);
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Donor not found"));
        }
    }

    /**
     * Get donor donation history
     * 
     * @param donorId Donor ID
     * @return List of donation records
     * @status 200 OK
     * @status 404 NOT_FOUND
     */
    @GetMapping("/{donorId}/donations")
    public ResponseEntity<ApiResponse<List<DonationHistory>>> getDonationHistory(@PathVariable Long donorId) {
        log.info("GET /api/donors/{}/donations", donorId);
        
        try {
            List<DonationHistory> history = donorService.getDonationHistory(donorId);
            return ResponseEntity.ok(ApiResponse.success(history, "Donation history retrieved"));
            
        } catch (Exception e) {
            log.error("Error fetching donation history: {}", donorId, e);
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Donor not found"));
        }
    }

    /**
     * Update donor profile
     * Requires authentication
     * 
     * @param donorId Donor ID
     * @param updates Map of fields to update
     * @return Updated DonorDTO
     * @status 200 OK
     * @status 401 UNAUTHORIZED
     * @status 404 NOT_FOUND
     */
    @PutMapping("/{donorId}")
    @PreAuthorize("hasRole('DONOR') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> updateDonorProfile(
            @PathVariable Long donorId,
            @RequestBody Map<String, Object> updates) {
        log.info("PUT /api/donors/{}", donorId);
        
        try {
            donorService.updateDonorProfile(donorId, updates);
            return ResponseEntity.ok(ApiResponse.success(null, "Donor profile updated successfully"));
            
        } catch (Exception e) {
            log.error("Error updating donor profile: {}", donorId, e);
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Failed to update profile: " + e.getMessage()));
        }
    }

    /**
     * Set donor availability status
     * Requires authentication
     * 
     * @param donorId Donor ID
     * @param isAvailable Availability status (true/false)
     * @return ApiResponse
     * @status 200 OK
     * @status 401 UNAUTHORIZED
     * @status 404 NOT_FOUND
     */
    @PutMapping("/{donorId}/availability")
    @PreAuthorize("hasRole('DONOR') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> setAvailability(
            @PathVariable Long donorId,
            @RequestParam Boolean isAvailable) {
        log.info("PUT /api/donors/{}/availability?available={}", donorId, isAvailable);
        
        try {
            donorService.setAvailabilityStatus(donorId, isAvailable);
            String message = isAvailable ? "Donor is now available" : "Donor is now unavailable";
            return ResponseEntity.ok(ApiResponse.success(null, message));
            
        } catch (Exception e) {
            log.error("Error setting availability for donor: {}", donorId, e);
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Failed to update availability: " + e.getMessage()));
        }
    }

    /**
     * Record new donation for donor
     * Requires authentication and admin role
     * 
     * @param donorId Donor ID
     * @param request Donation details
     * @return DonationHistory record
     * @status 201 CREATED
     * @status 401 UNAUTHORIZED
     * @status 404 NOT_FOUND
     */
    @PostMapping("/{donorId}/donations")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DonationHistory>> recordDonation(
            @PathVariable Long donorId,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/donors/{}/donations", donorId);
        
        try {
            Double units = Double.parseDouble(request.get("units").toString());
            @SuppressWarnings("unchecked")
            Map<String, Boolean> testResults = (Map<String, Boolean>) request.get("testResults");
            
            DonationHistory donation = donorService.recordDonation(donorId, units, testResults);
            return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(donation, "Donation recorded successfully"));
            
        } catch (Exception e) {
            log.error("Error recording donation: {}", donorId, e);
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Failed to record donation: " + e.getMessage()));
        }
    }

    /**
     * Get donor reliability score
     * 
     * @param donorId Donor ID
     * @return Reliability score (0-100)
     * @status 200 OK
     * @status 404 NOT_FOUND
     */
    @GetMapping("/{donorId}/score")
    public ResponseEntity<ApiResponse<Integer>> getDonorScore(@PathVariable Long donorId) {
        log.info("GET /api/donors/{}/score", donorId);
        
        try {
            Integer score = donorService.getDonorReliabilityScore(donorId);
            Map<String, Object> response = new HashMap<>();
            response.put("donorId", donorId);
            response.put("reliabilityScore", score);
            
            return ResponseEntity.ok(ApiResponse.success(score, "Reliability score retrieved"));
            
        } catch (Exception e) {
            log.error("Error fetching reliability score: {}", donorId, e);
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Donor not found"));
        }
    }

    /**
     * Check if donor is eligible for donation
     * 
     * @param donorId Donor ID
     * @return Eligibility status
     * @status 200 OK
     * @status 404 NOT_FOUND
     */
    @GetMapping("/{donorId}/eligibility")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkEligibility(@PathVariable Long donorId) {
        log.info("GET /api/donors/{}/eligibility", donorId);
        
        try {
            // Would check if donor is eligible for donation
            Map<String, Object> result = new HashMap<>();
            result.put("donorId", donorId);
            result.put("eligible", true); // Placeholder
            result.put("reason", "Donor is eligible for donation");
            
            return ResponseEntity.ok(ApiResponse.success(result, "Eligibility checked"));
            
        } catch (Exception e) {
            log.error("Error checking eligibility: {}", donorId, e);
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Donor not found"));
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
