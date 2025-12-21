package com.booking.flight.dto.response;

public record FlightResponse(
        Long id,
        String flightNumber,
        PlaneResponse plane // Nested Plane details
) {}