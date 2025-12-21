package com.booking.flight.controller;

import com.booking.flight.dto.BookingRequest;
import com.booking.flight.dto.response.BookingResponse;
import com.booking.flight.models.Booking;
import com.booking.flight.services.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<List<BookingResponse>> createBookings(@RequestBody BookingRequest request) {

        // Change the service call and return type
        List<BookingResponse> bookings = bookingService.createMultipleBookings(request);

        // Returns 201 CREATED with the list of newly created bookings
        return new ResponseEntity<>(bookings, HttpStatus.CREATED);
    }

    @GetMapping("/schedule/{scheduleId}/reserved")
    public ResponseEntity<List<String>> getReservedSeats(@PathVariable String scheduleId) {
        List<String> reservedSeats = bookingService.getReservedSeats(scheduleId);
        return ResponseEntity.ok(reservedSeats);
    }
}
