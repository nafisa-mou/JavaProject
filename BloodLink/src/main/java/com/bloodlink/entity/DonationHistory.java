package com.bloodlink.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DonationHistory Entity
 * Records each donation made by a donor
 * Demonstrates:
 * - Many-to-One relationship with Donor
 * - Detailed donation tracking
 */
@Entity
@Table(name = "donation_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DonationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long donationId;

    /**
     * Many-to-One: Many donations belong to one donor
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "donor_id", nullable = false)
    private Donor donor;

    @Column(nullable = false)
    private LocalDate donationDate;

    @Column(length = 5)
    private String bloodGroupDonated;

    @Column
    private Integer unitsDonated;

    @Column(length = 100)
    private String donationCenterName;

    @Column(length = 300)
    private String donationCenterLocation;

    @Column(length = 200)
    private String doctorName;

    @Column
    private Boolean donationSuccessful = true;

    @Column(length = 500)
    private String remarks;

    @Column(length = 100)
    private String status; // Completed, Rejected, etc.

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime recordedAt;

    // ============ BLOOD TEST RESULTS ============
    @Column
    private Double hemoglobin;

    @Column
    private Boolean hbsAg; // Hepatitis B test

    @Column
    private Boolean hcvAb; // Hepatitis C test

    @Column
    private Boolean hiv; // HIV test

    @Column
    private Boolean vdrl; // Syphilis test

    // ============ BUSINESS LOGIC METHODS ============
    public boolean isTestResultsNegative() {
        return !Boolean.TRUE.equals(hbsAg) && 
               !Boolean.TRUE.equals(hcvAb) && 
               !Boolean.TRUE.equals(hiv) && 
               !Boolean.TRUE.equals(vdrl);
    }

    public boolean isHealthyDonation() {
        return donationSuccessful && isTestResultsNegative() && 
               hemoglobin != null && hemoglobin >= 12.5;
    }

    public long getDaysSinceDonation() {
        return java.time.temporal.ChronoUnit.DAYS.between(donationDate, LocalDate.now());
    }
}
