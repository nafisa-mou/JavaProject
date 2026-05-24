package com.bloodlink.repository;

import com.bloodlink.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

/**
 * Repository Interface for User Entity
 * Demonstrates Spring Data JPA Repository Pattern
 * Extends JpaRepository to get CRUD operations automatically
 * 
 * SOLID PRINCIPLES:
 * - Single Responsibility: Only handles User data access
 * - Dependency Inversion: Depends on abstraction (JpaRepository)
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // ============ BASIC QUERIES ============
    Optional<User> findByEmail(String email);
    
    Optional<User> findByPhoneNumber(String phoneNumber);
    
    Optional<User> findByUserId(Long userId);

    // ============ CUSTOM QUERIES ============
    /**
     * Find all active, verified, and not blocked users
     */
    List<User> findByIsActiveAndIsVerifiedAndIsBlockedFalse(Boolean isActive, Boolean isVerified);

    /**
     * Find users by city (for location-based search)
     */
    List<User> findByCity(String city);

    /**
     * Find users by state
     */
    List<User> findByState(String state);

    /**
     * Find nearby users using latitude and longitude
     */
    @Query(value = "SELECT * FROM users u WHERE " +
            "SQRT(POWER(u.latitude - :latitude, 2) + POWER(u.longitude - :longitude, 2)) * 111.195 <= :radiusKm " +
            "ORDER BY SQRT(POWER(u.latitude - :latitude, 2) + POWER(u.longitude - :longitude, 2))", 
            nativeQuery = true)
    List<User> findNearbyUsers(@Param("latitude") Double latitude, 
                               @Param("longitude") Double longitude, 
                               @Param("radiusKm") Double radiusKm);

    /**
     * Search users by name containing
     */
    List<User> findByFullNameContainingIgnoreCase(String name);

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Check if phone number exists
     */
    boolean existsByPhoneNumber(String phoneNumber);

    /**
     * Find users by user type (JPQL)
     */
    @Query("SELECT u FROM User u WHERE TYPE(u) = com.bloodlink.entity.Donor")
    List<User> findAllDonors();

    @Query("SELECT u FROM User u WHERE TYPE(u) = com.bloodlink.entity.Patient")
    List<User> findAllPatients();

    /**
     * Find blocked users
     */
    List<User> findByIsBlockedTrue();

    /**
     * Find unverified users
     */
    List<User> findByIsVerifiedFalse();
}
