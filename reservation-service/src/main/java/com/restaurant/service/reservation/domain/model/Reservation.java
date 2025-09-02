package com.restaurant.service.reservation.domain.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Reservation domain entity representing a restaurant table reservation
 * Contains business logic and validation rules for reservations
 */
@Getter
public class Reservation {

    private Long id;
    
    @Setter
    private Customer customer;
    
    private final Long restaurantId;
    private final Long tableId;
    private final LocalDate reservationDate;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final Integer partySize;
    
    @Setter
    private ReservationStatus status;
    
    @Setter
    private String specialRequests;
    
    @Setter
    private LocalDateTime confirmedAt;
    
    @Setter
    private LocalDateTime cancelledAt;
    
    @Setter
    private String cancellationReason;
    
    private final LocalDateTime createdAt;

    /**
     * Creates a new reservation
     */
    public Reservation(Customer customer, Long restaurantId, Long tableId,
                      LocalDate reservationDate, LocalTime startTime, LocalTime endTime,
                      Integer partySize) {
        validateConstructorParams(customer, restaurantId, tableId, reservationDate, 
                                startTime, endTime, partySize);
        
        this.customer = customer;
        this.restaurantId = restaurantId;
        this.tableId = tableId;
        this.reservationDate = reservationDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.partySize = partySize;
        this.status = ReservationStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    // Constructor for JPA
    protected Reservation() {
        this.restaurantId = null;
        this.tableId = null;
        this.reservationDate = null;
        this.startTime = null;
        this.endTime = null;
        this.partySize = null;
        this.createdAt = LocalDateTime.now();
    }

    private void validateConstructorParams(Customer customer, Long restaurantId, Long tableId,
                                         LocalDate reservationDate, LocalTime startTime, 
                                         LocalTime endTime, Integer partySize) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null");
        }
        if (restaurantId == null) {
            throw new IllegalArgumentException("Restaurant ID cannot be null");
        }
        if (tableId == null) {
            throw new IllegalArgumentException("Table ID cannot be null");
        }
        if (reservationDate == null) {
            throw new IllegalArgumentException("Reservation date cannot be null");
        }
        if (reservationDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Reservation date cannot be in the past");
        }
        if (startTime == null) {
            throw new IllegalArgumentException("Start time cannot be null");
        }
        if (endTime == null) {
            throw new IllegalArgumentException("End time cannot be null");
        }
        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("End time must be after start time");
        }
        if (partySize == null || partySize <= 0) {
            throw new IllegalArgumentException("Party size must be positive");
        }
        if (partySize > 12) {
            throw new IllegalArgumentException("Party size cannot exceed 12 people");
        }
    }

    /**
     * Confirms the reservation
     */
    public void confirm() {
        if (status == ReservationStatus.CANCELLED) {
            throw new IllegalStateException("Cannot confirm cancelled reservation");
        }
        if (status == ReservationStatus.COMPLETED) {
            throw new IllegalStateException("Cannot confirm completed reservation");
        }
        
        this.status = ReservationStatus.CONFIRMED;
        this.confirmedAt = LocalDateTime.now();
    }

    /**
     * Cancels the reservation
     */
    public void cancel(String reason) {
        if (status == ReservationStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel completed reservation");
        }
        
        this.status = ReservationStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
        this.cancellationReason = reason;
    }

    /**
     * Completes the reservation (when customer shows up)
     */
    public void complete() {
        if (status != ReservationStatus.CONFIRMED) {
            throw new IllegalStateException("Only confirmed reservations can be completed");
        }
        
        this.status = ReservationStatus.COMPLETED;
    }

    /**
     * Marks reservation as no-show
     */
    public void markAsNoShow() {
        if (status != ReservationStatus.CONFIRMED) {
            throw new IllegalStateException("Only confirmed reservations can be marked as no-show");
        }
        
        this.status = ReservationStatus.NO_SHOW;
    }

    /**
     * Checks if reservation can be modified
     */
    public boolean canBeModified() {
        return status == ReservationStatus.PENDING;
    }

    /**
     * Checks if reservation can be cancelled
     */
    public boolean canBeCancelled() {
        return status == ReservationStatus.PENDING || status == ReservationStatus.CONFIRMED;
    }

    /**
     * Checks if reservation is active (not cancelled or completed)
     */
    public boolean isActive() {
        return status != ReservationStatus.CANCELLED && 
               status != ReservationStatus.COMPLETED && 
               status != ReservationStatus.NO_SHOW;
    }

    /**
     * Gets the duration of the reservation in minutes
     */
    public int getDurationMinutes() {
        return (int) java.time.Duration.between(startTime, endTime).toMinutes();
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reservation that = (Reservation) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(restaurantId, that.restaurantId) &&
               Objects.equals(tableId, that.tableId) &&
               Objects.equals(reservationDate, that.reservationDate) &&
               Objects.equals(startTime, that.startTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, restaurantId, tableId, reservationDate, startTime);
    }

    @Override
    public String toString() {
        return String.format("Reservation{id=%d, customer=%s, restaurant=%d, table=%d, date=%s, time=%s-%s, party=%d, status=%s}",
                id, customer != null ? customer.getEmail() : "null", restaurantId, tableId, 
                reservationDate, startTime, endTime, partySize, status);
    }
}