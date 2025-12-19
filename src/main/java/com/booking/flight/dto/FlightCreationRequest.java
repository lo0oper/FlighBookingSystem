package com.booking.flight.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for creating a new permanent Flight route.
 * A flight route links two airports and specifies the plane model used for the route.
 */
public record FlightCreationRequest(

        // The unique identifier for the flight (e.g., "UA123")
        @NotBlank(message = "Flight number is required")
        String flightNumber,

        // IATA code for the departure airport (e.g., "DEL")
        @NotBlank(message = "Departure airport is required")
        String departureAirport,

        // IATA code for the arrival airport (e.g., "BOM")
        @NotBlank(message = "Arrival airport is required")
        String arrivalAirport,

        // The ID of the Plane model (e.g., Boeing 737) associated with this route
        @NotNull(message = "Plane ID must be specified for the route")
        Long planeId
) {}