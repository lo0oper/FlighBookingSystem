package com.booking.flight.services;

import com.booking.flight.dto.FlightCreationRequest;
import com.booking.flight.dto.PlaneCreationRequest;
import com.booking.flight.dto.ScheduleCreationRequest;
import com.booking.flight.models.*;
import com.booking.flight.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
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
        Flight flight = flightRepository.findById(request.flightId())
                .orElseThrow(() -> new IllegalArgumentException("Flight route not found with ID: " + request.flightId()));

        // Create the specific instance (schedule)
        Schedule schedule = new Schedule();
        schedule.setFlight(flight);
        schedule.setDepartureTime(request.departureTime());
        schedule.setArrivalTime(request.arrivalTime());
        schedule.setBasePrice(request.basePrice());

        return scheduleRepository.save(schedule);
    }

    // ===============================================
    // NEW FUNCTION 2: REASSIGN PLANE TO FLIGHT ROUTE
    // ===============================================
    @Transactional
    public Flight reassignPlaneToFlightRoute(Long flightId, Long newPlaneId) {
        // Find the Flight Route entity
        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new IllegalArgumentException("Flight route not found with ID: " + flightId));

        // Find the new Plane entity
        Plane newPlane = planeRepository.findById(newPlaneId)
                .orElseThrow(() -> new IllegalArgumentException("Plane not found with ID: " + newPlaneId));

        // Update the Plane associated with the permanent Flight Route
        // This change will apply to all future schedules created for this route.
        flight.setPlane(newPlane);

        // Save the updated Flight entity
        return flightRepository.save(flight);
    }

    // --- Other Admin CRUD methods (included for completeness) ---

    @Transactional
    public Flight createFlight(FlightCreationRequest request) {
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
        Plane plane = new Plane();
        plane.setModel(request.model());
        plane.setTotalSeats(request.totalSeats());

        return planeRepository.save(plane);
    }
}