package com.restaurant.service.restaurant.domain.service;

import com.restaurant.service.restaurant.domain.model.Restaurant;
import com.restaurant.service.restaurant.domain.model.RestaurantTable;
import com.restaurant.service.restaurant.domain.model.TableLocation;
import com.restaurant.service.restaurant.domain.model.TimeSlot;
import com.restaurant.service.restaurant.domain.port.in.TableManagementUseCase;
import com.restaurant.service.restaurant.domain.port.in.TableManagementUseCase.*;
import com.restaurant.service.restaurant.domain.port.out.EventPublisherPort;
import com.restaurant.service.restaurant.domain.port.out.RestaurantRepositoryPort;
import com.restaurant.service.restaurant.domain.port.out.RestaurantTableRepositoryPort;
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
 * Unit tests for TableManagementUseCase implementation
 * Following TDD approach - testing the business logic before implementation
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Table Management Use Case Tests")
class TableManagementUseCaseTest {

    @Mock
    private RestaurantTableRepositoryPort tableRepository;

    @Mock
    private RestaurantRepositoryPort restaurantRepository;

    @Mock
    private EventPublisherPort eventPublisher;

    private TableManagementUseCase useCase;

    @BeforeEach
    void setUp() {
        // Will be implemented in step 7
        useCase = new TableManagementUseCaseImpl(tableRepository, restaurantRepository, eventPublisher);
    }

    @Nested
    @DisplayName("Create Table")
    class CreateTable {

        @Test
        @DisplayName("Should create table with valid command")
        void shouldCreateTableWithValidCommand() {
            // Given
            Long restaurantId = 1L;
            CreateTableCommand command = new CreateTableCommand(
                    restaurantId,
                    4,
                    TableLocation.WINDOW
            );

            Restaurant restaurant = createRestaurant(restaurantId, "Test Bistro");
            RestaurantTable savedTable = new RestaurantTable(command.seats(), command.location());
            savedTable.setId(1L);

            when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
            when(tableRepository.save(any(RestaurantTable.class))).thenReturn(savedTable);

            // When
            RestaurantTable result = useCase.createTable(command);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getSeats()).isEqualTo(command.seats());
            assertThat(result.getLocation()).isEqualTo(command.location());
            assertThat(result.isAvailable()).isTrue();

            verify(restaurantRepository).findById(restaurantId);
            verify(tableRepository).save(any(RestaurantTable.class));
            verify(eventPublisher).publishEvent(any(EventPublisherPort.TableAddedEvent.class));
        }

        @Test
        @DisplayName("Should throw exception when restaurant not found")
        void shouldThrowExceptionWhenRestaurantNotFound() {
            // Given
            Long restaurantId = 999L;
            CreateTableCommand command = new CreateTableCommand(
                    restaurantId,
                    4,
                    TableLocation.WINDOW
            );

            when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> useCase.createTable(command))
                    .isInstanceOf(RuntimeException.class) // Will be RestaurantNotFoundException
                    .hasMessageContaining("Restaurant not found");

            verify(restaurantRepository).findById(restaurantId);
            verify(tableRepository, never()).save(any());
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("Should throw exception for invalid table configuration")
        void shouldThrowExceptionForInvalidTableConfiguration() {
            // Given
            Long restaurantId = 1L;
            CreateTableCommand command = new CreateTableCommand(
                    restaurantId,
                    0, // Invalid seats count
                    TableLocation.WINDOW
            );

            Restaurant restaurant = createRestaurant(restaurantId, "Test Bistro");
            //when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));

            // When & Then
            assertThatThrownBy(() -> useCase.createTable(command))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Table seats must be positive");

            //verify(restaurantRepository).findById(restaurantId); // This should be called now
            verify(tableRepository, never()).save(any());
            verify(eventPublisher, never()).publishEvent(any());
        }
    }

    @Nested
    @DisplayName("Update Table")
    class UpdateTable {

        @Test
        @DisplayName("Should update existing table")
        void shouldUpdateExistingTable() {
            // Given
            Long tableId = 1L;
            UpdateTableCommand command = new UpdateTableCommand(
                    tableId,
                    6, // Updated seats
                    TableLocation.TERRACE // Updated location
            );

            RestaurantTable existingTable = new RestaurantTable(4, TableLocation.WINDOW);
            existingTable.setId(tableId);

            when(tableRepository.findById(tableId)).thenReturn(Optional.of(existingTable));
            when(tableRepository.save(any(RestaurantTable.class))).thenReturn(existingTable);

            // When
            RestaurantTable result = useCase.updateTable(command);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getSeats()).isEqualTo(command.seats());
            assertThat(result.getLocation()).isEqualTo(command.location());

            verify(tableRepository).findById(tableId);
            verify(tableRepository).save(existingTable);
        }

        @Test
        @DisplayName("Should throw exception when table not found")
        void shouldThrowExceptionWhenTableNotFound() {
            // Given
            Long tableId = 999L;
            UpdateTableCommand command = new UpdateTableCommand(
                    tableId,
                    6,
                    TableLocation.TERRACE
            );

            when(tableRepository.findById(tableId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> useCase.updateTable(command))
                    .isInstanceOf(TableNotFoundException.class)
                    .hasMessageContaining("Table not found with ID: 999");

            verify(tableRepository).findById(tableId);
            verify(tableRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Get Table")
    class GetTable {

        @Test
        @DisplayName("Should return table when found")
        void shouldReturnTableWhenFound() {
            // Given
            Long tableId = 1L;
            RestaurantTable table = new RestaurantTable(4, TableLocation.WINDOW);
            table.setId(tableId);

            when(tableRepository.findById(tableId)).thenReturn(Optional.of(table));

            // When
            RestaurantTable result = useCase.getTable(tableId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(tableId);
            assertThat(result.getSeats()).isEqualTo(4);
            assertThat(result.getLocation()).isEqualTo(TableLocation.WINDOW);

            verify(tableRepository).findById(tableId);
        }

        @Test
        @DisplayName("Should throw exception when table not found")
        void shouldThrowExceptionWhenTableNotFound() {
            // Given
            Long tableId = 999L;

            when(tableRepository.findById(tableId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> useCase.getTable(tableId))
                    .isInstanceOf(TableNotFoundException.class)
                    .hasMessageContaining("Table not found with ID: 999");

            verify(tableRepository).findById(tableId);
        }
    }

    @Nested
    @DisplayName("List Tables")
    class ListTables {

        @Test
        @DisplayName("Should return all tables for restaurant")
        void shouldReturnAllTablesForRestaurant() {
            // Given
            Long restaurantId = 1L;
            List<RestaurantTable> tables = List.of(
                    createTable(1L, 4, TableLocation.WINDOW),
                    createTable(2L, 6, TableLocation.TERRACE)
            );

            when(tableRepository.findByRestaurantId(restaurantId)).thenReturn(tables);

            // When
            List<RestaurantTable> result = useCase.getRestaurantTables(restaurantId);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getSeats()).isEqualTo(4);
            assertThat(result.get(1).getSeats()).isEqualTo(6);

            verify(tableRepository).findByRestaurantId(restaurantId);
        }

        @Test
        @DisplayName("Should return only available tables")
        void shouldReturnOnlyAvailableTables() {
            // Given
            Long restaurantId = 1L;
            List<RestaurantTable> availableTables = List.of(
                    createTable(1L, 4, TableLocation.WINDOW),
                    createTable(3L, 2, TableLocation.INDOOR)
            );

            when(tableRepository.findAvailableByRestaurantId(restaurantId)).thenReturn(availableTables);

            // When
            List<RestaurantTable> result = useCase.getAvailableTables(restaurantId);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).allMatch(RestaurantTable::isAvailable);

            verify(tableRepository).findAvailableByRestaurantId(restaurantId);
        }

        @Test
        @DisplayName("Should return tables by location")
        void shouldReturnTablesByLocation() {
            // Given
            Long restaurantId = 1L;
            TableLocation location = TableLocation.TERRACE;
            List<RestaurantTable> terraceTables = List.of(
                    createTable(2L, 6, TableLocation.TERRACE),
                    createTable(4L, 4, TableLocation.TERRACE)
            );

            when(tableRepository.findByRestaurantIdAndLocation(restaurantId, location)).thenReturn(terraceTables);

            // When
            List<RestaurantTable> result = useCase.getTablesByLocation(restaurantId, location);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).allMatch(table -> table.getLocation() == TableLocation.TERRACE);

            verify(tableRepository).findByRestaurantIdAndLocation(restaurantId, location);
        }

        @Test
        @DisplayName("Should return tables that accommodate party size")
        void shouldReturnTablesThatAccommodatePartySize() {
            // Given
            Long restaurantId = 1L;
            Integer partySize = 4;
            List<RestaurantTable> suitableTables = List.of(
                    createTable(1L, 4, TableLocation.WINDOW),
                    createTable(2L, 6, TableLocation.TERRACE)
            );

            when(tableRepository.findByRestaurantIdAndSeatsGreaterThanEqual(restaurantId, partySize))
                    .thenReturn(suitableTables);

            // When
            List<RestaurantTable> result = useCase.getTablesByPartySize(restaurantId, partySize);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).allMatch(table -> table.getSeats() >= partySize);

            verify(tableRepository).findByRestaurantIdAndSeatsGreaterThanEqual(restaurantId, partySize);
        }
    }

    @Nested
    @DisplayName("Table Availability Management")
    class TableAvailabilityManagement {

        @Test
        @DisplayName("Should make table available")
        void shouldMakeTableAvailable() {
            // Given
            Long tableId = 1L;
            RestaurantTable table = createTable(tableId, 4, TableLocation.WINDOW);
            table.makeUnavailable();

            when(tableRepository.findById(tableId)).thenReturn(Optional.of(table));
            when(tableRepository.save(table)).thenReturn(table);

            // When
            RestaurantTable result = useCase.makeTableAvailable(tableId);

            // Then
            assertThat(result.isAvailable()).isTrue();

            verify(tableRepository).findById(tableId);
            verify(tableRepository).save(table);
            verify(eventPublisher).publishEvent(any(EventPublisherPort.TableAvailabilityChangedEvent.class));
        }

        @Test
        @DisplayName("Should make table unavailable")
        void shouldMakeTableUnavailable() {
            // Given
            Long tableId = 1L;
            RestaurantTable table = createTable(tableId, 4, TableLocation.WINDOW);

            when(tableRepository.findById(tableId)).thenReturn(Optional.of(table));
            when(tableRepository.save(table)).thenReturn(table);

            // When
            RestaurantTable result = useCase.makeTableUnavailable(tableId);

            // Then
            assertThat(result.isAvailable()).isFalse();

            verify(tableRepository).findById(tableId);
            verify(tableRepository).save(table);
            verify(eventPublisher).publishEvent(any(EventPublisherPort.TableAvailabilityChangedEvent.class));
        }

        @Test
        @DisplayName("Should throw exception when table not found for availability change")
        void shouldThrowExceptionWhenTableNotFoundForAvailabilityChange() {
            // Given
            Long tableId = 999L;

            when(tableRepository.findById(tableId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> useCase.makeTableAvailable(tableId))
                    .isInstanceOf(TableNotFoundException.class);

            verify(tableRepository).findById(tableId);
            verify(tableRepository, never()).save(any());
            verify(eventPublisher, never()).publishEvent(any());
        }
    }

    @Nested
    @DisplayName("Delete Table")
    class DeleteTable {

        @Test
        @DisplayName("Should delete table when exists and no active reservations")
        void shouldDeleteTableWhenExistsAndNoActiveReservations() {
            // Given
            Long tableId = 1L;
            RestaurantTable table = createTable(tableId, 4, TableLocation.WINDOW);

            when(tableRepository.findById(tableId)).thenReturn(Optional.of(table));

            // When
            useCase.deleteTable(tableId);

            // Then
            verify(tableRepository).findById(tableId);
            verify(tableRepository).deleteById(tableId);
        }

        @Test
        @DisplayName("Should throw exception when table not found for deletion")
        void shouldThrowExceptionWhenTableNotFoundForDeletion() {
            // Given
            Long tableId = 999L;

            when(tableRepository.findById(tableId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> useCase.deleteTable(tableId))
                    .isInstanceOf(TableNotFoundException.class);

            verify(tableRepository).findById(tableId);
            verify(tableRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("Should not delete table with active reservations")
        void shouldNotDeleteTableWithActiveReservations() {
            // Given
            Long tableId = 1L;
            RestaurantTable table = createTable(tableId, 4, TableLocation.WINDOW);

            // Add a time slot to simulate active reservations
            LocalDate futureDate = LocalDate.now().plusDays(1);
            TimeSlot activeTimeSlot = new TimeSlot(futureDate, LocalTime.of(19, 0), LocalTime.of(21, 0), 4);
            table.addTimeSlot(activeTimeSlot);

            when(tableRepository.findById(tableId)).thenReturn(Optional.of(table));

            // When & Then
            assertThatThrownBy(() -> useCase.deleteTable(tableId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Cannot delete table with active reservations");

            verify(tableRepository).findById(tableId);
            verify(tableRepository, never()).deleteById(any());
        }
    }

    @Nested
    @DisplayName("Table Utilization Statistics")
    class TableUtilizationStatistics {

        @Test
        @DisplayName("Should get table utilization statistics")
        void shouldGetTableUtilizationStatistics() {
            // Given
            Long restaurantId = 1L;
            List<RestaurantTable> allTables = List.of(
                    createTable(1L, 4, TableLocation.WINDOW),
                    createTable(2L, 6, TableLocation.TERRACE),
                    createTable(3L, 2, TableLocation.INDOOR)
            );

            List<RestaurantTable> availableTables = List.of(
                    createTable(1L, 4, TableLocation.WINDOW),
                    createTable(3L, 2, TableLocation.INDOOR)
            );

            when(tableRepository.findByRestaurantId(restaurantId)).thenReturn(allTables);
            when(tableRepository.findAvailableByRestaurantId(restaurantId)).thenReturn(availableTables);

            // When
            TableUtilizationStats result = useCase.getTableUtilization(restaurantId);

            // Then
            assertThat(result.restaurantId()).isEqualTo(restaurantId);
            assertThat(result.totalTables()).isEqualTo(3);
            assertThat(result.availableTables()).isEqualTo(2);
            assertThat(result.unavailableTables()).isEqualTo(1);
            assertThat(result.totalSeats()).isEqualTo(12); // 4 + 6 + 2
            assertThat(result.availabilityRate()).isEqualTo(2.0 / 3.0); // 2 available out of 3

            verify(tableRepository).findByRestaurantId(restaurantId);
            verify(tableRepository).findAvailableByRestaurantId(restaurantId);
        }

        @Test
        @DisplayName("Should return zero stats when no tables")
        void shouldReturnZeroStatsWhenNoTables() {
            // Given
            Long restaurantId = 1L;

            when(tableRepository.findByRestaurantId(restaurantId)).thenReturn(List.of());
            when(tableRepository.findAvailableByRestaurantId(restaurantId)).thenReturn(List.of());

            // When
            TableUtilizationStats result = useCase.getTableUtilization(restaurantId);

            // Then
            assertThat(result.restaurantId()).isEqualTo(restaurantId);
            assertThat(result.totalTables()).isZero();
            assertThat(result.availableTables()).isZero();
            assertThat(result.unavailableTables()).isZero();
            assertThat(result.totalSeats()).isZero();
            assertThat(result.availabilityRate()).isZero();

            verify(tableRepository).findByRestaurantId(restaurantId);
            verify(tableRepository).findAvailableByRestaurantId(restaurantId);
        }
    }

    @Nested
    @DisplayName("Edge Cases and Validation")
    class EdgeCasesAndValidation {

        @Test
        @DisplayName("Should validate table capacity constraints")
        void shouldValidateTableCapacityConstraints() {
            // Test the actual use case validation since record constructor validation isn't working as expected
            // This tests the business logic in the use case implementation

            Long restaurantId = 1L;
            Restaurant restaurant = createRestaurant(restaurantId, "Test Bistro");
            //when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));

            // Test too small (0 seats) - this should be caught by use case validation
            CreateTableCommand commandTooSmall = new CreateTableCommand(restaurantId, 0, TableLocation.WINDOW);
            assertThatThrownBy(() -> useCase.createTable(commandTooSmall))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Table seats must be positive");

            // Test too large (10 seats > 8 max)
            CreateTableCommand commandTooLarge = new CreateTableCommand(restaurantId, 10, TableLocation.WINDOW);
            assertThatThrownBy(() -> useCase.createTable(commandTooLarge))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Table seats cannot exceed 8 seats");

            // Test null restaurant ID
            assertThatThrownBy(() -> useCase.createTable(new CreateTableCommand(null, 4, TableLocation.WINDOW)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Restaurant ID cannot be null");

            // Test null location
            CreateTableCommand commandNullLocation = new CreateTableCommand(restaurantId, 4, null);
            assertThatThrownBy(() -> useCase.createTable(commandNullLocation))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Table location cannot be null");
        }

        @Test
        @DisplayName("Should validate restaurant capacity when adding tables")
        void shouldValidateRestaurantCapacityWhenAddingTables() {
            // Given
            Long restaurantId = 1L;
            Restaurant restaurant = createRestaurant(restaurantId, "Small Bistro");
            restaurant.setCapacity(10); // Small capacity

            // Already has tables that use most capacity
            restaurant.addTable(new RestaurantTable(8, TableLocation.INDOOR));

            CreateTableCommand command = new CreateTableCommand(
                    restaurantId,
                    6, // Would exceed restaurant capacity
                    TableLocation.WINDOW
            );

            when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));

            // When & Then
            // This business rule will be implemented to prevent oversubscription
            assertThatThrownBy(() -> useCase.createTable(command))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Adding table would exceed restaurant capacity");
        }

        @Test
        @DisplayName("Should handle private room special requirements")
        void shouldHandlePrivateRoomSpecialRequirements() {
            // Given
            Long restaurantId = 1L;
            CreateTableCommand privateRoomCommand = new CreateTableCommand(
                    restaurantId,
                    2, // Too small for private room
                    TableLocation.PRIVATE_ROOM
            );

            Restaurant restaurant = createRestaurant(restaurantId, "Test Bistro");
            when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));

            // When & Then
            // Business rule: private rooms should have minimum 4 seats
            assertThatThrownBy(() -> useCase.createTable(privateRoomCommand))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Private room tables must have at least 4 seats");
        }

        @Test
        @DisplayName("Should validate null parameters")
        void shouldValidateNullParameters() {
            // Given & When & Then
            assertThatThrownBy(() -> useCase.getTable(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Table ID cannot be null");

            assertThatThrownBy(() -> useCase.getRestaurantTables(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Restaurant ID cannot be null");

            assertThatThrownBy(() -> useCase.getAvailableTables(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Restaurant ID cannot be null");

            assertThatThrownBy(() -> useCase.getTablesByLocation(null, TableLocation.WINDOW))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Restaurant ID cannot be null");

            assertThatThrownBy(() -> useCase.getTablesByLocation(1L, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Table location cannot be null");

            assertThatThrownBy(() -> useCase.getTablesByPartySize(1L, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Party size must be positive");

            assertThatThrownBy(() -> useCase.getTablesByPartySize(1L, -1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Party size must be positive");
        }

        @Test
        @DisplayName("Should validate command parameters")
        void shouldValidateCommandParameters() {
            // Given & When & Then
            assertThatThrownBy(() -> useCase.createTable(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Create table command cannot be null");

            assertThatThrownBy(() -> useCase.updateTable(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Update table command cannot be null");

            CreateTableCommand invalidCommand = new CreateTableCommand(null, 4, TableLocation.WINDOW);
            assertThatThrownBy(() -> useCase.createTable(invalidCommand))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Restaurant ID cannot be null");
        }

        @Test
        @DisplayName("Should handle weather dependent locations")
        void shouldHandleWeatherDependentLocations() {
            // Given
            Long restaurantId = 1L;
            CreateTableCommand terraceCommand = new CreateTableCommand(
                    restaurantId,
                    4,
                    TableLocation.TERRACE // Weather dependent
            );

            Restaurant restaurant = createRestaurant(restaurantId, "Test Bistro");
            RestaurantTable savedTable = new RestaurantTable(4, TableLocation.TERRACE);
            savedTable.setId(1L);

            when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
            when(tableRepository.save(any(RestaurantTable.class))).thenReturn(savedTable);

            // When
            RestaurantTable result = useCase.createTable(terraceCommand);

            // Then
            assertThat(result.getLocation()).isEqualTo(TableLocation.TERRACE);
            assertThat(result.getLocation().isWeatherDependent()).isTrue();

            verify(restaurantRepository).findById(restaurantId);
            verify(tableRepository).save(any(RestaurantTable.class));
        }

        @Test
        @DisplayName("Should handle table availability transitions properly")
        void shouldHandleTableAvailabilityTransitionsProperly() {
            // Given
            Long tableId = 1L;
            RestaurantTable table = createTable(tableId, 4, TableLocation.WINDOW);

            when(tableRepository.findById(tableId)).thenReturn(Optional.of(table));
            when(tableRepository.save(any(RestaurantTable.class))).thenAnswer(invocation -> {
                RestaurantTable savedTable = invocation.getArgument(0);
                return savedTable; // Return the actual modified table
            });

            // When - make unavailable
            RestaurantTable unavailableResult = useCase.makeTableUnavailable(tableId);

            // Then - verify first transition
            assertThat(unavailableResult.isAvailable()).isFalse();

            // When - make available again
            RestaurantTable availableResult = useCase.makeTableAvailable(tableId);

            // Then - verify second transition
            assertThat(availableResult.isAvailable()).isTrue();

            // Verify interactions
            verify(tableRepository, times(2)).findById(tableId);
            verify(tableRepository, times(2)).save(table);
            verify(eventPublisher, times(2)).publishEvent(any(EventPublisherPort.TableAvailabilityChangedEvent.class));
        }

        @Test
        @DisplayName("Should handle multiple table updates correctly")
        void shouldHandleMultipleTableUpdatesCorrectly() {
            // Given
            Long tableId = 1L;
            RestaurantTable table = createTable(tableId, 4, TableLocation.WINDOW);

            UpdateTableCommand updateSeats = new UpdateTableCommand(tableId, 6, TableLocation.WINDOW);
            UpdateTableCommand updateLocation = new UpdateTableCommand(tableId, 6, TableLocation.TERRACE);

            when(tableRepository.findById(tableId)).thenReturn(Optional.of(table));
            when(tableRepository.save(any(RestaurantTable.class))).thenAnswer(invocation -> {
                RestaurantTable savedTable = invocation.getArgument(0);
                return savedTable; // Return the actual modified table
            });

            // When - perform updates in sequence
            RestaurantTable firstUpdate = useCase.updateTable(updateSeats);
            // The table state is now modified, so subsequent calls work on the updated state
            RestaurantTable secondUpdate = useCase.updateTable(updateLocation);

            // Then - Check final state
            assertThat(firstUpdate.getSeats()).isEqualTo(6);
            assertThat(secondUpdate.getSeats()).isEqualTo(6);
            assertThat(secondUpdate.getLocation()).isEqualTo(TableLocation.TERRACE);

            verify(tableRepository, times(2)).findById(tableId);
            verify(tableRepository, times(2)).save(table);
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
}