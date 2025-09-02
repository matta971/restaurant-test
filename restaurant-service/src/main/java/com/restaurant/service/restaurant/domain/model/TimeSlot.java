package com.restaurant.service.restaurant.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * TimeSlot domain entity representing a reservation time slot
 */
@Entity
@Table(name = "time_slots")
@Getter
@Setter
public class TimeSlot {

    private static final int MIN_DURATION_MINUTES = 30;
    private static final int MAX_DURATION_HOURS = 4;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reservation_date", nullable = false)
    @NotNull(message = "Date cannot be null")
    private LocalDate date;

    @Column(name = "start_time", nullable = false)
    @NotNull(message = "Start time cannot be null")
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    @NotNull(message = "End time cannot be null")
    private LocalTime endTime;

    @Column(name = "reserved_seats", nullable = false)
    @Positive(message = "Reserved seats must be positive")
    private Integer reservedSeats;

    @Column(name = "party_size", nullable = false)
    @Positive(message = "Party size must be positive")
    private Integer partySize;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "customer_phone")
    private String customerPhone;

    @Column(name = "customer_email")
    private String customerEmail;

    @Column(name = "special_requests", length = 500)
    private String specialRequests;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version")
    private Long version;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private TimeSlotStatus status = TimeSlotStatus.AVAILABLE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id")
    @Setter
    private RestaurantTable table;

    // Default constructor for JPA
    protected TimeSlot() {
    }

    /**
     * Creates a new TimeSlot with the specified properties
     *
     * @param date the reservation date (cannot be in the past)
     * @param startTime the start time (required)
     * @param endTime the end time (required, must be after start time)
     * @param reservedSeats the number of reserved seats (must be positive)
     * @throws IllegalArgumentException if validation fails
     */
    public TimeSlot(LocalDate date, LocalTime startTime, LocalTime endTime, Integer reservedSeats) {
        validateDate(date);
        validateTimes(startTime, endTime);
        validateDuration(startTime, endTime);
        validateReservedSeats(reservedSeats);

        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.reservedSeats = reservedSeats;
        this.status = TimeSlotStatus.AVAILABLE;
    }

    /**
     * Validates that this time slot is within the restaurant's opening hours
     * This method should be called after the time slot is associated with a table/restaurant
     *
     * @param restaurant the restaurant to check opening hours against
     * @throws IllegalArgumentException if not within opening hours
     */
    public void validateOpeningHours(Restaurant restaurant) {
        if (restaurant != null && !restaurant.isWithinOpeningHours(startTime, endTime)) {
            throw new IllegalArgumentException(
                    String.format("Time slot must be within restaurant opening hours (%s-%s)",
                            restaurant.getOpeningTime(), restaurant.getClosingTime()));
        }
    }

    // Business Operations

    /**
     * Confirms the time slot reservation
     *
     * @throws IllegalStateException if status doesn't allow confirmation
     */
    public void confirm() {
        if (!status.allowsConfirmation()) {
            throw new IllegalStateException("Cannot confirm time slot that is not available");
        }
        this.status = TimeSlotStatus.CONFIRMED;
    }

    /**
     * Cancels the time slot reservation
     *
     * @throws IllegalStateException if status doesn't allow cancellation
     */
    public void cancel() {
        if (!status.allowsCancellation()) {
            if (status == TimeSlotStatus.AVAILABLE) {
                throw new IllegalStateException("Cannot cancel time slot that is not confirmed");
            } else if (status == TimeSlotStatus.COMPLETED) {
                throw new IllegalStateException("Cannot cancel completed time slot");
            } else {
                throw new IllegalStateException("Cannot cancel cancelled time slot");
            }
        }
        this.status = TimeSlotStatus.CANCELLED;
    }

    /**
     * Completes the time slot (service finished)
     *
     * @throws IllegalStateException if status doesn't allow completion
     */
    public void complete() {
        if (status == TimeSlotStatus.CANCELLED) {
            throw new IllegalStateException("Cannot complete cancelled time slot");
        }
        if (status == TimeSlotStatus.AVAILABLE) {
            throw new IllegalStateException("Cannot complete unconfirmed time slot");
        }
        this.status = TimeSlotStatus.COMPLETED;
    }

    /**
     * Checks if this time slot overlaps with another time slot
     *
     * @param other the other time slot
     * @return true if they overlap, false otherwise
     */
    public boolean overlapsWith(TimeSlot other) {
        if (other == null || !date.equals(other.date)) {
            return false;
        }

        return overlapsWith(other.date, other.startTime, other.endTime);
    }

    /**
     * Checks if this time slot overlaps with a given time range
     *
     * @param otherDate the date to check
     * @param otherStartTime the start time to check
     * @param otherEndTime the end time to check
     * @return true if they overlap, false otherwise
     */
    public boolean overlapsWith(LocalDate otherDate, LocalTime otherStartTime, LocalTime otherEndTime) {
        if (!date.equals(otherDate)) {
            return false;
        }

        // Two time ranges overlap if: start1 < end2 AND start2 < end1
        return startTime.isBefore(otherEndTime) && otherStartTime.isBefore(endTime);
    }

    /**
     * Calculates the duration of this time slot in minutes
     *
     * @return duration in minutes
     */
    public long getDurationInMinutes() {
        return ChronoUnit.MINUTES.between(startTime, endTime);
    }

    // Validation methods

    private void validateDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        if (date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Cannot create time slot for past dates");
        }
    }

    private void validateTimes(LocalTime startTime, LocalTime endTime) {
        if (startTime == null) {
            throw new IllegalArgumentException("Start time cannot be null");
        }
        if (endTime == null) {
            throw new IllegalArgumentException("End time cannot be null");
        }
        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("End time must be after start time");
        }
    }

    private void validateDuration(LocalTime startTime, LocalTime endTime) {
        long durationMinutes = ChronoUnit.MINUTES.between(startTime, endTime);

        if (durationMinutes < MIN_DURATION_MINUTES) {
            throw new IllegalArgumentException("Time slot duration must be at least 30 minutes");
        }

        if (durationMinutes > MAX_DURATION_HOURS * 60) {
            throw new IllegalArgumentException("Time slot duration cannot exceed 4 hours");
        }
    }

    private void validateReservedSeats(Integer reservedSeats) {
        if (reservedSeats == null || reservedSeats <= 0) {
            throw new IllegalArgumentException("Reserved seats must be positive");
        }
    }

    // Equals and HashCode

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TimeSlot timeSlot = (TimeSlot) o;

        // If both have IDs, compare by ID
        if (id != null && timeSlot.id != null) {
            return Objects.equals(id, timeSlot.id);
        }

        // If no IDs, compare by business key (date + startTime + endTime)
        return Objects.equals(date, timeSlot.date) &&
                Objects.equals(startTime, timeSlot.startTime) &&
                Objects.equals(endTime, timeSlot.endTime);
    }

    @Override
    public int hashCode() {
        if (id != null) {
            return Objects.hash(id);
        }
        return Objects.hash(date, startTime, endTime);
    }

    @Override
    public String toString() {
        return "TimeSlot{" +
                "id=" + id +
                ", date=" + date +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", reservedSeats=" + reservedSeats +
                ", status=" + status +
                ", durationMinutes=" + getDurationInMinutes() +
                '}';
    }
}