package com.bloodlink.repository;

import com.bloodlink.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository Interface for Patient Entity
 * Demonstrates specialized queries for patient operations
 */
@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByUserId(Long userId);

    // ============ BLOOD GROUP SEARCHES ============
    /**
     * Find patients needing specific blood group
     */
    List<Patient> findByRequiredBloodGroup(String bloodGroup);

    /**
     * Find active patients needing blood
     */
    List<Patient> findByIsActiveTrueAndIsVerifiedTrue();

    // ============ LOCATION SEARCHES ============
    /**
     * Find patients by city
     */
    List<Patient> findByCity(String city);

    /**
     * Find patients by hospital name
     */
    List<Patient> findByHospitalNameContainingIgnoreCase(String hospitalName);

    /**
     * Find patients within specified distance
     */
    @Query(value = "SELECT * FROM users u WHERE u.user_type = 'PATIENT' AND " +
            "SQRT(POWER(u.latitude - :latitude, 2) + POWER(u.longitude - :longitude, 2)) * 111.195 <= :radiusKm " +
            "ORDER BY SQRT(POWER(u.latitude - :latitude, 2) + POWER(u.longitude - :longitude, 2))", 
            nativeQuery = true)
    List<Patient> findNearbyPatients(@Param("latitude") Double latitude, 
                                      @Param("longitude") Double longitude, 
                                      @Param("radiusKm") Double radiusKm);

    // ============ EMERGENCY LEVEL QUERIES ============
    /**
     * Find critical/life-threatening patients
     */
    @Query("SELECT p FROM Patient p WHERE p.emergencyLevel IN " +
           "('CRITICAL', 'LIFE_THREATENING') AND p.isActive = true")
    List<Patient> findCriticalPatients();

    /**
     * Find urgent patients
     */
    @Query("SELECT p FROM Patient p WHERE p.emergencyLevel = 'URGENT' AND p.isActive = true")
    List<Patient> findUrgentPatients();

    // ============ REQUEST STATISTICS ============
    /**
     * Find patients with pending requests
     */
    @Query("SELECT DISTINCT p FROM Patient p WHERE p.pendingRequests > 0 AND p.isActive = true")
    List<Patient> findPatientsWithPendingRequests();

    /**
     * Find patients with most requests
     */
    @Query("SELECT p FROM Patient p WHERE p.isActive = true ORDER BY p.totalRequests DESC LIMIT :limit")
    List<Patient> findMostActivePatients(@Param("limit") int limit);

    // ============ VERIFICATION QUERIES ============
    /**
     * Find verified patients
     */
    List<Patient> findByIsVerifiedTrue();

    /**
     * Find unverified patients
     */
    List<Patient> findByIsVerifiedFalse();

    // ============ SEARCH QUERIES ============
    /**
     * Find patients by medical condition
     */
    List<Patient> findByMedicalConditionContainingIgneCase(String condition);

    /**
     * Search patients by name
     */
    List<Patient> findByFullNameContainingIgnoreCase(String name);

    /**
     * Count patients by emergency level
     */
    @Query("SELECT p.emergencyLevel, COUNT(p) FROM Patient p WHERE p.isActive = true GROUP BY p.emergencyLevel")
    List<Object[]> countPatientsByEmergencyLevel();

    /**
     * Count patients by blood group required
     */
    @Query("SELECT p.requiredBloodGroup, COUNT(p) FROM Patient p WHERE p.isActive = true " +
           "GROUP BY p.requiredBloodGroup")
    List<Object[]> countPatientsByBloodGroup();

    /**
     * Get total active patients
     */
    long countByIsActiveTrueAndIsVerifiedTrue();
}
