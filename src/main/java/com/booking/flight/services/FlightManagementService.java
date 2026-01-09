package com.booking.flight.services;

// DTO Imports
import com.booking.flight.dto.FlightCreationRequest;
import com.booking.flight.dto.PlaneCreationRequest;
import com.booking.flight.dto.ScheduleCreationRequest;
import com.booking.flight.dto.response.PlaneResponse;
import com.booking.flight.dto.response.FlightResponse;
import com.booking.flight.dto.response.ScheduleResponse;
import com.booking.flight.util.EntityToDtoConverter;


// Entity Imports
import com.booking.flight.models.*;
import com.booking.flight.repository.*;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors; // Added for stream operations
import java.util.stream.IntStream;

@Service
@Slf4j
@RequiredArgsConstructor
public class FlightManagementService {

    private final FlightRepository flightRepository;
    private final ScheduleRepository scheduleRepository;
    private final PlaneRepository planeRepository;
    private final BookingRepository bookingRepository;
    private final EntityToDtoConverter converter;



    // ===============================================
    // NEW FUNCTION 1: CREATE SCHEDULE
    // RETURN TYPE CHANGED TO ScheduleResponse
    // ===============================================
    @Transactional
    public ScheduleResponse createSchedule(ScheduleCreationRequest request) {
        // Find the base flight route (e.g., UA123)
        log.info("Creating schedule for Flight ID {} with price {}", request.flightId(), request.basePrice());

        // Ensure Flight (and its nested Plane) are loaded eagerly or accessed here
        Flight flight = flightRepository.findById(request.flightId())
                .orElseThrow(() -> {
                    log.warn("Failed to find Flight route with ID: {}", request.flightId());
                    return new IllegalArgumentException("Flight route not found with ID: " + request.flightId());
                });

        // Create the specific instance (schedule)
        Schedule schedule = new Schedule();
        schedule.setFlight(flight);
        schedule.setDepartureTime(request.departureTime());
        schedule.setArrivalTime(request.arrivalTime());
        schedule.setBasePrice(request.basePrice());
        // Note: Assuming a default status like "ACTIVE" or "SCHEDULED"
        schedule.setStatus("SCHEDULED");

        schedule.setSeatStatuses(initializeSeatStatuses(flight.getPlane()));
        log.debug("Initialized {} seats to 'AVAILABLE' for the new schedule.", flight.getPlane().getTotalSeats());

        Schedule savedSchedule = scheduleRepository.save(schedule);
        log.debug("Schedule created successfully. ID: {}", savedSchedule.getScheduleId());

        // CONVERSION STEP
        return converter.toScheduleResponse(savedSchedule);

    }

    // ===============================================
    // NEW FUNCTION 2: REASSIGN PLANE TO FLIGHT ROUTE
    // RETURN TYPE CHANGED TO FlightResponse
    // ===============================================
    @Transactional
    public FlightResponse reassignPlaneToFlightRoute(Long flightId, Long newPlaneId) {
        // Find the Flight Route entity
        log.info("Reassigning Plane for Flight ID {} to new Plane ID {}", flightId, newPlaneId);

        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> {
                    log.error("Flight route not found for reassignment: {}", flightId);
                    return new IllegalArgumentException("Flight route not found with ID: " + flightId);
                });

        Plane newPlane = planeRepository.findById(newPlaneId)
                .orElseThrow(() -> {
                    log.error("New plane model not found: {}", newPlaneId);
                    return new IllegalArgumentException("Plane not found with ID: " + newPlaneId);
                });

        // Update the Plane associated with the permanent Flight Route
        flight.setPlane(newPlane);

        Flight updatedFlight = flightRepository.save(flight);
        log.info("Flight {} successfully reassigned to plane model {}", flight.getFlightNumber(), newPlane.getModel());

        // CONVERSION STEP
        return converter.toFlightResponse(updatedFlight);
    }

    // --- Other Admin CRUD methods (updated return types) ---

    @Transactional
    public FlightResponse createFlight(FlightCreationRequest request) {
        log.info("Creating flight route {} with planeId {}", request.flightNumber(), request.planeId());

        Plane plane = planeRepository.findById(request.planeId())
                .orElseThrow(() -> new IllegalArgumentException("Plane not found with ID: " + request.planeId()));

        Flight flight = new Flight();
        flight.setFlightNumber(request.flightNumber());
        flight.setDepartureAirport(request.departureAirport());
        flight.setArrivalAirport(request.arrivalAirport());
        flight.setPlane(plane);


        Flight savedFlight = flightRepository.save(flight);

        // CONVERSION STEP
        return converter.toFlightResponse(savedFlight);
    }

    public List<FlightResponse> getAllFlights() {
        // CONVERSION STEP
        return flightRepository.findAll().stream()
                .map(converter::toFlightResponse)
                .collect(Collectors.toList());
    }

    // ... (deleteFlight method remains the same as it returns void) ...

    @Transactional
    public PlaneResponse createPlane(PlaneCreationRequest request) {
        log.info("Attempting to create new plane model: {}", request.model());
        Plane plane = new Plane();
        plane.setModel(request.model());
        plane.setTotalSeats(request.totalSeats());
        Plane savedPlane = planeRepository.save(plane);
        log.debug("Plane created successfully with ID: {}", savedPlane.getPlaneId());

        // CONVERSION STEP
        return converter.toPlaneResponse(savedPlane);
    }

    /**
     * Generates the initial map of seat statuses for a new schedule.
     * NOTE: This is a simplified seat generation (e.g., sequential numbering).
     * Real systems need a complex seat map utility.
     */
    private Map<String, String> initializeSeatStatuses(Plane plane) {
        int totalSeats = plane.getTotalSeats();


        // For simplicity, we generate sequential names like "001", "002", etc., up to totalSeats.
        return IntStream.rangeClosed(1, totalSeats)
                .mapToObj(i -> String.format("%03d", i)) // Pad to three digits (e.g., "001")
                .collect(Collectors.toMap(
                        seatNumber -> seatNumber, // Key is the seat number
                        seatNumber -> "AVAILABLE" // Value is the initial status
                ));
    }

}