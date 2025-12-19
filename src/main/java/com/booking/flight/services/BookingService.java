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
import lombok.extern.slf4j.Slf4j; // <-- NEW IMPORT
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j // <-- Inject the logger
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

        log.info("Attempting booking for Schedule ID {} and Seat {}",
                request.scheduleId(), request.seatNumber());

        Schedule schedule = scheduleRepository.findById(request.scheduleId())
                .orElseThrow(() -> {
                    log.warn("Schedule not found with ID: {}", request.scheduleId());
                    return new RuntimeException("Schedule not found"); // Use a proper custom exception
                });

        // 1. DISTRIBUTED LOCKING ATTEMPT (Aerospike CAS)
        // Key format: scheduleId:seatNumber (e.g., "123:14A")
        String lockKeyString = request.scheduleId() + ":" + request.seatNumber();
        Key lockKey = new Key(
                aerospikeConfig.getNamespace(),
                SEAT_LOCK_SET,
                lockKeyString
        );

        Bin lockBin = new Bin(SEAT_LOCK_BIN, request.userId());

        try {
            log.debug("Attempting to acquire Aerospike lock with key: {}", lockKeyString);

            // Attempt to create a record only if it DOES NOT EXIST.
            aerospikeClient.put(aerospikeWritePolicy, lockKey, lockBin);

            log.info("Aerospike lock successfully acquired for key: {}. Proceeding to DB transaction.", lockKeyString);

            // 2. TRANSACTIONAL PERSISTENCE (MariaDB)
            Booking newBooking = new Booking();
            newBooking.setSchedule(schedule);
            newBooking.setUserId(request.userId());
            newBooking.setSeatNumber(request.seatNumber());
            newBooking.setStatus("CONFIRMED");
            newBooking.setBookingTime(LocalDateTime.now());

            Booking savedBooking = bookingRepository.save(newBooking);
            log.debug("Booking persisted in MariaDB. Booking ID: {}", savedBooking.getBookingId());


            // 3. CLEANUP: Successfully saved, delete the temporary Aerospike lock.
            // Using a new WritePolicy here just for clarity/safety, but the default should suffice.
            aerospikeClient.delete(new WritePolicy(), lockKey);
            log.info("Aerospike lock successfully released for key: {}", lockKeyString);

            return savedBooking;

        } catch (AerospikeException e) {
            // Error Code 5 (KEY_EXISTS_ERROR) means the lock already exists (seat reserved).
            if (e.getResultCode() == ResultCode.KEY_EXISTS_ERROR) {
                log.warn("LOCK CONFLICT: Seat {} on schedule {} is already locked/reserved. Lock Key: {}",
                        request.seatNumber(), request.scheduleId(), lockKeyString);

                throw new SeatAlreadyReservedException("Seat " + request.seatNumber() + " on schedule " + request.scheduleId() + " is currently reserved or locked.");
            }
            // Log other Aerospike errors
            log.error("Aerospike locking failure for key {}: {}", lockKeyString, e.getMessage(), e);
            throw new RuntimeException("Aerospike locking failure: " + e.getMessage(), e);

        } catch (Exception e) {
            // Catch any unexpected DB or other errors.
            log.error("Critical error during booking persistence. Lock status is UNCERTAIN for {}. Error: {}",
                    lockKeyString, e.getMessage(), e);

            // NOTE: In a robust system, compensating logic must attempt to delete the lock here.
            throw new RuntimeException("Booking persistence failed: " + e.getMessage(), e);
        }
    }
}