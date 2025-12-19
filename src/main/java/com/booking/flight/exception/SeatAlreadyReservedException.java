package com.booking.flight.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception thrown when an attempt is made to reserve a seat
 * that is already locked (reserved) by another transaction via Aerospike.
 * * Maps directly to an HTTP 409 Conflict response.
 */
@ResponseStatus(HttpStatus.CONFLICT) // This annotation tells Spring to return HTTP 409
public class SeatAlreadyReservedException extends RuntimeException {

    public SeatAlreadyReservedException(String message) {
        super(message);
    }

    public SeatAlreadyReservedException(String message, Throwable cause) {
        super(message, cause);
    }
}
