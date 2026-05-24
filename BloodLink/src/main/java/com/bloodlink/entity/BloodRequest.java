package com.bloodlink.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

/**
 * BloodRequest Entity
 * Represents a blood request from a patient to a donor
 * Demonstrates:
 * - ENCAPSULATION: Request status management
 * - Many-to-One relationships: Patient and Donor
 * - Status tracking and workflow
 */
@Entity
@Table(name = "blood_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BloodRequest {

    // ============ PRIMARY KEY ============
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long requestId;

    // ============ REQUEST INFORMATION ============
    @Column(nullable = false, length = 5)
    private String bloodGroup;

    @Column(nullable = false)
    private Integer units; // Number of units required

    @Column(length = 500)
    private String reason;

    // ============ REQUEST STATUS (ENUM) ============
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RequestStatus status = RequestStatus.PENDING;

    /**
     * Enum for Request Status
     * Demonstrates: ENUM usage for type-safe status management
     */
    public enum RequestStatus {
        PENDING("Waiting for donor response"),
        ACCEPTED("Donor accepted the request"),
        DECLINED("Donor declined the request"),
        COMPLETED("Blood received successfully"),
        CANCELLED("Request cancelled by patient"),
        EXPIRED("Request expired");

        private final String description;

        RequestStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // ============ URGENCY LEVEL ============
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Patient.EmergencyLevel urgencyLevel = Patient.EmergencyLevel.NORMAL;

    // ============ TIMESTAMPS ============
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime respondedAt;

    @Column
    private LocalDateTime completedAt;

    // ============ ADDITIONAL INFORMATION ============
    @Column(length = 500)
    private String donorNotes;

    @Column(length = 500)
    private String patientNotes;

    @Column(length = 500)
    private String adminNotes;

    // ============ RELATIONSHIPS ============
    /**
     * Many-to-One: Many requests belong to one patient
     * Inverting side: Patient has @OneToMany
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    /**
     * Many-to-One: Many requests are sent to one donor
     * Inverting side: Donor has @OneToMany
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "donor_id", nullable = false)
    private Donor donor;

    // ============ BUSINESS LOGIC METHODS ============
    /**
     * Accept the blood request
     * ENCAPSULATION: Validates state before accepting
     */
    public void acceptRequest() {
        if (status == RequestStatus.PENDING) {
            this.status = RequestStatus.ACCEPTED;
            this.respondedAt = LocalDateTime.now();
        }
    }

    /**
     * Decline the blood request
     * ENCAPSULATION: Validates state before declining
     */
    public void declineRequest() {
        if (status == RequestStatus.PENDING) {
            this.status = RequestStatus.DECLINED;
            this.respondedAt = LocalDateTime.now();
        }
    }

    /**
     * Mark request as completed
     * ENCAPSULATION: Updates status and timestamps
     */
    public void completeRequest() {
        if (status == RequestStatus.ACCEPTED) {
            this.status = RequestStatus.COMPLETED;
            this.completedAt = LocalDateTime.now();
        }
    }

    /**
     * Check if request is still pending response
     */
    public boolean isPending() {
        return status == RequestStatus.PENDING;
    }

    /**
     * Check if request has expired (older than 7 days)
     */
    public boolean isExpired() {
        return createdAt.plusDays(7).isBefore(LocalDateTime.now()) && 
               isPending();
    }

    /**
     * Get time elapsed since request creation
     */
    public long getHoursElapsed() {
        return java.time.temporal.ChronoUnit.HOURS.between(createdAt, LocalDateTime.now());
    }

    /**
     * Get priority score for donor matching
     * Higher score = higher priority
     */
    public int getPriorityScore() {
        int score = urgencyLevel.getPriority() * 20;
        score += (int) Math.min(getHoursElapsed() / 2, 30); // Increase priority over time
        return Math.min(score, 100);
    }
}
