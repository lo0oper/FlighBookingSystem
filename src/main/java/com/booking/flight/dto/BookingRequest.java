package com.booking.flight.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record BookingRequest(

        @NotNull(message = "Schedule ID is required")
        Long scheduleId,

        // Changed from a single String to a List of Strings
        @NotEmpty(message = "At least one seat number is required")
        List<String> seatNumbers,

        @NotNull(message = "User ID is required for the booking")
        Long userId
) {}