// src/main/java/com/booking/flight/repository/ScheduleRepository.java
package com.booking.flight.repository;

import com.booking.flight.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {}