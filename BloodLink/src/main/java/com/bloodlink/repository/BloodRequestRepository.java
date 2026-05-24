package com.bloodlink.repository;

import com.bloodlink.entity.BloodRequest;
import com.bloodlink.entity.Donor;
import com.bloodlink.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository Interface for BloodRequest Entity
 */
@Repository
public interface BloodRequestRepository extends JpaRepository<BloodRequest, Long> {

    // ============ BASIC QUERIES ============
    Optional<BloodRequest> findByRequestId(Long requestId);

    // ============ PATIENT QUERIES ============
    /**
     * Find all requests from a patient
     */
    List<BloodRequest> findByPatient(Patient patient);

    /**
     * Find pending requests for a patient
     */
    List<BloodRequest> findByPatientAndStatus(Patient patient, BloodRequest.RequestStatus status);

    /**
     * Find patient's active requests
     */
    @Query("SELECT br FROM BloodRequest br WHERE br.patient.userId = :patientId AND br.status = 'PENDING'")
    List<BloodRequest> findPatientPendingRequests(@Param("patientId") Long patientId);

    // ============ DONOR QUERIES ============
    /**
     * Find all requests sent to a donor
     */
    List<BloodRequest> findByDonor(Donor donor);

    /**
     * Find pending requests for a donor
     */
    List<BloodRequest> findByDonorAndStatus(Donor donor, BloodRequest.RequestStatus status);

    /**
     * Find donor's active requests
     */
    @Query("SELECT br FROM BloodRequest br WHERE br.donor.userId = :donorId AND br.status = 'PENDING'")
    List<BloodRequest> findDonorPendingRequests(@Param("donorId") Long donorId);

    // ============ STATUS QUERIES ============
    /**
     * Find requests by status
     */
    List<BloodRequest> findByStatus(BloodRequest.RequestStatus status);

    /**
     * Find all pending requests
     */
    List<BloodRequest> findByStatusPending(BloodRequest.RequestStatus status);

    /**
     * Find completed requests
     */
    @Query("SELECT br FROM BloodRequest br WHERE br.status = 'COMPLETED'")
    List<BloodRequest> findCompletedRequests();

    /**
     * Find accepted requests
     */
    @Query("SELECT br FROM BloodRequest br WHERE br.status = 'ACCEPTED'")
    List<BloodRequest> findAcceptedRequests();

    // ============ URGENCY QUERIES ============
    /**
     * Find critical requests
     */
    @Query("SELECT br FROM BloodRequest br WHERE br.urgencyLevel IN ('CRITICAL', 'LIFE_THREATENING') " +
           "AND br.status = 'PENDING' ORDER BY br.createdAt ASC")
    List<BloodRequest> findCriticalPendingRequests();

    /**
     * Find urgent requests
     */
    @Query("SELECT br FROM BloodRequest br WHERE br.urgencyLevel = 'URGENT' " +
           "AND br.status = 'PENDING' ORDER BY br.createdAt ASC")
    List<BloodRequest> findUrgentPendingRequests();

    // ============ BLOOD GROUP QUERIES ============
    /**
     * Find requests for specific blood group
     */
    List<BloodRequest> findByBloodGroupAndStatus(String bloodGroup, BloodRequest.RequestStatus status);

    /**
     * Find all pending requests for specific blood group
     */
    @Query("SELECT br FROM BloodRequest br WHERE br.bloodGroup = :bloodGroup AND br.status = 'PENDING'")
    List<BloodRequest> findPendingRequestsByBloodGroup(@Param("bloodGroup") String bloodGroup);

    // ============ TIME-BASED QUERIES ============
    /**
     * Find requests created within time period
     */
    List<BloodRequest> findByCreatedAtAfter(LocalDateTime dateTime);

    /**
     * Find requests created between dates
     */
    @Query("SELECT br FROM BloodRequest br WHERE br.createdAt BETWEEN :startDate AND :endDate")
    List<BloodRequest> findRequestsBetweenDates(@Param("startDate") LocalDateTime startDate, 
                                                @Param("endDate") LocalDateTime endDate);

    /**
     * Find expired requests (older than 7 days and pending)
     */
    @Query("SELECT br FROM BloodRequest br WHERE br.status = 'PENDING' AND br.createdAt <= DATE_SUB(NOW(), INTERVAL 7 DAY)")
    List<BloodRequest> findExpiredRequests();

    // ============ STATISTICS QUERIES ============
    /**
     * Count requests by status
     */
    long countByStatus(BloodRequest.RequestStatus status);

    /**
     * Count pending requests for donor
     */
    long countByDonorAndStatus(Donor donor, BloodRequest.RequestStatus status);

    /**
     * Count total requests from patient
     */
    long countByPatient(Patient patient);

    /**
     * Get request success rate for donor
     */
    @Query("SELECT COUNT(CASE WHEN br.status = 'ACCEPTED' THEN 1 END) * 100 / COUNT(*) " +
           "FROM BloodRequest br WHERE br.donor = :donor")
    Double getDonorAcceptanceRate(@Param("donor") Donor donor);
}
