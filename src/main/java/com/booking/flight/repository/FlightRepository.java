// src/main/java/com/booking/flight/repository/FlightRepository.java
package com.booking.flight.repository;

import com.booking.flight.models.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FlightRepository extends JpaRepository<Flight, Long> {

    /**
     * Retrieves all Flight entities, eagerly fetching the associated Plane entity.
     * This is used by the public FlightService to efficiently list all routes
     * and prevent LazyInitializationException when converting to FlightResponse DTOs.
     * * @return A list of all Flight entities with their Plane details eagerly loaded.
     */
    @Query("SELECT f FROM Flight f JOIN FETCH f.plane p")
    List<Flight> findAllWithPlaneDetails();
}