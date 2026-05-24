package com.bloodlink.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Donor Entity - INHERITANCE from User
 * Demonstrates:
 * - INHERITANCE: Extends User class
 * - POLYMORPHISM: Overrides abstract methods
 * - ENCAPSULATION: Additional donor-specific fields
 * - SOLID PRINCIPLES: Single Responsibility (Donor-specific logic)
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DiscriminatorValue("DONOR")
public class Donor extends User {

    // ============ BLOOD DONATION INFORMATION ============
    @Column(nullable = false, length = 5)
    private String bloodGroup; // A+, A-, B+, B-, O+, O-, AB+, AB-

    @Column(nullable = false)
    private LocalDate lastDonationDate;

    @Column(nullable = false)
    private Integer totalDonations = 0;

    @Column(nullable = false)
    private Boolean isAvailable = true;

    @Column(length = 500)
    private String availableDonationArea;

    @Column
    private Boolean emergencyAvailable = false;

    // ============ MEDICAL INFORMATION ============
    @Column(length = 500)
    private String medicalHistory;

    @Column(length = 500)
    private String diseases; // Comma-separated diseases/restrictions

    @Column(length = 500)
    private String medications; // Current medications

    @Column
    private Double weight; // in kg

    @Column
    private Integer bloodPressureSystolic;

    @Column
    private Integer bloodPressureDiastolic;

    @Column(length = 200)
    private String allergies;

    // ============ RATING & STATISTICS ============
    @Column
    private Double averageRating = 0.0;

    @Column
    private Integer totalRequests = 0;

    @Column
    private Integer acceptedRequests = 0;

    @Column
    private Integer declinedRequests = 0;

    // ============ VERIFICATION ============
    @Column
    private Boolean donorVerified = false;

    @Column
    private LocalDate verificationDate;

    @Column(length = 100)
    private String verificationDocumentUrl;

    // ============ RELATIONSHIPS ============
    /**
     * One-to-Many: One donor can have multiple donation history records
     */
    @OneToMany(mappedBy = "donor", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<DonationHistory> donationHistory = new HashSet<>();

    /**
     * One-to-Many: One donor can receive multiple blood requests
     */
    @OneToMany(mappedBy = "donor", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<BloodRequest> receivedRequests = new HashSet<>();

    /**
     * One-to-Many: One donor can have multiple reviews/ratings
     */
    @OneToMany(mappedBy = "donor", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<DonorReview> reviews = new HashSet<>();

    /**
     * One-to-One: One donor has one medical record
     */
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "medical_record_id", unique = true)
    private MedicalRecord medicalRecord;

    // ============ POLYMORPHIC METHODS (Override abstract methods) ============
    /**
     * POLYMORPHISM: Implementation of abstract method from User
     */
    @Override
    public String getUserRole() {
        return "DONOR";
    }

    /**
     * POLYMORPHISM: Donor-specific display information
     */
    @Override
    public String getDisplayInfo() {
        return String.format("Donor: %s | Blood: %s | Donations: %d | Status: %s",
                getFullName(), bloodGroup, totalDonations, 
                isAvailable ? "Available" : "Unavailable");
    }

    // ============ BUSINESS LOGIC METHODS ============
    /**
     * Check if donor is eligible for donation
     * ENCAPSULATION: Hides complex validation logic
     */
    public boolean isEligibleForDonation() {
        if (!canPerformAction()) return false;
        
        // Check if last donation was at least 56 days ago
        LocalDate minDonationDate = LocalDate.now().minusDays(56);
        return lastDonationDate == null || lastDonationDate.isBefore(minDonationDate);
    }

    /**
     * Calculate days since last donation
     */
    public Long daysSinceLastDonation() {
        if (lastDonationDate == null) return null;
        return java.time.temporal.ChronoUnit.DAYS.between(lastDonationDate, LocalDate.now());
    }

    /**
     * Update donor availability status
     * ENCAPSULATION: Validates and updates atomically
     */
    public void setAvailabilityStatus(Boolean available) {
        if (isVerified && isActive) {
            this.isAvailable = available;
        }
    }

    /**
     * Record a new donation
     * ENCAPSULATION: Updates statistics atomically
     */
    public void recordDonation() {
        this.lastDonationDate = LocalDate.now();
        this.totalDonations++;
        this.acceptedRequests++;
    }

    /**
     * Calculate compatibility score with patient blood group
     * AI FEATURE: Used in smart matching
     */
    public double calculateBloodGroupCompatibility(String patientBloodGroup) {
        // Perfect match
        if (this.bloodGroup.equals(patientBloodGroup)) {
            return 1.0;
        }
        
        // Universal donor
        if ("O-".equals(this.bloodGroup)) {
            return 0.9;
        }
        
        // Partial compatibility check
        if (this.bloodGroup.charAt(0) == patientBloodGroup.charAt(0)) {
            return 0.7;
        }
        
        return 0.0;
    }

    /**
     * Get donor reliability score (0-100)
     * Used for ranking and recommendations
     */
    public int getReliabilityScore() {
        if (totalRequests == 0) return 50; // New donors get neutral score
        
        int score = (int) (50 + (acceptedRequests * 50.0 / totalRequests));
        score += Math.min(totalDonations * 2, 20); // Bonus for frequent donors
        score = (int) (score + (averageRating * 2.5)); // Rating influence
        
        return Math.min(score, 100); // Cap at 100
    }
}
