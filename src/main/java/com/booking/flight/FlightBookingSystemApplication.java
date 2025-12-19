package com.booking.flight;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The main entry point for the Flight Booking System application.
 */
@SpringBootApplication
// Ensure Spring scans all necessary components (controllers, services, config)
// The base package is usually sufficient, but explicit scanning is robust.
public class FlightBookingSystemApplication {

    public static void main(String[] args) {
        // Run the Spring Boot application
        SpringApplication.run(FlightBookingSystemApplication.class, args);
        System.out.println("Flight Booking System is running!");
    }

}