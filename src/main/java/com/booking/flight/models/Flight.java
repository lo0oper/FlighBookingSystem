package com.booking.flight.models;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}