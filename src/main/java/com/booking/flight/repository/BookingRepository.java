package com.booking.flight.repository;

import com.booking.flight.models.Booking;
import com.booking.flight.models.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    /**
     * Custom Derived Query Method:
     * Checks if any booking exists for a given schedule entity.
     * This is crucial for preventing the deletion of a Flight route/schedule
     * if customers have already booked seats.
     */
    boolean existsBySchedule(Schedule schedule);
}