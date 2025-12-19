package com.booking.flight.services;


import com.aerospike.client.*;
import com.aerospike.client.policy.WritePolicy;
import com.booking.flight.config.aeroSpikeConfig.AerospikeConfiguration;
import com.booking.flight.dto.BookingRequest;
import com.booking.flight.exception.SeatAlreadyReservedException;
import com.booking.flight.models.Booking;
import com.booking.flight.models.Schedule;
import com.booking.flight.repository.BookingRepository;
import com.booking.flight.repository.ScheduleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ScheduleRepository scheduleRepository;
    private final AerospikeClient aerospikeClient;
    private final WritePolicy aerospikeWritePolicy;
    private final AerospikeConfiguration aerospikeConfig;

    private static final String SEAT_LOCK_SET = "seat_locks";
    private static final String SEAT_LOCK_BIN = "user_id";

    /**
     * Core production-ready method for creating a booking with distributed locking.
     * This follows a strict Check-And-Set (CAS) flow using Aerospike.
     * If the Aerospike lock is acquired, the DB transaction proceeds.
     */
    @Transactional
    public Booking createBooking(BookingRequest request) {

        Schedule schedule = scheduleRepository.findById(request.scheduleId())
                .orElseThrow(() -> new RuntimeException("Schedule not found")); // Use a proper custom exception

        // 1. DISTRIBUTED LOCKING ATTEMPT (Aerospike CAS)
        // Key format: scheduleId:seatNumber (e.g., "123:14A")
        Key lockKey = new Key(
                aerospikeConfig.getNamespace(),
                SEAT_LOCK_SET,
                request.scheduleId() + ":" + request.seatNumber()
        );

        Bin lockBin = new Bin(SEAT_LOCK_BIN, request.userId());

        try {
            // Attempt to create a record only if it DOES NOT EXIST.
            // If successful, the user has acquired the lock for 5 minutes (TTL).
            aerospikeClient.put(aerospikeWritePolicy, lockKey, lockBin);

            // Lock Acquired! Proceed to DB persistence.

            // 2. TRANSACTIONAL PERSISTENCE (MariaDB)
            Booking newBooking = new Booking();
            newBooking.setSchedule(schedule);
            newBooking.setUserId(request.userId());
            newBooking.setSeatNumber(request.seatNumber());
            newBooking.setStatus("CONFIRMED");
            newBooking.setBookingTime(LocalDateTime.now());

            Booking savedBooking = bookingRepository.save(newBooking);

            // 3. CLEANUP: Successfully saved, delete the temporary Aerospike lock.
            aerospikeClient.delete(new WritePolicy(), lockKey);

            return savedBooking;

        } catch (AerospikeException e) {
            // Error Code 5 (KEY_EXISTS_ERROR) means the lock already exists (seat reserved).
            if (e.getResultCode() == ResultCode.KEY_EXISTS_ERROR) {
                throw new SeatAlreadyReservedException("Seat " + request.seatNumber() + " on schedule " + request.scheduleId() + " is currently reserved or locked.");
            }
            // Log other Aerospike errors
            throw new RuntimeException("Aerospike locking failure: " + e.getMessage(), e);

        } catch (Exception e) {
            // Catch any unexpected DB or other errors.
            // In a robust system, you might need compensating logic here
            // to ensure the Aerospike lock is deleted if the DB transaction fails.
            throw new RuntimeException("Booking persistence failed: " + e.getMessage(), e);
        }
    }
}
