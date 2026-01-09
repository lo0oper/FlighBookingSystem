package com.booking.flight.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // Maps this to HTTP 500
public class CapacityExceededException extends RuntimeException {

    public CapacityExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}
