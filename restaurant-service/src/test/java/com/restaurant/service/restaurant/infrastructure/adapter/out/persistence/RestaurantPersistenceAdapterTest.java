package com.restaurant.service.restaurant.infrastructure.adapter.out.persistence;

import com.restaurant.service.restaurant.domain.model.Restaurant;
import com.restaurant.service.restaurant.domain.model.RestaurantTable;
import com.restaurant.service.restaurant.domain.model.TableLocation;
import com.restaurant.service.restaurant.domain.model.TimeSlot;
import com.restaurant.service.restaurant.domain.model.TimeSlotStatus;
import com.restaurant.service.restaurant.infrastructure.adapter.out.persistence.mapper.RestaurantMapper;
import com.restaurant.service.restaurant.infrastructure.adapter.out.persistence.mapper.RestaurantTableMapper;
import com.restaurant.service.restaurant.infrastructure.adapter.out.persistence.mapper.TimeSlotMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for RestaurantPersistenceAdapter
 * Tests the complete persistence layer with real database
 */
@DataJpaTest
@ActiveProfiles("test")
@Import({
    RestaurantPersistenceAdapter.class,
    RestaurantMapper.class,
    RestaurantTableMapper.class,
    TimeSlotMapper.class
})
@DisplayName("Restaurant Persistence Adapter Integration Tests")
class RestaurantPersistenceAdapterTest {

    @Autowired
    private RestaurantPersistenceAdapter persistenceAdapter;

    @Autowired
    private TestEntityManager entityManager;

    private Restaurant testRestaurant;
    private RestaurantTable testTable;
    private TimeSlot testTimeSlot;

    @BeforeEach
    void setUp() {
        testRestaurant = new Restaurant(
            "Test Restaurant",
            "123 Test Street, Test City",
            "+33 1 23 45 67 89",
            "test@restaurant.com",
            50
        );

        testTable = new RestaurantTable(4, TableLocation.WINDOW);
        testTable.setTableNumber(1);

        testTimeSlot = new TimeSlot(
            LocalDate.now().plusDays(1),
            LocalTime.of(19, 0),
            LocalTime.of(21, 0),
            4
        );
    }

    @Test
    @DisplayName("Should save and retrieve restaurant")
    void shouldSaveAndRetrieveRestaurant() {
        // When
        Restaurant savedRestaurant = persistenceAdapter.save(testRestaurant);

        // Then
        assertThat(savedRestaurant.getId()).isNotNull();
        assertThat(savedRestaurant.getName()).isEqualTo("Test Restaurant");
        assertThat(savedRestaurant.getEmail()).isEqualTo("test@restaurant.com");
        assertThat(savedRestaurant.isActive()).isTrue();

        // Verify retrieval
        Optional<Restaurant> retrieved = persistenceAdapter.findById(savedRestaurant.getId());
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getName()).isEqualTo("Test Restaurant");
    }

    @Test
    @DisplayName("Should update existing restaurant")
    void shouldUpdateExistingRestaurant() {
        // Given
        Restaurant savedRestaurant = persistenceAdapter.save(testRestaurant);
        savedRestaurant.setName("Updated Restaurant");
        savedRestaurant.setCapacity(100);

        // When
        Restaurant updatedRestaurant = persistenceAdapter.save(savedRestaurant);

        // Then
        assertThat(updatedRestaurant.getId()).isEqualTo(savedRestaurant.getId());
        assertThat(updatedRestaurant.getName()).isEqualTo("Updated Restaurant");
        assertThat(updatedRestaurant.getCapacity()).isEqualTo(100);
    }

    @Test
    @DisplayName("Should find restaurant by email")
    void shouldFindRestaurantByEmail() {
        // Given
        persistenceAdapter.save(testRestaurant);

        // When
        Optional<Restaurant> found = persistenceAdapter.findByEmail("test@restaurant.com");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test Restaurant");
    }

    @Test
    @DisplayName("Should search restaurants by name")
    void shouldSearchRestaurantsByName() {
        // Given
        Restaurant restaurant1 = new Restaurant("Italian Bistro", "Address 1", "Phone 1", "italian@test.com", 30);
        Restaurant restaurant2 = new Restaurant("French Bistro", "Address 2", "Phone 2", "french@test.com", 40);
        Restaurant restaurant3 = new Restaurant("Pizza Place", "Address 3", "Phone 3", "pizza@test.com", 20);

        persistenceAdapter.save(restaurant1);
        persistenceAdapter.save(restaurant2);
        persistenceAdapter.save(restaurant3);

        // When
        List<Restaurant> bistros = persistenceAdapter.searchByName("Bistro");

        // Then
        assertThat(bistros).hasSize(2);
        assertThat(bistros).extracting(Restaurant::getName)
            .containsExactlyInAnyOrder("Italian Bistro", "French Bistro");
    }

    @Test
    @DisplayName("Should find active restaurants only")
    void shouldFindActiveRestaurantsOnly() {
        // Given
        Restaurant activeRestaurant = new Restaurant("Active Restaurant", "Address 1", "Phone 1", "active@test.com", 30);
        Restaurant inactiveRestaurant = new Restaurant("Inactive Restaurant", "Address 2", "Phone 2", "inactive@test.com", 40);
        inactiveRestaurant.setActive(false);

        persistenceAdapter.save(activeRestaurant);
        persistenceAdapter.save(inactiveRestaurant);

        // When
        List<Restaurant> activeRestaurants = persistenceAdapter.findActiveRestaurants();

        // Then
        assertThat(activeRestaurants).hasSize(1);
        assertThat(activeRestaurants.get(0).getName()).isEqualTo("Active Restaurant");
    }

    @Test
    @DisplayName("Should handle pagination")
    void shouldHandlePagination() {
        // Given - Create multiple restaurants
        for (int i = 1; i <= 5; i++) {
            Restaurant restaurant = new Restaurant("Restaurant " + i, "Address " + i, "Phone " + i, "email" + i + "@test.com", 30);
            persistenceAdapter.save(restaurant);
        }

        // When
        Page<Restaurant> firstPage = persistenceAdapter.findAll(PageRequest.of(0, 2));
        Page<Restaurant> secondPage = persistenceAdapter.findAll(PageRequest.of(1, 2));

        // Then
        assertThat(firstPage.getContent()).hasSize(2);
        assertThat(firstPage.getTotalElements()).isEqualTo(5);
        assertThat(firstPage.getTotalPages()).isEqualTo(3);
        assertThat(secondPage.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("Should save and retrieve restaurant table")
    void shouldSaveAndRetrieveRestaurantTable() {
        // Given
        Restaurant savedRestaurant = persistenceAdapter.save(testRestaurant);
        testTable.setRestaurantId(savedRestaurant.getId());

        // When
        RestaurantTable savedTable = persistenceAdapter.saveTable(testTable);

        // Then
        assertThat(savedTable.getId()).isNotNull();
        assertThat(savedTable.getSeats()).isEqualTo(4);
        assertThat(savedTable.getLocation()).isEqualTo(TableLocation.WINDOW);

        // Verify retrieval
        Optional<RestaurantTable> retrieved = persistenceAdapter.findTableById(savedTable.getId());
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getSeats()).isEqualTo(4);
    }

    @Test
    @DisplayName("Should find available tables for date and time")
    void shouldFindAvailableTablesForDateAndTime() {
        // Given
        Restaurant savedRestaurant = persistenceAdapter.save(testRestaurant);
        testTable.setRestaurantId(savedRestaurant.getId());
        RestaurantTable savedTable = persistenceAdapter.saveTable(testTable);

        LocalDate tomorrow = LocalDate.now().plusDays(1);
        LocalTime dinnerStart = LocalTime.of(19, 0);
        LocalTime dinnerEnd = LocalTime.of(21, 0);

        // When
        List<RestaurantTable> availableTables = persistenceAdapter.findAvailableTablesForDateAndTime(
            savedRestaurant.getId(), tomorrow, dinnerStart, dinnerEnd, 2);

        // Then
        assertThat(availableTables).hasSize(1);
        assertThat(availableTables.get(0).getId()).isEqualTo(savedTable.getId());
    }

    @Test
    @DisplayName("Should save and retrieve time slot")
    void shouldSaveAndRetrieveTimeSlot() {
        // Given
        Restaurant savedRestaurant = persistenceAdapter.save(testRestaurant);
        testTable.setRestaurantId(savedRestaurant.getId());
        RestaurantTable savedTable = persistenceAdapter.saveTable(testTable);
        testTimeSlot.setTableId(savedTable.getId());

        // When
        TimeSlot savedTimeSlot = persistenceAdapter.saveTimeSlot(testTimeSlot);

        // Then
        assertThat(savedTimeSlot.getId()).isNotNull();
        assertThat(savedTimeSlot.getDate()).isEqualTo(testTimeSlot.getDate());
        assertThat(savedTimeSlot.getStartTime()).isEqualTo(testTimeSlot.getStartTime());
        assertThat(savedTimeSlot.getStatus()).isEqualTo(TimeSlotStatus.AVAILABLE);

        // Verify retrieval
        Optional<TimeSlot> retrieved = persistenceAdapter.findTimeSlotById(savedTimeSlot.getId());
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getPartySize()).isEqualTo(4);
    }

    @Test
    @DisplayName("Should find time slots by table and date")
    void shouldFindTimeSlotsByTableAndDate() {
        // Given
        Restaurant savedRestaurant = persistenceAdapter.save(testRestaurant);
        testTable.setRestaurantId(savedRestaurant.getId());
        RestaurantTable savedTable = persistenceAdapter.saveTable(testTable);
        
        LocalDate targetDate = LocalDate.now().plusDays(1);
        
        TimeSlot slot1 = new TimeSlot(targetDate, LocalTime.of(19, 0), LocalTime.of(21, 0), 4);
        slot1.setTableId(savedTable.getId());
        TimeSlot slot2 = new TimeSlot(targetDate, LocalTime.of(21, 30), LocalTime.of(23, 0), 4);
        slot2.setTableId(savedTable.getId());

        persistenceAdapter.saveTimeSlot(slot1);
        persistenceAdapter.saveTimeSlot(slot2);

        // When
        List<TimeSlot> timeSlots = persistenceAdapter.findTimeSlotsByTableAndDate(savedTable.getId(), targetDate);

        // Then
        assertThat(timeSlots).hasSize(2);
        assertThat(timeSlots).extracting(TimeSlot::getStartTime)
            .containsExactlyInAnyOrder(LocalTime.of(19, 0), LocalTime.of(21, 30));
    }

    @Test
    @DisplayName("Should find reservations by customer email")
    void shouldFindReservationsByCustomerEmail() {
        // Given
        Restaurant savedRestaurant = persistenceAdapter.save(testRestaurant);
        testTable.setRestaurantId(savedRestaurant.getId());
        RestaurantTable savedTable = persistenceAdapter.saveTable(testTable);
        
        TimeSlot reservation = new TimeSlot(
            LocalDate.now().plusDays(1),
            LocalTime.of(19, 0),
            LocalTime.of(21, 0),
            4
        );
        reservation.setTableId(savedTable.getId());
        reservation.setStatus(TimeSlotStatus.RESERVED);
        reservation.setCustomerEmail("customer@test.com");
        reservation.setCustomerName("John Doe");

        persistenceAdapter.saveTimeSlot(reservation);

        // When
        List<TimeSlot> reservations = persistenceAdapter.findReservationsByCustomerEmail("customer@test.com");

        // Then
        assertThat(reservations).hasSize(1);
        assertThat(reservations.get(0).getCustomerName()).isEqualTo("John Doe");
        assertThat(reservations.get(0).getStatus()).isEqualTo(TimeSlotStatus.RESERVED);
    }

    @Test
    @DisplayName("Should delete restaurant and cascade to tables and time slots")
    void shouldDeleteRestaurantAndCascade() {
        // Given
        Restaurant savedRestaurant = persistenceAdapter.save(testRestaurant);
        testTable.setRestaurantId(savedRestaurant.getId());
        RestaurantTable savedTable = persistenceAdapter.saveTable(testTable);
        testTimeSlot.setTableId(savedTable.getId());
        TimeSlot savedTimeSlot = persistenceAdapter.saveTimeSlot(testTimeSlot);

        // When
        persistenceAdapter.deleteById(savedRestaurant.getId());
        entityManager.flush(); // Force the delete to happen

        // Then
        assertThat(persistenceAdapter.findById(savedRestaurant.getId())).isEmpty();
        assertThat(persistenceAdapter.findTableById(savedTable.getId())).isEmpty();
        assertThat(persistenceAdapter.findTimeSlotById(savedTimeSlot.getId())).isEmpty();
    }

    @Test
    @DisplayName("Should handle unique constraint violations")
    void shouldHandleUniqueConstraintViolations() {
        // Given
        Restaurant restaurant1 = new Restaurant("Restaurant 1", "Address 1", "Phone 1", "same@email.com", 30);
        Restaurant restaurant2 = new Restaurant("Restaurant 2", "Address 2", "Phone 2", "same@email.com", 40);

        persistenceAdapter.save(restaurant1);

        // When & Then
        assertThatThrownBy(() -> persistenceAdapter.save(restaurant2))
            .isInstanceOf(Exception.class); // Could be DataIntegrityViolationException or similar
    }
}