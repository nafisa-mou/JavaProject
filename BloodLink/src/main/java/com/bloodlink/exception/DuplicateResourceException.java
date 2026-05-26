package com.bloodlink.exception;

public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) { super(message); }
    public DuplicateResourceException(String message, Throwable cause) { super(message, cause); }
    public static DuplicateResourceException emailAlreadyExists(String email) { return new DuplicateResourceException("Email " + email + " is already registered"); }
    public static DuplicateResourceException phoneNumberAlreadyExists(String phone) { return new DuplicateResourceException("Phone number " + phone + " is already registered"); }
}
