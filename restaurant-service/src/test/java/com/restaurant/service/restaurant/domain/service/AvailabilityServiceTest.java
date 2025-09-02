package com.restaurant.service.restaurant.domain.service;

import com.restaurant.service.restaurant.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for AvailabilityService domain service
 */
@DisplayName("Availability Service Tests")
class AvailabilityServiceTest {

    private AvailabilityService availabilityService;
    private Restaurant restaurant;
    private RestaurantTable smallTable;
    private RestaurantTable mediumTable;
    private RestaurantTable largeTable;
    private RestaurantTable terraceTable;

    @BeforeEach
    void setUp() {
        availabilityService = new AvailabilityService();

        restaurant = new Restaurant(
                "Le Petit Bistro",
                "123 Rue de la Paix, Paris",
                "+33 1 42 86 87 88",
                "contact@petitbistro.fr",
                50
        );

        smallTable = new RestaurantTable(2, TableLocation.WINDOW);
        mediumTable = new RestaurantTable(4, TableLocation.INDOOR);
        largeTable = new RestaurantTable(6, TableLocation.INDOOR);
        terraceTable = new RestaurantTable(4, TableLocation.TERRACE);

        restaurant.addTable(smallTable);
        restaurant.addTable(mediumTable);
        restaurant.addTable(largeTable);
        restaurant.addTable(terraceTable);
    }

    @Nested
    @DisplayName("Find Available Tables")
    class FindAvailableTables {

        @Test
        @DisplayName("Should find tables that can accommodate party size")
        void shouldFindTablesThatCanAccommodatePartySize() {
            int partySize = 4;
            LocalDate date = LocalDate.now().plusDays(1);
            LocalTime startTime = LocalTime.of(19, 0);
            LocalTime endTime = LocalTime.of(21, 0);

            List<RestaurantTable> availableTables = availabilityService.findAvailableTables(
                    restaurant, partySize, date, startTime, endTime);

            assertThat(availableTables).hasSize(3);
            assertThat(availableTables).contains(mediumTable, largeTable, terraceTable);
            assertThat(availableTables).doesNotContain(smallTable);
        }

        @Test
        @DisplayName("Should exclude unavailable tables")
        void shouldExcludeUnavailableTables() {
            mediumTable.makeUnavailable();
            int partySize = 4;
            LocalDate date = LocalDate.now().plusDays(1);
            LocalTime startTime = LocalTime.of(19, 0);
            LocalTime endTime = LocalTime.of(21, 0);

            List<RestaurantTable> availableTables = availabilityService.findAvailableTables(
                    restaurant, partySize, date, startTime, endTime);

            assertThat(availableTables).hasSize(2); // large, terrace
            assertThat(availableTables).contains(largeTable, terraceTable);
            assertThat(availableTables).doesNotContain(mediumTable);
        }

        @Test
        @DisplayName("Should exclude tables with conflicting reservations")
        void shouldExcludeTablesWithConflictingReservations() {
            LocalDate date = LocalDate.now().plusDays(1);
            TimeSlot conflictingSlot = new TimeSlot(date, LocalTime.of(18, 30), LocalTime.of(20, 30), 4);
            mediumTable.addTimeSlot(conflictingSlot);

            int partySize = 4;
            LocalTime startTime = LocalTime.of(19, 0);
            LocalTime endTime = LocalTime.of(21, 0);

            List<RestaurantTable> availableTables = availabilityService.findAvailableTables(
                    restaurant, partySize, date, startTime, endTime);

            assertThat(availableTables).hasSize(2); // large, terrace
            assertThat(availableTables).contains(largeTable, terraceTable);
            assertThat(availableTables).doesNotContain(mediumTable);
        }
    }

    @Nested
    @DisplayName("Find Best Table")
    class FindBestTable {

        @Test
        @DisplayName("Should find smallest suitable table")
        void shouldFindSmallestSuitableTable() {
            int partySize = 4;
            LocalDate date = LocalDate.now().plusDays(1);
            LocalTime startTime = LocalTime.of(19, 0);
            LocalTime endTime = LocalTime.of(21, 0);

            Optional<RestaurantTable> bestTable = availabilityService.findBestTable(
                    restaurant, partySize, date, startTime, endTime);

            assertThat(bestTable).isPresent();
            assertThat(bestTable.get().getSeats()).isEqualTo(4);
            assertThat(bestTable.get()).isIn(mediumTable, terraceTable);
        }

        @Test
        @DisplayName("Should return empty when no suitable table")
        void shouldReturnEmptyWhenNoSuitableTable() {
            int partySize = 8;
            LocalDate date = LocalDate.now().plusDays(1);
            LocalTime startTime = LocalTime.of(19, 0);
            LocalTime endTime = LocalTime.of(21, 0);

            Optional<RestaurantTable> bestTable = availabilityService.findBestTable(
                    restaurant, partySize, date, startTime, endTime);

            assertThat(bestTable).isEmpty();
        }
    }

    @Nested
    @DisplayName("Calculate Availability Rate")
    class CalculateAvailabilityRate {

        @Test
        @DisplayName("Should calculate 100% when all tables available")
        void shouldCalculate100PercentWhenAllTablesAvailable() {
            LocalDate date = LocalDate.now().plusDays(1);

            double rate = availabilityService.calculateAvailabilityRate(restaurant, date);

            assertThat(rate).isEqualTo(1.0);
        }

        @Test
        @DisplayName("Should calculate correct rate when some tables unavailable")
        void shouldCalculateCorrectRateWhenSomeTablesUnavailable() {
            smallTable.makeUnavailable();
            mediumTable.makeUnavailable();
            LocalDate date = LocalDate.now().plusDays(1);

            double rate = availabilityService.calculateAvailabilityRate(restaurant, date);

            assertThat(rate).isEqualTo(0.5);
        }

        @Test
        @DisplayName("Should return 0 when no tables")
        void shouldReturn0WhenNoTables() {
            Restaurant emptyRestaurant = new Restaurant(
                    "Empty Restaurant", "Address", "+33123456789", "test@test.com", 10);
            LocalDate date = LocalDate.now().plusDays(1);

            double rate = availabilityService.calculateAvailabilityRate(emptyRestaurant, date);

            assertThat(rate).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("Calculate Utilization Rate")
    class CalculateUtilizationRate {

        @Test
        @DisplayName("Should calculate 0% when no reservations")
        void shouldCalculate0PercentWhenNoReservations() {
            LocalDate date = LocalDate.now().plusDays(1);
            LocalTime time = LocalTime.of(19, 0);

            double rate = availabilityService.calculateUtilizationRate(restaurant, date, time);

            assertThat(rate).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Should calculate correct rate with reservations")
        void shouldCalculateCorrectRateWithReservations() {
            LocalDate date = LocalDate.now().plusDays(1);
            LocalTime time = LocalTime.of(19, 30);

            // Add reservations that overlap with the check time
            TimeSlot slot1 = new TimeSlot(date, LocalTime.of(19, 0), LocalTime.of(21, 0), 2);
            TimeSlot slot2 = new TimeSlot(date, LocalTime.of(18, 30), LocalTime.of(20, 30), 4);

            smallTable.addTimeSlot(slot1);  // 2 seats occupied
            mediumTable.addTimeSlot(slot2); // 4 seats occupied

            double rate = availabilityService.calculateUtilizationRate(restaurant, date, time);

            int totalSeats = 2 + 4 + 6 + 4; // 16 total seats
            int occupiedSeats = 2 + 4; // 6 occupied seats
            double expectedRate = (double) occupiedSeats / totalSeats;
            assertThat(rate).isEqualTo(expectedRate);
        }
    }

    @Nested
    @DisplayName("Can Accommodate On Date")
    class CanAccommodateOnDate {

        @Test
        @DisplayName("Should return true when can accommodate")
        void shouldReturnTrueWhenCanAccommodate() {
            int partySize = 4;
            LocalDate date = LocalDate.now().plusDays(1);

            boolean canAccommodate = availabilityService.canAccommodateOnDate(restaurant, partySize, date);

            assertThat(canAccommodate).isTrue();
        }

        @Test
        @DisplayName("Should return false when cannot accommodate")
        void shouldReturnFalseWhenCannotAccommodate() {
            int partySize = 10; // Larger than any table
            LocalDate date = LocalDate.now().plusDays(1);

            boolean canAccommodate = availabilityService.canAccommodateOnDate(restaurant, partySize, date);

            assertThat(canAccommodate).isFalse();
        }

        @Test
        @DisplayName("Should consider only available tables")
        void shouldConsiderOnlyAvailableTables() {
            largeTable.makeUnavailable(); // Largest table unavailable
            int partySize = 6;
            LocalDate date = LocalDate.now().plusDays(1);

            boolean canAccommodate = availabilityService.canAccommodateOnDate(restaurant, partySize, date);

            assertThat(canAccommodate).isFalse();
        }
    }

    @Nested
    @DisplayName("Validate Reservation Constraints")
    class ValidateReservationConstraints {

        @Test
        @DisplayName("Should pass validation for valid reservation")
        void shouldPassValidationForValidReservation() {
            int partySize = 4;
            LocalDate date = LocalDate.now().plusDays(1);
            LocalTime startTime = LocalTime.of(19, 0);
            LocalTime endTime = LocalTime.of(21, 0);

            assertThatCode(() -> availabilityService.validateReservationConstraints(
                    restaurant, mediumTable, partySize, date, startTime, endTime))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should throw exception when restaurant inactive")
        void shouldThrowExceptionWhenRestaurantInactive() {
            restaurant.deactivate();
            int partySize = 4;
            LocalDate date = LocalDate.now().plusDays(1);
            LocalTime startTime = LocalTime.of(19, 0);
            LocalTime endTime = LocalTime.of(21, 0);

            assertThatThrownBy(() -> availabilityService.validateReservationConstraints(
                    restaurant, mediumTable, partySize, date, startTime, endTime))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Restaurant is not active");
        }

        @Test
        @DisplayName("Should throw exception when table unavailable")
        void shouldThrowExceptionWhenTableUnavailable() {
            mediumTable.makeUnavailable();
            int partySize = 4;
            LocalDate date = LocalDate.now().plusDays(1);
            LocalTime startTime = LocalTime.of(19, 0);
            LocalTime endTime = LocalTime.of(21, 0);

            assertThatThrownBy(() -> availabilityService.validateReservationConstraints(
                    restaurant, mediumTable, partySize, date, startTime, endTime))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Table is not available");
        }

        @Test
        @DisplayName("Should throw exception when table capacity insufficient")
        void shouldThrowExceptionWhenTableCapacityInsufficient() {
            int partySize = 6; // More than medium table capacity (4)
            LocalDate date = LocalDate.now().plusDays(1);
            LocalTime startTime = LocalTime.of(19, 0);
            LocalTime endTime = LocalTime.of(21, 0);

            assertThatThrownBy(() -> availabilityService.validateReservationConstraints(
                    restaurant, mediumTable, partySize, date, startTime, endTime))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Table capacity insufficient for party size");
        }

        @Test
        @DisplayName("Should throw exception when table not available at time")
        void shouldThrowExceptionWhenTableNotAvailableAtTime() {
            LocalDate date = LocalDate.now().plusDays(1);
            TimeSlot existingSlot = new TimeSlot(date, LocalTime.of(18, 0), LocalTime.of(20, 0), 4);
            mediumTable.addTimeSlot(existingSlot);

            int partySize = 4;
            LocalTime startTime = LocalTime.of(19, 0); // Overlaps with existing slot
            LocalTime endTime = LocalTime.of(21, 0);

            assertThatThrownBy(() -> availabilityService.validateReservationConstraints(
                    restaurant, mediumTable, partySize, date, startTime, endTime))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Table is not available at requested time");
        }

        @Test
        @DisplayName("Should throw exception for private room with small party")
        void shouldThrowExceptionForPrivateRoomWithSmallParty() {
            RestaurantTable privateTable = new RestaurantTable(8, TableLocation.PRIVATE_ROOM);
            restaurant.addTable(privateTable);

            int partySize = 2; // Less than minimum for private room
            LocalDate date = LocalDate.now().plusDays(1);
            LocalTime startTime = LocalTime.of(19, 0);
            LocalTime endTime = LocalTime.of(21, 0);

            assertThatThrownBy(() -> availabilityService.validateReservationConstraints(
                    restaurant, privateTable, partySize, date, startTime, endTime))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Private room requires minimum 4 guests");
        }
    }

    @Nested
    @DisplayName("Input Validation")
    class InputValidation {

        @Test
        @DisplayName("Should throw exception for null restaurant")
        void shouldThrowExceptionForNullRestaurant() {
            int partySize = 4;
            LocalDate date = LocalDate.now().plusDays(1);
            LocalTime startTime = LocalTime.of(19, 0);
            LocalTime endTime = LocalTime.of(21, 0);

            assertThatThrownBy(() -> availabilityService.findAvailableTables(
                    null, partySize, date, startTime, endTime))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Restaurant cannot be null");
        }

        @Test
        @DisplayName("Should throw exception for invalid party size")
        void shouldThrowExceptionForInvalidPartySize() {
            int partySize = -1;
            LocalDate date = LocalDate.now().plusDays(1);
            LocalTime startTime = LocalTime.of(19, 0);
            LocalTime endTime = LocalTime.of(21, 0);

            assertThatThrownBy(() -> availabilityService.findAvailableTables(
                    restaurant, partySize, date, startTime, endTime))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Party size must be positive");
        }

        @Test
        @DisplayName("Should throw exception for past date")
        void shouldThrowExceptionForPastDate() {
            int partySize = 4;
            LocalDate pastDate = LocalDate.of(2020, 1, 1);
            LocalTime startTime = LocalTime.of(19, 0);
            LocalTime endTime = LocalTime.of(21, 0);

            assertThatThrownBy(() -> availabilityService.findAvailableTables(
                    restaurant, partySize, pastDate, startTime, endTime))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Cannot book for past dates");
        }

        @Test
        @DisplayName("Should throw exception when end time before start time")
        void shouldThrowExceptionWhenEndTimeBeforeStartTime() {
            int partySize = 4;
            LocalDate date = LocalDate.now().plusDays(1);
            LocalTime startTime = LocalTime.of(21, 0);
            LocalTime endTime = LocalTime.of(19, 0); // Before start time

            assertThatThrownBy(() -> availabilityService.findAvailableTables(
                    restaurant, partySize, date, startTime, endTime))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("End time must be after start time");
        }
    }
}