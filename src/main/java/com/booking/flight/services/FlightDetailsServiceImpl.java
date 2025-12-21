package com.booking.flight.services;

import com.booking.flight.dto.ScheduleSearchRequest;
import com.booking.flight.dto.response.FlightResponse;
import com.booking.flight.dto.response.PlaneResponse;
import com.booking.flight.dto.response.ScheduleResponse;
import com.booking.flight.models.Flight;
import com.booking.flight.models.Plane;
import com.booking.flight.models.Schedule;
import com.booking.flight.repository.FlightRepository;
import com.booking.flight.repository.PlaneRepository;
import com.booking.flight.repository.ScheduleRepository;
import com.booking.flight.util.EntityToDtoConverter; // We will use a dedicated converter class

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true) // Set all methods to read-only for performance
public class FlightDetailsServiceImpl implements IFlightService {
    private final ScheduleRepository scheduleRepository;
    private final FlightRepository flightRepository;
    private final PlaneRepository planeRepository;
    private final EntityToDtoConverter converter; // Inject the centralized converter

    // ========================================================
    // ENDPOINT 1: SEARCH SCHEDULES
    // ========================================================
    @Override
    public List<ScheduleResponse> searchSchedules(ScheduleSearchRequest request) {
        log.info("Searching schedules from {} to {} on {}",
                request.origin(), request.destination(), request.departureDate());

        // NOTE: In a real application, you'd use a custom repository method (e.g., JpaRepository)
        // to perform a single query joining Schedule, Flight, and Plane, filtered by
        // departureDate, origin, and destination.

        // For this example, we assume the repository implements a finder method.
        List<Schedule> matchingSchedules = scheduleRepository.findAvailableSchedules(
                request.origin(),
                request.destination(),
                request.departureDate()
        );

        return matchingSchedules.stream()
                .map(converter::toScheduleResponse)
                .collect(Collectors.toList());
    }

    // ========================================================
    // ENDPOINT 2: GET SCHEDULE BY ID
    // ========================================================
    @Override
    public ScheduleResponse getScheduleById(Long scheduleId) {
        log.debug("Fetching schedule by ID: {}", scheduleId);

        // Ensure you use a repository method that fetches nested entities (Flight & Plane)
        // to avoid LazyInitializationException when calling the converter.
        Schedule schedule = scheduleRepository.findByIdWithFlightDetails(scheduleId) // Assume this custom method exists
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found with ID: " + scheduleId));

        return converter.toScheduleResponse(schedule);
    }

    // ========================================================
    // ENDPOINT 3: GET ALL FLIGHT ROUTES
    // ========================================================
    @Override
    public List<FlightResponse> getAllFlights() {
        log.debug("Fetching all flight routes.");
        // Fetch all Flights, ensuring nested Planes are loaded.
        List<Flight> flights = flightRepository.findAllWithPlaneDetails(); // Assume this method exists

        return flights.stream()
                .map(converter::toFlightResponse)
                .collect(Collectors.toList());
    }

    // ========================================================
    // ENDPOINT 4: GET PLANE BY ID
    // ========================================================
    @Override
    public PlaneResponse getPlaneById(Long planeId) {
        log.debug("Fetching plane by ID: {}", planeId);
        Plane plane = planeRepository.findById(planeId)
                .orElseThrow(() -> new IllegalArgumentException("Plane not found with ID: " + planeId));

        return converter.toPlaneResponse(plane);
    }
}