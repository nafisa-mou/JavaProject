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

import java.util.List;
import java.util.Map;

/**
 * PatientController - REST API for patient operations
 * 
 * Endpoints:
 * - GET /api/patients - Get all patients
 * - GET /api/patients/{id} - Get patient by ID
 * - GET /api/patients/{id}/profile - Get patient profile
 * - GET /api/patients/{id}/requests - Get patient's blood requests
 * - POST /api/patients/{id}/requests - Create blood request
 * - GET /api/patients/{id}/donors - Search available donors
 * - GET /api/patients/critical - Get critical patients
 * - PUT /api/patients/{id} - Update patient profile
 * - GET /api/patients/{id}/medical-records - Get medical records
 * - POST /api/patients/{id}/medical-records - Add medical record
 * 
 * Security:
 * - Public: GET all patients, search (limited)
 * - Protected: POST, PUT operations
 * 
 * OOP Principles:
 * - Encapsulation: Hides service implementation
 * - Single Responsibility: Only patient endpoints
 */
@Slf4j
@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:4200", "http://localhost:8080"})
public class PatientController {

    private final PatientService patientService;

    /**
     * Get all patients
     * 
     * @return List of all patients
     * @status 200 OK
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<PatientDTO>>> getAllPatients() {
        log.info("GET /api/patients");
        
        try {
            List<PatientDTO> patients = patientService.getAllPatients();
            return ResponseEntity.ok(ApiResponse.success(patients, "Patients retrieved successfully"));
            
        } catch (Exception e) {
            log.error("Error fetching patients", e);
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Failed to fetch patients"));
        }
    }

    /**
     * Get patient by ID
     * 
     * @param patientId Patient ID
     * @return PatientDTO
     * @status 200 OK
     * @status 404 NOT_FOUND
     */
    @GetMapping("/{patientId}")
    public ResponseEntity<ApiResponse<PatientDTO>> getPatientById(@PathVariable Long patientId) {
        log.info("GET /api/patients/{}", patientId);
        
        try {
            PatientDTO patient = patientService.getPatientById(patientId);
            return ResponseEntity.ok(ApiResponse.success(patient, "Patient retrieved successfully"));
            
        } catch (Exception e) {
            log.error("Patient not found: {}", patientId, e);
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Patient not found"));
        }
    }

    /**
     * Get patient profile with detailed information
     * 
     * @param patientId Patient ID
     * @return Profile map
     * @status 200 OK
     * @status 404 NOT_FOUND
     */
    @GetMapping("/{patientId}/profile")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPatientProfile(@PathVariable Long patientId) {
        log.info("GET /api/patients/{}/profile", patientId);
        
        try {
            Map<String, Object> profile = patientService.getPatientProfile(patientId);
            return ResponseEntity.ok(ApiResponse.success(profile, "Patient profile retrieved"));
            
        } catch (Exception e) {
            log.error("Error fetching patient profile: {}", patientId, e);
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Patient not found"));
        }
    }

    /**
     * Get patient's blood requests
     * 
     * @param patientId Patient ID
     * @return List of blood requests
     * @status 200 OK
     * @status 404 NOT_FOUND
     */
    @GetMapping("/{patientId}/requests")
    @PreAuthorize("hasRole('PATIENT') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<BloodRequest>>> getPatientRequests(@PathVariable Long patientId) {
        log.info("GET /api/patients/{}/requests", patientId);
        
        try {
            List<BloodRequest> requests = patientService.getPatientRequests(patientId);
            return ResponseEntity.ok(ApiResponse.success(requests, "Requests retrieved: " + requests.size()));
            
        } catch (Exception e) {
            log.error("Error fetching requests: {}", patientId, e);
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Patient not found"));
        }
    }

    /**
     * Create blood request
     * 
     * @param patientId Patient ID
     * @param request Blood request details
     * @return BloodRequest
     * @status 201 CREATED
     * @status 401 UNAUTHORIZED
     * @status 400 BAD_REQUEST
     */
    @PostMapping("/{patientId}/requests")
    @PreAuthorize("hasRole('PATIENT') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BloodRequest>> createBloodRequest(
            @PathVariable Long patientId,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/patients/{}/requests", patientId);
        
        try {
            String bloodGroup = (String) request.get("bloodGroup");
            String urgency = (String) request.getOrDefault("urgencyLevel", "URGENT");
            
            BloodRequest.BloodRequest newRequest = patientService.createBloodRequest(
                patientId, 
                bloodGroup, 
                Patient.EmergencyLevel.valueOf(urgency)
            );
            
            return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(newRequest, "Blood request created successfully"));
            
        } catch (Exception e) {
            log.error("Error creating blood request: {}", patientId, e);
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Failed to create request: " + e.getMessage()));
        }
    }

    /**
     * Search available donors for patient
     * 
     * @param patientId Patient ID
     * @param maxDistance Maximum distance in km
     * @return List of available donors
     * @status 200 OK
     * @status 404 NOT_FOUND
     */
    @GetMapping("/{patientId}/donors")
    public ResponseEntity<ApiResponse<List<?>>> searchAvailableDonors(
            @PathVariable Long patientId,
            @RequestParam(defaultValue = "50") Double maxDistance) {
        log.info("GET /api/patients/{}/donors?maxDistance={}", patientId, maxDistance);
        
        try {
            var donors = patientService.searchAvailableDonors(patientId, maxDistance);
            return ResponseEntity.ok(ApiResponse.success(donors, "Available donors found: " + donors.size()));
            
        } catch (Exception e) {
            log.error("Error searching donors: {}", patientId, e);
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Failed to search donors"));
        }
    }

    /**
     * Get critical patients (CRITICAL or LIFE_THREATENING)
     * Requires admin role
     * 
     * @return List of critical patients
     * @status 200 OK
     * @status 403 FORBIDDEN
     */
    @GetMapping("/critical")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<PatientDTO>>> getCriticalPatients() {
        log.info("GET /api/patients/critical");
        
        try {
            List<PatientDTO> patients = patientService.getCriticalPatients();
            return ResponseEntity.ok(ApiResponse.success(patients, "Critical patients retrieved: " + patients.size()));
            
        } catch (Exception e) {
            log.error("Error fetching critical patients", e);
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Failed to fetch critical patients"));
        }
    }

    /**
     * Update patient profile
     * 
     * @param patientId Patient ID
     * @param updates Profile updates
     * @return ApiResponse
     * @status 200 OK
     * @status 401 UNAUTHORIZED
     * @status 404 NOT_FOUND
     */
    @PutMapping("/{patientId}")
    @PreAuthorize("hasRole('PATIENT') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> updatePatientProfile(
            @PathVariable Long patientId,
            @RequestBody Map<String, Object> updates) {
        log.info("PUT /api/patients/{}", patientId);
        
        try {
            patientService.updatePatientProfile(patientId, updates);
            return ResponseEntity.ok(ApiResponse.success(null, "Patient profile updated successfully"));
            
        } catch (Exception e) {
            log.error("Error updating patient profile: {}", patientId, e);
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Failed to update profile: " + e.getMessage()));
        }
    }

    /**
     * Get patient medical records
     * 
     * @param patientId Patient ID
     * @return List of medical records
     * @status 200 OK
     * @status 404 NOT_FOUND
     */
    @GetMapping("/{patientId}/medical-records")
    @PreAuthorize("hasRole('PATIENT') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<MedicalRecord>>> getMedicalRecords(@PathVariable Long patientId) {
        log.info("GET /api/patients/{}/medical-records", patientId);
        
        try {
            List<MedicalRecord> records = patientService.getMedicalRecords(patientId);
            return ResponseEntity.ok(ApiResponse.success(records, "Medical records retrieved"));
            
        } catch (Exception e) {
            log.error("Error fetching medical records: {}", patientId, e);
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Patient not found"));
        }
    }

    /**
     * Add medical record for patient
     * 
     * @param patientId Patient ID
     * @param medicalRecord Medical record details
     * @return MedicalRecord
     * @status 201 CREATED
     * @status 401 UNAUTHORIZED
     * @status 404 NOT_FOUND
     */
    @PostMapping("/{patientId}/medical-records")
    @PreAuthorize("hasRole('PATIENT') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MedicalRecord>> addMedicalRecord(
            @PathVariable Long patientId,
            @RequestBody MedicalRecord medicalRecord) {
        log.info("POST /api/patients/{}/medical-records", patientId);
        
        try {
            MedicalRecord saved = patientService.addMedicalRecord(patientId, medicalRecord);
            return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(saved, "Medical record added successfully"));
            
        } catch (Exception e) {
            log.error("Error adding medical record: {}", patientId, e);
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Failed to add medical record"));
        }
    }

    /**
     * Get patients by emergency level statistics
     * Requires admin role
     * 
     * @return Map of emergency levels to patient counts
     * @status 200 OK
     */
    @GetMapping("/stats/emergency-levels")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> getEmergencyLevelStats() {
        log.info("GET /api/patients/stats/emergency-levels");
        
        try {
            Map<String, Integer> stats = patientService.getPatientsByEmergencyLevel();
            return ResponseEntity.ok(ApiResponse.success(stats, "Emergency level statistics retrieved"));
            
        } catch (Exception e) {
            log.error("Error fetching stats", e);
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Failed to fetch statistics"));
        }
    }

    // ==================== Helper Classes ====================

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class ApiResponse<T> {
        private boolean success;
        private T data;
        private String message;

        public static <T> ApiResponse<T> success(T data, String message) {
            return new ApiResponse<>(true, data, message);
        }

        public static <T> ApiResponse<T> error(String message) {
            return new ApiResponse<>(false, null, message);
        }
    }
}
