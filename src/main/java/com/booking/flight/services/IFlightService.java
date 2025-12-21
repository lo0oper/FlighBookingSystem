package com.booking.flight.services;


import com.booking.flight.dto.ScheduleSearchRequest;
import com.booking.flight.dto.response.FlightResponse;
import com.booking.flight.dto.response.PlaneResponse;
import com.booking.flight.dto.response.ScheduleResponse;
import java.util.List;


public interface IFlightService {

    List<ScheduleResponse> searchSchedules(ScheduleSearchRequest request);

    ScheduleResponse getScheduleById(Long scheduleId);

    List<FlightResponse> getAllFlights();

    PlaneResponse getPlaneById(Long planeId);
}
