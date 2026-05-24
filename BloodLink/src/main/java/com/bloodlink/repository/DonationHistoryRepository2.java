package com.bloodlink.repository;

import com.bloodlink.entity.DonationHistory;
import com.bloodlink.entity.Donor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository Interface for DonationHistory Entity
 */
@Repository
public interface DonationHistoryRepository extends JpaRepository<DonationHistory, Long> {

    Optional<DonationHistory> findByDonationId(Long donationId);

    // ============ DONOR DONATIONS ============
    /**
     * Find all donations by a donor
     */
    List<DonationHistory> findByDonor(Donor donor);

    /**
     * Find successful donations by donor
     */
    List<DonationHistory> findByDonorAndDonationSuccessfulTrue(Donor donor);

    /**
     * Count donations by donor
     */
    long countByDonor(Donor donor);

    // ============ TIME-BASED QUERIES ============
    /**
     * Find donations on specific date
     */
    List<DonationHistory> findByDonationDate(LocalDate date);

    /**
     * Find donations between dates
     */
    @Query("SELECT dh FROM DonationHistory dh WHERE dh.donationDate BETWEEN :startDate AND :endDate")
    List<DonationHistory> findDonationsBetweenDates(@Param("startDate") LocalDate startDate, 
                                                     @Param("endDate") LocalDate endDate);

    /**
     * Find donations by donor between dates
     */
    @Query("SELECT dh FROM DonationHistory dh WHERE dh.donor = :donor AND dh.donationDate BETWEEN :startDate AND :endDate")
    List<DonationHistory> findDonorDonationsBetweenDates(@Param("donor") Donor donor, 
                                                         @Param("startDate") LocalDate startDate, 
                                                         @Param("endDate") LocalDate endDate);

    // ============ BLOOD GROUP QUERIES ============
    /**
     * Find donations by blood group
     */
    List<DonationHistory> findByBloodGroupDonated(String bloodGroup);

    /**
     * Count donations by blood group
     */
    @Query("SELECT dh.bloodGroupDonated, COUNT(dh) FROM DonationHistory dh GROUP BY dh.bloodGroupDonated")
    List<Object[]> countDonationsByBloodGroup();

    // ============ TEST RESULTS QUERIES ============
    /**
     * Find donations with negative test results
     */
    @Query("SELECT dh FROM DonationHistory dh WHERE dh.hbsAg = false AND dh.hcvAb = false " +
           "AND dh.hiv = false AND dh.vdrl = false")
    List<DonationHistory> findDonationsWithNegativeResults();

    /**
     * Find donations with positive test results
     */
    @Query("SELECT dh FROM DonationHistory dh WHERE dh.hbsAg = true OR dh.hcvAb = true " +
           "OR dh.hiv = true OR dh.vdrl = true")
    List<DonationHistory> findDonationsWithPositiveResults();

    // ============ STATISTICS ============
    /**
     * Get last donation date for donor
     */
    @Query("SELECT MAX(dh.donationDate) FROM DonationHistory dh WHERE dh.donor = :donor")
    LocalDate findLastDonationDateForDonor(@Param("donor") Donor donor);

    /**
     * Get total units donated by donor
     */
    @Query("SELECT SUM(dh.unitsDonated) FROM DonationHistory dh WHERE dh.donor = :donor")
    Integer getTotalUnitsDonatedByDonor(@Param("donor") Donor donor);
}
