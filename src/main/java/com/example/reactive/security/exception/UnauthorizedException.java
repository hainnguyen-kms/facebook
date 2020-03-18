package com.example.reactive.security.exception;

/**
 * @author duc-d
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
