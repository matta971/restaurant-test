package com.restaurant.service.restaurant.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * RestaurantTable domain representing a table in a restaurant
 */

@Getter
@Setter
@NoArgsConstructor
public class RestaurantTable {

    private static final int MIN_SEATS = 1;
    private static final int MAX_SEATS = 8;

    @Setter
    private Long id;

    @Setter
    private Integer tableNumber;

    @Min(value = MIN_SEATS, message = "Table seats must be positive")
    @Max(value = MAX_SEATS, message = "Table seats cannot exceed 8 seats")
    private Integer seats;

    @Enumerated(EnumType.STRING)
    private TableLocation location;

    @Setter
    private Boolean available = true;

    @Setter
    private Restaurant restaurant;

    private List<TimeSlot> timeSlots = new ArrayList<>();

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Version
    private Long version;

    /**
     * Creates a new RestaurantTable with the specified properties
     *
     * @param seats the number of seats (1-8)
     * @param location the table location (required)
     * @throws IllegalArgumentException if validation fails
     */
    public RestaurantTable(Integer seats, TableLocation location) {
        validateSeats(seats);
        validateLocation(location);

        this.seats = seats;
        this.location = location;
        this.available = true;
        this.timeSlots = new ArrayList<>();
        this.tableNumber = generateTableNumber();
    }

    // Business Operations

    /**
     * Makes the table available
     */
    public void makeAvailable() {
        this.available = true;
    }

    /**
     * Makes the table unavailable
     */
    public void makeUnavailable() {
        this.available = false;
    }

    /**
     * Checks if the table is available at a specific date and time range
     *
     * @param date the date to check
     * @param startTime the start time
     * @param endTime the end time
     * @return true if available, false otherwise
     */
    public boolean isAvailableAt(LocalDate date, LocalTime startTime, LocalTime endTime) {
        if (!available) {
            return false;
        }

        return timeSlots.stream()
                .filter(slot -> slot.getStatus() != TimeSlotStatus.CANCELLED &&
                        slot.getStatus() != TimeSlotStatus.COMPLETED) // CANCELLED and COMPLETED don't block
                .noneMatch(slot -> slot.overlapsWith(date, startTime, endTime));
    }

    /**
     * Adds a time slot to the table
     *
     * @param timeSlot the time slot to add (must not be null)
     * @throws IllegalArgumentException if timeSlot is null, overlaps, or exceeds capacity
     */
    public void addTimeSlot(TimeSlot timeSlot) {
        if (timeSlot == null) {
            throw new IllegalArgumentException("TimeSlot cannot be null");
        }

        validateTimeSlotCapacity(timeSlot);
        validateNoOverlap(timeSlot);

        // Validate opening hours if table is associated with a restaurant
        if (restaurant != null) {
            timeSlot.validateOpeningHours(restaurant);
        }

        if (!timeSlots.contains(timeSlot)) {
            timeSlots.add(timeSlot);
            timeSlot.setTable(this);
        }
    }

    /**
     * Removes a time slot from the table
     *
     * @param timeSlot the time slot to remove
     */
    public void removeTimeSlot(TimeSlot timeSlot) {
        if (timeSlot != null && timeSlots.contains(timeSlot)) {
            timeSlots.remove(timeSlot);
            timeSlot.setTable(null);
        }
    }

    // Validation methods

    private void validateSeats(Integer seats) {
        if (seats == null || seats < MIN_SEATS) {
            throw new IllegalArgumentException("Table seats must be positive");
        }
        if (seats > MAX_SEATS) {
            throw new IllegalArgumentException("Table seats cannot exceed 8 seats");
        }
    }

    private void validateLocation(TableLocation location) {
        if (location == null) {
            throw new IllegalArgumentException("Table location cannot be null");
        }
    }

    private void validateTimeSlotCapacity(TimeSlot timeSlot) {
        if (timeSlot.getReservedSeats() > seats) {
            throw new IllegalArgumentException("Reserved seats cannot exceed table capacity");
        }
    }

    private void validateNoOverlap(TimeSlot newTimeSlot) {
        boolean hasOverlap = timeSlots.stream()
                .filter(slot -> slot.getStatus() != TimeSlotStatus.CANCELLED) // All non-cancelled slots cause conflicts
                .anyMatch(existingSlot -> existingSlot.overlapsWith(newTimeSlot));

        if (hasOverlap) {
            throw new IllegalArgumentException("Time slot overlaps with existing reservation");
        }
    }

    private Integer generateTableNumber() {
        return (int) (Math.random() * 9000) + 1000;
    }

    // Setters with validation (Lombok won't generate these because of validation logic)

    public void setSeats(Integer seats) {
        validateSeats(seats);
        this.seats = seats;
    }

    public void setLocation(TableLocation location) {
        validateLocation(location);
        this.location = location;
    }

    // Special methods for boolean field (Lombok generates isAvailable(), but we need consistency)
    public Boolean isAvailable() {
        return available;
    }

    // Defensive copy for timeSlots (override Lombok's default getter)
    public List<TimeSlot> getTimeSlots() {
        return new ArrayList<>(timeSlots); // Return defensive copy
    }


    // Equals and HashCode

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RestaurantTable table = (RestaurantTable) o;

        // If both have IDs, compare by ID
        if (id != null && table.id != null) {
            return Objects.equals(id, table.id);
        }

        // If no IDs, compare by table number
        return Objects.equals(tableNumber, table.tableNumber);
    }

    @Override
    public int hashCode() {
        if (id != null) {
            return Objects.hash(id);
        }
        return Objects.hash(tableNumber);
    }

    @Override
    public String toString() {
        return "RestaurantTable{" +
                "id=" + id +
                ", tableNumber='" + tableNumber + '\'' +
                ", seats=" + seats +
                ", location=" + location +
                ", available=" + available +
                ", timeSlotsCount=" + timeSlots.size() +
                '}';
    }
}