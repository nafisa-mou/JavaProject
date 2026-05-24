package com.bloodlink.exception;

/**
 * Custom Exception Classes
 * Demonstrates exception handling and proper error management
 */

/**
 * ResourceNotFoundException - Thrown when resource is not found
 */
public class ResourceNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public static ResourceNotFoundException userNotFound(Long userId) {
        return new ResourceNotFoundException("User with ID " + userId + " not found");
    }

    public static ResourceNotFoundException donorNotFound(Long donorId) {
        return new ResourceNotFoundException("Donor with ID " + donorId + " not found");
    }

    public static ResourceNotFoundException patientNotFound(Long patientId) {
        return new ResourceNotFoundException("Patient with ID " + patientId + " not found");
    }

    public static ResourceNotFoundException requestNotFound(Long requestId) {
        return new ResourceNotFoundException("Blood request with ID " + requestId + " not found");
    }

    public static ResourceNotFoundException chatNotFound(Long chatId) {
        return new ResourceNotFoundException("Chat with ID " + chatId + " not found");
    }
}

/**
 * DuplicateResourceException - Thrown when duplicate resource creation attempted
 */
class DuplicateResourceException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public DuplicateResourceException(String message) {
        super(message);
    }

    public DuplicateResourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public static DuplicateResourceException emailAlreadyExists(String email) {
        return new DuplicateResourceException("Email " + email + " is already registered");
    }

    public static DuplicateResourceException phoneNumberAlreadyExists(String phone) {
        return new DuplicateResourceException("Phone number " + phone + " is already registered");
    }
}

/**
 * InvalidOperationException - Thrown when invalid operation is attempted
 */
class InvalidOperationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public InvalidOperationException(String message) {
        super(message);
    }

    public InvalidOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}

/**
 * UnauthorizedException - Thrown when user is not authorized
 */
class UnauthorizedException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}

/**
 * ValidationException - Thrown when validation fails
 */
class ValidationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
