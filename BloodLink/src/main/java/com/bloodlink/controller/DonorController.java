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

import java.util.*;

/**
 * DonorController - REST API for donor operations
 * 
 * Endpoints:
 * - GET    /api/donors                     - Get all donors
 * - GET    /api/donors/{id}                - Get donor by ID
 * - GET    /api/donors/search              - Search donors by blood group
 * - GET    /api/donors/nearby              - Find nearby donors
 * - GET    /api/donors/{id}/profile        - Get donor profile
 * - GET    /api/donors/{id}/donations      - Get donation history
 * - GET    /api/donors/{id}/score          - Get reliability score
 * - PUT    /api/donors/{id}                - Update donor profile
 * - PUT    /api/donors/{id}/availability   - Set availability status
 * - POST   /api/donors/{id}/donation       - Record donation
 * 
 * OOP Principle: Encapsulation - Complex business logic delegated to DonorService
 * REST Principle: Resource-oriented - Each endpoint represents a donor resource
 * SOLID: Single Responsibility - Only handles HTTP concerns
 */
@Slf4j
@RestController
@RequestMapping("/api/donors")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080", "http://localhost:4200"})
public class DonorController {

    private final DonorService donorService;

    /**
     * Get all donors
     * 
     * @return List of all donors
     */
    @GetMapping
    public ResponseEntity<?> getAllDonors() {
        log.debug("Fetching all donors");
        
        try {
            List<DonorDTO> donors = donorService.getAllDonors();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "count", donors.size(),
                "data", donors
            ));
            
        } catch (Exception e) {
            log.error("Error fetching donors", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Get donor by ID
     * 
     * @param id Donor ID
     * @return Donor details
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getDonorById(@PathVariable Long id) {
        log.debug("Fetching donor: {}", id);
        
        try {
            DonorDTO donor = donorService.getDonorById(id);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", donor
            ));
            
        } catch (Exception e) {
            log.error("Error fetching donor", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Search donors by blood group
     * 
     * Query Parameters:
     * - bg: Blood group (e.g., "O+")
     * 
     * @param bloodGroup Blood group
     * @return List of donors with that blood group
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchByBloodGroup(@RequestParam String bloodGroup) {
        log.debug("Searching donors with blood group: {}", bloodGroup);
        
        try {
            List<DonorDTO> donors = donorService.searchByBloodGroup(bloodGroup);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "count", donors.size(),
                "bloodGroup", bloodGroup,
                "data", donors
            ));
            
        } catch (Exception e) {
            log.error("Error searching donors", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Find nearby donors
     * 
     * Query Parameters:
     * - lat: User latitude (required)
     * - lon: User longitude (required)
     * - radius: Search radius in km (optional, default 50)
     * 
     * @param latitude User latitude
     * @param longitude User longitude
     * @param radius Search radius in kilometers
     * @return List of nearby donors
     */
    @GetMapping("/nearby")
    public ResponseEntity<?> findNearbyDonors(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "50") Double radius) {
        
        log.debug("Finding donors near ({}, {}) within {} km", latitude, longitude, radius);
        
        try {
            List<DonorDTO> donors = donorService.findNearbyDonors(latitude, longitude, radius);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "count", donors.size(),
                "location", Map.of("latitude", latitude, "longitude", longitude, "radius", radius),
                "data", donors
            ));
            
        } catch (Exception e) {
            log.error("Error finding nearby donors", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Get donor profile with complete details
     * 
     * @param id Donor ID
     * @return Complete donor profile
     */
    @GetMapping("/{id}/profile")
    public ResponseEntity<?> getDonorProfile(@PathVariable Long id) {
        log.debug("Fetching donor profile: {}", id);
        
        try {
            Map<String, Object> profile = donorService.getDonorProfile(id);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", profile
            ));
            
        } catch (Exception e) {
            log.error("Error fetching donor profile", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Get donor's donation history
     * 
     * @param id Donor ID
     * @return List of donations
     */
    @GetMapping("/{id}/donations")
    public ResponseEntity<?> getDonationHistory(@PathVariable Long id) {
        log.debug("Fetching donation history for donor: {}", id);
        
        try {
            List<DonationHistory> history = donorService.getDonationHistory(id);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "count", history.size(),
                "data", history
            ));
            
        } catch (Exception e) {
            log.error("Error fetching donation history", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Get donor's reliability score
     * 
     * @param id Donor ID
     * @return Reliability score (0-100)
     */
    @GetMapping("/{id}/score")
    public ResponseEntity<?> getReliabilityScore(@PathVariable Long id) {
        log.debug("Getting reliability score for donor: {}", id);
        
        try {
            Integer score = donorService.getDonorReliabilityScore(id);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "donorId", id,
                "reliabilityScore", score
            ));
            
        } catch (Exception e) {
            log.error("Error fetching reliability score", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Update donor profile
     * 
     * Request Body:
     * {
     *   "fullName": "John Doe",
     *   "phoneNumber": "+1234567890",
     *   "city": "New York",
     *   "state": "NY",
     *   "latitude": 40.7128,
     *   "longitude": -74.0060
     * }
     * 
     * @param id Donor ID
     * @param updates Map of fields to update
     * @return Updated donor info
     */
    @PutMapping("/{id}")
    @PreAuthorize("@donorService.getDonorById(#id).userId == authentication.principal.userId")
    public ResponseEntity<?> updateDonorProfile(@PathVariable Long id, 
                                               @RequestBody Map<String, Object> updates) {
        log.info("Updating donor profile: {}", id);
        
        try {
            donorService.updateDonorProfile(id, updates);
            DonorDTO updated = donorService.getDonorById(id);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Profile updated successfully",
                "data", updated
            ));
            
        } catch (Exception e) {
            log.error("Error updating donor profile", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Set donor availability status
     * 
     * Request Body:
     * {
     *   "isAvailable": true
     * }
     * 
     * @param id Donor ID
     * @param request Availability status request
     * @return Updated status
     */
    @PutMapping("/{id}/availability")
    @PreAuthorize("@donorService.getDonorById(#id).userId == authentication.principal.userId")
    public ResponseEntity<?> setAvailability(@PathVariable Long id,
                                            @RequestBody AvailabilityRequest request) {
        log.info("Setting availability for donor {}: {}", id, request.isAvailable);
        
        try {
            donorService.setAvailabilityStatus(id, request.isAvailable);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Availability updated",
                "donorId", id,
                "isAvailable", request.isAvailable
            ));
            
        } catch (Exception e) {
            log.error("Error setting availability", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Record new donation
     * 
     * Request Body:
     * {
     *   "units": 450,
     *   "testResults": {
     *     "hbsAg": false,
     *     "hcv": false,
     *     "hiv": false,
     *     "vdrl": false
     *   }
     * }
     * 
     * @param id Donor ID
     * @param request Donation details
     * @return Recorded donation
     */
    @PostMapping("/{id}/donation")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> recordDonation(@PathVariable Long id,
                                           @RequestBody DonationRequest request) {
        log.info("Recording donation for donor: {}", id);
        
        try {
            DonationHistory donation = donorService.recordDonation(id, request.getUnits(), request.getTestResults());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "message", "Donation recorded successfully",
                "data", donation
            ));
            
        } catch (Exception e) {
            log.error("Error recording donation", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Check if donor is eligible for donation
     * 
     * @param id Donor ID
     * @return Eligibility status
     */
    @GetMapping("/{id}/eligible")
    public ResponseEntity<?> checkEligibility(@PathVariable Long id) {
        log.debug("Checking eligibility for donor: {}", id);
        
        try {
            Donor donor = new Donor(); // Placeholder - would get from repo
            boolean eligible = donorService.isDonorEligibleForDonation(donor);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "donorId", id,
                "isEligible", eligible
            ));
            
        } catch (Exception e) {
            log.error("Error checking eligibility", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // ==================== Helper Classes ====================

    /**
     * Availability status request
     */
    @lombok.Data
    public static class AvailabilityRequest {
        private boolean isAvailable;

        public boolean isAvailable() { return isAvailable; }
        public void setAvailable(boolean available) { isAvailable = available; }
    }

    /**
     * Donation request
     */
    @lombok.Data
    public static class DonationRequest {
        private Double units;
        private Map<String, Boolean> testResults;

        public Double getUnits() { return units; }
        public Map<String, Boolean> getTestResults() { return testResults; }
    }
}
