package com.restaurant.service.restaurant.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for RestaurantTable domain entity
 * Testing business rules and invariants following TDD approach
 */
@DisplayName("RestaurantTable Domain Entity Tests")
class RestaurantTableTest {

    @Nested
    @DisplayName("Table Creation")
    class TableCreation {

        @Test
        @DisplayName("Should create table with valid data")
        void shouldCreateTableWithValidData() {
            
            int seats = 4;
            TableLocation location = TableLocation.WINDOW;

            
            RestaurantTable table = new RestaurantTable(seats, location);

            
            assertThat(table.getSeats()).isEqualTo(seats);
            assertThat(table.getLocation()).isEqualTo(location);
            assertThat(table.isAvailable()).isTrue();
            assertThat(table.getRestaurant()).isNull();
            assertThat(table.getTimeSlots()).isNotNull().isEmpty();
            assertThat(table.getId()).isNull(); // Not persisted yet
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, -5})
        @DisplayName("Should throw exception when seats is zero or negative")
        void shouldThrowExceptionWhenSeatsIsInvalid(int invalidSeats) {
            
            TableLocation location = TableLocation.WINDOW;

            
            assertThatThrownBy(() -> new RestaurantTable(invalidSeats, location))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Table seats must be positive");
        }

        @Test
        @DisplayName("Should throw exception when location is null")
        void shouldThrowExceptionWhenLocationIsNull() {
            
            int seats = 4;

            
            assertThatThrownBy(() -> new RestaurantTable(seats, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Table location cannot be null");
        }

        @ParameterizedTest
        @ValueSource(ints = {9, 10, 15, 20})
        @DisplayName("Should throw exception when seats exceeds maximum allowed")
        void shouldThrowExceptionWhenSeatsExceedsMaximum(int tooManySeats) {
            
            TableLocation location = TableLocation.WINDOW;

            
            assertThatThrownBy(() -> new RestaurantTable(tooManySeats, location))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Table seats cannot exceed 8 seats");
        }
    }

    @Nested
    @DisplayName("Table Availability Operations")
    class TableAvailabilityOperations {

        @Test
        @DisplayName("Should make table available")
        void shouldMakeTableAvailable() {
            
            RestaurantTable table = createValidTable();
            table.makeUnavailable();

            
            table.makeAvailable();

            
            assertThat(table.isAvailable()).isTrue();
        }

        @Test
        @DisplayName("Should make table unavailable")
        void shouldMakeTableUnavailable() {
            
            RestaurantTable table = createValidTable();

            
            table.makeUnavailable();

            
            assertThat(table.isAvailable()).isFalse();
        }

        @Test
        @DisplayName("Should check if table is available at specific time")
        void shouldCheckIfTableIsAvailableAtSpecificTime() {
            
            RestaurantTable table = createValidTable();
            LocalDate date = LocalDate.now().plusDays(1); // Future date
            LocalTime startTime = LocalTime.of(19, 0);
            LocalTime endTime = LocalTime.of(21, 0);

            
            boolean isAvailable = table.isAvailableAt(date, startTime, endTime);

            
            assertThat(isAvailable).isTrue();
        }

        @Test
        @DisplayName("Should return false when table is generally unavailable")
        void shouldReturnFalseWhenTableIsGenerallyUnavailable() {
            
            RestaurantTable table = createValidTable();
            table.makeUnavailable();
            LocalDate date = LocalDate.now().plusDays(1); // Future date
            LocalTime startTime = LocalTime.of(19, 0);
            LocalTime endTime = LocalTime.of(21, 0);

            
            boolean isAvailable = table.isAvailableAt(date, startTime, endTime);

            
            assertThat(isAvailable).isFalse();
        }
    }

    @Nested
    @DisplayName("Table Time Slot Management")
    class TableTimeSlotManagement {

        @Test
        @DisplayName("Should add time slot to table")
        void shouldAddTimeSlotToTable() {
            
            RestaurantTable table = createValidTable();
            LocalDate date = LocalDate.now().plusDays(1); // Future date
            LocalTime startTime = LocalTime.of(19, 0);
            LocalTime endTime = LocalTime.of(21, 0);
            TimeSlot timeSlot = new TimeSlot(date, startTime, endTime, 4);

            
            table.addTimeSlot(timeSlot);

            
            assertThat(table.getTimeSlots()).hasSize(1);
            assertThat(table.getTimeSlots()).contains(timeSlot);
            assertThat(timeSlot.getTable()).isEqualTo(table);
        }

        @Test
        @DisplayName("Should not add null time slot")
        void shouldNotAddNullTimeSlot() {
            
            RestaurantTable table = createValidTable();

            
            assertThatThrownBy(() -> table.addTimeSlot(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("TimeSlot cannot be null");
        }

        @Test
        @DisplayName("Should not add overlapping time slot")
        void shouldNotAddOverlappingTimeSlot() {
            
            RestaurantTable table = createValidTable();
            LocalDate date = LocalDate.now().plusDays(1); // Future date

            TimeSlot existingSlot = new TimeSlot(date, LocalTime.of(19, 0), LocalTime.of(21, 0), 4);
            TimeSlot overlappingSlot = new TimeSlot(date, LocalTime.of(20, 0), LocalTime.of(22, 0), 2);

            table.addTimeSlot(existingSlot);

            
            assertThatThrownBy(() -> table.addTimeSlot(overlappingSlot))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Time slot overlaps with existing reservation");
        }

        @Test
        @DisplayName("Should check availability considering existing time slots")
        void shouldCheckAvailabilityConsideringExistingTimeSlots() {
            
            RestaurantTable table = createValidTable();
            LocalDate date = LocalDate.now().plusDays(1); // Future date

            // Add existing time slot (AVAILABLE)
            TimeSlot existingSlot = new TimeSlot(date, LocalTime.of(19, 0), LocalTime.of(21, 0), 4);
            table.addTimeSlot(existingSlot);

            boolean isAvailableOverlapping = table.isAvailableAt(date, LocalTime.of(20, 0), LocalTime.of(22, 0));

            boolean isAvailableNonOverlapping = table.isAvailableAt(date, LocalTime.of(21, 30), LocalTime.of(23, 0));

            
            assertThat(isAvailableOverlapping).isFalse(); // AVAILABLE slots block availability
            assertThat(isAvailableNonOverlapping).isTrue();

            existingSlot.confirm();
            existingSlot.complete();

            boolean isAvailableAfterCompletion = table.isAvailableAt(date, LocalTime.of(20, 0), LocalTime.of(22, 0));
            assertThat(isAvailableAfterCompletion).isTrue(); // COMPLETED slots don't block
        }
    }

    @Nested
    @DisplayName("Table Business Rules")
    class TableBusinessRules {

        @Test
        @DisplayName("Should validate seat capacity for time slot")
        void shouldValidateSeatCapacityForTimeSlot() {
            
            RestaurantTable table = new RestaurantTable(4, TableLocation.WINDOW);
            LocalDate date = LocalDate.now().plusDays(1); // Future date
            LocalTime startTime = LocalTime.of(19, 0);
            LocalTime endTime = LocalTime.of(21, 0);

            //Valid capacity
            assertThatCode(() -> {
                TimeSlot validSlot = new TimeSlot(date, startTime, endTime, 4);
                table.addTimeSlot(validSlot);
            }).doesNotThrowAnyException();

            // Exceeding capacity
            RestaurantTable anotherTable = new RestaurantTable(4, TableLocation.WINDOW);
            assertThatThrownBy(() -> {
                TimeSlot invalidSlot = new TimeSlot(date, startTime, endTime, 5);
                anotherTable.addTimeSlot(invalidSlot);
            }).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Reserved seats cannot exceed table capacity");
        }

        @Test
        @DisplayName("Should generate table number automatically")
        void shouldGenerateTableNumberAutomatically() {
            RestaurantTable table1 = new RestaurantTable(4, TableLocation.WINDOW);
            RestaurantTable table2 = new RestaurantTable(2, TableLocation.TERRACE);

            
            assertThat(table1.getTableNumber()).isNotNull();
            assertThat(table2.getTableNumber()).isNotNull();
            assertThat(table1.getTableNumber()).isNotEqualTo(table2.getTableNumber());
        }
    }

    @Nested
    @DisplayName("Table Equality and Hash Code")
    class TableEqualityAndHashCode {

        @Test
        @DisplayName("Should be equal when same id")
        void shouldBeEqualWhenSameId() {
            
            RestaurantTable table1 = createValidTable();
            RestaurantTable table2 = createValidTable();

            // Simulate persistence
            table1.setId(1L);
            table2.setId(1L);

            
            assertThat(table1).isEqualTo(table2);
            assertThat(table1.hashCode()).isEqualTo(table2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when different ids")
        void shouldNotBeEqualWhenDifferentIds() {
            
            RestaurantTable table1 = createValidTable();
            RestaurantTable table2 = createValidTable();

            // Simulate persistence
            table1.setId(1L);
            table2.setId(2L);

            
            assertThat(table1).isNotEqualTo(table2);
        }
    }

    // Helper method
    private RestaurantTable createValidTable() {
        return new RestaurantTable(4, TableLocation.WINDOW);
    }
}