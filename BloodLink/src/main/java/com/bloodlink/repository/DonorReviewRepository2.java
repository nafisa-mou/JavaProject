package com.bloodlink.repository;

import com.bloodlink.entity.DonorReview;
import com.bloodlink.entity.Donor;
import com.bloodlink.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository Interface for DonorReview Entity
 */
@Repository
public interface DonorReviewRepository extends JpaRepository<DonorReview, Long> {

    Optional<DonorReview> findByReviewId(Long reviewId);

    // ============ DONOR REVIEWS ============
    /**
     * Find all reviews for a donor
     */
    List<DonorReview> findByDonor(Donor donor);

    /**
     * Find approved reviews for donor
     */
    List<DonorReview> findByDonorAndIsApprovedTrue(Donor donor);

    /**
     * Count reviews for donor
     */
    long countByDonor(Donor donor);

    /**
     * Count approved reviews for donor
     */
    long countByDonorAndIsApprovedTrue(Donor donor);

    // ============ PATIENT REVIEWS ============
    /**
     * Find all reviews from a patient
     */
    List<DonorReview> findByPatient(Patient patient);

    /**
     * Count reviews from patient
     */
    long countByPatient(Patient patient);

    // ============ RATING QUERIES ============
    /**
     * Get average rating for donor
     */
    @Query("SELECT AVG(r.rating) FROM DonorReview r WHERE r.donor = :donor AND r.isApproved = true")
    Double getAverageRatingForDonor(@Param("donor") Donor donor);

    /**
     * Find highly rated reviews (4+ stars)
     */
    @Query("SELECT r FROM DonorReview r WHERE r.donor = :donor AND r.rating >= 4 AND r.isApproved = true")
    List<DonorReview> findHighlyRatedReviews(@Param("donor") Donor donor);

    /**
     * Find low rated reviews (< 3 stars)
     */
    @Query("SELECT r FROM DonorReview r WHERE r.donor = :donor AND r.rating < 3 AND r.isApproved = true")
    List<DonorReview> findLowRatedReviews(@Param("donor") Donor donor);

    // ============ FLAGGED & MODERATION ============
    /**
     * Find flagged reviews
     */
    List<DonorReview> findByIsFlaggedTrue();

    /**
     * Find unapproved reviews
     */
    List<DonorReview> findByIsApprovedFalse();

    /**
     * Find flagged reviews for donor
     */
    List<DonorReview> findByDonorAndIsFlaggedTrue(Donor donor);

    // ============ HELPFUL REVIEWS ============
    /**
     * Find helpful reviews
     */
    @Query("SELECT r FROM DonorReview r WHERE r.isHelpful = true ORDER BY r.createdAt DESC")
    List<DonorReview> findHelpfulReviews();

    /**
     * Find helpful reviews for donor
     */
    @Query("SELECT r FROM DonorReview r WHERE r.donor = :donor AND r.isHelpful = true")
    List<DonorReview> findHelpfulReviewsForDonor(@Param("donor") Donor donor);

    // ============ STATISTICS ============
    /**
     * Count reviews by rating
     */
    @Query("SELECT r.rating, COUNT(r) FROM DonorReview r WHERE r.donor = :donor AND r.isApproved = true GROUP BY r.rating")
    List<Object[]> countReviewsByRating(@Param("donor") Donor donor);
}
