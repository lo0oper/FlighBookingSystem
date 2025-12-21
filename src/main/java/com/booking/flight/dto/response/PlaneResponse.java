package com.booking.flight.dto.response;

public record PlaneResponse(
        Long id,
        String model,
        int totalSeats
) {}