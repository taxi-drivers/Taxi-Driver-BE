package com.driving.backend.exception;

/**
 * Defines custom exceptions or centralized exception handling behavior.
 */
public class SegmentDetailNotFoundException extends RuntimeException {
    public SegmentDetailNotFoundException(String message) {
        super(message);
    }
}

