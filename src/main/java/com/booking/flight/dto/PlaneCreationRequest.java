package com.booking.flight.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for creating a new Plane model.
 */
public record PlaneCreationRequest(

        @NotBlank(message = "Model name is required")
        String model, // e.g., "Boeing 737-800"

        @NotNull(message = "Total seats must be specified")
        @Min(value = 10, message = "Plane must have at least 10 seats")
        Integer totalSeats // e.g., 189
) {}