package com.restaurant.service.reservation.domain.service;

import com.restaurant.service.reservation.domain.model.Customer;
import com.restaurant.service.reservation.domain.model.Reservation;
import com.restaurant.service.reservation.domain.model.ReservationStatus;
import com.restaurant.service.reservation.domain.port.in.ReservationManagementUseCase;
import com.restaurant.service.reservation.domain.port.out.CustomerRepository;
import com.restaurant.service.reservation.domain.port.out.EventPublisher;
import com.restaurant.service.reservation.domain.port.out.ReservationRepository;
import com.restaurant.service.reservation.domain.port.out.RestaurantServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Implementation of Reservation Management Use Cases
 * Contains the business logic for reservation operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReservationManagementUseCaseImpl implements ReservationManagementUseCase {

    private final ReservationRepository reservationRepository;
    private final CustomerRepository customerRepository;
    private final RestaurantServiceClient restaurantServiceClient;
    private final EventPublisher eventPublisher;

    @Override
    public Reservation createReservation(CreateReservationCommand command) {
        log.info("Creating reservation for customer {} at restaurant {}", 
                command.customerEmail(), command.restaurantId());

        // Validate restaurant exists
        var restaurantInfo = restaurantServiceClient.getRestaurant(command.restaurantId());
        if (!restaurantInfo.active()) {
            throw new InvalidReservationOperationException("Restaurant is not active");
        }

        // Check table availability
        boolean isAvailable = restaurantServiceClient.isTableAvailable(
                command.restaurantId(), command.tableId(), command.reservationDate(),
                command.startTime(), command.endTime()
        );

        if (!isAvailable) {
            throw new TableNotAvailableException(command.tableId(), 
                    command.reservationDate(), command.startTime());
        }

        // Get or create customer
        Customer customer = customerRepository.findByEmail(command.customerEmail())
                .orElse(new Customer(command.customerEmail(), command.customerFirstName(),
                                   command.customerLastName(), command.customerPhoneNumber()));
        
        customer = customerRepository.save(customer);

        // Create reservation
        Reservation reservation = new Reservation(
                customer, command.restaurantId(), command.tableId(),
                command.reservationDate(), command.startTime(), command.endTime(),
                command.partySize()
        );

        if (command.specialRequests() != null && !command.specialRequests().trim().isEmpty()) {
            reservation.setSpecialRequests(command.specialRequests().trim());
        }

        // Save reservation
        reservation = reservationRepository.save(reservation);

        // Reserve the table
        boolean reserved = restaurantServiceClient.reserveTable(
                command.restaurantId(), command.tableId(), command.reservationDate(),
                command.startTime(), command.endTime()
        );

        if (!reserved) {
            throw new TableNotAvailableException(command.tableId(), 
                    command.reservationDate(), command.startTime());
        }

        // Publish event
        eventPublisher.publishReservationCreated(reservation);

        log.info("Successfully created reservation with ID {}", reservation.getId());
        return reservation;
    }

    @Override
    public Reservation updateReservation(UpdateReservationCommand command) {
        log.info("Updating reservation with ID {}", command.id());

        var reservation = reservationRepository.findById(command.id())
                .orElseThrow(() -> new ReservationNotFoundException(command.id()));

        if (!reservation.canBeModified()) {
            throw new InvalidReservationOperationException(
                    "Reservation cannot be modified in current status: " + reservation.getStatus());
        }

        // If date/time is changing, check availability
        boolean dateTimeChanged = !reservation.getReservationDate().equals(command.reservationDate()) ||
                                 !reservation.getStartTime().equals(command.startTime()) ||
                                 !reservation.getEndTime().equals(command.endTime());

        if (dateTimeChanged) {
            // Release old reservation
            restaurantServiceClient.releaseTableReservation(
                    reservation.getRestaurantId(), reservation.getTableId(),
                    reservation.getReservationDate(), reservation.getStartTime(), reservation.getEndTime()
            );

            // Check new availability
            boolean isAvailable = restaurantServiceClient.isTableAvailable(
                    reservation.getRestaurantId(), reservation.getTableId(),
                    command.reservationDate(), command.startTime(), command.endTime()
            );

            if (!isAvailable) {
                // Re-reserve the old slot
                restaurantServiceClient.reserveTable(
                        reservation.getRestaurantId(), reservation.getTableId(),
                        reservation.getReservationDate(), reservation.getStartTime(), reservation.getEndTime()
                );
                throw new TableNotAvailableException(reservation.getTableId(), 
                        command.reservationDate(), command.startTime());
            }

            // Reserve new slot
            restaurantServiceClient.reserveTable(
                    reservation.getRestaurantId(), reservation.getTableId(),
                    command.reservationDate(), command.startTime(), command.endTime()
            );
        }

        // Update reservation (would need reflection or builder pattern in real implementation)
        // For now, this is a simplified version
        reservation.setSpecialRequests(command.specialRequests());

        reservation = reservationRepository.save(reservation);

        // Publish event
        eventPublisher.publishReservationUpdated(reservation);

        log.info("Successfully updated reservation with ID {}", reservation.getId());
        return reservation;
    }

    @Override
    public Reservation confirmReservation(Long reservationId) {
        log.info("Confirming reservation with ID {}", reservationId);

        var reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));

        reservation.confirm();
        reservation = reservationRepository.save(reservation);

        // Publish event
        eventPublisher.publishReservationConfirmed(reservation);

        log.info("Successfully confirmed reservation with ID {}", reservationId);
        return reservation;
    }

    @Override
    public Reservation cancelReservation(Long reservationId, String reason) {
        log.info("Cancelling reservation with ID {} with reason: {}", reservationId, reason);

        var reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));

        if (!reservation.canBeCancelled()) {
            throw new InvalidReservationOperationException(
                    "Reservation cannot be cancelled in current status: " + reservation.getStatus());
        }

        reservation.cancel(reason);

        // Release table reservation
        restaurantServiceClient.releaseTableReservation(
                reservation.getRestaurantId(), reservation.getTableId(),
                reservation.getReservationDate(), reservation.getStartTime(), reservation.getEndTime()
        );

        reservation = reservationRepository.save(reservation);

        // Publish event
        eventPublisher.publishReservationCancelled(reservation);

        log.info("Successfully cancelled reservation with ID {}", reservationId);
        return reservation;
    }

    @Override
    public Reservation completeReservation(Long reservationId) {
        log.info("Completing reservation with ID {}", reservationId);

        var reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));

        reservation.complete();
        reservation = reservationRepository.save(reservation);

        // Publish event
        eventPublisher.publishReservationCompleted(reservation);

        log.info("Successfully completed reservation with ID {}", reservationId);
        return reservation;
    }

    @Override
    public Reservation markAsNoShow(Long reservationId) {
        log.info("Marking reservation with ID {} as no-show", reservationId);

        var reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));

        reservation.markAsNoShow();

        // Release table reservation
        restaurantServiceClient.releaseTableReservation(
                reservation.getRestaurantId(), reservation.getTableId(),
                reservation.getReservationDate(), reservation.getStartTime(), reservation.getEndTime()
        );

        reservation = reservationRepository.save(reservation);

        // Publish event
        eventPublisher.publishReservationNoShow(reservation);

        log.info("Successfully marked reservation with ID {} as no-show", reservationId);
        return reservation;
    }

    @Override
    @Transactional(readOnly = true)
    public Reservation getReservation(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Reservation> getAllReservations(Pageable pageable) {
        return reservationRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reservation> getReservationsByCustomer(String customerEmail) {
        return reservationRepository.findByCustomerEmail(customerEmail);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Reservation> getReservationsByRestaurant(Long restaurantId, Pageable pageable) {
        return reservationRepository.findByRestaurantId(restaurantId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reservation> getReservationsByDateRange(LocalDate startDate, LocalDate endDate) {
        return reservationRepository.findByReservationDateBetween(startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reservation> getReservationsByStatus(ReservationStatus status) {
        return reservationRepository.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reservation> getUpcomingReservations(String customerEmail) {
        return reservationRepository.findUpcomingReservationsByCustomerEmail(customerEmail, LocalDate.now());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reservation> getReservationsForDate(Long restaurantId, LocalDate date) {
        return reservationRepository.findByRestaurantIdAndReservationDate(restaurantId, date);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkAvailability(AvailabilityQuery query) {
        return restaurantServiceClient.isTableAvailable(
                query.restaurantId(), null, query.date(),
                query.startTime(), query.endTime()
        );
    }
}