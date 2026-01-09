package com.booking.flight.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long scheduleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flightId", nullable = false)
    private Flight flight;

    @Column(nullable = false)
    private LocalDateTime departureTime;

    @Column(nullable = false)
    private LocalDateTime arrivalTime;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    // Added to align with scheduling logic and DTO structure
    @Column(nullable = false, length = 20)
    private String status;

    // Calculated field, potentially stored in Aerospike for speed
    @Transient
    private Integer availableSeats;

    // Map to track seat status: SeatNumber -> Status (e.g., "A01" -> "AVAILABLE")
    @ElementCollection // For JPA to handle storing a collection/map
    @CollectionTable(name = "schedule_seat_status", // Creates a join table
            joinColumns = @JoinColumn(name = "scheduleId"))
    @MapKeyColumn(name = "seatNumber")
    @Column(name = "status")
    private Map<String, String> seatStatuses;
}