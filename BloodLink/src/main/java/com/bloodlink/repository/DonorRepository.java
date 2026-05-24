package com.bloodlink.repository;

import com.bloodlink.entity.Donor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository Interface for Donor Entity
 * Demonstrates specialized repository for domain-specific queries
 */
@Repository
public interface DonorRepository extends JpaRepository<Donor, Long> {

    // ============ BASIC QUERIES ============
    Optional<Donor> findByUserId(Long userId);

    // ============ BLOOD GROUP SEARCHES ============
    /**
     * Find all available donors with specific blood group
     */
    List<Donor> findByBloodGroupAndIsAvailableTrueAndIsActiveTrue(String bloodGroup);

    /**
     * Find donors with specific blood group in a city
     */
    List<Donor> findByBloodGroupAndCityAndIsAvailableTrue(String bloodGroup, String city);

    /**
     * Find donors with specific blood group sorted by rating
     */
    @Query("SELECT d FROM Donor d WHERE d.bloodGroup = :bloodGroup AND d.isAvailable = true " +
           "ORDER BY d.averageRating DESC")
    List<Donor> findTopDonorsByBloodGroup(@Param("bloodGroup") String bloodGroup);

    // ============ AVAILABILITY QUERIES ============
    /**
     * Find all available donors
     */
    List<Donor> findByIsAvailableTrueAndIsActiveTrue();

    /**
     * Find available donors with emergency availability
     */
    List<Donor> findByIsAvailableTrueAndEmergencyAvailableTrueAndIsActiveTrue();

    /**
     * Find donors in specific donation area
     */
    List<Donor> findByAvailableDonationAreaContainingIgnoreCase(String area);

    // ============ ELIGIBILITY QUERIES ============
    /**
     * Find donors eligible for donation (not donated in last 56 days)
     */
    @Query("SELECT d FROM Donor d WHERE d.lastDonationDate IS NULL OR " +
           "d.lastDonationDate <= :minDate ORDER BY d.lastDonationDate ASC")
    List<Donor> findEligibleDonors(@Param("minDate") LocalDate minDate);

    /**
     * Find donors who haven't donated in specified days
     */
    List<Donor> findByLastDonationDateBefore(LocalDate date);

    // ============ VERIFICATION QUERIES ============
    /**
     * Find verified donors
     */
    List<Donor> findByDonorVerifiedTrue();

    /**
     * Find unverified donors
     */
    List<Donor> findByDonorVerifiedFalse();

    // ============ RATING & STATISTICS ============
    /**
     * Find top donors by rating
     */
    @Query("SELECT d FROM Donor d WHERE d.isActive = true ORDER BY d.averageRating DESC LIMIT :limit")
    List<Donor> findTopRatedDonors(@Param("limit") int limit);

    /**
     * Find most frequent donors
     */
    @Query("SELECT d FROM Donor d WHERE d.isActive = true ORDER BY d.totalDonations DESC LIMIT :limit")
    List<Donor> findMostFrequentDonors(@Param("limit") int limit);

    /**
     * Find recently active donors (accepted requests recently)
     */
    @Query("SELECT d FROM Donor d WHERE d.isActive = true AND d.lastDonationDate >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) " +
           "ORDER BY d.lastDonationDate DESC LIMIT :limit")
    List<Donor> findRecentlyActiveDonors(@Param("limit") int limit);

    // ============ LOCATION-BASED QUERIES ============
    /**
     * Find donors within specified distance
     */
    @Query(value = "SELECT * FROM users u WHERE u.user_type = 'DONOR' AND " +
            "SQRT(POWER(u.latitude - :latitude, 2) + POWER(u.longitude - :longitude, 2)) * 111.195 <= :radiusKm " +
            "AND u.is_available = true ORDER BY SQRT(POWER(u.latitude - :latitude, 2) + POWER(u.longitude - :longitude, 2))", 
            nativeQuery = true)
    List<Donor> findNearbyDonors(@Param("latitude") Double latitude, 
                                  @Param("longitude") Double longitude, 
                                  @Param("radiusKm") Double radiusKm);

    /**
     * Find donors by city with blood group
     */
    List<Donor> findByCityAndBloodGroupAndIsAvailableTrue(String city, String bloodGroup);

    /**
     * Find donors by state
     */
    List<Donor> findByStateAndIsAvailableTrue(String state);

    // ============ STATUS QUERIES ============
    /**
     * Count available donors for statistics
     */
    long countByIsAvailableTrueAndIsActiveTrue();

    /**
     * Count donors by blood group
     */
    @Query("SELECT d.bloodGroup, COUNT(d) FROM Donor d WHERE d.isActive = true GROUP BY d.bloodGroup")
    List<Object[]> countDonorsByBloodGroup();

    // ============ SEARCH COMBINED QUERIES ============
    /**
     * Comprehensive search for smart donor matching (used by AI system)
     */
    @Query("SELECT d FROM Donor d WHERE d.bloodGroup = :bloodGroup AND d.isAvailable = true " +
           "AND d.isActive = true AND d.isVerified = true " +
           "ORDER BY d.averageRating DESC, d.totalDonations DESC")
    List<Donor> findBestMatchDonors(@Param("bloodGroup") String bloodGroup);
}
