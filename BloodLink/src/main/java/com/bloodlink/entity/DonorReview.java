package com.bloodlink.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

/**
 * DonorReview Entity
 * Patient reviews and rates donors
 * Demonstrates:
 * - Many-to-One relationships with Patient and Donor
 * - Rating and feedback system
 */
@Entity
@Table(name = "donor_reviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DonorReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;

    /**
     * Many-to-One: Multiple reviews for one donor
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "donor_id", nullable = false)
    private Donor donor;

    /**
     * Many-to-One: Multiple reviews from one patient
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    /**
     * Many-to-One: Review related to a blood request
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id")
    private BloodRequest bloodRequest;

    // ============ REVIEW INFORMATION ============
    @Column(nullable = false)
    private Integer rating; // 1-5 stars

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(nullable = false)
    private Boolean isAnonymous = false;

    // ============ ASPECTS OF REVIEW ============
    @Column
    private Integer reliabilityRating; // 1-5

    @Column
    private Integer communicationRating; // 1-5

    @Column
    private Integer professionalismRating; // 1-5

    // ============ TIMESTAMPS & STATUS ============
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @Column
    private Boolean isVerified = false; // Admin verified

    @Column
    private Boolean isHelpful = false;

    // ============ MODERATION ============
    @Column
    private Boolean isApproved = true;

    @Column
    private Boolean isFlagged = false;

    @Column(length = 500)
    private String flagReason;

    // ============ BUSINESS LOGIC METHODS ============
    /**
     * Validate review rating
     */
    public boolean isValidRating() {
        return rating >= 1 && rating <= 5 &&
               (reliabilityRating == null || (reliabilityRating >= 1 && reliabilityRating <= 5)) &&
               (communicationRating == null || (communicationRating >= 1 && communicationRating <= 5)) &&
               (professionalismRating == null || (professionalismRating >= 1 && professionalismRating <= 5));
    }

    /**
     * Calculate average aspect rating
     */
    public Double getAverageAspectRating() {
        int count = 0;
        int sum = 0;

        if (reliabilityRating != null) {
            sum += reliabilityRating;
            count++;
        }
        if (communicationRating != null) {
            sum += communicationRating;
            count++;
        }
        if (professionalismRating != null) {
            sum += professionalismRating;
            count++;
        }

        return count > 0 ? (double) sum / count : null;
    }

    /**
     * Flag review as inappropriate
     */
    public void flagReview(String reason) {
        this.isFlagged = true;
        this.flagReason = reason;
    }

    /**
     * Get age of review in days
     */
    public long getAgeInDays() {
        return java.time.temporal.ChronoUnit.DAYS.between(createdAt, LocalDateTime.now());
    }

    /**
     * Check if review is recent (less than 30 days old)
     */
    public boolean isRecent() {
        return getAgeInDays() <= 30;
    }

    /**
     * Get quality score for review
     */
    public int getQualityScore() {
        int score = 0;
        if (comment != null && comment.length() > 20) score += 2;
        if (reliabilityRating != null && communicationRating != null && professionalismRating != null) score += 2;
        if (isVerified) score += 1;
        if (isFlagged) score -= 5;
        return Math.max(score, 0);
    }
}
