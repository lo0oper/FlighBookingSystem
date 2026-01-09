package com.booking.flight.controller;


import com.booking.flight.dto.ScheduleSearchRequest; // <-- DTO for search parameters
import com.booking.flight.dto.response.FlightResponse;
import com.booking.flight.dto.response.PlaneResponse;
import com.booking.flight.dto.response.ScheduleResponse;
import com.booking.flight.dto.response.SeatMapResponse;
import com.booking.flight.services.FlightDetailsServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

        import java.util.List;

@RestController
@RequestMapping("/api/v1/flights") // Base path for public flight information
@RequiredArgsConstructor
@Tag(name = "Public Flight View", description = "Endpoints for searching and viewing schedules, flights, and planes.")
public class ViewOnlyFlightDetailsController {

    private final FlightDetailsServiceImpl flightService; // The read-only service

    // ========================================================
    // ENDPOINT 1: SEARCH SCHEDULES (Most common query)
    // POST /api/v1/flights/search
    // ========================================================
    @PostMapping("/search")
    @Operation(summary = "Search available flight schedules",
            description = "Finds all available flight schedules based on origin, destination, and departure date.")
    public ResponseEntity<List<ScheduleResponse>> searchSchedules(
            @Valid @RequestBody ScheduleSearchRequest request) {

        List<ScheduleResponse> schedules = flightService.searchSchedules(request);
        return ResponseEntity.ok(schedules);
    }

    // ========================================================
    // ENDPOINT 2: GET SCHEDULE BY ID
    // GET /api/v1/flights/schedules/{scheduleId}
    // ========================================================
    @GetMapping("/schedules/{scheduleId}")
    @Operation(summary = "Get a specific flight schedule",
            description = "Retrieves details of a single schedule by its unique ID.")
    public ResponseEntity<ScheduleResponse> getScheduleById(@PathVariable Long scheduleId) {
        ScheduleResponse schedule = flightService.getScheduleById(scheduleId);
        return ResponseEntity.ok(schedule);
    }

    // ========================================================
    // ENDPOINT 3: GET ALL FLIGHT ROUTES
    // GET /api/v1/flights/routes
    // ========================================================
    @GetMapping("/routes")
    @Operation(summary = "Get all defined Flight Routes",
            description = "Retrieves a list of all permanent flight routes (e.g., DEL->BOM) with their assigned plane model.")
    public ResponseEntity<List<FlightResponse>> getAllFlightRoutes() {
        List<FlightResponse> routes = flightService.getAllFlights();
        return ResponseEntity.ok(routes);
    }

    // ========================================================
    // ENDPOINT 4: GET PLANE BY ID
    // GET /api/v1/flights/planes/{planeId}
    // ========================================================
    @GetMapping("/planes/{planeId}")
    @Operation(summary = "Get Plane details by ID",
            description = "Retrieves the capacity and model of a specific aircraft type.")
    public ResponseEntity<PlaneResponse> getPlaneById(@PathVariable Long planeId) {
        PlaneResponse plane = flightService.getPlaneById(planeId);
        return ResponseEntity.ok(plane);
    }


    @GetMapping("/schedules/{scheduleId}/seats")
    public ResponseEntity<SeatMapResponse> getSeats(@PathVariable Long scheduleId) {
        try {
            SeatMapResponse seatMap = flightService.getScheduleSeats(scheduleId);
            return ResponseEntity.ok(seatMap);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}