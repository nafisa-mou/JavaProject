package com.bloodlink.service;

import com.bloodlink.dto.UserDTO.PatientDTO;
import com.bloodlink.entity.*;
import com.bloodlink.exception.BloodLinkExceptions.*;
import com.bloodlink.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * PatientService - Handles all patient-related operations
 * 
 * Responsibilities:
 * - Patient profile management
 * - Blood request creation and management
 * - Medical history tracking
 * - Request status management
 * - Donor search and filtering
 * 
 * OOP Principles Applied:
 * - Encapsulation: Hides business logic for patient operations
 * - Polymorphism: Works with User hierarchy (Patient extends User)
 * - Abstraction: Provides high-level patient operations
 * - Single Responsibility: Only handles patient operations
 * 
 * SOLID Principles Applied:
 * - S: Single Responsibility - Only patient logic
 * - D: Dependency Inversion - Depends on repository abstractions
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PatientService {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final BloodRequestRepository bloodRequestRepository;
    private final DonorRepository donorRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final NotificationService notificationService;
    private final DonorService donorService;

    /**
     * Get all patients
     * 
     * @return List of all patients
     */
    public List<PatientDTO> getAllPatients() {
        log.debug("Fetching all patients");
        return patientRepository.findAll()
            .stream()
            .map(this::convertToPatientDTO)
            .collect(Collectors.toList());
    }

    /**
     * Get patient by ID
     * Business Logic: Validates patient exists and is active
     * 
     * @param patientId Patient ID
     * @return PatientDTO with patient details
     * @throws ResourceNotFoundException if patient not found
     */
    public PatientDTO getPatientById(Long patientId) {
        log.debug("Fetching patient by ID: {}", patientId);
        
        Patient patient = patientRepository.findById(patientId)
            .orElseThrow(() -> BloodLinkExceptions.patientNotFound());
        
        if (!patient.getIsActive()) {
            throw new UnauthorizedException("Patient account is inactive");
        }
        
        return convertToPatientDTO(patient);
    }

    /**
     * Get patient profile with all details
     * 
     * @param patientId Patient ID
     * @return Complete patient profile
     */
    public Map<String, Object> getPatientProfile(Long patientId) {
        log.debug("Fetching complete profile for patient: {}", patientId);
        
        Patient patient = patientRepository.findById(patientId)
            .orElseThrow(() -> BloodLinkExceptions.patientNotFound());
        
        Map<String, Object> profile = new HashMap<>();
        profile.put("patient", convertToPatientDTO(patient));
        profile.put("bloodGroup", patient.getRequiredBloodGroup());
        profile.put("urgencyLevel", patient.getEmergencyLevel());
        profile.put("activeBlodrequests", bloodRequestRepository.findByPatientAndStatus(patient, BloodRequest.RequestStatus.PENDING).size());
        profile.put("medicalHistory", medicalRecordRepository.findByPatient(patient));
        
        return profile;
    }

    /**
     * Create blood request
     * Business Logic: Validates patient can make request, creates request with proper status
     * Encapsulation: Hides request creation logic
     * 
     * @param patientId Patient ID
     * @param bloodGroup Blood group needed
     * @param urgencyLevel Urgency level (ROUTINE, URGENT, CRITICAL, LIFE_THREATENING)
     * @return Created BloodRequest
     * @throws InvalidOperationException if patient cannot make request
     */
    public BloodRequest createBloodRequest(Long patientId, String bloodGroup, Patient.EmergencyLevel urgencyLevel) {
        log.info("Creating blood request for patient: {}", patientId);
        
        Patient patient = patientRepository.findById(patientId)
            .orElseThrow(() -> BloodLinkExceptions.patientNotFound());
        
        // Validation
        if (!patient.getIsActive()) {
            throw new InvalidOperationException("Patient account is inactive");
        }
        
        if (bloodGroup == null || bloodGroup.trim().isEmpty()) {
            throw new ValidationException("Blood group is required");
        }
        
        if (!bloodGroup.matches("^(O|A|B|AB)[+-]$")) {
            throw new ValidationException("Invalid blood group format");
        }
        
        try {
            BloodRequest request = new BloodRequest();
            request.setPatient(patient);
            request.setBloodGroup(bloodGroup);
            request.setEmergencyLevel(urgencyLevel);
            request.setStatus(BloodRequest.RequestStatus.PENDING);
            request.setCreatedAt(LocalDateTime.now());
            
            BloodRequest saved = bloodRequestRepository.save(request);
            log.info("Blood request created: {}", saved.getRequestId());
            
            return saved;
            
        } catch (Exception e) {
            log.error("Error creating blood request", e);
            throw new InvalidOperationException("Failed to create blood request: " + e.getMessage());
        }
    }

    /**
     * Get patient's blood requests
     * 
     * @param patientId Patient ID
     * @return List of patient's blood requests
     */
    public List<BloodRequest> getPatientRequests(Long patientId) {
        log.debug("Fetching blood requests for patient: {}", patientId);
        
        Patient patient = patientRepository.findById(patientId)
            .orElseThrow(() -> BloodLinkExceptions.patientNotFound());
        
        return bloodRequestRepository.findByPatientOrderByCreatedAtDesc(patient);
    }

    /**
     * Get pending blood requests for patient
     * 
     * @param patientId Patient ID
     * @return List of pending requests
     */
    public List<BloodRequest> getPendingRequests(Long patientId) {
        log.debug("Fetching pending requests for patient: {}", patientId);
        
        Patient patient = patientRepository.findById(patientId)
            .orElseThrow(() -> BloodLinkExceptions.patientNotFound());
        
        return bloodRequestRepository.findByPatientAndStatus(patient, BloodRequest.RequestStatus.PENDING);
    }

    /**
     * Cancel blood request
     * Business Logic: Only allows cancellation if request is still PENDING
     * 
     * @param patientId Patient ID
     * @param requestId Request ID
     * @throws InvalidOperationException if request cannot be cancelled
     */
    public void cancelRequest(Long patientId, Long requestId) {
        log.info("Cancelling blood request: {} for patient: {}", requestId, patientId);
        
        BloodRequest request = bloodRequestRepository.findById(requestId)
            .orElseThrow(() -> BloodLinkExceptions.requestNotFound());
        
        // Validate ownership
        if (!request.getPatient().getUserId().equals(patientId)) {
            throw new UnauthorizedException("You can only cancel your own requests");
        }
        
        // Validate status
        if (!request.getStatus().equals(BloodRequest.RequestStatus.PENDING)) {
            throw new InvalidOperationException("Only pending requests can be cancelled");
        }
        
        try {
            request.setStatus(BloodRequest.RequestStatus.CANCELLED);
            request.setUpdatedAt(LocalDateTime.now());
            bloodRequestRepository.save(request);
            
            log.info("Blood request cancelled: {}", requestId);
            
        } catch (Exception e) {
            log.error("Error cancelling request", e);
            throw new InvalidOperationException("Failed to cancel request: " + e.getMessage());
        }
    }

    /**
     * Search available donors
     * Business Logic: Finds donors matching patient's blood group and location
     * 
     * @param patientId Patient ID
     * @param maxDistance Maximum distance in kilometers
     * @return List of available donors
     */
    public List<UserDTO.DonorDTO> searchAvailableDonors(Long patientId, Double maxDistance) {
        log.info("Searching available donors for patient: {}", patientId);
        
        Patient patient = patientRepository.findById(patientId)
            .orElseThrow(() -> BloodLinkExceptions.patientNotFound());
        
        if (patient.getLatitude() == null || patient.getLongitude() == null) {
            throw new ValidationException("Patient location is required");
        }
        
        // Use donor service to find eligible donors
        return donorService.findNearbyDonors(
            patient.getLatitude(), 
            patient.getLongitude(), 
            maxDistance != null ? maxDistance : 50.0
        );
    }

    /**
     * Update patient profile
     * 
     * @param patientId Patient ID
     * @param updates Map of fields to update
     */
    public void updatePatientProfile(Long patientId, Map<String, Object> updates) {
        log.info("Updating patient profile: {}", patientId);
        
        Patient patient = patientRepository.findById(patientId)
            .orElseThrow(() -> BloodLinkExceptions.patientNotFound());
        
        try {
            if (updates.containsKey("fullName")) {
                patient.setFullName((String) updates.get("fullName"));
            }
            if (updates.containsKey("phoneNumber")) {
                patient.setPhoneNumber((String) updates.get("phoneNumber"));
            }
            if (updates.containsKey("city")) {
                patient.setCity((String) updates.get("city"));
            }
            if (updates.containsKey("state")) {
                patient.setState((String) updates.get("state"));
            }
            if (updates.containsKey("latitude")) {
                patient.setLatitude(Double.parseDouble(updates.get("latitude").toString()));
            }
            if (updates.containsKey("longitude")) {
                patient.setLongitude(Double.parseDouble(updates.get("longitude").toString()));
            }
            if (updates.containsKey("hospital")) {
                patient.setHospital((String) updates.get("hospital"));
            }
            
            patient.setUpdatedAt(LocalDateTime.now());
            patientRepository.save(patient);
            log.info("Patient profile updated: {}", patientId);
            
        } catch (Exception e) {
            log.error("Error updating patient profile", e);
            throw new ValidationException("Failed to update profile: " + e.getMessage());
        }
    }

    /**
     * Get medical records for patient
     * 
     * @param patientId Patient ID
     * @return List of medical records
     */
    public List<MedicalRecord> getMedicalRecords(Long patientId) {
        log.debug("Fetching medical records for patient: {}", patientId);
        
        Patient patient = patientRepository.findById(patientId)
            .orElseThrow(() -> BloodLinkExceptions.patientNotFound());
        
        return medicalRecordRepository.findByPatient(patient);
    }

    /**
     * Add medical record for patient
     * 
     * @param patientId Patient ID
     * @param medicalRecord Medical record to add
     * @return Created MedicalRecord
     */
    public MedicalRecord addMedicalRecord(Long patientId, MedicalRecord medicalRecord) {
        log.info("Adding medical record for patient: {}", patientId);
        
        Patient patient = patientRepository.findById(patientId)
            .orElseThrow(() -> BloodLinkExceptions.patientNotFound());
        
        try {
            medicalRecord.setPatient(patient);
            medicalRecord.setRecordDate(LocalDateTime.now());
            
            MedicalRecord saved = medicalRecordRepository.save(medicalRecord);
            log.info("Medical record added for patient: {}", patientId);
            
            return saved;
            
        } catch (Exception e) {
            log.error("Error adding medical record", e);
            throw new InvalidOperationException("Failed to add medical record: " + e.getMessage());
        }
    }

    /**
     * Check if patient can make blood request
     * Business Logic: Validates patient status and constraints
     * 
     * @param patientId Patient ID
     * @return true if patient can make request, false otherwise
     */
    public boolean canPatientMakeRequest(Long patientId) {
        log.debug("Checking if patient can make request: {}", patientId);
        
        Patient patient = patientRepository.findById(patientId)
            .orElseThrow(() -> BloodLinkExceptions.patientNotFound());
        
        // Check if patient is active
        if (!patient.getIsActive()) {
            return false;
        }
        
        // Check location info
        if (patient.getLatitude() == null || patient.getLongitude() == null) {
            return false;
        }
        
        return true;
    }

    /**
     * Get critical patients (CRITICAL or LIFE_THREATENING)
     * 
     * @return List of critical patients
     */
    public List<PatientDTO> getCriticalPatients() {
        log.debug("Fetching critical patients");
        return patientRepository.findCriticalPatients()
            .stream()
            .map(this::convertToPatientDTO)
            .collect(Collectors.toList());
    }

    /**
     * Count patients by emergency level
     * 
     * @return Map of emergency levels to patient counts
     */
    public Map<String, Integer> getPatientsByEmergencyLevel() {
        log.debug("Getting patients by emergency level");
        
        Map<String, Integer> result = new HashMap<>();
        for (Patient.EmergencyLevel level : Patient.EmergencyLevel.values()) {
            int count = patientRepository.countByEmergencyLevel(level);
            result.put(level.toString(), count);
        }
        
        return result;
    }

    // ==================== Helper Methods ====================

    /**
     * Convert Patient entity to PatientDTO
     * Encapsulation: Hides DTO conversion logic
     * 
     * @param patient Patient entity
     * @return PatientDTO
     */
    private PatientDTO convertToPatientDTO(Patient patient) {
        return PatientDTO.builder()
            .userId(patient.getUserId())
            .fullName(patient.getFullName())
            .email(patient.getEmail())
            .phoneNumber(patient.getPhoneNumber())
            .city(patient.getCity())
            .state(patient.getState())
            .latitude(patient.getLatitude())
            .longitude(patient.getLongitude())
            .bloodGroup(patient.getRequiredBloodGroup())
            .hospital(patient.getHospital())
            .userRole(patient.getUserRole())
            .build();
    }
}
