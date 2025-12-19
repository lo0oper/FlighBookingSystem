package com.booking.flight.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;


@Builder
public record BookingRequest(
        @NotNull Long scheduleId,
        @NotNull Long userId,
        @NotBlank @Pattern(regexp = "^[0-9]{1,2}[A-F]$") String seatNumber
) {}