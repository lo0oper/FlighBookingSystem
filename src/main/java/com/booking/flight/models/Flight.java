package com.booking.flight.models;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Flight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long flightId;

    @Column(nullable = false, unique = true)
    private String flightNumber; // e.g., "UA123"

    @Column(nullable = false)
    private String departureAirport; // IATA code

    @Column(nullable = false)
    private String arrivalAirport; // IATA code

    // A flight route typically has a plane associated with it.
    //TODO: What is meant by LAZY here?
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "planeId", nullable = false)
    private Plane plane;


    // ==========================================================
    // **CRITICAL FIX: Define the Schedules collection field**
    // This allows Hibernate to track the relationship and Lombok
    // to generate the working getSchedules() method.
    // ==========================================================
    @OneToMany(mappedBy = "flight", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Schedule> schedules = new HashSet<>();

    // If you are using @AllArgsConstructor, you need a custom constructor
    // that omits the 'schedules' field for DTO mapping convenience:
    public Flight(Long flightId, String flightNumber, String departureAirport,
                  String arrivalAirport, Plane plane) {
        this.flightId = flightId;
        this.flightNumber = flightNumber;
        this.departureAirport = departureAirport;
        this.arrivalAirport = arrivalAirport;
        this.plane = plane;
    }

}