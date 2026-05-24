package com.bloodlink.repository;

import com.bloodlink.entity.MedicalRecord;
import com.bloodlink.entity.Donor;
import com.bloodlink.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository Interface for MedicalRecord Entity
 */
@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {

    Optional<MedicalRecord> findByRecordId(Long recordId);

    // ============ DONOR RECORDS ============
    Optional<MedicalRecord> findByDonor(Donor donor);

    /**
     * Find verified medical records
     */
    List<MedicalRecord> findByIsVerifiedTrue();

    /**
     * Find unverified medical records
     */
    List<MedicalRecord> findByIsVerifiedFalse();

    // ============ PATIENT RECORDS ============
    /**
     * Find all medical records for a patient
     */
    List<MedicalRecord> findByPatient(Patient patient);

    /**
     * Count medical records for patient
     */
    long countByPatient(Patient patient);

    // ============ HEALTH ASSESSMENT ============
    /**
     * Find donors eligible for donation
     */
    @Query("SELECT mr FROM MedicalRecord mr WHERE mr.donor IS NOT NULL AND mr.hemoglobin >= 12.5")
    List<MedicalRecord> findEligibleDonorRecords();

    /**
     * Find records with chronic diseases
     */
    @Query("SELECT mr FROM MedicalRecord mr WHERE mr.chronicDiseases IS NOT NULL AND mr.chronicDiseases != ''")
    List<MedicalRecord> findRecordsWithChronicDiseases();

    /**
     * Find smoker records
     */
    List<MedicalRecord> findBySmokersTrue();

    /**
     * Find drug user records
     */
    List<MedicalRecord> findByDrugUserTrue();
}
