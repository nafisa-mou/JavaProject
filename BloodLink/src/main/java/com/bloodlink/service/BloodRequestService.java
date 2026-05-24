package com.bloodlink.service;

import com.bloodlink.entity.*;
import com.bloodlink.exception.BloodLinkExceptions.*;
import com.bloodlink.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * BloodRequestService - Handles all blood request operations
 * 
 * Responsibilities:
 * - Blood request lifecycle management
 * - Request status transitions (PENDING → ACCEPTED/DECLINED → COMPLETED)
 * - Request matching with suitable donors
 * - Priority scoring for urgent requests
 * - Request expiration handling
 * 
 * OOP Principles Applied:
 * - Encapsulation: Hides complex request state management
 * - Abstraction: Provides high-level request operations
 * - Single Responsibility: Only handles blood request logic
 * 
 * SOLID Principles Applied:
 * - S: Single Responsibility - Only blood request logic
 * - D: Dependency Inversion - Depends on repository abstractions
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BloodRequestService {

    private final BloodRequestRepository bloodRequestRepository;
    private final DonorRepository donorRepository;
    private final PatientRepository patientRepository;
    private final NotificationService notificationService;
    private final DonorService donorService;

    /**
     * Get blood request by ID
     * 
     * @param requestId Request ID
     * @return BloodRequest
     * @throws ResourceNotFoundException if not found
     */
    public BloodRequest getRequestById(Long requestId) {
        log.debug("Fetching blood request: {}", requestId);
        
        return bloodRequestRepository.findById(requestId)
            .orElseThrow(() -> BloodLinkExceptions.requestNotFound());
    }

    /**
     * Get all pending requests
     * 
     * @return List of pending blood requests
     */
    public List<BloodRequest> getAllPendingRequests() {
        log.debug("Fetching all pending blood requests");
        return bloodRequestRepository.findByStatus(BloodRequest.RequestStatus.PENDING);
    }

    /**
     * Get critical pending requests (CRITICAL or LIFE_THREATENING)
     * Business Logic: Returns urgent requests that need immediate attention
     * 
     * @return List of critical pending requests
     */
    public List<BloodRequest> getCriticalPendingRequests() {
        log.info("Fetching critical pending blood requests");
        return bloodRequestRepository.findCriticalPendingRequests();
    }

    /**
     * Accept blood request (donor accepting)
     * Business Logic: Validates donor eligibility, updates request status, sends notifications
     * Encapsulation: Hides state transition logic
     * 
     * @param requestId Request ID
     * @param donorId Donor ID accepting the request
     * @throws InvalidOperationException if request cannot be accepted
     * @throws UnauthorizedException if donor not eligible
     */
    public BloodRequest acceptRequest(Long requestId, Long donorId) {
        log.info("Donor {} accepting blood request {}", donorId, requestId);
        
        BloodRequest request = bloodRequestRepository.findById(requestId)
            .orElseThrow(() -> BloodLinkExceptions.requestNotFound());
        
        Donor donor = donorRepository.findById(donorId)
            .orElseThrow(() -> BloodLinkExceptions.donorNotFound());
        
        // Validation - Encapsulation
        if (!request.getStatus().equals(BloodRequest.RequestStatus.PENDING)) {
            throw new InvalidOperationException("Request is not in PENDING status");
        }
        
        // Check donor eligibility
        if (!donorService.isDonorEligibleForDonation(donor)) {
            throw new UnauthorizedException("Donor is not eligible to donate");
        }
        
        // Check blood group compatibility
        if (!donor.getBloodGroup().equals(request.getBloodGroup())) {
            throw new InvalidOperationException("Donor blood group does not match request");
        }
        
        try {
            // Update request status
            request.setDonor(donor);
            request.setStatus(BloodRequest.RequestStatus.ACCEPTED);
            request.setAcceptedAt(LocalDateTime.now());
            request.setUpdatedAt(LocalDateTime.now());
            
            BloodRequest saved = bloodRequestRepository.save(request);
            log.info("Blood request accepted: {}", requestId);
            
            // Notify patient of acceptance
            notificationService.notifyPatientOfAcceptance(
                request.getPatient(), 
                donor, 
                request
            );
            
            // Start chat between donor and patient
            // This would involve ChatService
            
            return saved;
            
        } catch (Exception e) {
            log.error("Error accepting blood request", e);
            throw new InvalidOperationException("Failed to accept request: " + e.getMessage());
        }
    }

    /**
     * Decline blood request (donor declining)
     * Business Logic: Updates request status, allows request to be offered to other donors
     * 
     * @param requestId Request ID
     * @param donorId Donor ID declining the request
     * @param reason Reason for declining
     * @throws InvalidOperationException if request cannot be declined
     */
    public BloodRequest declineRequest(Long requestId, Long donorId, String reason) {
        log.info("Donor {} declining blood request {}", donorId, requestId);
        
        BloodRequest request = bloodRequestRepository.findById(requestId)
            .orElseThrow(() -> BloodLinkExceptions.requestNotFound());
        
        // For now, just update status (in production, track decline history)
        // Status stays PENDING for other donors to accept
        
        try {
            // Update last declined info
            request.setUpdatedAt(LocalDateTime.now());
            BloodRequest saved = bloodRequestRepository.save(request);
            
            // Find donor who is declining
            Donor donor = donorRepository.findById(donorId)
                .orElseThrow(() -> BloodLinkExceptions.donorNotFound());
            
            log.info("Blood request declined by donor: {}", donorId);
            
            // Notify patient of decline
            notificationService.notifyPatientOfDecline(
                request.getPatient(), 
                donor, 
                request
            );
            
            return saved;
            
        } catch (Exception e) {
            log.error("Error declining blood request", e);
            throw new InvalidOperationException("Failed to decline request: " + e.getMessage());
        }
    }

    /**
     * Complete blood request (mark as completed after donation)
     * Business Logic: Verifies donation was completed, updates status
     * 
     * @param requestId Request ID
     * @param donationDetails Details of the completed donation
     * @return Completed BloodRequest
     * @throws InvalidOperationException if request cannot be completed
     */
    public BloodRequest completeRequest(Long requestId, Map<String, Object> donationDetails) {
        log.info("Completing blood request: {}", requestId);
        
        BloodRequest request = bloodRequestRepository.findById(requestId)
            .orElseThrow(() -> BloodLinkExceptions.requestNotFound());
        
        // Validation
        if (!request.getStatus().equals(BloodRequest.RequestStatus.ACCEPTED)) {
            throw new InvalidOperationException("Only ACCEPTED requests can be completed");
        }
        
        if (request.getDonor() == null) {
            throw new InvalidOperationException("No donor assigned to this request");
        }
        
        try {
            // Update request status
            request.setStatus(BloodRequest.RequestStatus.COMPLETED);
            request.setCompletedAt(LocalDateTime.now());
            request.setUpdatedAt(LocalDateTime.now());
            
            BloodRequest saved = bloodRequestRepository.save(request);
            log.info("Blood request completed: {}", requestId);
            
            // Notify donor of completion
            notificationService.notifyDonorOfCompletion(request.getDonor(), request);
            
            // Record donation in donation history
            // This would involve DonationHistoryService
            
            return saved;
            
        } catch (Exception e) {
            log.error("Error completing blood request", e);
            throw new InvalidOperationException("Failed to complete request: " + e.getMessage());
        }
    }

    /**
     * Find suitable donors for a blood request
     * Business Logic: Matches donors based on blood group, location, availability
     * 
     * @param requestId Request ID
     * @return List of suitable donors sorted by match score
     */
    public List<Donor> findSuitableDonors(Long requestId) {
        log.info("Finding suitable donors for request: {}", requestId);
        
        BloodRequest request = bloodRequestRepository.findById(requestId)
            .orElseThrow(() -> BloodLinkExceptions.requestNotFound());
        
        Patient patient = request.getPatient();
        
        // Find donors with matching blood group
        List<Donor> matchingDonors = donorRepository
            .findByBloodGroupAndIsAvailableTrueAndIsActiveTrue(request.getBloodGroup());
        
        // Filter by location proximity and eligibility
        return matchingDonors.stream()
            .filter(donor -> {
                // Check donation eligibility
                if (!donorService.isDonorEligibleForDonation(donor)) {
                    return false;
                }
                
                // Check location (within reasonable distance)
                double distance = calculateDistance(
                    patient.getLatitude(), patient.getLongitude(),
                    donor.getLatitude(), donor.getLongitude()
                );
                
                return distance <= 50; // 50 km radius
            })
            .sorted((d1, d2) -> {
                // Sort by distance from patient
                double dist1 = calculateDistance(
                    patient.getLatitude(), patient.getLongitude(),
                    d1.getLatitude(), d1.getLongitude()
                );
                
                double dist2 = calculateDistance(
                    patient.getLatitude(), patient.getLongitude(),
                    d2.getLatitude(), d2.getLongitude()
                );
                
                return Double.compare(dist1, dist2);
            })
            .collect(Collectors.toList());
    }

    /**
     * Notify nearby donors of a blood request
     * Business Logic: Sends notifications to suitable donors for manual acceptance
     * 
     * @param requestId Request ID
     */
    public void notifyNearbyDonors(Long requestId) {
        log.info("Notifying nearby donors for request: {}", requestId);
        
        BloodRequest request = bloodRequestRepository.findById(requestId)
            .orElseThrow(() -> BloodLinkExceptions.requestNotFound());
        
        try {
            List<Donor> suitableDonors = findSuitableDonors(requestId);
            
            suitableDonors.forEach(donor -> {
                try {
                    notificationService.notifyDonorOfRequest(donor, request);
                } catch (Exception e) {
                    log.warn("Failed to notify donor: {}", donor.getUserId(), e);
                }
            });
            
            log.info("Notified {} donors for request: {}", suitableDonors.size(), requestId);
            
        } catch (Exception e) {
            log.error("Error notifying donors", e);
        }
    }

    /**
     * Get request priority score
     * Business Logic: Combines urgency level and time elapsed since creation
     * 
     * @param requestId Request ID
     * @return Priority score (0-100)
     */
    public double getRequestPriorityScore(Long requestId) {
        log.debug("Calculating priority score for request: {}", requestId);
        
        BloodRequest request = bloodRequestRepository.findById(requestId)
            .orElseThrow(() -> BloodLinkExceptions.requestNotFound());
        
        // Base score from emergency level
        double score = 0;
        switch (request.getEmergencyLevel()) {
            case ROUTINE:
                score = 10;
                break;
            case URGENT:
                score = 50;
                break;
            case CRITICAL:
                score = 75;
                break;
            case LIFE_THREATENING:
                score = 100;
                break;
        }
        
        // Time-based adjustment (older requests get higher priority)
        if (request.getCreatedAt() != null) {
            long hoursElapsed = java.time.temporal.ChronoUnit.HOURS.between(
                request.getCreatedAt(), 
                LocalDateTime.now()
            );
            
            double timeBonus = Math.min(hoursElapsed * 2, 20); // Max 20 point bonus
            score += timeBonus;
        }
        
        return Math.min(score, 100);
    }

    /**
     * Get blood requests by status
     * 
     * @param status Request status
     * @return List of requests with that status
     */
    public List<BloodRequest> getRequestsByStatus(BloodRequest.RequestStatus status) {
        log.debug("Fetching requests by status: {}", status);
        return bloodRequestRepository.findByStatus(status);
    }

    /**
     * Get expired requests (no acceptance after 24 hours)
     * 
     * @return List of expired requests
     */
    public List<BloodRequest> getExpiredRequests() {
        log.debug("Fetching expired requests");
        return bloodRequestRepository.findExpiredRequests();
    }

    /**
     * Cancel expired requests
     * Business Logic: Marks requests as expired if no donor accepts within time limit
     */
    public void handleExpiredRequests() {
        log.info("Handling expired blood requests");
        
        try {
            List<BloodRequest> expiredRequests = getExpiredRequests();
            
            expiredRequests.forEach(request -> {
                try {
                    request.setStatus(BloodRequest.RequestStatus.EXPIRED);
                    request.setUpdatedAt(LocalDateTime.now());
                    bloodRequestRepository.save(request);
                    
                    log.info("Request marked as expired: {}", request.getRequestId());
                } catch (Exception e) {
                    log.warn("Error marking request as expired: {}", request.getRequestId(), e);
                }
            });
            
        } catch (Exception e) {
            log.error("Error handling expired requests", e);
        }
    }

    /**
     * Get request statistics
     * 
     * @return Map with request statistics
     */
    public Map<String, Object> getRequestStatistics() {
        log.debug("Fetching request statistics");
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRequests", bloodRequestRepository.count());
        stats.put("pendingRequests", bloodRequestRepository.findByStatus(BloodRequest.RequestStatus.PENDING).size());
        stats.put("acceptedRequests", bloodRequestRepository.findByStatus(BloodRequest.RequestStatus.ACCEPTED).size());
        stats.put("completedRequests", bloodRequestRepository.findByStatus(BloodRequest.RequestStatus.COMPLETED).size());
        stats.put("declinedRequests", bloodRequestRepository.findByStatus(BloodRequest.RequestStatus.DECLINED).size());
        stats.put("expiredRequests", bloodRequestRepository.findByStatus(BloodRequest.RequestStatus.EXPIRED).size());
        stats.put("criticalRequests", getCriticalPendingRequests().size());
        
        return stats;
    }

    // ==================== Helper Methods ====================

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
}
