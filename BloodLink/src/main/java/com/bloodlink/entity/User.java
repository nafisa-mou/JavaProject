package com.bloodlink.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * ABSTRACT User Entity - Base class demonstrating OOP Principles:
 * - ENCAPSULATION: Private fields with protected/public accessors
 * - ABSTRACTION: Abstract class defining common user properties
 * - INHERITANCE: Donor and Patient classes extend this
 * - POLYMORPHISM: Different behaviors for Donor/Patient (Discriminator column)
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "user_type", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("USER")
public abstract class User {

    // ============ PRIMARY KEY ============
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    // ============ BASIC INFORMATION (ENCAPSULATION) ============
    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, unique = true, length = 15)
    private String phoneNumber;

    @Column(nullable = false)
    private String password; // Will be encrypted using BCrypt

    // ============ PERSONAL INFORMATION ============
    @Column(length = 50)
    private String gender; // Male, Female, Other

    @Column
    private Integer age;

    @Column(length = 500)
    private String profilePhotoUrl;

    // ============ ADDRESS INFORMATION ============
    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(length = 100)
    private String country;

    @Column(length = 300)
    private String fullAddress;

    @Column(name = "latitude")
    private Double latitude; // For location-based search

    @Column(name = "longitude")
    private Double longitude; // For location-based search

    // ============ ACCOUNT STATUS ============
    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(nullable = false)
    private Boolean isVerified = false;

    @Column(nullable = false)
    private Boolean isBlocked = false;

    // ============ TIMESTAMPS ============
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime lastLoginAt;

    // ============ RELATIONSHIPS ============
    /**
     * One-to-Many: One user can have multiple chat conversations
     * - Mapped by the 'recipient' field in Chat entity
     */
    @OneToMany(mappedBy = "initiator", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Chat> initiatedChats = new HashSet<>();

    /**
     * One-to-Many: One user can receive multiple messages
     */
    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Message> sentMessages = new HashSet<>();

    /**
     * One-to-Many: One user can receive multiple notifications
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Notification> notifications = new HashSet<>();

    // ============ ABSTRACT METHODS (ABSTRACTION) ============
    /**
     * Abstract method to be implemented by subclasses
     * Demonstrates polymorphism
     */
    public abstract String getUserRole();

    /**
     * Abstract method for role-specific information
     */
    public abstract String getDisplayInfo();

    // ============ BUSINESS LOGIC METHODS ============
    /**
     * Method to check if user can perform actions
     * Encapsulation: Hides internal state check logic
     */
    public boolean canPerformAction() {
        return isActive && isVerified && !isBlocked;
    }

    /**
     * Update user location
     * Encapsulation: Validates and updates location atomically
     */
    public void updateLocation(Double latitude, Double longitude) {
        if (latitude != null && longitude != null) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }

    /**
     * Check if user is online (mock implementation)
     */
    public boolean isOnline() {
        return lastLoginAt != null && 
               lastLoginAt.isAfter(LocalDateTime.now().minusMinutes(5));
    }
}
