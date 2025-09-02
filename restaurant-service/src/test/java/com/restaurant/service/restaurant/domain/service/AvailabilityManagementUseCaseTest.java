package com.restaurant.service.restaurant.domain.service;

import com.restaurant.service.restaurant.domain.model.*;
import com.restaurant.service.restaurant.domain.port.in.AvailabilityManagementUseCase;
import com.restaurant.service.restaurant.domain.port.in.AvailabilityManagementUseCase.*;
import com.restaurant.service.restaurant.domain.port.out.EventPublisherPort;
import com.restaurant.service.restaurant.domain.port.out.NotificationPort;
import com.restaurant.service.restaurant.domain.port.out.RestaurantRepositoryPort;
import com.restaurant.service.restaurant.domain.port.out.RestaurantTableRepositoryPort;
import com.restaurant.service.restaurant.domain.port.out.TimeSlotRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AvailabilityManagementUseCase implementation
 * Following TDD approach - testing the business logic before implementation
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Availability Management Use Case Tests")
class AvailabilityManagementUseCaseTest {

    @Mock
    private RestaurantRepositoryPort restaurantRepository;

    @Mock
    private RestaurantTableRepositoryPort tableRepository;

    @Mock
    private TimeSlotRepositoryPort timeSlotRepository;

    @Mock
    private EventPublisherPort eventPublisher;

    @Mock
    private NotificationPort notificationPort;

    @Mock
    private AvailabilityService availabilityService;

    private AvailabilityManagementUseCase useCase;

    @BeforeEach
    void setUp() {
        // Will be implemented in step 7
        useCase = new AvailabilityManagementUseCaseImpl(
                restaurantRepository,
                tableRepository,
                timeSlotRepository,
                availabilityService,
                eventPublisher,
                notificationPort
        );
    }

    @Nested
    @DisplayName("Find Available Tables")
    class FindAvailableTables {

        @Test
        @DisplayName("Should find available tables for valid query")
        void shouldFindAvailableTablesForValidQuery() {
            // Given
            Long restaurantId = 1L;
            LocalDate date = LocalDate.now().plusDays(1);
            LocalTime startTime = LocalTime.of(19, 0);
            LocalTime endTime = LocalTime.of(21, 0);
            Integer partySize = 4;

            AvailabilityQuery query = new AvailabilityQuery(
                    restaurantId, date, startTime, endTime, partySize
            );

            Restaurant restaurant = createRestaurant(restaurantId, "Test Bistro");
            List<RestaurantTable> availableTables = List.of(
                    createTable(1L, 4, TableLocation.WINDOW),
                    createTable(2L, 6, TableLocation.TERRACE)
            );

            when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
            when(availabilityService.findAvailableTables(restaurant, partySize, date, startTime, endTime))
                    .thenReturn(availableTables);

            // When
            List<RestaurantTable> result = useCase.findAvailableTables(query);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).containsAll(availableTables);

            verify(restaurantRepository).findById(restaurantId);
            verify(availabilityService).findAvailableTables(restaurant, partySize, date, startTime, endTime);
        }

        @Test
        @DisplayName("Should throw exception when restaurant not found")
        void shouldThrowExceptionWhenRestaurantNotFound() {
            // Given
            Long restaurantId = 999L;
            AvailabilityQuery query = new AvailabilityQuery(
                    restaurantId,
                    LocalDate.now().plusDays(1),
                    LocalTime.of(19, 0),
                    LocalTime.of(21, 0),
                    4
            );

            when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> useCase.findAvailableTables(query))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Restaurant not found");

            verify(restaurantRepository).findById(restaurantId);
            verify(availabilityService, never()).findAvailableTables(any(), anyInt(), any(), any(), any());
        }

        @Test
        @DisplayName("Should return empty list when no tables available")
        void shouldReturnEmptyListWhenNoTablesAvailable() {
            // Given
            Long restaurantId = 1L;
            AvailabilityQuery query = new AvailabilityQuery(
                    restaurantId,
                    LocalDate.now().plusDays(1),
                    LocalTime.of(19, 0),
                    LocalTime.of(21, 0),
                    8 // Large party size
            );

            Restaurant restaurant = createRestaurant(restaurantId, "Test Bistro");

            when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
            when(availabilityService.findAvailableTables(any(), anyInt(), any(), any(), any()))
                    .thenReturn(List.of()); // Return empty list instead of throwing

            // When
            List<RestaurantTable> result = useCase.findAvailableTables(query);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Find Best Available Table")
    class FindBestAvailableTable {

        @Test
        @DisplayName("Should find best available table")
        void shouldFindBestAvailableTable() {
            // Given
            Long restaurantId = 1L;
            AvailabilityQuery query = new AvailabilityQuery(
                    restaurantId,
                    LocalDate.now().plusDays(1),
                    LocalTime.of(19, 0),
                    LocalTime.of(21, 0),
                    4
            );

            Restaurant restaurant = createRestaurant(restaurantId, "Test Bistro");
            RestaurantTable bestTable = createTable(1L, 4, TableLocation.WINDOW); // Smallest suitable

            when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
            when(availabilityService.findBestTable(restaurant, 4, query.date(), query.startTime(), query.endTime()))
                    .thenReturn(Optional.of(bestTable));

            // When
            RestaurantTable result = useCase.findBestAvailableTable(query);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(bestTable);
            assertThat(result.getSeats()).isEqualTo(4);

            verify(availabilityService).findBestTable(restaurant, 4, query.date(), query.startTime(), query.endTime());
        }

        @Test
        @DisplayName("Should return null when no suitable table found")
        void shouldReturnNullWhenNoSuitableTableFound() {
            // Given
            Long restaurantId = 1L;
            AvailabilityQuery query = new AvailabilityQuery(
                    restaurantId,
                    LocalDate.now().plusDays(1),
                    LocalTime.of(19, 0),
                    LocalTime.of(21, 0),
                    10 // Too large
            );

            Restaurant restaurant = createRestaurant(restaurantId, "Test Bistro");

            when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
            when(availabilityService.findBestTable(any(), anyInt(), any(), any(), any()))
                    .thenReturn(Optional.empty());

            // When
            RestaurantTable result = useCase.findBestAvailableTable(query);

            // Then
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("Create Reservation")
    class CreateReservation {

        @Test
        @DisplayName("Should create reservation with valid command")
        void shouldCreateReservationWithValidCommand() {
            // Given
            Long tableId = 1L;
            LocalDate date = LocalDate.now().plusDays(1);
            LocalTime startTime = LocalTime.of(19, 0);
            LocalTime endTime = LocalTime.of(21, 0);
            Integer partySize = 4;

            CreateReservationCommand command = new CreateReservationCommand(
                    tableId, date, startTime, endTime, partySize
            );

            RestaurantTable table = createTable(tableId, 4, TableLocation.WINDOW);
            Restaurant restaurant = createRestaurant(1L, "Test Bistro");
            table.setRestaurant(restaurant);

            TimeSlot createdTimeSlot = new TimeSlot(date, startTime, endTime, partySize);
            createdTimeSlot.setId(1L);

            when(tableRepository.findById(tableId)).thenReturn(Optional.of(table));
            when(timeSlotRepository.save(any(TimeSlot.class))).thenReturn(createdTimeSlot);

            // When
            TimeSlot result = useCase.createReservation(command);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getDate()).isEqualTo(date);
            assertThat(result.getStartTime()).isEqualTo(startTime);
            assertThat(result.getEndTime()).isEqualTo(endTime);
            assertThat(result.getReservedSeats()).isEqualTo(partySize);
            assertThat(result.getStatus()).isEqualTo(TimeSlotStatus.AVAILABLE);

            verify(tableRepository).findById(tableId);
            verify(availabilityService).validateReservationConstraints(
                    restaurant, table, partySize, date, startTime, endTime
            );
            verify(timeSlotRepository).save(any(TimeSlot.class));
            verify(eventPublisher).publishEvent(any(EventPublisherPort.ReservationCreatedEvent.class));
        }

        @Test
        @DisplayName("Should throw exception when table not found")
        void shouldThrowExceptionWhenTableNotFound() {
            // Given
            Long tableId = 999L;
            CreateReservationCommand command = new CreateReservationCommand(
                    tableId,
                    LocalDate.now().plusDays(1),
                    LocalTime.of(19, 0),
                    LocalTime.of(21, 0),
                    4
            );

            when(tableRepository.findById(tableId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> useCase.createReservation(command))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Table not found");

            verify(tableRepository).findById(tableId);
            verify(timeSlotRepository, never()).save(any());
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("Should throw exception when reservation constraints not met")
        void shouldThrowExceptionWhenReservationConstraintsNotMet() {
            // Given
            Long tableId = 1L;
            CreateReservationCommand command = new CreateReservationCommand(
                    tableId,
                    LocalDate.now().plusDays(1),
                    LocalTime.of(19, 0),
                    LocalTime.of(21, 0),
                    6 // Exceeds table capacity
            );

            RestaurantTable table = createTable(tableId, 4, TableLocation.WINDOW);
            Restaurant restaurant = createRestaurant(1L, "Test Bistro");
            table.setRestaurant(restaurant);

            when(tableRepository.findById(tableId)).thenReturn(Optional.of(table));
            doThrow(new IllegalArgumentException("Table capacity insufficient"))
                    .when(availabilityService)
                    .validateReservationConstraints(any(), any(), anyInt(), any(), any(), any());

            // When & Then
            assertThatThrownBy(() -> useCase.createReservation(command))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Table capacity insufficient");

            verify(timeSlotRepository, never()).save(any());
            verify(eventPublisher, never()).publishEvent(any());
        }
    }

    @Nested
    @DisplayName("Reservation Status Management")
    class ReservationStatusManagement {

        @Test
        @DisplayName("Should confirm reservation")
        void shouldConfirmReservation() {
            // Given
            Long timeSlotId = 1L;
            TimeSlot timeSlot = new TimeSlot(
                    LocalDate.now().plusDays(1),
                    LocalTime.of(19, 0),
                    LocalTime.of(21, 0),
                    4
            );
            timeSlot.setId(timeSlotId);

            when(timeSlotRepository.findById(timeSlotId)).thenReturn(Optional.of(timeSlot));
            when(timeSlotRepository.save(timeSlot)).thenReturn(timeSlot);

            // When
            TimeSlot result = useCase.confirmReservation(timeSlotId);

            // Then
            assertThat(result.getStatus()).isEqualTo(TimeSlotStatus.CONFIRMED);

            verify(timeSlotRepository).findById(timeSlotId);
            verify(timeSlotRepository).save(timeSlot);
            verify(eventPublisher).publishEvent(any(EventPublisherPort.ReservationStatusChangedEvent.class));
        }

        @Test
        @DisplayName("Should cancel reservation")
        void shouldCancelReservation() {
            // Given
            Long timeSlotId = 1L;
            TimeSlot timeSlot = new TimeSlot(
                    LocalDate.now().plusDays(1),
                    LocalTime.of(19, 0),
                    LocalTime.of(21, 0),
                    4
            );
            timeSlot.setId(timeSlotId);
            timeSlot.confirm(); // First confirm it

            when(timeSlotRepository.findById(timeSlotId)).thenReturn(Optional.of(timeSlot));
            when(timeSlotRepository.save(timeSlot)).thenReturn(timeSlot);

            // When
            TimeSlot result = useCase.cancelReservation(timeSlotId);

            // Then
            assertThat(result.getStatus()).isEqualTo(TimeSlotStatus.CANCELLED);

            verify(timeSlotRepository).findById(timeSlotId);
            verify(timeSlotRepository).save(timeSlot);
            verify(eventPublisher).publishEvent(any(EventPublisherPort.ReservationStatusChangedEvent.class));
        }

        @Test
        @DisplayName("Should complete reservation")
        void shouldCompleteReservation() {
            // Given
            Long timeSlotId = 1L;
            TimeSlot timeSlot = new TimeSlot(
                    LocalDate.now().plusDays(1),
                    LocalTime.of(19, 0),
                    LocalTime.of(21, 0),
                    4
            );
            timeSlot.setId(timeSlotId);
            timeSlot.confirm(); // First confirm it

            when(timeSlotRepository.findById(timeSlotId)).thenReturn(Optional.of(timeSlot));
            when(timeSlotRepository.save(timeSlot)).thenReturn(timeSlot);

            // When
            TimeSlot result = useCase.completeReservation(timeSlotId);

            // Then
            assertThat(result.getStatus()).isEqualTo(TimeSlotStatus.COMPLETED);

            verify(timeSlotRepository).findById(timeSlotId);
            verify(timeSlotRepository).save(timeSlot);
            verify(eventPublisher).publishEvent(any(EventPublisherPort.ReservationStatusChangedEvent.class));
        }

        @Test
        @DisplayName("Should throw exception when time slot not found")
        void shouldThrowExceptionWhenTimeSlotNotFound() {
            // Given
            Long timeSlotId = 999L;

            when(timeSlotRepository.findById(timeSlotId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> useCase.confirmReservation(timeSlotId))
                    .isInstanceOf(TimeSlotNotFoundException.class)
                    .hasMessageContaining("Time slot not found with ID: 999");

            verify(timeSlotRepository).findById(timeSlotId);
            verify(timeSlotRepository, never()).save(any());
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("Should throw exception for invalid status transitions")
        void shouldThrowExceptionForInvalidStatusTransitions() {
            // Given
            Long timeSlotId = 1L;
            TimeSlot timeSlot = new TimeSlot(
                    LocalDate.now().plusDays(1),
                    LocalTime.of(19, 0),
                    LocalTime.of(21, 0),
                    4
            );
            timeSlot.setId(timeSlotId);
            timeSlot.confirm();
            timeSlot.complete(); // Already completed

            when(timeSlotRepository.findById(timeSlotId)).thenReturn(Optional.of(timeSlot));

            // When & Then
            assertThatThrownBy(() -> useCase.cancelReservation(timeSlotId))
                    .isInstanceOf(InvalidReservationStateException.class)
                    .hasMessageContaining("Cannot cancel completed time slot");

            verify(timeSlotRepository, never()).save(any());
            verify(eventPublisher, never()).publishEvent(any());
        }
    }

    @Nested
    @DisplayName("Get Reservations")
    class GetReservations {

        @Test
        @DisplayName("Should get reservations for date")
        void shouldGetReservationsForDate() {
            // Given
            Long restaurantId = 1L;
            LocalDate date = LocalDate.now().plusDays(1);
            List<TimeSlot> reservations = List.of(
                    createTimeSlot(1L, date, LocalTime.of(19, 0), LocalTime.of(21, 0)),
                    createTimeSlot(2L, date, LocalTime.of(21, 30), LocalTime.of(23, 0))
            );

            when(timeSlotRepository.findByRestaurantIdAndDate(restaurantId, date)).thenReturn(reservations);

            // When
            List<TimeSlot> result = useCase.getReservationsForDate(restaurantId, date);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).containsAll(reservations);

            verify(timeSlotRepository).findByRestaurantIdAndDate(restaurantId, date);
        }

        @Test
        @DisplayName("Should get reservations by status")
        void shouldGetReservationsByStatus() {
            // Given
            Long restaurantId = 1L;
            TimeSlotStatus status = TimeSlotStatus.CONFIRMED;
            List<TimeSlot> confirmedReservations = List.of(
                    createTimeSlot(1L, LocalDate.now().plusDays(1), LocalTime.of(19, 0), LocalTime.of(21, 0))
            );
            confirmedReservations.forEach(TimeSlot::confirm);

            when(timeSlotRepository.findByRestaurantIdAndStatus(restaurantId, status))
                    .thenReturn(confirmedReservations);

            // When
            List<TimeSlot> result = useCase.getReservationsByStatus(restaurantId, status);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStatus()).isEqualTo(TimeSlotStatus.CONFIRMED);

            verify(timeSlotRepository).findByRestaurantIdAndStatus(restaurantId, status);
        }

        @Test
        @DisplayName("Should get upcoming reservations")
        void shouldGetUpcomingReservations() {
            // Given
            Long restaurantId = 1L;
            LocalDate today = LocalDate.now();
            List<TimeSlot> upcomingReservations = List.of(
                    createTimeSlot(1L, today.plusDays(1), LocalTime.of(19, 0), LocalTime.of(21, 0)),
                    createTimeSlot(2L, today.plusDays(2), LocalTime.of(20, 0), LocalTime.of(22, 0))
            );

            when(timeSlotRepository.findUpcomingByRestaurantId(restaurantId, today))
                    .thenReturn(upcomingReservations);

            // When
            List<TimeSlot> result = useCase.getUpcomingReservations(restaurantId);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).allMatch(slot -> !slot.getDate().isBefore(today));

            verify(timeSlotRepository).findUpcomingByRestaurantId(restaurantId, today);
        }
    }

    @Nested
    @DisplayName("Calculate Statistics")
    class CalculateStatistics {

        @Test
        @DisplayName("Should calculate availability rate")
        void shouldCalculateAvailabilityRate() {
            // Given
            Long restaurantId = 1L;
            LocalDate date = LocalDate.now().plusDays(1);
            Restaurant restaurant = createRestaurant(restaurantId, "Test Bistro");
            double expectedRate = 0.75; // 75% availability

            when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
            when(availabilityService.calculateAvailabilityRate(restaurant, date)).thenReturn(expectedRate);

            // When
            double result = useCase.calculateAvailabilityRate(restaurantId, date);

            // Then
            assertThat(result).isEqualTo(expectedRate);

            verify(restaurantRepository).findById(restaurantId);
            verify(availabilityService).calculateAvailabilityRate(restaurant, date);
        }

        @Test
        @DisplayName("Should calculate utilization rate")
        void shouldCalculateUtilizationRate() {
            // Given
            Long restaurantId = 1L;
            LocalDate date = LocalDate.now().plusDays(1);
            LocalTime time = LocalTime.of(20, 0);
            Restaurant restaurant = createRestaurant(restaurantId, "Test Bistro");
            double expectedRate = 0.60; // 60% utilization

            when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
            when(availabilityService.calculateUtilizationRate(restaurant, date, time)).thenReturn(expectedRate);

            // When
            double result = useCase.calculateUtilizationRate(restaurantId, date, time);

            // Then
            assertThat(result).isEqualTo(expectedRate);

            verify(restaurantRepository).findById(restaurantId);
            verify(availabilityService).calculateUtilizationRate(restaurant, date, time);
        }

        @Test
        @DisplayName("Should get capacity stats")
        void shouldGetCapacityStats() {
            // Given
            Long restaurantId = 1L;
            Restaurant restaurant = createRestaurant(restaurantId, "Test Bistro");
            restaurant.addTable(new RestaurantTable(4, TableLocation.WINDOW));
            restaurant.addTable(new RestaurantTable(6, TableLocation.TERRACE));

            when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));

            // When
            CapacityStats result = useCase.getCapacityStats(restaurantId);

            // Then
            assertThat(result.restaurantId()).isEqualTo(restaurantId);
            assertThat(result.totalSeats()).isEqualTo(10);
            assertThat(result.availableSeats()).isEqualTo(10);
            assertThat(result.totalTables()).isEqualTo(2);
            assertThat(result.availableTables()).isEqualTo(2);
            assertThat(result.availabilityRate()).isEqualTo(1.0);

            verify(restaurantRepository).findById(restaurantId);
        }
    }

    @Nested
    @DisplayName("Edge Cases and Notifications")
    class EdgeCasesAndNotifications {

        @Test
        @DisplayName("Should send capacity alert when utilization high")
        void shouldSendCapacityAlertWhenUtilizationHigh() {
            // Given
            Long restaurantId = 1L;
            LocalDate date = LocalDate.now().plusDays(1);
            LocalTime time = LocalTime.of(20, 0);
            Restaurant restaurant = createRestaurant(restaurantId, "Test Bistro");
            double highUtilization = 0.90; // 90% utilization

            when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
            when(availabilityService.calculateUtilizationRate(restaurant, date, time))
                    .thenReturn(highUtilization);

            // When
            double result = useCase.calculateUtilizationRate(restaurantId, date, time);

            // Then
            assertThat(result).isEqualTo(highUtilization);

            // Should send capacity alert notification
            verify(notificationPort).sendNotification(any(NotificationPort.CapacityAlertNotification.class));
        }

        @Test
        @DisplayName("Should handle no tables available scenario gracefully")
        void shouldHandleNoTablesAvailableScenarioGracefully() {
            // Given
            Long restaurantId = 1L;
            AvailabilityQuery query = new AvailabilityQuery(
                    restaurantId,
                    LocalDate.now().plusDays(1),
                    LocalTime.of(19, 0),
                    LocalTime.of(21, 0),
                    4
            );

            Restaurant restaurant = createRestaurant(restaurantId, "Test Bistro");

            when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
            when(availabilityService.findAvailableTables(any(), anyInt(), any(), any(), any()))
                    .thenThrow(new NoTablesAvailableException(restaurantId, query.date(), query.startTime(), query.endTime()));

            // When & Then
            assertThatThrownBy(() -> useCase.findAvailableTables(query))
                    .isInstanceOf(NoTablesAvailableException.class)
                    .hasMessageContaining("No tables available at restaurant");
        }
    }

    // Helper methods
    private Restaurant createRestaurant(Long id, String name) {
        Restaurant restaurant = new Restaurant(
                name,
                "123 Test Street, Paris",
                "+33 1 42 86 87 88",
                "test@test.com",
                50
        );
        restaurant.setId(id);
        return restaurant;
    }

    private RestaurantTable createTable(Long id, int seats, TableLocation location) {
        RestaurantTable table = new RestaurantTable(seats, location);
        table.setId(id);
        return table;
    }

    private TimeSlot createTimeSlot(Long id, LocalDate date, LocalTime startTime, LocalTime endTime) {
        TimeSlot timeSlot = new TimeSlot(date, startTime, endTime, 4);
        timeSlot.setId(id);
        return timeSlot;
    }
}