package com.booking.flight.controller;

import org.springframework.web.bind.annotation.RestController;
import com.booking.flight.dto.BookingRequest;
import com.booking.flight.exception.SeatAlreadyReservedException;
import com.booking.flight.models.Booking;
import com.booking.flight.services.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<Booking> createBooking(@Valid @RequestBody BookingRequest request) {
        try {
            Booking booking = bookingService.createBooking(request);
            return new ResponseEntity<>(booking, HttpStatus.CREATED);
        } catch (SeatAlreadyReservedException e) {
            // Production-ready: return 409 Conflict if the resource is unavailable
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }
    }
}
