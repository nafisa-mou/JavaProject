package com.bloodlink.repository;

import com.bloodlink.entity.DonationHistory;
import com.bloodlink.entity.Donor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for DonationHistory entity.
 * Provides data access operations for donation history records.
 * 
 * OOP Principle: Encapsulation - Data access logic is encapsulated
 * SOLID: Dependency Inversion - Service depends on this interface
 */
@Repository
public interface DonationHistoryRepository extends JpaRepository<DonationHistory, Long> {

    /**
     * Find all donations by a specific donor
     */
    List<DonationHistory> findByDonor(Donor donor);

    /**
     * Find donations by donor ID
     */
    @Query("SELECT dh FROM DonationHistory dh WHERE dh.donor.userId = :donorId ORDER BY dh.donationDate DESC")
    List<DonationHistory> findByDonorId(@Param("donorId") Long donorId);

    /**
     * Find donations within a date range
     */
    @Query("SELECT dh FROM DonationHistory dh WHERE dh.donationDate BETWEEN :startDate AND :endDate ORDER BY dh.donationDate DESC")
    List<DonationHistory> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Find donations with negative test results (safe to donate)
     */
    @Query("SELECT dh FROM DonationHistory dh WHERE dh.hbsAgResult = false AND dh.hcvResult = false AND dh.hivResult = false AND dh.vdrlResult = false")
    List<DonationHistory> findDonationsWithNegativeResults();

    /**
     * Find donations with positive results (flagged for review)
     */
    @Query("SELECT dh FROM DonationHistory dh WHERE dh.hbsAgResult = true OR dh.hcvResult = true OR dh.hivResult = true OR dh.vdrlResult = true")
    List<DonationHistory> findDonationsWithPositiveResults();

    /**
     * Count total donations by a donor
     */
    @Query("SELECT COUNT(dh) FROM DonationHistory dh WHERE dh.donor.userId = :donorId")
    int countDonationsByDonor(@Param("donorId") Long donorId);

    /**
     * Get total units donated by a donor
     */
    @Query("SELECT SUM(dh.unitsCollected) FROM DonationHistory dh WHERE dh.donor.userId = :donorId")
    Double getTotalUnitsDonatedByDonor(@Param("donorId") Long donorId);

    /**
     * Find low hemoglobin donations (health risk)
     */
    @Query("SELECT dh FROM DonationHistory dh WHERE dh.hemoglobinLevel < 12.5 ORDER BY dh.donationDate DESC")
    List<DonationHistory> findLowHemoglobinDonations();

    /**
     * Find recent donations for a donor (last 56 days - minimum donation interval)
     */
    @Query("SELECT dh FROM DonationHistory dh WHERE dh.donor.userId = :donorId AND dh.donationDate > :date ORDER BY dh.donationDate DESC")
    List<DonationHistory> findRecentDonations(@Param("donorId") Long donorId, @Param("date") LocalDate date);

    /**
     * Get average hemoglobin level for a donor
     */
    @Query("SELECT AVG(dh.hemoglobinLevel) FROM DonationHistory dh WHERE dh.donor.userId = :donorId")
    Double getAverageHemoglobinLevel(@Param("donorId") Long donorId);

    /**
     * Get last donation date for a donor
     */
    @Query("SELECT MAX(dh.donationDate) FROM DonationHistory dh WHERE dh.donor.userId = :donorId")
    LocalDate getLastDonationDate(@Param("donorId") Long donorId);
}
