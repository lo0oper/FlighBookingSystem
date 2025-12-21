package com.booking.flight.dto.response;

import com.booking.flight.models.Booking;
import java.time.LocalDateTime;

public record BookingResponse(
        Long id,
        Long scheduleId,
        Long userId,
        String seatNumber,
        String status,
        LocalDateTime bookingTime
) {
    // FIX: Make the conversion method public and static
    public static BookingResponse fromEntity(Booking booking) {
        if (booking == null) {
            return null;
        }
        return new BookingResponse(
                // Note: Ensure your Booking entity uses getBookingId()
                booking.getBookingId(),
                booking.getSchedule().getScheduleId(),
                booking.getUserId(),
                booking.getSeatNumber(),
                booking.getStatus(),
                booking.getBookingTime()
        );
    }
}