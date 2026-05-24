package com.bloodlink.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Patient Entity - INHERITANCE from User
 * Demonstrates:
 * - INHERITANCE: Extends User class
 * - POLYMORPHISM: Overrides abstract methods
 * - ENCAPSULATION: Additional patient-specific fields
 * - SOLID PRINCIPLES: Single Responsibility (Patient-specific logic)
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DiscriminatorValue("PATIENT")
public class Patient extends User {

    // ============ MEDICAL INFORMATION ============
    @Column(nullable = false, length = 5)
    private String requiredBloodGroup; // Blood group required

    @Column(length = 500)
    private String medicalCondition; // Reason for blood requirement

    @Column(length = 500)
    private String currentMedications;

    @Column
    private Double temperature; // Current body temperature

    @Column
    private Integer bloodPressureSystolic;

    @Column
    private Integer bloodPressureDiastolic;

    @Column
    private Integer pulseRate;

    @Column(length = 200)
    private String allergies;

    // ============ HOSPITAL INFORMATION ============
    @Column(nullable = false, length = 200)
    private String hospitalName;

    @Column(length = 100)
    private String hospitalCity;

    @Column(length = 300)
    private String hospitalAddress;

    @Column(length = 20)
    private String hospitalPhoneNumber;

    @Column(length = 100)
    private String wardNumber;

    @Column(length = 100)
    private String bedNumber;

    // ============ REQUEST INFORMATION ============
    @Column
    private Integer requiredUnits = 1; // Units of blood required

    @Column
    private LocalDate requiredDate; // When blood is needed

    @Column
    @Enumerated(EnumType.STRING)
    private EmergencyLevel emergencyLevel = EmergencyLevel.NORMAL;

    @Column(length = 500)
    private String additionalNotes;

    // ============ STATISTICS ============
    @Column
    private Integer totalRequests = 0;

    @Column
    private Integer fulfilledRequests = 0;

    @Column
    private Integer pendingRequests = 0;

    @Column
    private Double averageRating = 0.0;

    // ============ PREFERENCES ============
    @Column
    private Integer preferredDonorDistance = 10; // in km

    @Column
    private Boolean preferredDonorsOnly = false;

    // ============ RELATIONSHIPS ============
    /**
     * One-to-Many: One patient can send multiple blood requests
     */
    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<BloodRequest> bloodRequests = new HashSet<>();

    /**
     * One-to-Many: One patient can have multiple medical records
     */
    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<MedicalRecord> medicalRecords = new HashSet<>();

    /**
     * One-to-Many: One patient can rate multiple donors
     */
    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<DonorReview> donorReviews = new HashSet<>();

    // ============ ENUM FOR EMERGENCY LEVELS ============
    public enum EmergencyLevel {
        ROUTINE(1),
        URGENT(2),
        CRITICAL(3),
        LIFE_THREATENING(4);

        private final int priority;

        EmergencyLevel(int priority) {
            this.priority = priority;
        }

        public int getPriority() {
            return priority;
        }
    }

    // ============ POLYMORPHIC METHODS (Override abstract methods) ============
    /**
     * POLYMORPHISM: Implementation of abstract method from User
     */
    @Override
    public String getUserRole() {
        return "PATIENT";
    }

    /**
     * POLYMORPHISM: Patient-specific display information
     */
    @Override
    public String getDisplayInfo() {
        return String.format("Patient: %s | Blood Needed: %s | Hospital: %s | Emergency: %s",
                getFullName(), requiredBloodGroup, hospitalName, emergencyLevel);
    }

    // ============ BUSINESS LOGIC METHODS ============
    /**
     * Check if patient can make a blood request
     * ENCAPSULATION: Hides complex validation logic
     */
    public boolean canMakeRequest() {
        return canPerformAction() && pendingRequests < 5; // Max 5 pending requests
    }

    /**
     * Create a new blood request
     * ENCAPSULATION: Updates statistics atomically
     */
    public void createBloodRequest() {
        if (canMakeRequest()) {
            totalRequests++;
            pendingRequests++;
        }
    }

    /**
     * Mark a request as fulfilled
     * ENCAPSULATION: Updates statistics atomically
     */
    public void markRequestFulfilled() {
        if (pendingRequests > 0) {
            pendingRequests--;
            fulfilledRequests++;
        }
    }

    /**
     * Calculate urgency score (0-100)
     * Higher score = more urgent
     * Used for prioritizing donor matching
     */
    public int getUrgencyScore() {
        int score = emergencyLevel.getPriority() * 25;
        
        // If required date is today or tomorrow, increase urgency
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        if (requiredDate != null && !requiredDate.isAfter(tomorrow)) {
            score += 20;
        }
        
        return Math.min(score, 100);
    }

    /**
     * Check if patient requires immediate blood
     */
    public boolean isEmergency() {
        return emergencyLevel == EmergencyLevel.CRITICAL || 
               emergencyLevel == EmergencyLevel.LIFE_THREATENING;
    }

    /**
     * Calculate days until blood is needed
     */
    public Long daysUntilRequired() {
        if (requiredDate == null) return null;
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), requiredDate);
    }

    /**
     * Check if patient is within preferred distance from donor
     */
    public boolean isWithinPreferredDistance(Double donorLatitude, Double donorLongitude) {
        if (getLatitude() == null || getLongitude() == null) return true;
        
        double distance = calculateDistance(getLatitude(), getLongitude(), 
                                           donorLatitude, donorLongitude);
        return distance <= preferredDonorDistance;
    }

    /**
     * Calculate distance between two coordinates (Haversine formula)
     * Utility method for location-based search
     */
    private double calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        int R = 6371; // Radius of earth in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
