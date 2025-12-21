package com.booking.flight.controller;

import com.booking.flight.dto.PlaneCreationRequest; // <-- NEW IMPORT
import com.booking.flight.dto.FlightCreationRequest; // <-- REQUIRED DTO FOR FLIGHT CREATION
import com.booking.flight.dto.ScheduleCreationRequest;
import com.booking.flight.dto.response.FlightResponse;
import com.booking.flight.dto.response.PlaneResponse;
import com.booking.flight.dto.response.ScheduleResponse;
import com.booking.flight.models.Plane; // <-- NEW IMPORT
import com.booking.flight.models.Flight;
import com.booking.flight.models.Schedule;
import com.booking.flight.services.FlightManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

@RestController
// NOTE: Use the plural 'flights' or 'management' for the base path
@RequestMapping("/api/v1/admin/management")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Flight Management", description = "Endpoints for creating planes, flights, schedules, and managing routes.")
public class AdminFlightController {


    private final FlightManagementService managementService;

    // ========================================================
    // ENDPOINT 1: CREATE PLANES (New Requirement)
    // POST /api/v1/admin/management/planes
    // ========================================================
    @PostMapping("/planes")
    @Operation(summary = "Create a new Plane model", description = "Defines a new aircraft type with its total seat capacity.")
    public ResponseEntity<PlaneResponse> createPlane(@Valid @RequestBody PlaneCreationRequest request) {
        PlaneResponse planeResponse = managementService.createPlane(request);
        return new ResponseEntity<>(planeResponse, HttpStatus.CREATED);
    }

    // ========================================================
    // ENDPOINT 2: CREATE FLIGHT ROUTE
    // POST /api/v1/admin/management/flights
    // ========================================================
    // This allows the admin to set up a permanent route (e.g., DEL->BOM)
    @PostMapping("/flights")
    @Operation(summary = "Reassign Plane Model to Flight Route",
               description = "Updates the aircraft model used for all future schedules of a permanent flight route.")
    public ResponseEntity<FlightResponse> createFlight(@Valid @RequestBody FlightCreationRequest request) {
        FlightResponse flightResponse = managementService.createFlight(request);
        return new ResponseEntity<>(flightResponse, HttpStatus.CREATED);
    }


    // ========================================================
    // ENDPOINT 3: CREATE SCHEDULES
    // POST /api/v1/admin/management/schedules
    // ========================================================
    @PostMapping("/schedules")
    public ResponseEntity<ScheduleResponse> createSchedule(@RequestBody ScheduleCreationRequest request) {        ScheduleResponse schedule = managementService.createSchedule(request);
        ScheduleResponse response = managementService.createSchedule(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // ========================================================
    // ENDPOINT 4: MATCH/REASSIGN PLANES TO FLIGHT ROUTE
    // PUT /api/v1/admin/management/flights/{flightId}/plane/{newPlaneId}
    // ========================================================
    /**
     * API to match the planes to the flight route (which applies to all future schedules).
     */
    @PutMapping("/flights/{flightId}/plane/{newPlaneId}")
    public ResponseEntity<FlightResponse> reassignPlane(
            @PathVariable Long flightId,
            @PathVariable Long newPlaneId) {

        FlightResponse updatedFlight = managementService.reassignPlaneToFlightRoute(flightId, newPlaneId);
        return ResponseEntity.ok(updatedFlight);
    }
}