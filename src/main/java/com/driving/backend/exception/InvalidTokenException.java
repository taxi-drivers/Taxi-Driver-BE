package com.driving.backend.exception;

/**
 * Defines custom exceptions or centralized exception handling behavior.
 */
public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String message) {
        super(message);
    }
}

