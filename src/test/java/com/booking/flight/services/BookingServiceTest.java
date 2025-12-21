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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import static org.mockito.Mockito.lenient;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {

    // Mocks for dependencies
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private ScheduleRepository scheduleRepository;
    @Mock
    private AerospikeClient aerospikeClient;
    @Mock
    private WritePolicy aerospikeWritePolicy;
    @Mock
    private AerospikeConfiguration aerospikeConfig;

    // Inject the mocks into the service being tested
    @InjectMocks
    private BookingService bookingService;

    // Test data setup
    private Long SCHEDULE_ID = 100L;
    private Long USER_ID = 200L;
    private Schedule mockSchedule;
    private BookingRequest validRequestTwoSeats;
    private List<String> seatNumbersTwoSeats = Arrays.asList("A01", "A02");
    private List<Booking> mockSavedBookings;

    // Constants used internally in the service
    private static final String SEAT_LOCK_SET = "seat_locks";
    private static final String NAMESPACE = "test_namespace";

    @BeforeEach
    void setUp() {
        // Mock the Aerospike config to return a fixed namespace
        lenient().when(aerospikeConfig.getNamespace()).thenReturn(NAMESPACE);
        // Setup mock schedule
        mockSchedule = new Schedule();
        mockSchedule.setScheduleId(SCHEDULE_ID);

        // Setup valid request
        validRequestTwoSeats = new BookingRequest(SCHEDULE_ID, seatNumbersTwoSeats, USER_ID);

        // Setup mock saved bookings (must match the input seats)
        Booking bookingA01 = new Booking();
        bookingA01.setBookingId(1L);
        bookingA01.setSeatNumber("A01");
        Booking bookingA02 = new Booking();
        bookingA02.setBookingId(2L);
        bookingA02.setSeatNumber("A02");
        mockSavedBookings = Arrays.asList(bookingA01, bookingA02);
    }

    // --- TEST CASES ---

    @Test
    void createMultipleBookings_Success() {
        // GIVEN: Schedule exists, Aerospike lock is acquired for both, DB save succeeds.
        when(scheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(mockSchedule));

        // Aerospike put() is called twice, and we ensure it completes successfully (no exception)
        // No specific stubbing needed for aerospikeClient.put() if we expect success,
        // as the default behavior is to do nothing and succeed.

        // DB saveAll returns the list of saved bookings
        when(bookingRepository.saveAll(any(List.class))).thenReturn(mockSavedBookings);

        // WHEN: Calling the service method
        List<Booking> result = bookingService.createMultipleBookings(validRequestTwoSeats);

        // THEN:
        assertNotNull(result);
        assertEquals(2, result.size());

        // Verify all interactions
        // 1. Schedule lookup happened once
        verify(scheduleRepository, times(1)).findById(SCHEDULE_ID);

        // 2. Aerospike put for lock acquisition happened once for A01 and once for A02
        verify(aerospikeClient, times(1)).put(eq(aerospikeWritePolicy), eq(new Key(NAMESPACE, SEAT_LOCK_SET, "100:A01")), any(Bin.class));
        verify(aerospikeClient, times(1)).put(eq(aerospikeWritePolicy), eq(new Key(NAMESPACE, SEAT_LOCK_SET, "100:A02")), any(Bin.class));

        // 3. DB save happened once
        verify(bookingRepository, times(1)).saveAll(any(List.class));

        // 4. Aerospike delete for lock release happened once for A01 and once for A02
        verify(aerospikeClient, times(2)).delete(any(WritePolicy.class), any(Key.class));
    }

    @Test
    void createMultipleBookings_ScheduleNotFound() {
        // GIVEN: ScheduleRepository returns empty
        when(scheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.empty());

        // WHEN/THEN: Expect ScheduleNotFoundException
        assertThrows(ScheduleNotFoundException.class, () ->
                bookingService.createMultipleBookings(validRequestTwoSeats));

        // Verify no DB or Aerospike operations occurred
        verify(aerospikeClient, never()).put(any(), any(), any());
        verify(bookingRepository, never()).saveAll(any());
    }

    @Test
    void createMultipleBookings_SeatAlreadyReservedConflict() {
        when(scheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(mockSchedule));

        // 1. Explicitly stub the success for the first seat (A01)
        doNothing().when(aerospikeClient).put(
                eq(aerospikeWritePolicy),
                eq(new Key(NAMESPACE, SEAT_LOCK_SET, "100:A01")),
                any(Bin[].class) // CRITICAL: Use Bin[].class for varargs matching
        );

        // 2. Stub Aerospike to FAIL on the SECOND seat (A02) with KEY_EXISTS_ERROR
        doThrow(new AerospikeException(ResultCode.KEY_EXISTS_ERROR))
                .when(aerospikeClient)
                .put(eq(aerospikeWritePolicy), eq(new Key(NAMESPACE, SEAT_LOCK_SET, "100:A02")), any(Bin[].class));

        // WHEN/THEN: Expect SeatAlreadyReservedException
        assertThrows(SeatAlreadyReservedException.class, () ->
                bookingService.createMultipleBookings(validRequestTwoSeats));

        // Verify COMPENSATION occurred:
        // 1. Put was called successfully for A01
        verify(aerospikeClient, times(1)).put(eq(aerospikeWritePolicy), eq(new Key(NAMESPACE, SEAT_LOCK_SET, "100:A01")), any(Bin.class));
        // 2. Delete (compensation) was called for the lock that WAS acquired (A01)
        verify(aerospikeClient, times(1)).delete(any(WritePolicy.class), eq(new Key(NAMESPACE, SEAT_LOCK_SET, "100:A01")));
        // 3. DB save was NEVER called (atomic failure)
        verify(bookingRepository, never()).saveAll(any());
    }

    @Test
    void createMultipleBookings_AerospikeUnexpectedFailure_CompensationCheck() {
        // ... (Stubbing block remains correct) ...
        when(scheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(mockSchedule));

        doNothing()
                .doThrow(new AerospikeException(ResultCode.TIMEOUT))
                .when(aerospikeClient)
                .put(
                        any(WritePolicy.class),
                        any(Key.class),
                        any(Bin[].class)
                );

        // WHEN/THEN: Expect AerospikeLockFailureException
        assertThrows(AerospikeLockFailureException.class, () ->
                bookingService.createMultipleBookings(validRequestTwoSeats));

        // Verify COMPENSATION occurred:
        // 1. Delete (compensation) was called for the lock that WAS acquired (A01).
        verify(aerospikeClient, times(1)).delete(
                any(WritePolicy.class),
                // CRITICAL FIX: Match the Key's userKey object directly to the expected String key
                argThat(key -> key.userKey.toString().equals(SCHEDULE_ID + ":" + "A01"))
        );

        // 2. DB save was NEVER called
        verify(bookingRepository, never()).saveAll(any());
    }

    @Test
    void createMultipleBookings_DBPersistenceFailure() {
        // GIVEN: Schedule exists, Aerospike locks are acquired for both seats
        when(scheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(mockSchedule));

        // Stub DB save to fail with a DataAccessException (e.g., constraint violation)
        doThrow(new DataAccessException("DB connection error") {} )
                .when(bookingRepository).saveAll(any(List.class));

        // WHEN/THEN: Expect BookingPersistenceException
        assertThrows(RuntimeException.class, () -> // The service catches DataAccessException and re-throws a RuntimeException wrapper
                bookingService.createMultipleBookings(validRequestTwoSeats));

        // Verify COMPENSATION occurred:
        // 1. Aerospike Puts happened successfully twice
        verify(aerospikeClient, times(2)).put(any(), any(), any());

        // 2. Aerospike Deletes (compensation) were called twice for both A01 and A02
        verify(aerospikeClient, times(1)).delete(any(WritePolicy.class), eq(new Key(NAMESPACE, SEAT_LOCK_SET, "100:A01")));
        verify(aerospikeClient, times(1)).delete(any(WritePolicy.class), eq(new Key(NAMESPACE, SEAT_LOCK_SET, "100:A02")));
    }

    @Test
    void createMultipleBookings_DBPersistenceFailure_CompensationCheck() {
        // GIVEN: Schedule exists, Aerospike locks are acquired for both seats
        when(scheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(mockSchedule));

        // Stub DB save to fail with a DataAccessException (Spring's wrapper for DB errors)
        doThrow(new org.springframework.dao.DataIntegrityViolationException("MariaDB constraint violation"))
                .when(bookingRepository).saveAll(any(List.class));

        // WHEN/THEN: Expect BookingPersistenceException (our custom wrapper)
        assertThrows(BookingPersistenceException.class, () ->
                bookingService.createMultipleBookings(validRequestTwoSeats));

        // Verify COMPENSATION occurred:
        // 1. Aerospike Puts happened successfully twice (A01, A02)
        verify(aerospikeClient, times(2)).put(any(), any(), any());

        // 2. Aerospike Deletes (compensation) were called twice, releasing both acquired locks
        verify(aerospikeClient, times(1)).delete(any(WritePolicy.class), eq(new Key(NAMESPACE, SEAT_LOCK_SET, "100:A01")));
        verify(aerospikeClient, times(1)).delete(any(WritePolicy.class), eq(new Key(NAMESPACE, SEAT_LOCK_SET, "100:A02")));
    }


    @Test
    void createSingleBooking_Success() {
        // Test the edge case where the list contains only one seat
        BookingRequest singleSeatRequest = new BookingRequest(SCHEDULE_ID, Collections.singletonList("B01"), USER_ID);

        when(scheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(mockSchedule));

        Booking singleBooking = new Booking();
        singleBooking.setBookingId(3L);
        singleBooking.setSeatNumber("B01");

        when(bookingRepository.saveAll(any(List.class))).thenReturn(Collections.singletonList(singleBooking));

        // WHEN
        List<Booking> result = bookingService.createMultipleBookings(singleSeatRequest);

        // THEN
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("B01", result.get(0).getSeatNumber());

        // Verify only one lock acquired and one lock released
        verify(aerospikeClient, times(1)).put(eq(aerospikeWritePolicy), eq(new Key(NAMESPACE, SEAT_LOCK_SET, "100:B01")), any(Bin.class));
        verify(aerospikeClient, times(1)).delete(any(WritePolicy.class), eq(new Key(NAMESPACE, SEAT_LOCK_SET, "100:B01")));
    }

    @Test
    void createMultipleBookings_EmptySeatsList() {
        // GIVEN: A request with an empty list of seat numbers
        // Note: Use USER_ID for the user_id field in BookingRequest, not SCHEDULE_ID
        BookingRequest emptyRequest = new BookingRequest( SCHEDULE_ID, Collections.emptyList(),USER_ID);

        when(scheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(mockSchedule));

        // Stub the repository to return an empty list when saveAll is called with any list (even an empty one)
        when(bookingRepository.saveAll(any())).thenReturn(Collections.emptyList());

        // WHEN
        List<Booking> result = bookingService.createMultipleBookings(emptyRequest);

        // THEN
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // Verify:

        // 1. Aerospike PUT should NOT occur because the seats list is empty. (Correct as is)
        verify(aerospikeClient, never()).put(any(), any(), any());

        // 2. Aerospike DELETE (cleanup) should NOT occur (Correct as is)
        verify(aerospikeClient, never()).delete(any(), any());

        // 3. FIX: DB saveAll IS INVOKED, even if with an empty list.
        // We verify it was called exactly once.
        verify(bookingRepository, times(1)).saveAll(any());
    }
}