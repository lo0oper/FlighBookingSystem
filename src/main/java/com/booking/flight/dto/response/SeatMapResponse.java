package com.booking.flight.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class SeatMapResponse {
        private Long scheduleId;
        private Map<String, String> seatStatuses; // Key: Seat Number (e.g., "001"), Value: Status (e.g., "AVAILABLE", "BOOKED")
}
