package com.bloodlink.repository;

import com.bloodlink.entity.Donor;
import com.bloodlink.entity.DonorReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for DonorReview entity.
 * Provides data access operations for donor reviews and ratings.
 * 
 * OOP Principle: Encapsulation - Data access logic is encapsulated
 * SOLID: Dependency Inversion - Service depends on this interface
 */
@Repository
public interface DonorReviewRepository extends JpaRepository<DonorReview, Long> {

    /**
     * Find all reviews for a specific donor
     */
    List<DonorReview> findByDonor(Donor donor);

    /**
     * Find all reviews for a donor by ID
     */
    @Query("SELECT dr FROM DonorReview dr WHERE dr.donor.userId = :donorId ORDER BY dr.createdAt DESC")
    List<DonorReview> findByDonorId(@Param("donorId") Long donorId);

    /**
     * Get average rating for a donor
     */
    @Query("SELECT AVG(dr.rating) FROM DonorReview dr WHERE dr.donor.userId = :donorId")
    Double getAverageRatingForDonor(@Param("donorId") Long donorId);

    /**
     * Count reviews for a donor
     */
    @Query("SELECT COUNT(dr) FROM DonorReview dr WHERE dr.donor.userId = :donorId")
    int countReviewsForDonor(@Param("donorId") Long donorId);

    /**
     * Find highly rated reviews (4-5 stars)
     */
    @Query("SELECT dr FROM DonorReview dr WHERE dr.rating >= 4 ORDER BY dr.rating DESC, dr.createdAt DESC")
    List<DonorReview> findHighlyRatedReviews();

    /**
     * Find low-rated reviews (1-2 stars) - need follow-up
     */
    @Query("SELECT dr FROM DonorReview dr WHERE dr.rating <= 2 ORDER BY dr.createdAt DESC")
    List<DonorReview> findLowRatedReviews();

    /**
     * Count reviews by rating
     */
    @Query("SELECT COUNT(dr) FROM DonorReview dr WHERE dr.rating = :rating AND dr.donor.userId = :donorId")
    int countReviewsByRating(@Param("donorId") Long donorId, @Param("rating") int rating);

    /**
     * Find flagged reviews (require admin review)
     */
    @Query("SELECT dr FROM DonorReview dr WHERE dr.isFlagged = true ORDER BY dr.createdAt DESC")
    List<DonorReview> findFlaggedReviews();

    /**
     * Get top rated donors
     */
    @Query(value = "SELECT dr.donor_id, AVG(dr.rating) as avg_rating FROM donor_reviews dr GROUP BY dr.donor_id ORDER BY avg_rating DESC LIMIT :limit", nativeQuery = true)
    List<Object[]> getTopRatedDonors(@Param("limit") int limit);

    /**
     * Find reviews mentioning reliability
     */
    @Query("SELECT dr FROM DonorReview dr WHERE dr.reliabilityRating > 0 ORDER BY dr.reliabilityRating DESC")
    List<DonorReview> findReviewsByReliability();

    /**
     * Find reviews mentioning communication
     */
    @Query("SELECT dr FROM DonorReview dr WHERE dr.communicationRating > 0 ORDER BY dr.communicationRating DESC")
    List<DonorReview> findReviewsByCommunication();

    /**
     * Find reviews mentioning professionalism
     */
    @Query("SELECT dr FROM DonorReview dr WHERE dr.professionalismRating > 0 ORDER BY dr.professionalismRating DESC")
    List<DonorReview> findReviewsByProfessionalism();

    /**
     * Check if a patient already reviewed a donor
     */
    @Query("SELECT CASE WHEN COUNT(dr) > 0 THEN true ELSE false END FROM DonorReview dr WHERE dr.donor.userId = :donorId AND dr.patient.userId = :patientId")
    boolean hasPatientReviewedDonor(@Param("donorId") Long donorId, @Param("patientId") Long patientId);

    /**
     * Get quality score (average of all aspect ratings)
     */
    @Query("SELECT AVG((dr.reliabilityRating + dr.communicationRating + dr.professionalismRating) / 3.0) FROM DonorReview dr WHERE dr.donor.userId = :donorId")
    Double getQualityScoreForDonor(@Param("donorId") Long donorId);

    /**
     * Find recent reviews (last 30 days)
     */
    @Query("SELECT dr FROM DonorReview dr WHERE dr.createdAt > CURRENT_DATE - 30 ORDER BY dr.createdAt DESC")
    List<DonorReview> findRecentReviews();

    /**
     * Find reviews with comments
     */
    @Query("SELECT dr FROM DonorReview dr WHERE dr.comments IS NOT NULL AND dr.comments != '' ORDER BY dr.createdAt DESC")
    List<DonorReview> findReviewsWithComments();
}
