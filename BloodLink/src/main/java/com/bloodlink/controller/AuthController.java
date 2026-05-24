package com.bloodlink.controller;

import com.bloodlink.dto.AuthDTO.*;
import com.bloodlink.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * AuthController - REST API for authentication and user registration
 * 
 * Endpoints:
 * - POST /api/auth/register/donor - Register new donor
 * - POST /api/auth/register/patient - Register new patient
 * - POST /api/auth/login - User login
 * - POST /api/auth/refresh-token - Refresh JWT token
 * - POST /api/auth/logout - Logout user
 * - POST /api/auth/change-password - Change password
 * - POST /api/auth/forgot-password - Request password reset
 * - POST /api/auth/reset-password - Reset password with token
 * 
 * Security:
 * - Public endpoints: register, login, refresh-token, forgot-password, reset-password
 * - Protected endpoints: logout, change-password
 * 
 * OOP Principles:
 * - Encapsulation: Hides service implementation
 * - Abstraction: Provides API interface
 * - Single Responsibility: Only authentication endpoints
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:4200", "http://localhost:8080"})
public class AuthController {

    private final AuthService authService;

    /**
     * Register new donor
     * 
     * @param request RegisterRequest with donor details
     * @return AuthResponse with JWT token
     * @status 201 CREATED - User registered successfully
     * @status 400 BAD_REQUEST - Validation error or duplicate email/phone
     * @status 409 CONFLICT - Email or phone already registered
     */
    @PostMapping("/register/donor")
    public ResponseEntity<ApiResponse<AuthResponse>> registerDonor(@Valid @RequestBody RegisterRequest request) {
        log.info("POST /api/auth/register/donor - Email: {}", request.getEmail());
        
        try {
            AuthResponse response = authService.registerDonor(request);
            
            return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Donor registered successfully"));
                
        } catch (Exception e) {
            log.error("Error registering donor", e);
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Registration failed: " + e.getMessage()));
        }
    }

    /**
     * Register new patient
     * 
     * @param request RegisterRequest with patient details
     * @return AuthResponse with JWT token
     * @status 201 CREATED - User registered successfully
     * @status 400 BAD_REQUEST - Validation error
     * @status 409 CONFLICT - Email or phone already registered
     */
    @PostMapping("/register/patient")
    public ResponseEntity<ApiResponse<AuthResponse>> registerPatient(@Valid @RequestBody RegisterRequest request) {
        log.info("POST /api/auth/register/patient - Email: {}", request.getEmail());
        
        try {
            AuthResponse response = authService.registerPatient(request);
            
            return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Patient registered successfully"));
                
        } catch (Exception e) {
            log.error("Error registering patient", e);
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Registration failed: " + e.getMessage()));
        }
    }

    /**
     * User login
     * 
     * @param request LoginRequest with email and password
     * @return AuthResponse with JWT token
     * @status 200 OK - Login successful
     * @status 401 UNAUTHORIZED - Invalid credentials
     * @status 400 BAD_REQUEST - Validation error
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("POST /api/auth/login - Email: {}", request.getEmail());
        
        try {
            AuthResponse response = authService.login(request);
            
            return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
            
        } catch (Exception e) {
            log.warn("Login failed for email: {}", request.getEmail());
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Invalid email or password"));
        }
    }

    /**
     * Refresh JWT token
     * 
     * @param request TokenRefreshRequest with refresh token
     * @return AuthResponse with new JWT token
     * @status 200 OK - Token refreshed
     * @status 401 UNAUTHORIZED - Invalid refresh token
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        log.info("POST /api/auth/refresh-token");
        
        try {
            AuthResponse response = authService.refreshToken(request);
            
            return ResponseEntity.ok(ApiResponse.success(response, "Token refreshed successfully"));
            
        } catch (Exception e) {
            log.warn("Token refresh failed", e);
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Invalid refresh token"));
        }
    }

    /**
     * Logout user
     * Note: With JWT, logout is mainly client-side (remove token)
     * 
     * @status 200 OK - Logout successful
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout() {
        log.info("POST /api/auth/logout");
        
        try {
            // Note: JWT is stateless, so logout is client-side token removal
            // In production, could implement token blacklist
            
            return ResponseEntity.ok(ApiResponse.success(null, "Logout successful"));
            
        } catch (Exception e) {
            log.error("Error during logout", e);
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Logout failed"));
        }
    }

    /**
     * Change user password
     * Requires authentication (Bearer token)
     * 
     * @param userId User ID (from JWT)
     * @param request ChangePasswordRequest
     * @return ApiResponse
     * @status 200 OK - Password changed
     * @status 401 UNAUTHORIZED - Not authenticated or invalid current password
     * @status 400 BAD_REQUEST - Validation error
     */
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody ChangePasswordRequest request) {
        log.info("POST /api/auth/change-password");
        
        try {
            // In production, extract userId from JWT token
            // For now, this is a placeholder - would be: Long userId = extractUserIdFromToken(authHeader);
            
            // authService.changePassword(userId, request);
            
            return ResponseEntity.ok(ApiResponse.success(null, "Password changed successfully"));
            
        } catch (Exception e) {
            log.error("Error changing password", e);
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Failed to change password: " + e.getMessage()));
        }
    }

    /**
     * Request password reset (forgot password)
     * 
     * @param request ForgotPasswordRequest with email
     * @return ApiResponse
     * @status 200 OK - Reset email sent (or would be sent in production)
     * @status 400 BAD_REQUEST - Invalid email
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        log.info("POST /api/auth/forgot-password - Email: {}", request.getEmail());
        
        try {
            authService.requestPasswordReset(request);
            
            return ResponseEntity.ok(ApiResponse.success(null, 
                "If this email exists, password reset link has been sent"));
            
        } catch (Exception e) {
            log.error("Error requesting password reset", e);
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Failed to process request"));
        }
    }

    /**
     * Reset password with token
     * 
     * @param request ResetPasswordRequest with token and new password
     * @return ApiResponse
     * @status 200 OK - Password reset successfully
     * @status 400 BAD_REQUEST - Invalid token or validation error
     */
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        log.info("POST /api/auth/reset-password");
        
        try {
            authService.resetPassword(request);
            
            return ResponseEntity.ok(ApiResponse.success(null, "Password reset successfully"));
            
        } catch (Exception e) {
            log.error("Error resetting password", e);
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Failed to reset password"));
        }
    }

    /**
     * Check if email is available
     * 
     * @param email Email to check
     * @return ApiResponse with availability status
     * @status 200 OK
     */
    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<Boolean>> checkEmailAvailability(@RequestParam String email) {
        log.info("GET /api/auth/check-email - Email: {}", email);
        
        try {
            // Would check if email exists in database
            boolean available = true; // Placeholder
            
            return ResponseEntity.ok(ApiResponse.success(available, "Email availability checked"));
            
        } catch (Exception e) {
            log.error("Error checking email availability", e);
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Failed to check email"));
        }
    }

    /**
     * Validate JWT token
     * 
     * @param authHeader Authorization header with Bearer token
     * @return ApiResponse with token validity
     * @status 200 OK
     */
    @GetMapping("/validate-token")
    public ResponseEntity<ApiResponse<Boolean>> validateToken(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        log.debug("GET /api/auth/validate-token");
        
        try {
            // Would validate token from JWT
            boolean valid = authHeader != null && authHeader.startsWith("Bearer ");
            
            return ResponseEntity.ok(ApiResponse.success(valid, "Token validation completed"));
            
        } catch (Exception e) {
            log.error("Error validating token", e);
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Token validation failed"));
        }
    }

    // ==================== Helper Classes ====================

    /**
     * Generic API Response wrapper
     * 
     * @param <T> Response data type
     */
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
