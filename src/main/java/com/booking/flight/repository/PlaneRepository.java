// src/main/java/com/booking/flight/repository/PlaneRepository.java
package com.booking.flight.repository;

import com.booking.flight.models.Plane;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaneRepository extends JpaRepository<Plane, Long> {}