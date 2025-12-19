package com.booking.flight.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ScheduleCreationRequest(
        @NotNull Long flightId, // ID of the pre-defined flight route (e.g., UA123)
        @NotNull @Future LocalDateTime departureTime,
        @NotNull @Future LocalDateTime arrivalTime,
        @NotNull @DecimalMin("0.01") BigDecimal basePrice
) {}