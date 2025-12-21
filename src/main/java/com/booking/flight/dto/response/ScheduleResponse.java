package com.booking.flight.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ScheduleResponse(
        Long id,
        FlightResponse flight, // Nested Flight details
        LocalDateTime departureTime,
        LocalDateTime arrivalTime,
        BigDecimal basePrice,
        String status
) {}