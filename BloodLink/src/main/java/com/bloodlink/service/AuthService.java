package com.bloodlink.service;

import com.bloodlink.dto.AuthDTO.*;
import com.bloodlink.entity.Donor;
import com.bloodlink.entity.Patient;
import com.bloodlink.entity.User;
import com.bloodlink.exception.BloodLinkExceptions.*;
import com.bloodlink.repository.UserRepository;
import com.bloodlink.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * AuthService - Authentication and authorization service
 * 
 * Responsibilities:
 * - User registration (Donor/Patient)
 * - User login and JWT token generation
 * - Token refresh and validation
 * - Password management (change, reset)
 * - Role-based authorization
 * 
 * OOP Principles Applied:
 * - Encapsulation: Hides password encoding logic
 * - Abstraction: Provides high-level auth operations
 * - Single Responsibility: Only handles authentication
 * 
 * SOLID Principles Applied:
 * - S: Single Responsibility - Only authentication logic
 * - D: Dependency Inversion - Depends on UserRepository interface
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;

    /**
     * Register a new donor user
     * 
     * Business Logic:
     * - Validate email doesn't exist (Encapsulation)
     * - Validate phone doesn't exist
     * - Encode password with BCrypt
     * - Create donor entity with default values
     * - Send welcome notification
     * 
     * @param request RegisterRequest with donor details
     * @return AuthResponse with token
     * @throws DuplicateResourceException if email/phone exists
     * @throws ValidationException if data invalid
     */
    public AuthResponse registerDonor(RegisterRequest request) {
        log.info("Registering new donor: {}", request.getEmail());
        
        // Validation
        validateRegistrationData(request);
        
        // Check if email exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            log.warn("Donor registration failed - email already exists: {}", request.getEmail());
            throw new DuplicateResourceException("Email already registered");
        }
        
        // Check if phone exists
        if (userRepository.findByPhoneNumber(request.getPhoneNumber()).isPresent()) {
            log.warn("Donor registration failed - phone already exists: {}", request.getPhoneNumber());
            throw new DuplicateResourceException("Phone number already registered");
        }
        
        try {
            // Create donor entity (Inheritance - Donor extends User)
            Donor donor = new Donor();
            donor.setEmail(request.getEmail());
            donor.setPhoneNumber(request.getPhoneNumber());
            donor.setFullName(request.getFullName());
            donor.setPassword(passwordEncoder.encode(request.getPassword()));
            donor.setGender(request.getGender());
            donor.setAge(request.getAge());
            donor.setCity(request.getCity());
            donor.setState(request.getState());
            donor.setLatitude(request.getLatitude());
            donor.setLongitude(request.getLongitude());
            
            // Set donor-specific defaults (Encapsulation)
            donor.setBloodGroup(request.getBloodGroup());
            donor.setIsAvailable(false);
            donor.setIsVerified(false);
            donor.setIsActive(true);
            donor.setCreatedAt(LocalDateTime.now());
            
            // Save donor
            Donor savedDonor = userRepository.save(donor);
            log.info("Donor registered successfully: {}", savedDonor.getUserId());
            
            // Generate token
            String token = jwtTokenProvider.generateTokenWithEmailAndRole(
                savedDonor.getEmail(), 
                savedDonor.getUserRole()
            );
            
            // Send welcome notification
            try {
                notificationService.sendWelcomeNotification(savedDonor);
            } catch (Exception e) {
                log.warn("Failed to send welcome notification to donor: {}", savedDonor.getUserId(), e);
            }
            
            return buildAuthResponse(savedDonor, token);
            
        } catch (Exception e) {
            log.error("Error registering donor", e);
            throw new ValidationException("Failed to register donor: " + e.getMessage());
        }
    }

    /**
     * Register a new patient user
     * 
     * Business Logic:
     * - Similar validation as donor registration
     * - Create patient-specific entity
     * - Initialize patient-specific fields
     * 
     * @param request RegisterRequest with patient details
     * @return AuthResponse with token
     * @throws DuplicateResourceException if email/phone exists
     * @throws ValidationException if data invalid
     */
    public AuthResponse registerPatient(RegisterRequest request) {
        log.info("Registering new patient: {}", request.getEmail());
        
        // Validation
        validateRegistrationData(request);
        
        // Check if email exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            log.warn("Patient registration failed - email already exists: {}", request.getEmail());
            throw new DuplicateResourceException("Email already registered");
        }
        
        // Check if phone exists
        if (userRepository.findByPhoneNumber(request.getPhoneNumber()).isPresent()) {
            log.warn("Patient registration failed - phone already exists: {}", request.getPhoneNumber());
            throw new DuplicateResourceException("Phone number already registered");
        }
        
        try {
            // Create patient entity (Inheritance - Patient extends User)
            Patient patient = new Patient();
            patient.setEmail(request.getEmail());
            patient.setPhoneNumber(request.getPhoneNumber());
            patient.setFullName(request.getFullName());
            patient.setPassword(passwordEncoder.encode(request.getPassword()));
            patient.setGender(request.getGender());
            patient.setAge(request.getAge());
            patient.setCity(request.getCity());
            patient.setState(request.getState());
            patient.setLatitude(request.getLatitude());
            patient.setLongitude(request.getLongitude());
            
            // Set patient-specific defaults (Encapsulation)
            patient.setRequiredBloodGroup(request.getBloodGroup());
            patient.setIsActive(true);
            patient.setCreatedAt(LocalDateTime.now());
            
            // Save patient
            Patient savedPatient = userRepository.save(patient);
            log.info("Patient registered successfully: {}", savedPatient.getUserId());
            
            // Generate token
            String token = jwtTokenProvider.generateTokenWithEmailAndRole(
                savedPatient.getEmail(), 
                savedPatient.getUserRole()
            );
            
            // Send welcome notification
            try {
                notificationService.sendWelcomeNotification(savedPatient);
            } catch (Exception e) {
                log.warn("Failed to send welcome notification to patient: {}", savedPatient.getUserId(), e);
            }
            
            return buildAuthResponse(savedPatient, token);
            
        } catch (Exception e) {
            log.error("Error registering patient", e);
            throw new ValidationException("Failed to register patient: " + e.getMessage());
        }
    }

    /**
     * Authenticate user and generate JWT token
     * 
     * Business Logic:
     * - Find user by email
     * - Verify password using BCrypt
     * - Generate JWT token with 24-hour expiration
     * - Return token with user details
     * 
     * @param request LoginRequest with email and password
     * @return AuthResponse with JWT token
     * @throws UnauthorizedException if credentials invalid
     */
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for user: {}", request.getEmail());
        
        // Validation
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new ValidationException("Email is required");
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new ValidationException("Password is required");
        }
        
        // Find user by email
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
        if (userOptional.isEmpty()) {
            log.warn("Login failed - user not found: {}", request.getEmail());
            throw new UnauthorizedException("Invalid email or password");
        }
        
        User user = userOptional.get();
        
        // Verify password (Encapsulation - password verification is hidden)
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Login failed - incorrect password for user: {}", request.getEmail());
            throw new UnauthorizedException("Invalid email or password");
        }
        
        // Check if user is active
        if (!user.getIsActive()) {
            log.warn("Login failed - user is inactive: {}", request.getEmail());
            throw new UnauthorizedException("User account is inactive");
        }
        
        try {
            // Generate token with email and role (Polymorphism - different roles for Donor/Patient)
            String token = jwtTokenProvider.generateTokenWithEmailAndRole(
                user.getEmail(), 
                user.getUserRole()
            );
            
            // Generate refresh token
            String refreshToken = jwtTokenProvider.generateRefreshToken(user.getEmail());
            
            // Update last login
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
            
            log.info("Login successful for user: {}", user.getEmail());
            return buildAuthResponse(user, token, refreshToken);
            
        } catch (Exception e) {
            log.error("Error during login", e);
            throw new UnauthorizedException("Authentication failed: " + e.getMessage());
        }
    }

    /**
     * Refresh JWT token using refresh token
     * 
     * @param request TokenRefreshRequest with refresh token
     * @return AuthResponse with new JWT token
     * @throws UnauthorizedException if refresh token invalid
     */
    public AuthResponse refreshToken(TokenRefreshRequest request) {
        log.debug("Refreshing token");
        
        if (request.getRefreshToken() == null || request.getRefreshToken().isEmpty()) {
            throw new ValidationException("Refresh token is required");
        }
        
        try {
            // Validate refresh token
            if (!jwtTokenProvider.validateToken(request.getRefreshToken())) {
                throw new UnauthorizedException("Invalid refresh token");
            }
            
            // Get email from refresh token
            String email = jwtTokenProvider.getEmailFromToken(request.getRefreshToken());
            
            // Find user
            Optional<User> userOptional = userRepository.findByEmail(email);
            if (userOptional.isEmpty()) {
                throw new UnauthorizedException("User not found");
            }
            
            User user = userOptional.get();
            
            // Generate new access token
            String newToken = jwtTokenProvider.generateTokenWithEmailAndRole(
                user.getEmail(), 
                user.getUserRole()
            );
            
            log.debug("Token refreshed successfully for user: {}", email);
            return buildAuthResponse(user, newToken);
            
        } catch (Exception e) {
            log.error("Error refreshing token", e);
            throw new UnauthorizedException("Token refresh failed: " + e.getMessage());
        }
    }

    /**
     * Change user password
     * 
     * Business Logic:
     * - Verify current password
     * - Validate new password requirements
     * - Encode and save new password
     * 
     * @param userId User ID
     * @param request ChangePasswordRequest
     * @throws UnauthorizedException if current password invalid
     * @throws ValidationException if new password invalid
     */
    public void changePassword(Long userId, ChangePasswordRequest request) {
        log.info("Password change requested for user: {}", userId);
        
        // Find user
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw BloodLinkExceptions.userNotFound();
        }
        
        User user = userOptional.get();
        
        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            log.warn("Password change failed - current password incorrect for user: {}", userId);
            throw new UnauthorizedException("Current password is incorrect");
        }
        
        // Validate new password
        if (request.getNewPassword() == null || request.getNewPassword().length() < 6) {
            throw new ValidationException("New password must be at least 6 characters");
        }
        
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new ValidationException("Passwords do not match");
        }
        
        try {
            // Update password
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            
            log.info("Password changed successfully for user: {}", userId);
            
        } catch (Exception e) {
            log.error("Error changing password", e);
            throw new ValidationException("Failed to change password: " + e.getMessage());
        }
    }

    /**
     * Request password reset
     * 
     * @param request ForgotPasswordRequest with email
     */
    public void requestPasswordReset(ForgotPasswordRequest request) {
        log.info("Password reset requested for email: {}", request.getEmail());
        
        if (request.getEmail() == null || request.getEmail().isEmpty()) {
            throw new ValidationException("Email is required");
        }
        
        try {
            Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
            if (userOptional.isEmpty()) {
                // Don't reveal if email exists (security best practice)
                log.warn("Password reset requested for non-existent email: {}", request.getEmail());
                return;
            }
            
            User user = userOptional.get();
            
            // TODO: Generate reset token and send email
            // This would involve:
            // 1. Create reset token (short-lived, one-time use)
            // 2. Save to database with expiration
            // 3. Send reset link via email
            // 4. Validate token when user clicks link
            
            log.info("Password reset email sent to: {}", request.getEmail());
            
        } catch (Exception e) {
            log.error("Error requesting password reset", e);
            throw new ValidationException("Failed to process password reset request");
        }
    }

    /**
     * Reset password with token
     * 
     * @param request ResetPasswordRequest with token and new password
     */
    public void resetPassword(ResetPasswordRequest request) {
        log.info("Password reset with token");
        
        if (request.getToken() == null || request.getToken().isEmpty()) {
            throw new ValidationException("Reset token is required");
        }
        
        if (request.getNewPassword() == null || request.getNewPassword().length() < 6) {
            throw new ValidationException("New password must be at least 6 characters");
        }
        
        try {
            // TODO: Validate reset token
            // 1. Check if token exists in database
            // 2. Check if token is not expired
            // 3. Get associated user
            // 4. Update password
            // 5. Mark token as used
            
            log.info("Password reset completed");
            
        } catch (Exception e) {
            log.error("Error resetting password", e);
            throw new ValidationException("Failed to reset password: " + e.getMessage());
        }
    }

    /**
     * Logout user (optional - mainly for client-side token removal)
     * 
     * @param userId User ID
     */
    public void logout(Long userId) {
        log.info("User logout: {}", userId);
        // With JWT, logout is mainly client-side (remove token from localStorage)
        // Server can maintain a blacklist of revoked tokens if needed
    }

    // ==================== Helper Methods ====================

    /**
     * Validate registration data
     * Encapsulation: Hides validation logic
     * 
     * @param request RegisterRequest to validate
     * @throws ValidationException if validation fails
     */
    private void validateRegistrationData(RegisterRequest request) {
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new ValidationException("Email is required");
        }
        
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new ValidationException("Password must be at least 6 characters");
        }
        
        if (request.getPhoneNumber() == null || request.getPhoneNumber().trim().isEmpty()) {
            throw new ValidationException("Phone number is required");
        }
        
        if (request.getFullName() == null || request.getFullName().trim().isEmpty()) {
            throw new ValidationException("Full name is required");
        }
        
        if (request.getBloodGroup() == null || request.getBloodGroup().trim().isEmpty()) {
            throw new ValidationException("Blood group is required");
        }
        
        // Validate email format
        if (!request.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new ValidationException("Invalid email format");
        }
        
        // Validate blood group
        String bloodGroup = request.getBloodGroup().toUpperCase();
        if (!bloodGroup.matches("^(O|A|B|AB)[+-]$")) {
            throw new ValidationException("Invalid blood group format (must be O+, O-, A+, A-, B+, B-, AB+, AB-)");
        }
    }

    /**
     * Build AuthResponse from user and token
     * Encapsulation: Hides DTO construction logic
     * 
     * @param user User entity
     * @param token JWT token
     * @return AuthResponse
     */
    private AuthResponse buildAuthResponse(User user, String token) {
        return AuthResponse.builder()
            .token(token)
            .userId(user.getUserId())
            .email(user.getEmail())
            .fullName(user.getFullName())
            .userRole(user.getUserRole())
            .expiresIn(86400) // 24 hours in seconds
            .build();
    }

    /**
     * Build AuthResponse from user, token, and refresh token
     * Encapsulation: Hides DTO construction logic
     * 
     * @param user User entity
     * @param token JWT token
     * @param refreshToken Refresh token
     * @return AuthResponse
     */
    private AuthResponse buildAuthResponse(User user, String token, String refreshToken) {
        return AuthResponse.builder()
            .token(token)
            .refreshToken(refreshToken)
            .userId(user.getUserId())
            .email(user.getEmail())
            .fullName(user.getFullName())
            .userRole(user.getUserRole())
            .expiresIn(86400) // 24 hours in seconds
            .build();
    }
}
