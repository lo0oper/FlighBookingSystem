package com.booking.flight.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE) // Maps this to HTTP 503
public class AerospikeLockFailureException extends RuntimeException {

    public AerospikeLockFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}