package com.booking.flight.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

public record ScheduleResponse(
        Long id,
        FlightResponse flight, // Nested Flight details
        LocalDateTime departureTime,
        LocalDateTime arrivalTime,
        BigDecimal basePrice,
        String status,
        Map<String, String> seatStatuses // Key: Seat Number, Value: Status
) {}