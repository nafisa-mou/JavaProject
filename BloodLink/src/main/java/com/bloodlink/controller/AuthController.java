package com.bloodlink.controller;

import com.bloodlink.dto.AuthDTO.*;
import com.bloodlink.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AuthController - REST API for authentication and authorization
 * 
 * Endpoints:
 * - POST /api/auth/register-donor     - Register new donor
 * - POST /api/auth/register-patient   - Register new patient
 * - POST /api/auth/login              - User login
 * - POST /api/auth/refresh-token      - Refresh JWT token
 * - POST /api/auth/logout             - Logout user
 * - POST /api/auth/change-password    - Change password
 * - POST /api/auth/forgot-password    - Request password reset
 * - POST /api/auth/reset-password     - Reset password with token
 * 
 * OOP Principle: Encapsulation - HTTP request/response handling is encapsulated
 * REST Principle: Stateless - Each request contains all necessary information
 * SOLID: Single Responsibility - Only handles authentication endpoints
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080", "http://localhost:4200"})
@Tag(name = "Authentication", description = "Authentication and user registration endpoints")
public class AuthController {

    private final AuthService authService;

    /**
     * Register new donor
     * 
     * @param request RegisterRequest with donor details
     * @return AuthResponse with JWT token and user info
     */
    @PostMapping("/register-donor")
    @Operation(summary = "Register new donor", description = "Creates a new donor account and returns JWT token")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Donor registered successfully", 
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "409", description = "Email already exists")
    })
    public ResponseEntity<?> registerDonor(@RequestBody RegisterRequest request) {
        log.info("Registering new donor: {}", request.getEmail());
        
        try {
            AuthResponse response = authService.registerDonor(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            log.error("Donor registration failed", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("Registration failed", e.getMessage()));
        }
    }

    /**
     * Register new patient
     * 
     * @param request RegisterRequest with patient details
     * @return AuthResponse with JWT token and user info
     */
    @PostMapping("/register-patient")
    @Operation(summary = "Register new patient", description = "Creates a new patient account and returns JWT token")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Patient registered successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "409", description = "Email already exists")
    })
    public ResponseEntity<?> registerPatient(@RequestBody RegisterRequest request) {
        log.info("Registering new patient: {}", request.getEmail());
        
        try {
            AuthResponse response = authService.registerPatient(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            log.error("Patient registration failed", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("Registration failed", e.getMessage()));
        }
    }

    /**
     * User login
     * 
     * Request Body:
     * {
     *   "email": "user@example.com",
     *   "password": "password123"
     * }
     * 
     * @param request LoginRequest with email and password
     * @return AuthResponse with JWT token
     */
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticates user and returns JWT token")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login successful"),
        @ApiResponse(responseCode = "400", description = "Invalid credentials"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        log.info("Login attempt for: {}", request.getEmail());
        
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Login failed", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("Login failed", e.getMessage()));
        }
    }

    /**
     * Refresh JWT token
     * 
     * Request Body:
     * {
     *   "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
     * }
     * 
     * @param request TokenRefreshRequest with refresh token
     * @return AuthResponse with new JWT token
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody TokenRefreshRequest request) {
        log.debug("Token refresh request");
        
        try {
            AuthResponse response = authService.refreshToken(request);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Token refresh failed", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("Token refresh failed", e.getMessage()));
        }
    }

    /**
     * Logout user
     * 
     * @return Success response
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        log.debug("Logout request");
        
        try {
            // With JWT, logout is mainly client-side (remove token)
            // Server can maintain token blacklist if needed
            return ResponseEntity.ok(new SuccessResponse("Logged out successfully"));
            
        } catch (Exception e) {
            log.error("Logout failed", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("Logout failed", e.getMessage()));
        }
    }

    /**
     * Change password
     * 
     * Request Body:
     * {
     *   "currentPassword": "oldPassword123",
     *   "newPassword": "newPassword123",
     *   "confirmPassword": "newPassword123"
     * }
     * 
     * @param userId User ID
     * @param request ChangePasswordRequest
     * @return Success response
     */
    @PostMapping("/change-password/{userId}")
    public ResponseEntity<?> changePassword(@PathVariable Long userId, 
                                           @RequestBody ChangePasswordRequest request) {
        log.info("Password change request for user: {}", userId);
        
        try {
            authService.changePassword(userId, request);
            return ResponseEntity.ok(new SuccessResponse("Password changed successfully"));
            
        } catch (Exception e) {
            log.error("Password change failed", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("Password change failed", e.getMessage()));
        }
    }

    /**
     * Request password reset
     * 
     * Request Body:
     * {
     *   "email": "user@example.com"
     * }
     * 
     * @param request ForgotPasswordRequest with email
     * @return Success response
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        log.info("Password reset requested for: {}", request.getEmail());
        
        try {
            authService.requestPasswordReset(request);
            // Don't reveal if email exists (security best practice)
            return ResponseEntity.ok(new SuccessResponse("Password reset email sent if account exists"));
            
        } catch (Exception e) {
            log.error("Password reset request failed", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("Failed to process request", e.getMessage()));
        }
    }

    /**
     * Reset password with token
     * 
     * Request Body:
     * {
     *   "token": "reset-token-here",
     *   "newPassword": "newPassword123",
     *   "confirmPassword": "newPassword123"
     * }
     * 
     * @param request ResetPasswordRequest with token and new password
     * @return Success response
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        log.debug("Password reset with token");
        
        try {
            authService.resetPassword(request);
            return ResponseEntity.ok(new SuccessResponse("Password reset successfully"));
            
        } catch (Exception e) {
            log.error("Password reset failed", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("Password reset failed", e.getMessage()));
        }
    }

    /**
     * Health check endpoint
     * 
     * @return Health status
     */
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        return ResponseEntity.ok(Map.of("status", "AUTH_SERVICE_RUNNING"));
    }

    // ==================== Helper Classes ====================

    /**
     * Error response DTO
     */
    public static class ErrorResponse {
        private String error;
        private String message;
        private long timestamp = System.currentTimeMillis();

        public ErrorResponse(String error, String message) {
            this.error = error;
            this.message = message;
        }

        public String getError() { return error; }
        public String getMessage() { return message; }
        public long getTimestamp() { return timestamp; }
    }

    /**
     * Success response DTO
     */
    public static class SuccessResponse {
        private String message;
        private long timestamp = System.currentTimeMillis();

        public SuccessResponse(String message) {
            this.message = message;
        }

        public String getMessage() { return message; }
        public long getTimestamp() { return timestamp; }
    }
}
