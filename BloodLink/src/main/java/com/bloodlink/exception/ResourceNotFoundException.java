package com.bloodlink.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) { super(message); }
    public ResourceNotFoundException(String message, Throwable cause) { super(message, cause); }
    public static ResourceNotFoundException userNotFound(Long userId) { return new ResourceNotFoundException("User with ID " + userId + " not found"); }
    public static ResourceNotFoundException donorNotFound(Long donorId) { return new ResourceNotFoundException("Donor with ID " + donorId + " not found"); }
    public static ResourceNotFoundException patientNotFound(Long patientId) { return new ResourceNotFoundException("Patient with ID " + patientId + " not found"); }
    public static ResourceNotFoundException requestNotFound(Long requestId) { return new ResourceNotFoundException("Blood request with ID " + requestId + " not found"); }
    public static ResourceNotFoundException chatNotFound(Long chatId) { return new ResourceNotFoundException("Chat with ID " + chatId + " not found"); }
}
