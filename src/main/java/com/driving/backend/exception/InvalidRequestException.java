package com.driving.backend.exception;

/**
 * Defines custom exceptions or centralized exception handling behavior.
 */
public class InvalidRequestException extends RuntimeException {
    public InvalidRequestException(String message) {
        super(message);
    }
}

