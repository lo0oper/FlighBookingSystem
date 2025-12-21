package com.booking.flight.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.FutureOrPresent;
import java.time.LocalDate;

public record ScheduleSearchRequest(
        @NotBlank(message = "Origin airport code is required.")
        String origin,

        @NotBlank(message = "Destination airport code is required.")
        String destination,

        @NotNull(message = "Departure date is required.")
        @FutureOrPresent(message = "Departure date must be today or in the future.")
        LocalDate departureDate

        // Optional: Add other filters like cabin class, max price, etc.
) { }