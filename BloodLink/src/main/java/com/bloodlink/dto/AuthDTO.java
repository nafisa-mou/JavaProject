package com.bloodlink.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * LoginRequest - For user login
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {
    private String email;
    private String password;
}

/**
 * RegisterRequest - For user registration
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {
    private String fullName;
    private String email;
    private String phoneNumber;
    private String password;
    private String confirmPassword;
    private String userRole; // DONOR or PATIENT
    private String gender;
    private Integer age;
    private String city;
    private String state;
    private String country;
}

/**
 * AuthResponse - JWT token response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String token;
    private String refreshToken;
    private Long userId;
    private String email;
    private String userRole;
    private String message;
}

/**
 * TokenRefreshRequest - For refreshing JWT token
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenRefreshRequest {
    private String refreshToken;
}

/**
 * ChangePasswordRequest - For changing password
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangePasswordRequest {
    private String oldPassword;
    private String newPassword;
    private String confirmPassword;
}

/**
 * ForgotPasswordRequest - For password reset
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ForgotPasswordRequest {
    private String email;
}

/**
 * ResetPasswordRequest - For resetting password with token
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResetPasswordRequest {
    private String token;
    private String newPassword;
    private String confirmPassword;
}
