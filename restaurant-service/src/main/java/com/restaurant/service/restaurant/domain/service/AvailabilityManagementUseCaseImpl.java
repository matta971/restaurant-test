package com.restaurant.service.restaurant.domain.service;

import com.restaurant.service.restaurant.domain.model.RestaurantTable;
import com.restaurant.service.restaurant.domain.model.TimeSlot;
import com.restaurant.service.restaurant.domain.model.TimeSlotStatus;
import com.restaurant.service.restaurant.domain.port.in.AvailabilityManagementUseCase;
import com.restaurant.service.restaurant.domain.port.out.RestaurantRepositoryPort;
import com.restaurant.service.restaurant.domain.port.out.RestaurantTableRepositoryPort;
import com.restaurant.service.restaurant.domain.port.out.TimeSlotRepositoryPort;
import com.restaurant.service.restaurant.domain.port.out.EventPublisherPort;
import com.restaurant.service.restaurant.domain.port.out.NotificationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * Implementation of Availability Management Use Case
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AvailabilityManagementUseCaseImpl implements AvailabilityManagementUseCase {

    private final RestaurantRepositoryPort restaurantRepository;
    private final RestaurantTableRepositoryPort tableRepository;
    private final TimeSlotRepositoryPort timeSlotRepository;
    private final AvailabilityService availabilityService;
    private final EventPublisherPort eventPublisher;
    private final NotificationPort notificationPort;

    @Override
    @Transactional(readOnly = true)
    public List<RestaurantTable> findAvailableTables(AvailabilityQuery query) {
        var restaurant = restaurantRepository.findById(query.restaurantId())
                .orElseThrow(() -> new RuntimeException("Restaurant not found: " + query.restaurantId()));

        return availabilityService.findAvailableTables(
                restaurant,
                query.partySize(),
                query.date(),
                query.startTime(),
                query.endTime()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public RestaurantTable findBestAvailableTable(AvailabilityQuery query) {
        var restaurant = restaurantRepository.findById(query.restaurantId())
                .orElseThrow(() -> new RuntimeException("Restaurant not found: " + query.restaurantId()));

        return availabilityService.findBestTable(
                restaurant,
                query.partySize(),
                query.date(),
                query.startTime(),
                query.endTime()
        ).orElseThrow(() -> new NoTablesAvailableException(
                query.restaurantId(), query.date(), query.startTime(), query.endTime()));
    }

    @Override
    public TimeSlot createReservation(CreateReservationCommand command) {
        var table = tableRepository.findById(command.tableId())
                .orElseThrow(() -> new RuntimeException("Table not found: " + command.tableId()));

        var timeSlot = new TimeSlot(
                command.date(),
                command.startTime(),
                command.endTime(),
                command.partySize()
        );

        table.addTimeSlot(timeSlot);
        var savedTimeSlot = timeSlotRepository.save(timeSlot);

        // Publish domain event
        var event = new EventPublisherPort.ReservationCreatedEvent(
                table.getRestaurant() != null ? table.getRestaurant().getId() : null,
                table.getId(),
                savedTimeSlot.getId(),
                savedTimeSlot.getDate(),
                savedTimeSlot.getStartTime(),
                savedTimeSlot.getEndTime(),
                savedTimeSlot.getReservedSeats(),
                savedTimeSlot.getCustomerEmail(),
                Instant.now()
        );
        eventPublisher.publishEvent(event);

        return savedTimeSlot;
    }

    @Override
    public TimeSlot confirmReservation(Long timeSlotId) {
        var timeSlot = timeSlotRepository.findById(timeSlotId)
                .orElseThrow(() -> new TimeSlotNotFoundException(timeSlotId));

        var previousStatus = timeSlot.getStatus().name();
        timeSlot.confirm();
        var savedTimeSlot = timeSlotRepository.save(timeSlot);

        // Publish domain event
        var event = new EventPublisherPort.ReservationStatusChangedEvent(
                timeSlot.getTable() != null && timeSlot.getTable().getRestaurant() != null ?
                        timeSlot.getTable().getRestaurant().getId() : null,
                timeSlot.getTable() != null ? timeSlot.getTable().getId() : null,
                savedTimeSlot.getId(),
                previousStatus,
                savedTimeSlot.getStatus().name(),
                Instant.now()
        );
        eventPublisher.publishEvent(event);

        // Send notification
        if (timeSlot.getTable() != null && timeSlot.getTable().getRestaurant() != null) {
            var notification = new NotificationPort.ReservationConfirmationNotification(
                    "customer@example.com", // In real app, get from reservation
                    timeSlot.getTable().getRestaurant().getId(),
                    timeSlot.getTable().getRestaurant().getName(),
                    savedTimeSlot.getDate(),
                    savedTimeSlot.getStartTime(),
                    savedTimeSlot.getEndTime(),
                    savedTimeSlot.getReservedSeats(),
                    "T-"+timeSlot.getTable().getTableNumber(),
                    Map.of("reservationId", savedTimeSlot.getId())
            );
            notificationPort.sendNotification(notification);
        }

        return savedTimeSlot;
    }

    @Override
    public TimeSlot cancelReservation(Long timeSlotId) {
        var timeSlot = timeSlotRepository.findById(timeSlotId)
                .orElseThrow(() -> new TimeSlotNotFoundException(timeSlotId));

        var previousStatus = timeSlot.getStatus().name();
        timeSlot.cancel();
        var savedTimeSlot = timeSlotRepository.save(timeSlot);

        // Publish domain event
        var event = new EventPublisherPort.ReservationStatusChangedEvent(
                timeSlot.getTable() != null && timeSlot.getTable().getRestaurant() != null ?
                        timeSlot.getTable().getRestaurant().getId() : null,
                timeSlot.getTable() != null ? timeSlot.getTable().getId() : null,
                savedTimeSlot.getId(),
                previousStatus,
                savedTimeSlot.getStatus().name(),
                Instant.now()
        );
        eventPublisher.publishEvent(event);

        return savedTimeSlot;
    }

    @Override
    public TimeSlot completeReservation(Long timeSlotId) {
        var timeSlot = timeSlotRepository.findById(timeSlotId)
                .orElseThrow(() -> new TimeSlotNotFoundException(timeSlotId));

        var previousStatus = timeSlot.getStatus().name();
        timeSlot.complete();
        var savedTimeSlot = timeSlotRepository.save(timeSlot);

        // Publish domain event
        var event = new EventPublisherPort.ReservationStatusChangedEvent(
                timeSlot.getTable() != null && timeSlot.getTable().getRestaurant() != null ?
                        timeSlot.getTable().getRestaurant().getId() : null,
                timeSlot.getTable() != null ? timeSlot.getTable().getId() : null,
                savedTimeSlot.getId(),
                previousStatus,
                savedTimeSlot.getStatus().name(),
                Instant.now()
        );
        eventPublisher.publishEvent(event);

        return savedTimeSlot;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimeSlot> getReservationsForDate(Long restaurantId, LocalDate date) {
        return timeSlotRepository.findByRestaurantIdAndDate(restaurantId, date);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimeSlot> getReservationsByStatus(Long restaurantId, TimeSlotStatus status) {
        return timeSlotRepository.findByRestaurantIdAndStatus(restaurantId, status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimeSlot> getUpcomingReservations(Long restaurantId) {
        return timeSlotRepository.findUpcomingByRestaurantId(restaurantId, LocalDate.now());
    }

    @Override
    @Transactional(readOnly = true)
    public double calculateAvailabilityRate(Long restaurantId, LocalDate date) {
        var restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found: " + restaurantId));

        return availabilityService.calculateAvailabilityRate(restaurant, date);
    }

    @Override
    @Transactional(readOnly = true)
    public double calculateUtilizationRate(Long restaurantId, LocalDate date, LocalTime time) {
        var restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found: " + restaurantId));

        return availabilityService.calculateUtilizationRate(restaurant, date, time);
    }

    @Override
    @Transactional(readOnly = true)
    public CapacityStats getCapacityStats(Long restaurantId) {
        var restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found: " + restaurantId));

        var tables = restaurant.getTables();
        int totalSeats = restaurant.getCapacity();
        int availableSeats = restaurant.getTotalAvailableSeats();
        long totalTables = tables.size();
        long availableTables = tables.stream().mapToLong(t -> t.isAvailable() ? 1 : 0).sum();
        double availabilityRate = totalTables > 0 ? (double) availableTables / totalTables : 0.0;

        return new CapacityStats(
                restaurantId,
                totalSeats,
                availableSeats,
                totalTables,
                availableTables,
                availabilityRate
        );
    }
}