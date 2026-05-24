# BloodLink - Implementation Guide & Code Examples

## 📖 Complete Implementation Guide with OOP Principles

This document provides detailed code examples showing how OOP principles are implemented throughout the BloodLink application.

---

## 1️⃣ SERVICE LAYER EXAMPLE

### DonorService - Demonstrating Encapsulation & Business Logic

```java
package com.bloodlink.service;

import com.bloodlink.entity.Donor;
import com.bloodlink.dto.DonorDTO;
import com.bloodlink.repository.DonorRepository;
import com.bloodlink.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DonorService
 * 
 * SOLID PRINCIPLES:
 * - Single Responsibility: Only handles donor-related business logic
 * - Open/Closed: Can be extended with new methods without modification
 * - Dependency Inversion: Depends on DonorRepository abstraction
 * 
 * OOP PRINCIPLES:
 * - ENCAPSULATION: Hides complex donor operations
 * - ABSTRACTION: Provides simple interface for controller
 */
@Service
@Slf4j
@Transactional
public class DonorService {

    @Autowired
    private DonorRepository donorRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private DonationHistoryService donationHistoryService;

    /**
     * Register a new donor
     * ENCAPSULATION: Hides validation and initialization logic
     * 
     * Steps:
     * 1. Validate input
     * 2. Check if email exists
     * 3. Create donor entity
     * 4. Initialize default values
     * 5. Save to database
     * 6. Send welcome notification
     */
    public Donor registerDonor(Donor donor) {
        log.info("Registering new donor: {}", donor.getEmail());
        
        // Validation
        if (donor.getEmail() == null || donor.getEmail().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        
        // Check duplicate
        if (donorRepository.existsByEmail(donor.getEmail())) {
            throw new RuntimeException("Email already registered");
        }
        
        // Initialize default values (ENCAPSULATION)
        donor.setIsAvailable(true);
        donor.setTotalDonations(0);
        donor.setAverageRating(0.0);
        donor.setDonorVerified(false);
        
        // Save donor
        Donor savedDonor = donorRepository.save(donor);
        
        // Send notification
        notificationService.sendWelcomeNotification(savedDonor);
        
        log.info("Donor registered successfully: {}", savedDonor.getUserId());
        return savedDonor;
    }

    /**
     * Find available donors by blood group
     * ABSTRACTION: Client doesn't know about complex query logic
     */
    public List<Donor> findAvailableDonorsByBloodGroup(String bloodGroup) {
        log.debug("Searching available donors for blood group: {}", bloodGroup);
        
        List<Donor> donors = donorRepository
            .findByBloodGroupAndIsAvailableTrueAndIsActiveTrue(bloodGroup);
        
        // Post-processing: Sort by rating
        return donors.stream()
            .sorted((d1, d2) -> d2.getAverageRating().compareTo(d1.getAverageRating()))
            .collect(Collectors.toList());
    }

    /**
     * Find nearby donors
     * ENCAPSULATION: Hides distance calculation
     */
    public List<Donor> findNearbyDonors(Double latitude, Double longitude, int radiusKm) {
        log.debug("Finding donors within {} km of ({}, {})", radiusKm, latitude, longitude);
        
        List<Donor> nearbyDonors = donorRepository
            .findNearbyDonors(latitude, longitude, (double) radiusKm);
        
        // Filter by availability
        return nearbyDonors.stream()
            .filter(Donor::getIsAvailable)
            .collect(Collectors.toList());
    }

    /**
     * Smart donor matching using AI
     * POLYMORPHISM: Different matching strategies can be used
     */
    public List<Donor> smartMatchDonors(Long patientId, int maxResults) {
        // Get patient requirements
        // Apply AI algorithm
        // Return ranked list of donors
        // This would use DonorMatcher service
        
        return new ArrayList<>();
    }

    /**
     * Update donor availability
     * ENCAPSULATION: Business rule: only verified donors can change availability
     */
    public void setDonorAvailability(Long donorId, Boolean available) {
        log.info("Updating donor {} availability to: {}", donorId, available);
        
        Donor donor = donorRepository.findByUserId(donorId)
            .orElseThrow(() -> ResourceNotFoundException.donorNotFound(donorId));
        
        // ENCAPSULATION: Check business rules
        if (!donor.getDonorVerified()) {
            throw new RuntimeException("Only verified donors can change availability");
        }
        
        if (!donor.getIsActive()) {
            throw new RuntimeException("Blocked donors cannot change availability");
        }
        
        // Update using encapsulated method
        donor.setAvailabilityStatus(available);
        donorRepository.save(donor);
        
        log.info("Donor availability updated successfully");
    }

    /**
     * Check if donor is eligible for donation
     * ENCAPSULATION: Complex eligibility logic hidden in entity method
     */
    public boolean isDonorEligibleForDonation(Long donorId) {
        Donor donor = donorRepository.findByUserId(donorId)
            .orElseThrow(() -> ResourceNotFoundException.donorNotFound(donorId));
        
        // Use encapsulated method from entity
        return donor.isEligibleForDonation();
    }

    /**
     * Get donor's reliability score
     * ENCAPSULATION: Scoring algorithm hidden in entity
     */
    public int getDonorReliabilityScore(Long donorId) {
        Donor donor = donorRepository.findByUserId(donorId)
            .orElseThrow(() -> ResourceNotFoundException.donorNotFound(donorId));
        
        // Use encapsulated method
        return donor.getReliabilityScore();
    }

    /**
     * Convert Donor entity to DTO
     * DTO Pattern: Separates internal representation from API representation
     */
    public DonorDTO convertToDTO(Donor donor) {
        return DonorDTO.builder()
            .userId(donor.getUserId())
            .fullName(donor.getFullName())
            .email(donor.getEmail())
            .bloodGroup(donor.getBloodGroup())
            .isAvailable(donor.getIsAvailable())
            .totalDonations(donor.getTotalDonations())
            .averageRating(donor.getAverageRating())
            .reliabilityScore(donor.getReliabilityScore())
            .city(donor.getCity())
            .state(donor.getState())
            .latitude(donor.getLatitude())
            .longitude(donor.getLongitude())
            .build();
    }
}
```

---

## 2️⃣ CONTROLLER LAYER EXAMPLE

### DonorController - REST Endpoints

```java
package com.bloodlink.controller;

import com.bloodlink.dto.DonorDTO;
import com.bloodlink.entity.Donor;
import com.bloodlink.service.DonorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DonorController
 * 
 * Demonstrates:
 * - REST Controller pattern
 * - Request/Response handling
 * - Authorization and authentication
 * - Error handling
 * - DTO conversion
 */
@RestController
@RequestMapping("/api/donors")
@Slf4j
public class DonorController {

    @Autowired
    private DonorService donorService;

    /**
     * GET /api/donors - Get all donors
     * 
     * @return List of all active donors
     */
    @GetMapping
    public ResponseEntity<?> getAllDonors() {
        try {
            log.info("Fetching all donors");
            List<Donor> donors = donorService.findAllActiveDonors();
            
            List<DonorDTO> donorDTOs = donors.stream()
                .map(donorService::convertToDTO)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(donorDTOs);
        } catch (Exception e) {
            log.error("Error fetching donors", e);
            return ResponseEntity.status(500).body("Error fetching donors");
        }
    }

    /**
     * GET /api/donors/{id} - Get donor details
     * 
     * @param donorId Donor ID
     * @return Donor details
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getDonorDetails(@PathVariable("id") Long donorId) {
        try {
            Donor donor = donorService.getDonorById(donorId);
            DonorDTO dto = donorService.convertToDTO(donor);
            return ResponseEntity.ok(dto);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    /**
     * POST /api/donors/register - Register new donor
     * 
     * @param donor Donor registration data
     * @return Registered donor with JWT token
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerDonor(@RequestBody Donor donor) {
        try {
            log.info("Registering new donor: {}", donor.getEmail());
            Donor registeredDonor = donorService.registerDonor(donor);
            DonorDTO dto = donorService.convertToDTO(registeredDonor);
            return ResponseEntity.status(201).body(dto);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * PUT /api/donors/{id} - Update donor profile
     * Only authenticated donor can update their own profile
     * 
     * @param donorId Donor ID
     * @param donorData Updated donor data
     * @return Updated donor details
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DONOR')")
    public ResponseEntity<?> updateDonorProfile(
            @PathVariable("id") Long donorId,
            @RequestBody Donor donorData) {
        try {
            log.info("Updating donor profile: {}", donorId);
            Donor updatedDonor = donorService.updateDonorProfile(donorId, donorData);
            DonorDTO dto = donorService.convertToDTO(updatedDonor);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * GET /api/donors/search?bloodGroup=O+ - Search donors by blood group
     * 
     * @param bloodGroup Blood group to search
     * @return List of matching donors
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchDonorsByBloodGroup(
            @RequestParam String bloodGroup) {
        try {
            List<Donor> donors = donorService.findAvailableDonorsByBloodGroup(bloodGroup);
            List<DonorDTO> dtos = donors.stream()
                .map(donorService::convertToDTO)
                .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * GET /api/donors/nearby - Find nearby donors
     * Location-based search using latitude and longitude
     * 
     * @param latitude Current latitude
     * @param longitude Current longitude
     * @param radiusKm Search radius in kilometers
     * @return List of nearby donors
     */
    @GetMapping("/nearby")
    public ResponseEntity<?> findNearbyDonors(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "10") int radiusKm) {
        try {
            List<Donor> donors = donorService.findNearbyDonors(latitude, longitude, radiusKm);
            List<DonorDTO> dtos = donors.stream()
                .map(donorService::convertToDTO)
                .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * PUT /api/donors/{id}/availability - Set donor availability
     * Only authenticated donor can update their availability
     * 
     * @param donorId Donor ID
     * @param available Availability status
     * @return Success message
     */
    @PutMapping("/{id}/availability")
    @PreAuthorize("hasRole('DONOR')")
    public ResponseEntity<?> setDonorAvailability(
            @PathVariable("id") Long donorId,
            @RequestParam Boolean available) {
        try {
            donorService.setDonorAvailability(donorId, available);
            return ResponseEntity.ok("Availability updated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * GET /api/donors/{id}/reliability-score - Get donor reliability score
     * Score used for AI matching and ranking
     * 
     * @param donorId Donor ID
     * @return Reliability score (0-100)
     */
    @GetMapping("/{id}/reliability-score")
    public ResponseEntity<?> getDonorReliabilityScore(@PathVariable("id") Long donorId) {
        try {
            int score = donorService.getDonorReliabilityScore(donorId);
            return ResponseEntity.ok(new ScoreResponse(score));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    /**
     * GET /api/donors/{id}/donations - Get donor's donation history
     * Only donor or admin can view
     * 
     * @param donorId Donor ID
     * @return List of donations
     */
    @GetMapping("/{id}/donations")
    @PreAuthorize("hasAnyRole('DONOR', 'ADMIN')")
    public ResponseEntity<?> getDonationHistory(@PathVariable("id") Long donorId) {
        try {
            // Would call donationHistoryService
            return ResponseEntity.ok("Donation history");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

// Helper classes
@Data
class ScoreResponse {
    private int score;
    
    public ScoreResponse(int score) {
        this.score = score;
    }
}
```

---

## 3️⃣ Exception Handling Example

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleResourceNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(404).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<?> handleValidation(ValidationException ex) {
        return ResponseEntity.status(400).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<?> handleUnauthorized(UnauthorizedException ex) {
        return ResponseEntity.status(401).body(new ErrorResponse(ex.getMessage()));
    }
}

@Data
class ErrorResponse {
    private String message;
    private LocalDateTime timestamp = LocalDateTime.now();
    
    public ErrorResponse(String message) {
        this.message = message;
    }
}
```

---

## 4️⃣ Service to Service Communication

### BloodRequestService using other services

```java
@Service
@Transactional
public class BloodRequestService {

    @Autowired
    private BloodRequestRepository requestRepository;
    
    @Autowired
    private DonorService donorService;
    
    @Autowired
    private PatientService patientService;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private DonorMatchingService matchingService;

    /**
     * Create blood request
     * Demonstrates service composition and orchestration
     */
    public BloodRequest createBloodRequest(Long patientId, Long donorId, int units) {
        // Get patient
        Patient patient = patientService.getPatientById(patientId);
        
        // Get donor
        Donor donor = donorService.getDonorById(donorId);
        
        // Validate
        if (!donor.getIsAvailable()) {
            throw new InvalidOperationException("Donor is not available");
        }
        
        if (!patient.canMakeRequest()) {
            throw new InvalidOperationException("Patient cannot make requests");
        }
        
        // Create request
        BloodRequest request = BloodRequest.builder()
            .patient(patient)
            .donor(donor)
            .bloodGroup(patient.getRequiredBloodGroup())
            .units(units)
            .status(BloodRequest.RequestStatus.PENDING)
            .build();
        
        // Save
        BloodRequest savedRequest = requestRepository.save(request);
        
        // Update patient stats
        patient.createBloodRequest();
        
        // Send notification
        notificationService.notifyDonorOfRequest(donor, savedRequest);
        
        return savedRequest;
    }
}
```

---

## 5️⃣ OOP Principles Summary Table

| Principle | Example | Benefit |
|-----------|---------|---------|
| **Abstraction** | Abstract `User` class | Hide complexity, define contracts |
| **Encapsulation** | `donor.setAvailabilityStatus()` | Validate changes, maintain consistency |
| **Inheritance** | `Donor extends User` | Code reuse, polymorphic behavior |
| **Polymorphism** | `getUserRole()` override | Runtime flexibility, loose coupling |
| **Single Responsibility** | `DonorService` | Easy maintenance, focused tests |
| **Open/Closed** | `DonorRepository` interface | Extend without modification |
| **Liskov Substitution** | `User donor = new Donor()` | Interchangeable implementations |
| **Interface Segregation** | `DonorService`, `DonorSearchService` | Specific contracts |
| **Dependency Inversion** | `@Autowired DonorRepository` | Abstract dependencies |

---

## 6️⃣ Testing Example

```java
@SpringBootTest
public class DonorServiceTest {

    @Autowired
    private DonorService donorService;
    
    @MockBean
    private DonorRepository donorRepository;
    
    @Test
    public void testRegisterDonor() {
        // Arrange
        Donor donor = new Donor();
        donor.setEmail("test@donor.com");
        donor.setBloodGroup("O+");
        
        when(donorRepository.save(donor)).thenReturn(donor);
        
        // Act
        Donor result = donorService.registerDonor(donor);
        
        // Assert
        assertNotNull(result);
        assertEquals("O+", result.getBloodGroup());
    }
}
```

---

## 🎯 Best Practices Applied

1. **Separation of Concerns**: Each layer has specific responsibilities
2. **DRY (Don't Repeat Yourself)**: Common logic in base classes
3. **Fail Fast**: Validate early in service methods
4. **Logging**: Track important operations
5. **Transaction Management**: Atomic operations
6. **Error Handling**: Meaningful exceptions
7. **Documentation**: Clear JavaDoc comments
8. **Testing**: Unit and integration tests
9. **Security**: Authorization checks
10. **Performance**: Optimized queries

