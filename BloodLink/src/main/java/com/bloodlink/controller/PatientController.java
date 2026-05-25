package com.bloodlink.controller;

import com.bloodlink.dto.UserDTO.PatientDTO;
import com.bloodlink.entity.*;
import com.bloodlink.service.PatientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * PatientController - REST API for patient operations
 * 
 * Endpoints:
 * - GET    /api/patients                   - Get all patients
 * - GET    /api/patients/{id}              - Get patient by ID
 * - GET    /api/patients/{id}/profile      - Get patient profile
 * - GET    /api/patients/{id}/requests     - Get patient's requests
 * - GET    /api/patients/{id}/medical      - Get medical records
 * - GET    /api/patients/{id}/donors       - Search available donors
 * - GET    /api/patients/critical          - Get critical patients
 * - PUT    /api/patients/{id}              - Update patient profile
 * - POST   /api/patients/{id}/request      - Create blood request
 * - POST   /api/patients/{id}/medical      - Add medical record
 * - DELETE /api/patients/{id}/request/{rId} - Cancel blood request
 * 
 * OOP Principle: Encapsulation - Complex logic delegated to PatientService
 * REST Principle: Resource-oriented - Each endpoint represents a patient resource
 * SOLID: Single Responsibility - Only handles HTTP concerns
 */
@Slf4j
@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080", "http://localhost:4200"})
public class PatientController {

    private final PatientService patientService;

    /**
     * Get all patients
     * 
     * @return List of all patients
     */
    @GetMapping
    public ResponseEntity<?> getAllPatients() {
        log.debug("Fetching all patients");
        
        try {
            List<PatientDTO> patients = patientService.getAllPatients();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "count", patients.size(),
                "data", patients
            ));
            
        } catch (Exception e) {
            log.error("Error fetching patients", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Get patient by ID
     * 
     * @param id Patient ID
     * @return Patient details
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getPatientById(@PathVariable Long id) {
        log.debug("Fetching patient: {}", id);
        
        try {
            PatientDTO patient = patientService.getPatientById(id);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", patient
            ));
            
        } catch (Exception e) {
            log.error("Error fetching patient", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Get patient profile with all details
     * 
     * @param id Patient ID
     * @return Complete patient profile
     */
    @GetMapping("/{id}/profile")
    public ResponseEntity<?> getPatientProfile(@PathVariable Long id) {
        log.debug("Fetching patient profile: {}", id);
        
        try {
            Map<String, Object> profile = patientService.getPatientProfile(id);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", profile
            ));
            
        } catch (Exception e) {
            log.error("Error fetching patient profile", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Get patient's blood requests
     * 
     * @param id Patient ID
     * @return List of patient's requests
     */
    @GetMapping("/{id}/requests")
    public ResponseEntity<?> getPatientRequests(@PathVariable Long id) {
        log.debug("Fetching requests for patient: {}", id);
        
        try {
            List<BloodRequest> requests = patientService.getPatientRequests(id);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "count", requests.size(),
                "data", requests
            ));
            
        } catch (Exception e) {
            log.error("Error fetching patient requests", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Get patient's pending blood requests
     * 
     * @param id Patient ID
     * @return List of pending requests
     */
    @GetMapping("/{id}/requests/pending")
    public ResponseEntity<?> getPendingRequests(@PathVariable Long id) {
        log.debug("Fetching pending requests for patient: {}", id);
        
        try {
            List<BloodRequest> requests = patientService.getPendingRequests(id);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "count", requests.size(),
                "data", requests
            ));
            
        } catch (Exception e) {
            log.error("Error fetching pending requests", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Get patient's medical records
     * 
     * @param id Patient ID
     * @return List of medical records
     */
    @GetMapping("/{id}/medical")
    public ResponseEntity<?> getMedicalRecords(@PathVariable Long id) {
        log.debug("Fetching medical records for patient: {}", id);
        
        try {
            List<MedicalRecord> records = patientService.getMedicalRecords(id);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "count", records.size(),
                "data", records
            ));
            
        } catch (Exception e) {
            log.error("Error fetching medical records", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Search available donors for patient
     * 
     * Query Parameters:
     * - maxDistance: Max distance in km (optional, default 50)
     * 
     * @param id Patient ID
     * @param maxDistance Maximum distance in kilometers
     * @return List of available donors
     */
    @GetMapping("/{id}/donors")
    public ResponseEntity<?> searchAvailableDonors(@PathVariable Long id,
                                                  @RequestParam(defaultValue = "50") Double maxDistance) {
        log.debug("Searching donors for patient: {}", id);
        
        try {
            var donors = patientService.searchAvailableDonors(id, maxDistance);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "count", donors.size(),
                "maxDistance", maxDistance,
                "data", donors
            ));
            
        } catch (Exception e) {
            log.error("Error searching donors", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Update patient profile
     * 
     * @param id Patient ID
     * @param updates Map of fields to update
     * @return Updated patient info
     */
    @PutMapping("/{id}")
    @PreAuthorize("@patientService.getPatientById(#id).userId == authentication.principal.userId")
    public ResponseEntity<?> updatePatientProfile(@PathVariable Long id,
                                                 @RequestBody Map<String, Object> updates) {
        log.info("Updating patient profile: {}", id);
        
        try {
            patientService.updatePatientProfile(id, updates);
            PatientDTO updated = patientService.getPatientById(id);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Profile updated successfully",
                "data", updated
            ));
            
        } catch (Exception e) {
            log.error("Error updating patient profile", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Create blood request
     * 
     * Request Body:
     * {
     *   "bloodGroup": "O+",
     *   "emergencyLevel": "CRITICAL"
     * }
     * 
     * @param id Patient ID
     * @param request Blood request details
     * @return Created blood request
     */
    @PostMapping("/{id}/request")
    @PreAuthorize("@patientService.canPatientMakeRequest(#id)")
    public ResponseEntity<?> createBloodRequest(@PathVariable Long id,
                                               @RequestBody BloodRequestDto request) {
        log.info("Creating blood request for patient: {}", id);
        
        try {
            BloodRequest bloodRequest = patientService.createBloodRequest(
                id, 
                request.getBloodGroup(), 
                Patient.EmergencyLevel.valueOf(request.getEmergencyLevel())
            );
            
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "message", "Blood request created successfully",
                "data", bloodRequest
            ));
            
        } catch (Exception e) {
            log.error("Error creating blood request", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Cancel blood request
     * 
     * @param id Patient ID
     * @param requestId Blood request ID
     * @return Success response
     */
    @DeleteMapping("/{id}/request/{requestId}")
    @PreAuthorize("@patientService.getPatientById(#id).userId == authentication.principal.userId")
    public ResponseEntity<?> cancelRequest(@PathVariable Long id,
                                          @PathVariable Long requestId) {
        log.info("Cancelling blood request {} for patient: {}", requestId, id);
        
        try {
            patientService.cancelRequest(id, requestId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Request cancelled successfully",
                "requestId", requestId
            ));
            
        } catch (Exception e) {
            log.error("Error cancelling request", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Add medical record for patient
     * 
     * @param id Patient ID
     * @param record Medical record to add
     * @return Created medical record
     */
    @PostMapping("/{id}/medical")
    @PreAuthorize("@patientService.getPatientById(#id).userId == authentication.principal.userId || hasRole('ADMIN')")
    public ResponseEntity<?> addMedicalRecord(@PathVariable Long id,
                                             @RequestBody MedicalRecord record) {
        log.info("Adding medical record for patient: {}", id);
        
        try {
            MedicalRecord savedRecord = patientService.addMedicalRecord(id, record);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "message", "Medical record added successfully",
                "data", savedRecord
            ));
            
        } catch (Exception e) {
            log.error("Error adding medical record", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Get critical patients
     * 
     * @return List of critical patients
     */
    @GetMapping("/critical")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getCriticalPatients() {
        log.debug("Fetching critical patients");
        
        try {
            List<PatientDTO> patients = patientService.getCriticalPatients();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "count", patients.size(),
                "data", patients
            ));
            
        } catch (Exception e) {
            log.error("Error fetching critical patients", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Get patients by emergency level
     * 
     * @return Map of emergency levels to patient counts
     */
    @GetMapping("/statistics/emergency")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getPatientsByEmergencyLevel() {
        log.debug("Getting patients by emergency level");
        
        try {
            Map<String, Integer> stats = patientService.getPatientsByEmergencyLevel();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", stats
            ));
            
        } catch (Exception e) {
            log.error("Error fetching statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // ==================== Helper Classes ====================

    /**
     * Blood request DTO
     */
    @lombok.Data
    public static class BloodRequestDto {
        private String bloodGroup;
        private String emergencyLevel;

        public String getBloodGroup() { return bloodGroup; }
        public String getEmergencyLevel() { return emergencyLevel; }
    }
}
