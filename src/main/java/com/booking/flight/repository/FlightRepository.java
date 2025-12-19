// src/main/java/com/booking/flight/repository/FlightRepository.java
package com.booking.flight.repository;

import com.booking.flight.models.Flight;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlightRepository extends JpaRepository<Flight, Long> {}