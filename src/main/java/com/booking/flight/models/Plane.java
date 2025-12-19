package com.booking.flight.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Plane {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long planeId;

        @Column(nullable = false, unique = true)
        private String model;

        @Column(nullable = false)
        private Integer totalSeats; // Renamed 'capacity' for clarity
}

