// src/main/java/com/booking/flight/repository/ScheduleRepository.java
package com.booking.flight.repository;

import com.booking.flight.models.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    /**
     * Finds available schedules based on origin, destination, and date.
     * Uses JOIN FETCH to eagerly load the nested Flight and Plane entities
     * in a single query to prevent LazyInitializationException during DTO conversion.
     * * @param origin The departure airport code.
     * @param destination The arrival airport code.
     * @param departureDate The desired date of departure.
     * @return A list of matching Schedule entities with Flight and Plane eagerly loaded.
     */
    @Query("SELECT s FROM Schedule s " +
            // Eagerly fetch Flight associated with the Schedule
            "JOIN FETCH s.flight f " +
            // Eagerly fetch Plane associated with the Flight
            "JOIN FETCH f.plane p " +
            "WHERE f.departureAirport = :origin " +
            "AND f.arrivalAirport = :destination " +
            // Cast the schedule's departureTime (e.g., LocalDateTime) to date for comparison
            "AND CAST(s.departureTime AS date) = :departureDate " +
            "AND s.status = 'SCHEDULED'")
    List<Schedule> findAvailableSchedules(
            @Param("origin") String origin,
            @Param("destination") String destination,
            @Param("departureDate") LocalDate departureDate
    );

    /**
     * Retrieves a single schedule by ID, eagerly fetching all necessary nested details.
     * * @param id The unique ID of the schedule.
     * @return An Optional containing the Schedule entity with Flight and Plane eagerly loaded.
     */
    @Query("SELECT s FROM Schedule s " +
            "JOIN FETCH s.flight f " +
            "JOIN FETCH f.plane p " +
            "WHERE s.scheduleId = :id")
    Optional<Schedule> findByIdWithFlightDetails(@Param("id") Long id);
}