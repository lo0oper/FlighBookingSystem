package com.booking.flight.services;


import com.aerospike.client.*;
import com.aerospike.client.policy.WritePolicy;
import com.booking.flight.config.aeroSpikeConfig.AerospikeConfiguration;
import com.booking.flight.dto.BookingRequest;
import com.booking.flight.exception.AerospikeLockFailureException;
import com.booking.flight.exception.BookingPersistenceException;
import com.booking.flight.exception.ScheduleNotFoundException;
import com.booking.flight.exception.SeatAlreadyReservedException;
import com.booking.flight.models.Booking;
import com.booking.flight.models.Schedule;
import com.booking.flight.repository.BookingRepository;
import com.booking.flight.repository.ScheduleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ScheduleRepository scheduleRepository;
    private final AerospikeClient aerospikeClient;
    private final WritePolicy aerospikeWritePolicy;
    private final AerospikeConfiguration aerospikeConfig;

    private static final String SEAT_LOCK_SET = "seat_locks";
    private static final String SEAT_LOCK_BIN = "user_id";

    @Transactional
    public List<Booking> createMultipleBookings(BookingRequest request) {

        log.info("Attempting multiple seat booking for Schedule ID {} and {} seats by User ID {}",
                request.scheduleId(), request.seatNumbers().size(), request.userId());

        Schedule schedule = scheduleRepository.findById(request.scheduleId())
                .orElseThrow(() -> {
                    log.warn("Schedule not found with ID: {}", request.scheduleId());
                    // Use the specific custom exception for NOT FOUND
                    return new ScheduleNotFoundException(request.scheduleId());
                });

        List<Booking> bookingsToSave = new ArrayList<>();
        int locksAcquiredCount = 0; // Counter for compensation logic

        // 1. ITERATE AND ACQUIRE DISTRIBUTED LOCK FOR EACH SEAT
        try {
            for (String seatNumber : request.seatNumbers()) {

                String lockKeyString = request.scheduleId() + ":" + seatNumber;
                Key lockKey = new Key(
                        aerospikeConfig.getNamespace(),
                        SEAT_LOCK_SET,
                        lockKeyString
                );

                Bin lockBin = new Bin(SEAT_LOCK_BIN, request.userId());

                log.debug("Attempting to acquire lock for seat: {}", seatNumber);

                // Attempt to create a record ONLY if it DOES NOT EXIST.
                aerospikeClient.put(aerospikeWritePolicy, lockKey, lockBin);
                locksAcquiredCount++; // Increment only on successful lock acquisition

                log.debug("Lock acquired for seat: {}", seatNumber);

                // If lock is successful, prepare the Booking entity for later saving
                Booking newBooking = new Booking();
                newBooking.setSchedule(schedule);
                newBooking.setUserId(request.userId());
                newBooking.setSeatNumber(seatNumber);
                newBooking.setStatus("CONFIRMED");
                newBooking.setBookingTime(LocalDateTime.now());
                bookingsToSave.add(newBooking);
            }

            // 2. TRANSACTIONAL PERSISTENCE (MariaDB)
            List<Booking> savedBookings;
            try {
                savedBookings = bookingRepository.saveAll(bookingsToSave);
            } catch (DataAccessException e) {
                // Throw specific exception for DB errors
                throw new BookingPersistenceException("Failed to save bookings to MariaDB.", e);
            }

            log.info("Successfully persisted {} bookings in MariaDB.", savedBookings.size());

            // 3. CLEANUP: Successfully saved, delete all temporary Aerospike locks.
            releaseLocks(request.scheduleId(), request.seatNumbers());

            return savedBookings;

        }catch (AerospikeException e) {
            // Error Code 5 (KEY_EXISTS_ERROR) means one of the seats was already locked.
            if (e.getResultCode() == ResultCode.KEY_EXISTS_ERROR) {
                // Execute compensation logic before re-throwing the conflict exception
                compensateForFailedLock(request.scheduleId(), request.seatNumbers(), locksAcquiredCount);

                log.warn("LOCK CONFLICT: One or more requested seats were already reserved. Booking failed.");
                throw new SeatAlreadyReservedException("One or more requested seats are currently reserved or locked.");
            }

            // For other Aerospike connection/server errors (Error 9: Timeout, etc.)
            log.error("Aerospike critical failure: {}", e.getMessage(), e);

            // <<< FIX HERE: ADD COMPENSATION FOR CRITICAL ERRORS >>>
            compensateForFailedLock(request.scheduleId(), request.seatNumbers(), locksAcquiredCount);

            throw new AerospikeLockFailureException("Aerospike locking failure (non-conflict error).", e);

        } catch (Exception e) {
            // ... existing logic for PersistenceException and RuntimeException ...
            log.error("Critical error during booking process. Executing lock compensation. Error: {}", e.getMessage(), e);

            // This compensation is now redundant for AerospikeException, but keeps it safe for others.
            compensateForFailedLock(request.scheduleId(), request.seatNumbers(), locksAcquiredCount);

            if (e instanceof BookingPersistenceException) {
                throw (BookingPersistenceException) e;
            }
            throw new RuntimeException("Unexpected error during booking transaction.", e);
        }
    }

    /**
     * Helper to release all locks post-successful transaction.
     */
    private void releaseLocks(Long scheduleId, List<String> seatNumbers) {
        for (String seatNumber : seatNumbers) {
            Key lockKey = new Key(
                    aerospikeConfig.getNamespace(),
                    SEAT_LOCK_SET,
                    scheduleId + ":" + seatNumber
            );
            aerospikeClient.delete(new WritePolicy(), lockKey);
            log.debug("Released lock for seat {}", seatNumber);
        }
        log.info("Successfully released all {} Aerospike locks.", seatNumbers.size());
    }

    /**
     * Compensation logic to delete locks acquired *before* a lock conflict or other error occurred.
     */
    private void compensateForFailedLock(Long scheduleId, List<String> requestedSeats, int locksAcquiredCount) {
        log.warn("Executing compensation: Deleting {} locks acquired before conflict/error.", locksAcquiredCount);
        // Only delete the locks that were successfully acquired (up to the point of failure)
        List<String> seatsToUnlock = requestedSeats.subList(0, locksAcquiredCount);

        for (String seatNumber : seatsToUnlock) {
            Key lockKey = new Key(
                    aerospikeConfig.getNamespace(),
                    SEAT_LOCK_SET,
                    scheduleId + ":" + seatNumber
            );
            aerospikeClient.delete(new WritePolicy(), lockKey);
            log.debug("Compensation: Released lock for seat {}", seatNumber);
        }
    }
}