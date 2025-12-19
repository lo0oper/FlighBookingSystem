package com.booking.flight.services;

import com.booking.flight.dto.FlightCreationRequest;
import com.booking.flight.dto.PlaneCreationRequest;
import com.booking.flight.dto.ScheduleCreationRequest;
import com.booking.flight.models.*;
import com.booking.flight.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FlightManagementService {

    private final FlightRepository flightRepository;
    private final ScheduleRepository scheduleRepository;
    private final PlaneRepository planeRepository;
    private final BookingRepository bookingRepository;

    // ===============================================
    // NEW FUNCTION 1: CREATE SCHEDULE
    // ===============================================
    @Transactional
    public Schedule createSchedule(ScheduleCreationRequest request) {
        // Find the base flight route (e.g., UA123)
        log.info("Creating schedule for Flight ID {} with price {}", request.flightId(), request.basePrice());
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

        Schedule savedSchedule = scheduleRepository.save(schedule);
        log.debug("Schedule created successfully. ID: {}", savedSchedule.getScheduleId());
        return savedSchedule;

    }

    // ===============================================
    // NEW FUNCTION 2: REASSIGN PLANE TO FLIGHT ROUTE
    // ===============================================
    @Transactional
    public Flight reassignPlaneToFlightRoute(Long flightId, Long newPlaneId) {
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
        // This change will apply to all future schedules created for this route.
        flight.setPlane(newPlane);

        Flight updatedFlight = flightRepository.save(flight);
        log.info("Flight {} successfully reassigned to plane model {}", flight.getFlightNumber(), newPlane.getModel());
        // Save the updated Flight entity
        return updatedFlight;
    }

    // --- Other Admin CRUD methods (included for completeness) ---

    @Transactional
    public Flight createFlight(FlightCreationRequest request) {
        log.info("Creating schedule for Flight ID {} with planeId {}", request.flightNumber(), request.planeId());

        Plane plane = planeRepository.findById(request.planeId())
                .orElseThrow(() -> new IllegalArgumentException("Plane not found with ID: " + request.planeId()));

        Flight flight = new Flight();
        flight.setFlightNumber(request.flightNumber());
        flight.setDepartureAirport(request.departureAirport());
        flight.setArrivalAirport(request.arrivalAirport());
        flight.setPlane(plane);


        return flightRepository.save(flight);
    }

    public List<Flight> getAllFlights() {
        return flightRepository.findAll();
    }

    @Transactional
    public void deleteFlight(Long flightId) {
        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new IllegalArgumentException("Flight not found"));

        boolean hasFutureBookings = flight.getSchedules().stream()
                .anyMatch(schedule -> schedule.getDepartureTime().isAfter(java.time.LocalDateTime.now())
                        && bookingRepository.existsBySchedule(schedule));

        if (hasFutureBookings) {
            throw new IllegalStateException("Cannot delete flight: It has future scheduled bookings.");
        }

        flightRepository.delete(flight);
    }

    @Transactional
    public Plane createPlane(PlaneCreationRequest request) {
        log.info("Attempting to create new plane model: {}", request.model());
        Plane plane = new Plane();
        plane.setModel(request.model());
        plane.setTotalSeats(request.totalSeats());
        Plane savedPlane = planeRepository.save(plane);
        log.debug("Plane created successfully with ID: {}", savedPlane.getPlaneId());
        return savedPlane;
    }
}