package com.bloodlink.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

/**
 * MedicalRecord Entity
 * Stores comprehensive medical information for users
 * Demonstrates:
 * - One-to-One relationship with Donor/Patient
 * - Detailed medical history storage
 */
@Entity
@Table(name = "medical_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long recordId;

    // ============ RELATIONSHIPS ============
    /**
     * Many-to-One: Multiple records can belong to a patient
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private Patient patient;

    /**
     * One-to-One: One donor has one primary medical record
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donor_id")
    private Donor donor;

    // ============ MEDICAL INFORMATION ============
    @Column(columnDefinition = "TEXT")
    private String chronicDiseases; // Comma-separated

    @Column(columnDefinition = "TEXT")
    private String surgicalHistory;

    @Column(columnDefinition = "TEXT")
    private String familyHistory;

    @Column(columnDefinition = "TEXT")
    private String currentMedications;

    @Column(columnDefinition = "TEXT")
    private String allergies;

    @Column(columnDefinition = "TEXT")
    private String vaccinations;

    // ============ VITAL SIGNS ============
    @Column
    private Integer bloodPressureSystolic;

    @Column
    private Integer bloodPressureDiastolic;

    @Column
    private Integer pulseRate;

    @Column
    private Double bodyTemperature;

    @Column
    private Double weight; // in kg

    @Column
    private Double height; // in cm

    @Column
    private Double bmi; // Calculated

    // ============ LAB TEST RESULTS ============
    @Column
    private Double hemoglobin;

    @Column
    private Double whiteBloodCells;

    @Column
    private Double platelets;

    @Column
    private String bloodGroup;

    @Column
    private String rhFactor; // Positive or Negative

    // ============ LIFESTYLE ============
    @Column
    private Boolean smoker = false;

    @Column
    private Boolean alcoholConsumer = false;

    @Column
    private Boolean drugUser = false;

    @Column(length = 300)
    private String occupation;

    @Column
    private Integer hoursExercisePerWeek;

    // ============ TIMESTAMPS & STATUS ============
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime lastUpdatedAt;

    @Column
    private LocalDateTime lastReviewedAt;

    @Column(nullable = false)
    private Boolean isVerified = false;

    @Column
    private String verifiedByDoctor;

    // ============ BUSINESS LOGIC METHODS ============
    /**
     * Calculate BMI from height and weight
     */
    public void calculateBMI() {
        if (weight != null && height != null && height > 0) {
            double heightInMeters = height / 100.0;
            this.bmi = weight / (heightInMeters * heightInMeters);
        }
    }

    /**
     * Check if person is eligible to donate blood
     */
    public boolean isDonationEligible() {
        if (hemoglobin == null || weight == null) return false;
        
        // Must have sufficient hemoglobin (>= 12.5 for females, >= 13.5 for males)
        if (hemoglobin < 12.5) return false;
        
        // Minimum weight 45 kg
        if (weight < 45) return false;
        
        // Should not have chronic diseases, drug use, or be smoker/alcoholic
        if (Boolean.TRUE.equals(drugUser)) return false;
        
        // Age should be between 18-65 (handled in service layer)
        
        return true;
    }

    /**
     * Get health risk assessment
     */
    public HealthRisk assessHealthRisk() {
        if (Boolean.TRUE.equals(smoker) && Boolean.TRUE.equals(alcoholConsumer)) {
            return HealthRisk.HIGH;
        }
        if (Boolean.TRUE.equals(smoker) || Boolean.TRUE.equals(alcoholConsumer)) {
            return HealthRisk.MEDIUM;
        }
        if (bmi != null && (bmi < 18.5 || bmi > 30)) {
            return HealthRisk.MEDIUM;
        }
        return HealthRisk.LOW;
    }

    public enum HealthRisk {
        LOW, MEDIUM, HIGH
    }
}
