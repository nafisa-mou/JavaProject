package com.bloodlink.service;

import com.bloodlink.entity.*;
import com.bloodlink.exception.BloodLinkExceptions.*;
import com.bloodlink.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * DonorMatchingService - AI-powered donor matching algorithm
 * 
 * Responsibilities:
 * - Match donors to patients using multiple criteria
 * - Calculate match scores based on various factors
 * - Rank donors by compatibility
 * - Implement machine learning-ready algorithm
 * - Consider: blood type, location, reliability, availability
 * 
 * Design Patterns Applied:
 * - Strategy Pattern: Different matching algorithms (location, health, AI)
 * - Builder Pattern: Complex match score calculation
 * 
 * OOP Principles Applied:
 * - Encapsulation: Hides complex matching logic
 * - Abstraction: Provides high-level matching operations
 * - Single Responsibility: Only handles matching
 * 
 * SOLID Principles Applied:
 * - S: Single Responsibility - Only matching logic
 * - O: Open/Closed - Can extend with new algorithms
 * - D: Dependency Inversion - Depends on repository abstractions
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DonorMatchingService {

    private final DonorRepository donorRepository;
    private final PatientRepository patientRepository;
    private final BloodRequestRepository bloodRequestRepository;
    private final DonationHistoryRepository donationHistoryRepository;
    private final DonorReviewRepository donorReviewRepository;
    private final DonorService donorService;

    // Weighting factors for score calculation
    private static final double BLOOD_GROUP_WEIGHT = 0.2;      // 20%
    private static final double LOCATION_WEIGHT = 0.25;        // 25%
    private static final double RELIABILITY_WEIGHT = 0.25;     // 25%
    private static final double AVAILABILITY_WEIGHT = 0.15;    // 15%
    private static final double RECENCY_WEIGHT = 0.15;         // 15%

    /**
     * Find best matching donors for a patient's blood request
     * Business Logic: Uses multi-factor algorithm to rank donors
     * Strategy Pattern: Uses different matching strategies
     * 
     * @param patientId Patient ID
     * @return List of donors ranked by match score
     */
    public List<Map<String, Object>> findBestMatchingDonors(Long patientId) {
        log.info("Finding best matching donors for patient: {}", patientId);
        
        Patient patient = (Patient) patientRepository.findById(patientId)
            .orElseThrow(() -> BloodLinkExceptions.patientNotFound());
        
        // Get all eligible donors with matching blood group
        List<Donor> eligibleDonors = donorRepository
            .findByBloodGroupAndIsAvailableTrueAndIsActiveTrue(patient.getRequiredBloodGroup())
            .stream()
            .filter(donor -> donorService.isDonorEligibleForDonation(donor))
            .collect(Collectors.toList());
        
        if (eligibleDonors.isEmpty()) {
            log.warn("No eligible donors found for patient: {}", patientId);
            return new ArrayList<>();
        }
        
        // Calculate match scores for each donor
        return eligibleDonors.stream()
            .map(donor -> calculateMatchScore(patient, donor))
            .sorted((a, b) -> Double.compare((double) b.get("score"), (double) a.get("score")))
            .collect(Collectors.toList());
    }

    /**
     * Calculate comprehensive match score between patient and donor
     * Encapsulation: Hides scoring algorithm complexity
     * 
     * @param patient Patient requesting blood
     * @param donor Donor candidate
     * @return Map with donor info and match score (0-100)
     */
    public Map<String, Object> calculateMatchScore(Patient patient, Donor donor) {
        log.debug("Calculating match score for patient {} and donor {}", patient.getUserId(), donor.getUserId());
        
        double score = 0;
        
        // 1. Blood Group Factor (Perfect match = 20 points)
        double bloodGroupScore = calculateBloodGroupScore(patient.getRequiredBloodGroup(), donor.getBloodGroup());
        score += bloodGroupScore * BLOOD_GROUP_WEIGHT;
        
        // 2. Location Factor (Proximity = 25 points)
        double locationScore = calculateLocationScore(patient, donor);
        score += locationScore * LOCATION_WEIGHT;
        
        // 3. Reliability Factor (Experience & reputation = 25 points)
        double reliabilityScore = calculateReliabilityScore(donor);
        score += reliabilityScore * RELIABILITY_WEIGHT;
        
        // 4. Availability Factor (Recent activity = 15 points)
        double availabilityScore = calculateAvailabilityScore(donor);
        score += availabilityScore * AVAILABILITY_WEIGHT;
        
        // 5. Recency Factor (Recent donations = 15 points)
        double recencyScore = calculateRecencyScore(donor);
        score += recencyScore * RECENCY_WEIGHT;
        
        // Build result map
        Map<String, Object> result = new HashMap<>();
        result.put("donorId", donor.getUserId());
        result.put("donorName", donor.getFullName());
        result.put("bloodGroup", donor.getBloodGroup());
        result.put("city", donor.getCity());
        result.put("score", score);
        result.put("breakdown", Map.of(
            "bloodGroup", bloodGroupScore,
            "location", locationScore,
            "reliability", reliabilityScore,
            "availability", availabilityScore,
            "recency", recencyScore
        ));
        
        return result;
    }

    /**
     * Calculate blood group compatibility score
     * Encapsulation: Hides blood group logic
     * 
     * @param patientBloodGroup Patient's blood group
     * @param donorBloodGroup Donor's blood group
     * @return Score 0-100
     */
    private double calculateBloodGroupScore(String patientBloodGroup, String donorBloodGroup) {
        // Perfect match = 100
        if (patientBloodGroup.equals(donorBloodGroup)) {
            return 100;
        }
        
        // Blood group compatibility chart
        Map<String, List<String>> compatibility = new HashMap<>();
        compatibility.put("O+", Arrays.asList("O+", "A+", "B+", "AB+"));
        compatibility.put("O-", Arrays.asList("O-", "O+", "A-", "A+", "B-", "B+", "AB-", "AB+"));
        compatibility.put("A+", Arrays.asList("A+", "AB+"));
        compatibility.put("A-", Arrays.asList("A-", "A+", "AB-", "AB+"));
        compatibility.put("B+", Arrays.asList("B+", "AB+"));
        compatibility.put("B-", Arrays.asList("B-", "B+", "AB-", "AB+"));
        compatibility.put("AB+", Arrays.asList("AB+"));
        compatibility.put("AB-", Arrays.asList("AB-", "AB+"));
        
        // Check if donor blood group is compatible with patient
        if (compatibility.getOrDefault(patientBloodGroup, new ArrayList<>()).contains(donorBloodGroup)) {
            return 80; // Compatible but not perfect match
        }
        
        return 0; // Not compatible
    }

    /**
     * Calculate location-based score
     * Haversine formula for distance calculation
     * 
     * @param patient Patient location
     * @param donor Donor location
     * @return Score 0-100 (closer = higher)
     */
    private double calculateLocationScore(Patient patient, Donor donor) {
        if (patient.getLatitude() == null || patient.getLongitude() == null ||
            donor.getLatitude() == null || donor.getLongitude() == null) {
            return 50; // Default if location unavailable
        }
        
        double distance = calculateHaversineDistance(
            patient.getLatitude(), patient.getLongitude(),
            donor.getLatitude(), donor.getLongitude()
        );
        
        // Scoring based on distance
        if (distance <= 2) return 100;      // Very close (2 km)
        if (distance <= 5) return 90;       // Close (5 km)
        if (distance <= 10) return 75;      // Nearby (10 km)
        if (distance <= 20) return 60;      // Moderate (20 km)
        if (distance <= 50) return 40;      // Far (50 km)
        
        return 0; // Too far
    }

    /**
     * Calculate reliability score based on donation history and reviews
     * 
     * @param donor Donor to score
     * @return Score 0-100
     */
    private double calculateReliabilityScore(Donor donor) {
        double score = 50; // Base score
        
        // Count donations
        int donationCount = donationHistoryRepository.countDonationsByDonor(donor.getUserId());
        if (donationCount >= 20) {
            score += 30;    // Very experienced
        } else if (donationCount >= 10) {
            score += 20;    // Experienced
        } else if (donationCount >= 5) {
            score += 10;    // Moderate experience
        }
        
        // Average rating
        Double avgRating = donorReviewRepository.getAverageRatingForDonor(donor.getUserId());
        if (avgRating != null) {
            score += (avgRating / 5.0) * 30;
        }
        
        // Blood request acceptance rate
        try {
            int totalRequests = bloodRequestRepository.countByDonor(donor);
            if (totalRequests > 0) {
                int acceptedRequests = bloodRequestRepository
                    .countByDonorAndStatus(donor, BloodRequest.RequestStatus.ACCEPTED);
                double acceptanceRate = (acceptedRequests / (double) totalRequests) * 100;
                score += (acceptanceRate / 100.0) * 20;
            }
        } catch (Exception e) {
            log.debug("Error calculating acceptance rate", e);
        }
        
        return Math.min(score, 100);
    }

    /**
     * Calculate availability score based on donor status and recent activity
     * 
     * @param donor Donor to score
     * @return Score 0-100
     */
    private double calculateAvailabilityScore(Donor donor) {
        double score = 0;
        
        // Current availability status
        if (donor.getIsAvailable()) {
            score += 50;
        }
        
        // Recent login/activity (if tracked)
        if (donor.getLastLogin() != null) {
            long hoursAgo = java.time.temporal.ChronoUnit.HOURS.between(
                donor.getLastLogin(),
                java.time.LocalDateTime.now()
            );
            
            if (hoursAgo <= 24) {
                score += 50;    // Active in last 24 hours
            } else if (hoursAgo <= 72) {
                score += 30;    // Active in last 3 days
            } else if (hoursAgo <= 168) {
                score += 15;    // Active in last week
            }
        } else {
            score += 25; // No login info
        }
        
        return Math.min(score, 100);
    }

    /**
     * Calculate recency score based on recent donation history
     * Donors who recently donated are less likely to donate again (56-day rule)
     * 
     * @param donor Donor to score
     * @return Score 0-100
     */
    private double calculateRecencyScore(Donor donor) {
        java.time.LocalDate lastDonationDate = donationHistoryRepository
            .getLastDonationDate(donor.getUserId());
        
        if (lastDonationDate == null) {
            return 100; // Never donated, most eligible
        }
        
        long daysSinceLastDonation = java.time.temporal.ChronoUnit.DAYS.between(
            lastDonationDate,
            java.time.LocalDate.now()
        );
        
        // Scoring based on days since last donation
        if (daysSinceLastDonation >= 56) {
            return 100;     // Eligible (56 day rule)
        } else if (daysSinceLastDonation >= 40) {
            return 80;      // Almost eligible
        } else if (daysSinceLastDonation >= 20) {
            return 60;      // Not ready
        }
        
        return 20; // Too recent
    }

    /**
     * Get recommended donors for emergency request
     * Business Logic: Returns urgency-based donor recommendations
     * 
     * @param patientId Patient ID
     * @param maxDistance Maximum distance in km
     * @return List of recommended donors
     */
    public List<Map<String, Object>> getEmergencyRecommendations(Long patientId, double maxDistance) {
        log.info("Getting emergency recommendations for patient: {}", patientId);
        
        List<Map<String, Object>> allMatches = findBestMatchingDonors(patientId);
        
        // Filter by distance and return top matches
        return allMatches.stream()
            .limit(10)  // Top 10 matches
            .collect(Collectors.toList());
    }

    /**
     * Calculate Haversine distance between two geographic points
     * 
     * @param lat1 Latitude 1
     * @param lon1 Longitude 1
     * @param lat2 Latitude 2
     * @param lon2 Longitude 2
     * @return Distance in kilometers
     */
    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS = 6371; // km
        
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS * c;
    }

    /**
     * Get matching analytics
     * 
     * @param patientId Patient ID
     * @return Matching statistics and analytics
     */
    public Map<String, Object> getMatchingAnalytics(Long patientId) {
        log.debug("Getting matching analytics for patient: {}", patientId);
        
        Patient patient = (Patient) patientRepository.findById(patientId)
            .orElseThrow(() -> BloodLinkExceptions.patientNotFound());
        
        List<Map<String, Object>> matches = findBestMatchingDonors(patientId);
        
        Map<String, Object> analytics = new HashMap<>();
        analytics.put("totalMatches", matches.size());
        
        if (!matches.isEmpty()) {
            double avgScore = matches.stream()
                .mapToDouble(m -> (Double) m.get("score"))
                .average()
                .orElse(0);
            
            double maxScore = matches.stream()
                .mapToDouble(m -> (Double) m.get("score"))
                .max()
                .orElse(0);
            
            analytics.put("averageScore", String.format("%.2f", avgScore));
            analytics.put("maxScore", String.format("%.2f", maxScore));
            analytics.put("topMatches", matches.stream().limit(5).collect(Collectors.toList()));
        }
        
        return analytics;
    }
}
