package com.bloodlink.service;

import com.bloodlink.dto.UserDTO;
import com.bloodlink.dto.UserDTO.DonorDTO;
import com.bloodlink.entity.*;
import com.bloodlink.exception.BloodLinkExceptions.*;
import com.bloodlink.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * DonorService - Handles all donor-related operations
 * 
 * Responsibilities:
 * - Donor registration and profile management
 * - Availability status management
 * - Eligibility checking for donations
 * - Reliability scoring
 * - Search and filter operations
 * - Location-based queries
 * 
 * OOP Principles Applied:
 * - Encapsulation: Hides business logic for donor operations
 * - Polymorphism: Works with User hierarchy (Donor extends User)
 * - Abstraction: Provides high-level donor operations
 * - Single Responsibility: Only handles donor operations
 * 
 * SOLID Principles Applied:
 * - S: Single Responsibility - Only donor logic
 * - O: Open/Closed - Can extend with new matching algorithms
 * - L: Liskov Substitution - Works with Donor as User
 * - I: Interface Segregation - Specific repository interfaces
 * - D: Dependency Inversion - Depends on repository abstractions
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DonorService {

    private final DonorRepository donorRepository;
    private final UserRepository userRepository;
    private final DonationHistoryRepository donationHistoryRepository;
    private final DonorReviewRepository donorReviewRepository;
    private final BloodRequestRepository bloodRequestRepository;
    private final NotificationService notificationService;

    /**
     * Get all donors
     * 
     * @return List of all donors
     */
    public List<DonorDTO> getAllDonors() {
        log.debug("Fetching all donors");
        return donorRepository.findAll()
            .stream()
            .map(this::convertToDonorDTO)
            .collect(Collectors.toList());
    }

    /**
     * Get donor by ID
     * Business Logic: Validates donor exists and is active
     * 
     * @param donorId Donor ID
     * @return DonorDTO with donor details
     * @throws ResourceNotFoundException if donor not found
     */
    public DonorDTO getDonorById(Long donorId) {
        log.debug("Fetching donor by ID: {}", donorId);
        
        Donor donor = donorRepository.findById(donorId)
            .orElseThrow(() -> BloodLinkExceptions.donorNotFound());
        
        if (!donor.getIsActive()) {
            throw new UnauthorizedException("Donor account is inactive");
        }
        
        return convertToDonorDTO(donor);
    }

    /**
     * Search donors by blood group
     * Business Logic: Returns only available, verified, active donors
     * Encapsulation: Hides filtering logic
     * 
     * @param bloodGroup Blood group (e.g., "O+")
     * @return List of available donors with that blood group
     */
    public List<DonorDTO> searchByBloodGroup(String bloodGroup) {
        log.info("Searching donors by blood group: {}", bloodGroup);
        
        List<Donor> donors = donorRepository
            .findByBloodGroupAndIsAvailableTrueAndIsActiveTrue(bloodGroup);
        
        return donors.stream()
            .map(this::convertToDonorDTO)
            .collect(Collectors.toList());
    }

    /**
     * Find nearby donors
     * Business Logic: Uses geospatial calculation with distance threshold
     * Polymorphism: Works with any User location data
     * 
     * @param latitude User latitude
     * @param longitude User longitude
     * @param radiusKm Search radius in kilometers
     * @return List of nearby donors within radius
     */
    public List<DonorDTO> findNearbyDonors(Double latitude, Double longitude, Double radiusKm) {
        log.info("Finding nearby donors at ({}, {}) within {} km", latitude, longitude, radiusKm);
        
        if (latitude == null || longitude == null || radiusKm == null) {
            throw new ValidationException("Latitude, longitude, and radius are required");
        }
        
        if (radiusKm <= 0) {
            throw new ValidationException("Radius must be greater than 0");
        }
        
        // Get all active donors
        List<Donor> allDonors = donorRepository.findAllActive();
        
        // Filter by distance and availability
        return allDonors.stream()
            .filter(Donor::getIsAvailable)
            .filter(donor -> calculateDistance(latitude, longitude, 
                donor.getLatitude(), donor.getLongitude()) <= radiusKm)
            .map(this::convertToDonorDTO)
            .sorted(Comparator.comparingDouble(dto -> 
                calculateDistance(latitude, longitude, 
                    Double.parseDouble(String.valueOf(dto.getLatitude())), 
                    Double.parseDouble(String.valueOf(dto.getLongitude())))))
            .collect(Collectors.toList());
    }

    /**
     * Find eligible donors for a patient request
     * Business Logic: Combines multiple criteria for donor matching
     * Encapsulation: Hides complex matching logic
     * 
     * @param patientId Patient ID
     * @return List of eligible donors sorted by match score
     */
    public List<DonorDTO> findEligibleDonors(Long patientId) {
        log.info("Finding eligible donors for patient: {}", patientId);
        
        // Get patient
        Patient patient = (Patient) userRepository.findById(patientId)
            .orElseThrow(() -> BloodLinkExceptions.patientNotFound());
        
        // Find donors with matching blood group
        List<Donor> matchingDonors = donorRepository
            .findByBloodGroupAndIsAvailableTrueAndIsActiveTrue(patient.getRequiredBloodGroup());
        
        // Filter and sort by proximity, reliability
        return matchingDonors.stream()
            .filter(donor -> isDonorEligibleForDonation(donor))
            .map(donor -> {
                DonorDTO dto = convertToDonorDTO(donor);
                // Calculate match score (distance, reliability, recent donations)
                dto.setMatchScore(calculateMatchScore(donor, patient));
                return dto;
            })
            .sorted(Comparator.comparingDouble(DonorDTO::getMatchScore).reversed())
            .collect(Collectors.toList());
    }

    /**
     * Set donor availability status
     * Business Logic: Validates donor is verified and active before allowing status change
     * Encapsulation: Hides validation logic
     * 
     * @param donorId Donor ID
     * @param isAvailable New availability status
     * @throws UnauthorizedException if donor not verified/active
     */
    public void setAvailabilityStatus(Long donorId, Boolean isAvailable) {
        log.info("Setting availability for donor {}: {}", donorId, isAvailable);
        
        Donor donor = donorRepository.findById(donorId)
            .orElseThrow(() -> BloodLinkExceptions.donorNotFound());
        
        // Validation - Encapsulation principle
        if (!donor.getIsVerified()) {
            throw new UnauthorizedException("Donor must be verified before setting availability");
        }
        
        if (!donor.getIsActive()) {
            throw new UnauthorizedException("Donor account is inactive");
        }
        
        // Check donation eligibility if setting to available
        if (isAvailable && !isDonorEligibleForDonation(donor)) {
            throw new InvalidOperationException("Donor is not eligible for donation at this time");
        }
        
        donor.setIsAvailable(isAvailable);
        donorRepository.save(donor);
        
        log.info("Availability updated for donor: {}", donorId);
        
        // Notify if becoming available
        if (isAvailable) {
            notifyPatientOfAvailableDonor(donor);
        }
    }

    /**
     * Check if donor is eligible for donation
     * Business Logic: Validates against 56-day rule and other criteria
     * Encapsulation: Hides eligibility checking logic
     * 
     * @param donor Donor to check
     * @return true if donor can donate, false otherwise
     */
    public boolean isDonorEligibleForDonation(Donor donor) {
        // Check if verified
        if (!donor.getIsVerified() || !donor.getIsActive()) {
            return false;
        }
        
        // Get last donation date
        LocalDate lastDonationDate = donationHistoryRepository
            .getLastDonationDate(donor.getUserId());
        
        // Check 56-day minimum interval
        if (lastDonationDate != null) {
            LocalDate earliestNextDonation = lastDonationDate.plusDays(56);
            if (LocalDate.now().isBefore(earliestNextDonation)) {
                log.debug("Donor {} not eligible - last donation too recent", donor.getUserId());
                return false;
            }
        }
        
        // Check medical record if available
        // In production, would check hemoglobin, blood pressure, etc.
        
        return true;
    }

    /**
     * Get donor reliability score
     * Business Logic: Calculates score from donations, reviews, and acceptance rate
     * Encapsulation: Hides complex scoring logic
     * 
     * @param donorId Donor ID
     * @return Reliability score (0-100)
     */
    public Integer getDonorReliabilityScore(Long donorId) {
        log.debug("Calculating reliability score for donor: {}", donorId);
        
        Donor donor = donorRepository.findById(donorId)
            .orElseThrow(() -> BloodLinkExceptions.donorNotFound());
        
        return calculateReliabilityScore(donor);
    }

    /**
     * Get donor profile with all details
     * 
     * @param donorId Donor ID
     * @return Complete donor profile
     */
    public Map<String, Object> getDonorProfile(Long donorId) {
        log.debug("Fetching complete profile for donor: {}", donorId);
        
        Donor donor = donorRepository.findById(donorId)
            .orElseThrow(() -> BloodLinkExceptions.donorNotFound());
        
        Map<String, Object> profile = new HashMap<>();
        profile.put("donor", convertToDonorDTO(donor));
        profile.put("totalDonations", donationHistoryRepository.countDonationsByDonor(donorId));
        profile.put("totalUnits", donationHistoryRepository.getTotalUnitsDonatedByDonor(donorId));
        profile.put("averageRating", donorReviewRepository.getAverageRatingForDonor(donorId));
        profile.put("reviewCount", donorReviewRepository.countReviewsForDonor(donorId));
        profile.put("reliabilityScore", calculateReliabilityScore(donor));
        profile.put("acceptanceRate", calculateAcceptanceRate(donor));
        
        return profile;
    }

    /**
     * Get donor donation history
     * 
     * @param donorId Donor ID
     * @return List of donation records
     */
    public List<DonationHistory> getDonationHistory(Long donorId) {
        log.debug("Fetching donation history for donor: {}", donorId);
        return donationHistoryRepository.findByDonorId(donorId);
    }

    /**
     * Record new donation
     * Business Logic: Validates eligibility, creates history record
     * 
     * @param donorId Donor ID
     * @param units Units collected
     * @param testResults Test results
     * @return Created DonationHistory record
     */
    public DonationHistory recordDonation(Long donorId, Double units, Map<String, Boolean> testResults) {
        log.info("Recording donation for donor: {}", donorId);
        
        Donor donor = donorRepository.findById(donorId)
            .orElseThrow(() -> BloodLinkExceptions.donorNotFound());
        
        // Validate eligibility
        if (!isDonorEligibleForDonation(donor)) {
            throw new InvalidOperationException("Donor is not eligible for donation");
        }
        
        try {
            DonationHistory history = new DonationHistory();
            history.setDonor(donor);
            history.setDonationDate(LocalDate.now());
            history.setUnitsCollected(units);
            
            // Set test results
            if (testResults != null) {
                history.setHbsAgResult(testResults.getOrDefault("hbsAg", false));
                history.setHcvResult(testResults.getOrDefault("hcv", false));
                history.setHivResult(testResults.getOrDefault("hiv", false));
                history.setVdrlResult(testResults.getOrDefault("vdrl", false));
            }
            
            DonationHistory saved = donationHistoryRepository.save(history);
            log.info("Donation recorded for donor: {}", donorId);
            
            return saved;
            
        } catch (Exception e) {
            log.error("Error recording donation", e);
            throw new InvalidOperationException("Failed to record donation: " + e.getMessage());
        }
    }

    /**
     * Update donor profile
     * 
     * @param donorId Donor ID
     * @param updates Map of fields to update
     */
    public void updateDonorProfile(Long donorId, Map<String, Object> updates) {
        log.info("Updating donor profile: {}", donorId);
        
        Donor donor = donorRepository.findById(donorId)
            .orElseThrow(() -> BloodLinkExceptions.donorNotFound());
        
        try {
            if (updates.containsKey("fullName")) {
                donor.setFullName((String) updates.get("fullName"));
            }
            if (updates.containsKey("phoneNumber")) {
                donor.setPhoneNumber((String) updates.get("phoneNumber"));
            }
            if (updates.containsKey("city")) {
                donor.setCity((String) updates.get("city"));
            }
            if (updates.containsKey("state")) {
                donor.setState((String) updates.get("state"));
            }
            if (updates.containsKey("latitude")) {
                donor.setLatitude(Double.parseDouble(updates.get("latitude").toString()));
            }
            if (updates.containsKey("longitude")) {
                donor.setLongitude(Double.parseDouble(updates.get("longitude").toString()));
            }
            
            donorRepository.save(donor);
            log.info("Donor profile updated: {}", donorId);
            
        } catch (Exception e) {
            log.error("Error updating donor profile", e);
            throw new ValidationException("Failed to update profile: " + e.getMessage());
        }
    }

    // ==================== Helper Methods ====================

    /**
     * Convert Donor entity to DonorDTO
     * Encapsulation: Hides DTO conversion logic
     * 
     * @param donor Donor entity
     * @return DonorDTO
     */
    private DonorDTO convertToDonorDTO(Donor donor) {
        return DonorDTO.builder()
            .userId(donor.getUserId())
            .fullName(donor.getFullName())
            .email(donor.getEmail())
            .phoneNumber(donor.getPhoneNumber())
            .city(donor.getCity())
            .state(donor.getState())
            .latitude(donor.getLatitude())
            .longitude(donor.getLongitude())
            .bloodGroup(donor.getBloodGroup())
            .isAvailable(donor.getIsAvailable())
            .isVerified(donor.getIsVerified())
            .userRole(donor.getUserRole())
            .build();
    }

    /**
     * Calculate distance between two geographic points using Haversine formula
     * 
     * @param lat1 First latitude
     * @param lon1 First longitude
     * @param lat2 Second latitude
     * @param lon2 Second longitude
     * @return Distance in kilometers
     */
    private double calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            return Double.MAX_VALUE;
        }
        
        final int EARTH_RADIUS = 6371; // Radius in kilometers
        
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS * c;
    }

    /**
     * Calculate match score for donor-patient pair
     * Score combines: proximity, reliability, availability
     * 
     * @param donor Donor to score
     * @param patient Patient requesting
     * @return Match score (0-100)
     */
    private double calculateMatchScore(Donor donor, Patient patient) {
        double score = 50; // Base score
        
        // Distance factor (proximity = higher score)
        double distance = calculateDistance(
            patient.getLatitude(), patient.getLongitude(),
            donor.getLatitude(), donor.getLongitude()
        );
        
        if (distance <= 5) {
            score += 30; // Very close
        } else if (distance <= 10) {
            score += 20; // Close
        } else if (distance <= 20) {
            score += 10; // Moderate
        }
        
        // Reliability factor
        Integer reliabilityScore = calculateReliabilityScore(donor);
        score += (reliabilityScore / 100.0) * 20;
        
        return Math.min(score, 100);
    }

    /**
     * Calculate reliability score for donor
     * Based on: donation frequency, acceptance rate, review ratings
     * 
     * @param donor Donor to score
     * @return Reliability score (0-100)
     */
    private Integer calculateReliabilityScore(Donor donor) {
        int score = 50; // Base score
        
        // Donation count factor
        int donationCount = donationHistoryRepository.countDonationsByDonor(donor.getUserId());
        if (donationCount >= 10) {
            score += 20;
        } else if (donationCount >= 5) {
            score += 10;
        }
        
        // Review rating factor
        Double avgRating = donorReviewRepository.getAverageRatingForDonor(donor.getUserId());
        if (avgRating != null) {
            score += (avgRating / 5.0) * 30;
        }
        
        // Acceptance rate factor
        double acceptanceRate = calculateAcceptanceRate(donor);
        score += (acceptanceRate / 100.0) * 10;
        
        return Math.min(score, 100);
    }

    /**
     * Calculate donor's blood request acceptance rate
     * 
     * @param donor Donor to calculate for
     * @return Acceptance rate percentage (0-100)
     */
    private double calculateAcceptanceRate(Donor donor) {
        try {
            int totalRequests = bloodRequestRepository.countByDonor(donor);
            if (totalRequests == 0) {
                return 50; // Default for new donors
            }
            
            int acceptedRequests = bloodRequestRepository
                .countByDonorAndStatus(donor, BloodRequest.RequestStatus.ACCEPTED);
            
            return (acceptedRequests / (double) totalRequests) * 100;
        } catch (Exception e) {
            log.warn("Error calculating acceptance rate for donor: {}", donor.getUserId(), e);
            return 50;
        }
    }

    /**
     * Notify patients of newly available donor
     * 
     * @param donor Available donor
     */
    private void notifyPatientOfAvailableDonor(Donor donor) {
        try {
            // Find patients needing this blood group
            // This would be done through database query or search service
            // For now, it's a placeholder for the notification system
            log.debug("Notifying patients of available donor: {}", donor.getUserId());
        } catch (Exception e) {
            log.warn("Error notifying patients", e);
        }
    }
}
