package com.booking.flight.util;

import com.booking.flight.dto.response.PlaneResponse;
import com.booking.flight.dto.response.FlightResponse;
import com.booking.flight.dto.response.ScheduleResponse;
import com.booking.flight.models.Plane;
import com.booking.flight.models.Flight;
import com.booking.flight.models.Schedule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EntityToDtoConverter {

    // --- Plane Conversion ---
    public PlaneResponse toPlaneResponse(Plane plane) {
        if (plane == null) return null;
        return new PlaneResponse(
                plane.getPlaneId(),
                plane.getModel(),
                plane.getTotalSeats()
        );
    }

    // --- Flight Conversion ---
    public FlightResponse toFlightResponse(Flight flight) {
        if (flight == null) return null;

        // CRUCIAL: Access the nested entity within the same (transactional) method call
        // to ensure the proxy is initialized.
        Plane plane = flight.getPlane();
        PlaneResponse planeDto = toPlaneResponse(plane);

        return new FlightResponse(
                flight.getFlightId(),
                flight.getFlightNumber(),
                planeDto
        );
    }

    // --- Schedule Conversion ---
    public ScheduleResponse toScheduleResponse(Schedule schedule) {
        if (schedule == null) return null;

        // CRUCIAL: Access the nested entity within the same (transactional) method call
        Flight flight = schedule.getFlight();
        FlightResponse flightDto = toFlightResponse(flight);

        return new ScheduleResponse(
                schedule.getScheduleId(),
                flightDto,
                schedule.getDepartureTime(),
                schedule.getArrivalTime(),
                schedule.getBasePrice(),
                schedule.getStatus(),
                schedule.getSeatStatuses()
        );
    }
}