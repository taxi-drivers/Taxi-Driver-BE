package com.driving.backend.exception;

/**
 * Defines custom exceptions or centralized exception handling behavior.
 */
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}

