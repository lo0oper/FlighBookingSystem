package com.booking.flight.models;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Column;
import jakarta.persistence.Version;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scheduleId", nullable = false)
    private Schedule schedule;

    // Assuming a simple User ID for now
    @Column(name = "userId", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 5)
    private String seatNumber;

    @Column(nullable = false)
    private String status; // CONFIRMED, CANCELLED, etc.

    @Column(nullable = false)
    private LocalDateTime bookingTime;

    /**
     * JPA Optimistic Locking mechanism.
     * Hibernate/JPA automatically manages this column.
     * If two transactions try to update the same row (same booking)
     * but have different version numbers, the second update fails.
     */
    @Version
    private Integer version;
}