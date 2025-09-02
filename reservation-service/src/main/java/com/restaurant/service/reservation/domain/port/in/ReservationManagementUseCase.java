package com.restaurant.service.reservation.domain.port.in;

import com.restaurant.service.reservation.domain.model.Reservation;
import com.restaurant.service.reservation.domain.model.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Use Case interface for Reservation management operations
 * This defines the business operations available for reservations
 */
public interface ReservationManagementUseCase {

    /**
     * Creates a new reservation
     */
    Reservation createReservation(CreateReservationCommand command);

    /**
     * Updates an existing reservation
     */
    Reservation updateReservation(UpdateReservationCommand command);

    /**
     * Confirms a reservation
     */
    Reservation confirmReservation(Long reservationId);

    /**
     * Cancels a reservation
     */
    Reservation cancelReservation(Long reservationId, String reason);

    /**
     * Completes a reservation
     */
    Reservation completeReservation(Long reservationId);

    /**
     * Marks a reservation as no-show
     */
    Reservation markAsNoShow(Long reservationId);

    /**
     * Retrieves a reservation by its ID
     */
    Reservation getReservation(Long reservationId);

    /**
     * Retrieves all reservations with pagination
     */
    Page<Reservation> getAllReservations(Pageable pageable);

    /**
     * Retrieves reservations by customer email
     */
    List<Reservation> getReservationsByCustomer(String customerEmail);

    /**
     * Retrieves reservations by restaurant
     */
    Page<Reservation> getReservationsByRestaurant(Long restaurantId, Pageable pageable);

    /**
     * Retrieves reservations by date range
     */
    List<Reservation> getReservationsByDateRange(LocalDate startDate, LocalDate endDate);

    /**
     * Retrieves reservations by status
     */
    List<Reservation> getReservationsByStatus(ReservationStatus status);

    /**
     * Retrieves upcoming reservations for a customer
     */
    List<Reservation> getUpcomingReservations(String customerEmail);

    /**
     * Retrieves reservations for a specific date
     */
    List<Reservation> getReservationsForDate(Long restaurantId, LocalDate date);

    /**
     * Checks availability for a reservation
     */
    boolean checkAvailability(AvailabilityQuery query);

    /**
     * Command for creating a reservation
     */
    record CreateReservationCommand(
        String customerEmail,
        String customerFirstName,
        String customerLastName,
        String customerPhoneNumber,
        Long restaurantId,
        Long tableId,
        LocalDate reservationDate,
        LocalTime startTime,
        LocalTime endTime,
        Integer partySize,
        String specialRequests
    ) {}

    /**
     * Command for updating a reservation
     */
    record UpdateReservationCommand(
        Long id,
        LocalDate reservationDate,
        LocalTime startTime,
        LocalTime endTime,
        Integer partySize,
        String specialRequests
    ) {}

    /**
     * Query for checking availability
     */
    record AvailabilityQuery(
        Long restaurantId,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        Integer partySize
    ) {}

    /**
     * Exception thrown when reservation is not found
     */
    class ReservationNotFoundException extends RuntimeException {
        public ReservationNotFoundException(Long reservationId) {
            super("Reservation not found with ID: " + reservationId);
        }
    }

    /**
     * Exception thrown when table is not available
     */
    class TableNotAvailableException extends RuntimeException {
        public TableNotAvailableException(Long tableId, LocalDate date, LocalTime startTime) {
            super(String.format("Table %d is not available on %s at %s", tableId, date, startTime));
        }
    }

    /**
     * Exception thrown when reservation operation is not allowed
     */
    class InvalidReservationOperationException extends RuntimeException {
        public InvalidReservationOperationException(String message) {
            super(message);
        }
    }
}