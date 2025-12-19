package com.booking.flight.controller;

import com.booking.flight.dto.PlaneCreationRequest; // <-- NEW IMPORT
import com.booking.flight.dto.FlightCreationRequest; // <-- REQUIRED DTO FOR FLIGHT CREATION
import com.booking.flight.dto.ScheduleCreationRequest;
import com.booking.flight.models.Plane; // <-- NEW IMPORT
import com.booking.flight.models.Flight;
import com.booking.flight.models.Schedule;
import com.booking.flight.services.FlightManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
// NOTE: Use the plural 'flights' or 'management' for the base path
@RequestMapping("/api/v1/admin/management")
@RequiredArgsConstructor
public class AdminFlightController {


    private final FlightManagementService managementService;

    // ========================================================
    // ENDPOINT 1: CREATE PLANES (New Requirement)
    // POST /api/v1/admin/management/planes
    // ========================================================
    @PostMapping("/planes")
    public ResponseEntity<Plane> createPlane(@Valid @RequestBody PlaneCreationRequest request) {
        Plane plane = managementService.createPlane(request);
        return new ResponseEntity<>(plane, HttpStatus.CREATED);
    }

    // ========================================================
    // ENDPOINT 2: CREATE FLIGHT ROUTE
    // POST /api/v1/admin/management/flights
    // ========================================================
    // This allows the admin to set up a permanent route (e.g., DEL->BOM)
    @PostMapping("/flights")
    public ResponseEntity<Flight> createFlight(@Valid @RequestBody FlightCreationRequest request) {
        Flight flight = managementService.createFlight(request);
        return new ResponseEntity<>(flight, HttpStatus.CREATED);
    }


    // ========================================================
    // ENDPOINT 3: CREATE SCHEDULES
    // POST /api/v1/admin/management/schedules
    // ========================================================
    @PostMapping("/schedules")
    public ResponseEntity<Schedule> createSchedule(@Valid @RequestBody ScheduleCreationRequest request) {
        Schedule schedule = managementService.createSchedule(request);
        return new ResponseEntity<>(schedule, HttpStatus.CREATED);
    }

    // ========================================================
    // ENDPOINT 4: MATCH/REASSIGN PLANES TO FLIGHT ROUTE
    // PUT /api/v1/admin/management/flights/{flightId}/plane/{newPlaneId}
    // ========================================================
    /**
     * API to match the planes to the flight route (which applies to all future schedules).
     */
    @PutMapping("/flights/{flightId}/plane/{newPlaneId}")
    public ResponseEntity<Flight> reassignPlane(
            @PathVariable Long flightId,
            @PathVariable Long newPlaneId) {

        Flight updatedFlight = managementService.reassignPlaneToFlightRoute(flightId, newPlaneId);
        return ResponseEntity.ok(updatedFlight);
    }
}