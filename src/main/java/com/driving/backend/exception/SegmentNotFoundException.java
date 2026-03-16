package com.driving.backend.exception;

/**
 * Defines custom exceptions or centralized exception handling behavior.
 */
public class SegmentNotFoundException extends RuntimeException {
    public SegmentNotFoundException(String message) {
        super(message);
    }
}

