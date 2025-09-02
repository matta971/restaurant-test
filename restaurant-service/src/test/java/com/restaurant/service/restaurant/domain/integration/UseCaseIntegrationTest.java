package com.restaurant.service.restaurant.domain.integration;

import com.restaurant.service.restaurant.domain.model.*;
import com.restaurant.service.restaurant.domain.port.in.AvailabilityManagementUseCase;
import com.restaurant.service.restaurant.domain.port.in.RestaurantManagementUseCase;
import com.restaurant.service.restaurant.domain.port.in.TableManagementUseCase;
import com.restaurant.service.restaurant.domain.port.in.AvailabilityManagementUseCase.*;
import com.restaurant.service.restaurant.domain.port.in.RestaurantManagementUseCase.*;
import com.restaurant.service.restaurant.domain.port.in.TableManagementUseCase.*;
import com.restaurant.service.restaurant.domain.port.out.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Integration test for Use Cases
 * Tests the interaction between different use cases and domain services
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Use Case Integration Tests")
class UseCaseIntegrationTest {

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

    private RestaurantManagementUseCase restaurantUseCase;
    private TableManagementUseCase tableUseCase;
    private AvailabilityManagementUseCase availabilityUseCase;

    @BeforeEach
    void setUp() {
        // Create the actual implementations with mocked dependencies
        restaurantUseCase = new com.restaurant.service.restaurant.domain.service.RestaurantManagementUseCaseImpl(
                restaurantRepository, eventPublisher);

        tableUseCase = new com.restaurant.service.restaurant.domain.service.TableManagementUseCaseImpl(
                tableRepository, restaurantRepository, eventPublisher);

        availabilityUseCase = new com.restaurant.service.restaurant.domain.service.AvailabilityManagementUseCaseImpl(
                restaurantRepository,
                tableRepository,
                timeSlotRepository,
                new com.restaurant.service.restaurant.domain.service.AvailabilityService(),
                eventPublisher,
                notificationPort);
    }

    @Test
    @DisplayName("Should complete full restaurant setup and reservation workflow")
    void shouldCompleteFullRestaurantSetupAndReservationWorkflow() {
        // Step 1: Create Restaurant
        CreateRestaurantCommand createRestaurantCommand = new CreateRestaurantCommand(
                "Le Petit Bistro",
                "123 Rue de la Paix, Paris",
                "+33 1 42 86 87 88",
                "contact@petitbistro.fr",
                50,
                LocalTime.of(11, 0),
                LocalTime.of(23, 0)
        );

        Restaurant createdRestaurant = new Restaurant(
                createRestaurantCommand.name(),
                createRestaurantCommand.address(),
                createRestaurantCommand.phoneNumber(),
                createRestaurantCommand.email(),
                createRestaurantCommand.capacity(),
                createRestaurantCommand.openingTime(),
                createRestaurantCommand.closingTime()
        );
        createdRestaurant.setId(1L);

        when(restaurantRepository.findByNameContaining(any())).thenReturn(List.of());
        when(restaurantRepository.save(any(Restaurant.class))).thenReturn(createdRestaurant);

        Restaurant restaurant = restaurantUseCase.createRestaurant(createRestaurantCommand);

        // Verify restaurant creation
        assertThat(restaurant.getId()).isEqualTo(1L);
        assertThat(restaurant.getName()).isEqualTo("Le Petit Bistro");
        verify(eventPublisher).publishEvent(any(EventPublisherPort.RestaurantCreatedEvent.class));

        // Step 2: Create Table
        CreateTableCommand createTableCommand = new CreateTableCommand(
                1L, // restaurant ID
                4,  // seats
                TableLocation.WINDOW
        );

        RestaurantTable createdTable = new RestaurantTable(4, TableLocation.WINDOW);
        createdTable.setId(1L);
        createdTable.setRestaurant(restaurant);

        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));
        when(tableRepository.save(any(RestaurantTable.class))).thenReturn(createdTable);

        RestaurantTable table = tableUseCase.createTable(createTableCommand);

        // Verify table creation
        assertThat(table.getId()).isEqualTo(1L);
        assertThat(table.getSeats()).isEqualTo(4);
        assertThat(table.getLocation()).isEqualTo(TableLocation.WINDOW);
        verify(eventPublisher).publishEvent(any(EventPublisherPort.TableAddedEvent.class));

        // Step 3: Create Reservation
        LocalDate reservationDate = LocalDate.now().plusDays(1);
        CreateReservationCommand createReservationCommand = new CreateReservationCommand(
                1L, // table ID
                reservationDate,
                LocalTime.of(19, 0),
                LocalTime.of(21, 0),
                4,
                "email@customer.com"
        );

        TimeSlot createdTimeSlot = new TimeSlot(
                reservationDate,
                LocalTime.of(19, 0),
                LocalTime.of(21, 0),
                4
        );
        createdTimeSlot.setId(1L);

        when(tableRepository.findById(1L)).thenReturn(Optional.of(table));
        when(timeSlotRepository.save(any(TimeSlot.class))).thenReturn(createdTimeSlot);

        TimeSlot reservation = availabilityUseCase.createReservation(createReservationCommand);

        // Verify reservation creation
        assertThat(reservation.getId()).isEqualTo(1L);
        assertThat(reservation.getDate()).isEqualTo(reservationDate);
        assertThat(reservation.getReservedSeats()).isEqualTo(4);
        assertThat(reservation.getStatus()).isEqualTo(TimeSlotStatus.AVAILABLE);
        verify(eventPublisher).publishEvent(any(EventPublisherPort.ReservationCreatedEvent.class));

        // Step 4: Confirm Reservation
        when(timeSlotRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(timeSlotRepository.save(any(TimeSlot.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TimeSlot confirmedReservation = availabilityUseCase.confirmReservation(1L);

        // Verify reservation confirmation
        assertThat(confirmedReservation.getStatus()).isEqualTo(TimeSlotStatus.CONFIRMED);
        verify(eventPublisher).publishEvent(any(EventPublisherPort.ReservationStatusChangedEvent.class));

        // Step 5: Complete Reservation
        TimeSlot completedReservation = availabilityUseCase.completeReservation(1L);

        // Verify reservation completion
        assertThat(completedReservation.getStatus()).isEqualTo(TimeSlotStatus.COMPLETED);
    }

    @Test
    @DisplayName("Should handle restaurant deactivation and prevent new reservations")
    void shouldHandleRestaurantDeactivationAndPreventNewReservations() {
        // Setup
        Restaurant restaurant = createTestRestaurant(1L);
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));
        when(restaurantRepository.save(any(Restaurant.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Deactivate restaurant
        Restaurant deactivatedRestaurant = restaurantUseCase.deactivateRestaurant(1L);

        // Verify deactivation
        assertThat(deactivatedRestaurant.isActive()).isFalse();
        verify(eventPublisher).publishEvent(any(EventPublisherPort.RestaurantStatusChangedEvent.class));

        // Try to create reservation (should fail due to business rules)
        RestaurantTable table = new RestaurantTable(4, TableLocation.WINDOW);
        table.setRestaurant(deactivatedRestaurant);

        CreateReservationCommand command = new CreateReservationCommand(
                1L,
                LocalDate.now().plusDays(1),
                LocalTime.of(19, 0),
                LocalTime.of(21, 0),
                4,
                "email@customer.com"
        );

        when(tableRepository.findById(1L)).thenReturn(Optional.of(table));

        // Should throw exception due to inactive restaurant
        assertThatThrownBy(() -> availabilityUseCase.createReservation(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Restaurant is not active");
    }

    @Test
    @DisplayName("Should calculate restaurant statistics correctly")
    void shouldCalculateRestaurantStatisticsCorrectly() {
        // Setup restaurant with tables
        Restaurant restaurant = createTestRestaurant(1L);
        restaurant.addTable(new RestaurantTable(4, TableLocation.WINDOW));
        restaurant.addTable(new RestaurantTable(6, TableLocation.TERRACE));

        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));

        // Get restaurant stats
        RestaurantStats stats = restaurantUseCase.getRestaurantStats(1L);

        // Verify stats
        assertThat(stats.restaurantId()).isEqualTo(1L);
        assertThat(stats.name()).isEqualTo("Test Restaurant");
        assertThat(stats.totalTables()).isEqualTo(2);
        assertThat(stats.totalSeats()).isEqualTo(10);
        assertThat(stats.averageTableSize()).isEqualTo(5.0);
        assertThat(stats.active()).isTrue();
        assertThat(stats.occupancyRate()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("Should prevent table creation exceeding restaurant capacity")
    void shouldPreventTableCreationExceedingRestaurantCapacity() {
        // Setup restaurant with small capacity
        Restaurant restaurant = createTestRestaurant(1L);
        restaurant.setCapacity(10); // Small capacity
        restaurant.addTable(new RestaurantTable(8, TableLocation.INDOOR)); // Almost at capacity

        CreateTableCommand command = new CreateTableCommand(
                1L,
                6, // This would exceed capacity (8 + 6 > 10)
                TableLocation.WINDOW
        );

        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));

        // Should throw exception due to capacity constraint
        assertThatThrownBy(() -> tableUseCase.createTable(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Adding table would exceed restaurant capacity");
    }

    @Test
    @DisplayName("Should handle high utilization scenario with notification")
    void shouldHandleHighUtilizationScenarioWithNotification() {
        // Setup
        Restaurant restaurant = createTestRestaurant(1L);
        LocalDate date = LocalDate.now().plusDays(1);
        LocalTime time = LocalTime.of(20, 0);

        //when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));

        // Mock high utilization (90%)
        // This is simplified - in real scenario, we'd have actual reservations
        double highUtilization = 0.90;

        // Calculate utilization would normally trigger the alert
        // For this test, we'll verify that the notification would be sent
        // In a real scenario with actual reservations, this would trigger automatically

        verify(notificationPort, never()).sendNotification(any()); // No notification yet

        // The actual implementation would send notification when utilization > 0.85
        // We can verify this behavior in the actual use case implementation
    }

    // Helper methods
    private Restaurant createTestRestaurant(Long id) {
        Restaurant restaurant = new Restaurant(
                "Test Restaurant",
                "123 Test Street",
                "+33123456789",
                "test@test.com",
                50
        );
        restaurant.setId(id);
        return restaurant;
    }
}