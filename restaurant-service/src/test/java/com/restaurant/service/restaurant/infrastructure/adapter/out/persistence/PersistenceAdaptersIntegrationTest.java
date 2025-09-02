package com.restaurant.service.restaurant.infrastructure.adapter.out.persistence;

import com.restaurant.service.restaurant.domain.model.*;
import com.restaurant.service.restaurant.domain.port.out.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for all persistence adapters working together
 * Tests the complete persistence layer with relationships
 */
@DataJpaTest
@Import({
        RestaurantPersistenceAdapter.class,
        RestaurantTablePersistenceAdapter.class,
        TimeSlotPersistenceAdapter.class
})
@ActiveProfiles("test")
@DisplayName("Persistence Adapters Integration Tests")
class PersistenceAdaptersIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RestaurantRepositoryPort restaurantRepository;

    @Autowired
    private RestaurantTableRepositoryPort tableRepository;

    @Autowired
    private TimeSlotRepositoryPort timeSlotRepository;

    private Restaurant restaurant;
    private RestaurantTable table;

    @BeforeEach
    void setUp() {
        // Create and persist a restaurant
        restaurant = new Restaurant(
                "Test Restaurant",
                "123 Test Street, Test City",
                "+33123456789",
                "test@restaurant.com",
                50
        );
        restaurant = restaurantRepository.save(restaurant);

        // Create and persist a table
        table = new RestaurantTable(4, TableLocation.WINDOW);
        restaurant.addTable(table);
        table = tableRepository.save(table);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("Should save and retrieve complete restaurant with tables and time slots")
    void shouldSaveAndRetrieveCompleteRestaurantWithTablesAndTimeSlots() {
        // Given - Create time slots
        LocalDate futureDate = LocalDate.now().plusDays(1);
        TimeSlot slot1 = new TimeSlot(futureDate, LocalTime.of(19, 0), LocalTime.of(21, 0), 4);
        TimeSlot slot2 = new TimeSlot(futureDate, LocalTime.of(21, 30), LocalTime.of(23, 0), 2);

        table.addTimeSlot(slot1);
        table.addTimeSlot(slot2);

        timeSlotRepository.save(slot1);
        timeSlotRepository.save(slot2);
        entityManager.flush();

        // When
        List<RestaurantTable> tables = tableRepository.findByRestaurantId(restaurant.getId());
        List<TimeSlot> timeSlots = timeSlotRepository.findByTableId(table.getId());

        // Then
        assertThat(tables).hasSize(1);
        assertThat(tables.get(0).getSeats()).isEqualTo(4);
        assertThat(tables.get(0).getLocation()).isEqualTo(TableLocation.WINDOW);

        assertThat(timeSlots).hasSize(2);
        assertThat(timeSlots)
                .extracting(TimeSlot::getStartTime)
                .containsExactly(LocalTime.of(19, 0), LocalTime.of(21, 30));
    }

    @Test
    @DisplayName("Should find available tables for date, time and party size")
    void shouldFindAvailableTablesForDateTimeAndPartySize() {
        // Given - Create additional tables
        RestaurantTable table2 = new RestaurantTable(6, TableLocation.TERRACE);
        RestaurantTable table3 = new RestaurantTable(2, TableLocation.INDOOR);

        restaurant.addTable(table2);
        restaurant.addTable(table3);

        tableRepository.save(table2);
        tableRepository.save(table3);

        // Add a time slot that conflicts with our search
        LocalDate searchDate = LocalDate.now().plusDays(1);
        TimeSlot conflictingSlot = new TimeSlot(searchDate, LocalTime.of(18, 0), LocalTime.of(20, 0), 4);
        table.addTimeSlot(conflictingSlot);
        timeSlotRepository.save(conflictingSlot);

        entityManager.flush();

        // When - Search for available tables
        LocalTime startTime = LocalTime.of(19, 0);
        LocalTime endTime = LocalTime.of(21, 0);
        Integer partySize = 4;

        List<RestaurantTable> availableTables = tableRepository.findAvailableTablesForDateTimeAndPartySize(
                restaurant.getId(), searchDate, startTime, endTime, partySize);

        // Then - Should find table2 (6 seats, no conflicts) but not table (conflicting slot) or table3 (too small)
        assertThat(availableTables).hasSize(1);
        assertThat(availableTables.get(0).getSeats()).isEqualTo(6);
        assertThat(availableTables.get(0).getLocation()).isEqualTo(TableLocation.TERRACE);
    }

    @Test
    @DisplayName("Should find overlapping time slots")
    void shouldFindOverlappingTimeSlots() {
        // Given - Create existing time slots
        LocalDate date = LocalDate.now().plusDays(1);
        TimeSlot slot1 = new TimeSlot(date, LocalTime.of(18, 0), LocalTime.of(20, 0), 4);
        TimeSlot slot2 = new TimeSlot(date, LocalTime.of(21, 0), LocalTime.of(23, 0), 2);

        table.addTimeSlot(slot1);
        table.addTimeSlot(slot2);

        timeSlotRepository.save(slot1);
        timeSlotRepository.save(slot2);
        entityManager.flush();

        // When - Search for overlapping slots
        List<TimeSlot> overlappingSlots = timeSlotRepository.findOverlappingTimeSlots(
                table.getId(), date, LocalTime.of(19, 0), LocalTime.of(22, 0));

        // Then - Should find both slots (19-22 overlaps with 18-20 and 21-23)
        assertThat(overlappingSlots).hasSize(2);
        assertThat(overlappingSlots)
                .extracting(TimeSlot::getStartTime)
                .containsExactly(LocalTime.of(18, 0), LocalTime.of(21, 0));
    }

    @Test
    @DisplayName("Should manage time slot status transitions")
    void shouldManageTimeSlotStatusTransitions() {
        // Given - Create time slot
        LocalDate date = LocalDate.now().plusDays(1);
        TimeSlot timeSlot = new TimeSlot(date, LocalTime.of(19, 0), LocalTime.of(21, 0), 4);
        table.addTimeSlot(timeSlot);
        timeSlot = timeSlotRepository.save(timeSlot);
        entityManager.flush();

        // When - Confirm the slot
        timeSlot.confirm();
        timeSlot = timeSlotRepository.save(timeSlot);
        entityManager.flush();

        // Then
        List<TimeSlot> confirmedSlots = timeSlotRepository.findByTableIdAndStatus(
                table.getId(), TimeSlotStatus.CONFIRMED);

        assertThat(confirmedSlots).hasSize(1);
        assertThat(confirmedSlots.get(0).getStatus()).isEqualTo(TimeSlotStatus.CONFIRMED);

        // When - Complete the slot
        timeSlot.complete();
        timeSlot = timeSlotRepository.save(timeSlot);
        entityManager.flush();

        // Then
        List<TimeSlot> completedSlots = timeSlotRepository.findByTableIdAndStatus(
                table.getId(), TimeSlotStatus.COMPLETED);

        assertThat(completedSlots).hasSize(1);
        assertThat(completedSlots.get(0).getStatus()).isEqualTo(TimeSlotStatus.COMPLETED);
    }

    @Test
    @DisplayName("Should find restaurant time slots by date")
    void shouldFindRestaurantTimeSlotsByDate() {
        // Given - Create tables and time slots for restaurant
        RestaurantTable table2 = new RestaurantTable(6, TableLocation.TERRACE);
        restaurant.addTable(table2);
        table2 = tableRepository.save(table2);

        LocalDate date = LocalDate.now().plusDays(1);
        TimeSlot slot1 = new TimeSlot(date, LocalTime.of(19, 0), LocalTime.of(21, 0), 4);
        TimeSlot slot2 = new TimeSlot(date, LocalTime.of(20, 0), LocalTime.of(22, 0), 6);

        table.addTimeSlot(slot1);
        table2.addTimeSlot(slot2);

        timeSlotRepository.save(slot1);
        timeSlotRepository.save(slot2);
        entityManager.flush();

        // When
        List<TimeSlot> restaurantSlots = timeSlotRepository.findByRestaurantIdAndDate(
                restaurant.getId(), date);

        // Then
        assertThat(restaurantSlots).hasSize(2);
        assertThat(restaurantSlots)
                .extracting(TimeSlot::getReservedSeats)
                .containsExactly(4, 6);
    }

    @Test
    @DisplayName("Should count statistics correctly")
    void shouldCountStatisticsCorrectly() {
        // Given - Create additional data
        RestaurantTable table2 = new RestaurantTable(6, TableLocation.TERRACE);
        restaurant.addTable(table2);
        table2 = tableRepository.save(table2);

        LocalDate date = LocalDate.now().plusDays(1);
        TimeSlot availableSlot = new TimeSlot(date, LocalTime.of(19, 0), LocalTime.of(21, 0), 4);
        TimeSlot confirmedSlot = new TimeSlot(date, LocalTime.of(20, 0), LocalTime.of(22, 0), 6);
        confirmedSlot.confirm();

        table.addTimeSlot(availableSlot);
        table2.addTimeSlot(confirmedSlot);

        timeSlotRepository.save(availableSlot);
        timeSlotRepository.save(confirmedSlot);
        entityManager.flush();

        // When
        long totalTables = tableRepository.countByRestaurantId(restaurant.getId());
        long availableTables = tableRepository.countAvailableByRestaurantId(restaurant.getId());
        long availableSlots = timeSlotRepository.countByRestaurantIdAndStatus(
                restaurant.getId(), TimeSlotStatus.AVAILABLE);
        long confirmedSlots = timeSlotRepository.countByRestaurantIdAndStatus(
                restaurant.getId(), TimeSlotStatus.CONFIRMED);

        // Then
        assertThat(totalTables).isEqualTo(2);
        assertThat(availableTables).isEqualTo(2);
        assertThat(availableSlots).isEqualTo(1);
        assertThat(confirmedSlots).isEqualTo(1);
    }

    @Test
    @DisplayName("Should handle cascade operations correctly")
    void shouldHandleCascadeOperationsCorrectly() {
        // Given - Create time slot
        LocalDate date = LocalDate.now().plusDays(1);
        TimeSlot timeSlot = new TimeSlot(date, LocalTime.of(19, 0), LocalTime.of(21, 0), 4);
        table.addTimeSlot(timeSlot);
        timeSlot = timeSlotRepository.save(timeSlot);
        entityManager.flush();

        // When - Delete table (should cascade to time slots)
        Long tableId = table.getId();
        Long timeSlotId = timeSlot.getId();

        tableRepository.deleteById(tableId);
        entityManager.flush();

        // Then
        assertThat(tableRepository.existsById(tableId)).isFalse();
        assertThat(timeSlotRepository.existsById(timeSlotId)).isFalse();
    }
}