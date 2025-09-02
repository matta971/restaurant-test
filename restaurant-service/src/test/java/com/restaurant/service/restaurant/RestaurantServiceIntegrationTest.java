package com.restaurant.service.restaurant;

import com.restaurant.service.restaurant.domain.model.Restaurant;
import com.restaurant.service.restaurant.domain.model.RestaurantTable;
import com.restaurant.service.restaurant.domain.model.TableLocation;
import com.restaurant.service.restaurant.domain.port.out.RestaurantRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Full integration test for Restaurant Service
 * Tests the complete application context and functionality
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Restaurant Service Integration Tests")
class RestaurantServiceIntegrationTest {

    @Autowired
    private RestaurantRepositoryPort restaurantRepository;

    @Test
    @DisplayName("Should start application context successfully")
    void shouldStartApplicationContext() {
        // This test verifies that the Spring Boot application context loads successfully
        // with all the configured beans, repositories, and services
        assertThat(restaurantRepository).isNotNull();
    }

    @Test
    @DisplayName("Should perform complete restaurant workflow")
    void shouldPerformCompleteRestaurantWorkflow() {
        // Given - Create a restaurant
        Restaurant restaurant = new Restaurant(
            "Integration Test Restaurant",
            "123 Integration Street, Test City",
            "+33 1 11 11 11 11",
            "integration@test.com",
            60
        );
        restaurant.setOpeningTime(LocalTime.of(11, 0));
        restaurant.setClosingTime(LocalTime.of(23, 0));

        // When - Save restaurant
        Restaurant savedRestaurant = restaurantRepository.save(restaurant);

        // Then - Verify restaurant is saved
        assertThat(savedRestaurant.getId()).isNotNull();
        assertThat(savedRestaurant.getName()).isEqualTo("Integration Test Restaurant");

        // Given - Create tables for the restaurant
        RestaurantTable table1 = new RestaurantTable(4, TableLocation.WINDOW);
        table1.setRestaurantId(savedRestaurant.getId());
        table1.setTableNumber(1);

        RestaurantTable table2 = new RestaurantTable(6, TableLocation.TERRACE);
        table2.setRestaurantId(savedRestaurant.getId());
        table2.setTableNumber(2);

        // When - Save tables
        RestaurantTable savedTable1 = restaurantRepository.saveTable(table1);
        RestaurantTable savedTable2 = restaurantRepository.saveTable(table2);

        // Then - Verify tables are saved
        assertThat(savedTable1.getId()).isNotNull();
        assertThat(savedTable2.getId()).isNotNull();

        // When - Retrieve restaurant with tables
        Optional<Restaurant> retrievedRestaurant = restaurantRepository.findByIdWithTables(savedRestaurant.getId());

        // Then - Verify restaurant is retrieved with tables
        assertThat(retrievedRestaurant).isPresent();
        assertThat(retrievedRestaurant.get().getId()).isEqualTo(savedRestaurant.getId());

        // When - Find tables by restaurant
        List<RestaurantTable> tables = restaurantRepository.findTablesByRestaurantId(savedRestaurant.getId());

        // Then - Verify tables are found
        assertThat(tables).hasSize(2);
        assertThat(tables).extracting(RestaurantTable::getSeats)
            .containsExactlyInAnyOrder(4, 6);
        assertThat(tables).extracting(RestaurantTable::getLocation)
            .containsExactlyInAnyOrder(TableLocation.WINDOW, TableLocation.TERRACE);

        // When - Search restaurants
        List<Restaurant> foundRestaurants = restaurantRepository.searchByName("Integration");

        // Then - Verify search works
        assertThat(foundRestaurants).hasSize(1);
        assertThat(foundRestaurants.get(0).getId()).isEqualTo(savedRestaurant.getId());

        // When - Update restaurant
        savedRestaurant.setCapacity(80);
        savedRestaurant.setName("Updated Integration Restaurant");
        Restaurant updatedRestaurant = restaurantRepository.save(savedRestaurant);

        // Then - Verify update
        assertThat(updatedRestaurant.getCapacity()).isEqualTo(80);
        assertThat(updatedRestaurant.getName()).isEqualTo("Updated Integration Restaurant");
        assertThat(updatedRestaurant.getVersion()).isGreaterThan(savedRestaurant.getVersion());
    }

    @Test
    @DisplayName("Should handle Liquibase migrations correctly")
    void shouldHandleLiquibaseMigrationsCorrectly() {
        // Given - Check that sample data from migrations exists
        List<Restaurant> allRestaurants = restaurantRepository.findAll();

        // Then - Should have sample restaurants from migration
        assertThat(allRestaurants).isNotEmpty();
        
        // Verify specific sample data exists
        Optional<Restaurant> lePetitBistro = restaurantRepository.findByEmail("contact@petitbistro.fr");
        assertThat(lePetitBistro).isPresent();
        assertThat(lePetitBistro.get().getName()).isEqualTo("Le Petit Bistro");
        assertThat(lePetitBistro.get().getCapacity()).isEqualTo(50);

        // Verify tables exist for sample restaurant
        List<RestaurantTable> bistroTables = restaurantRepository.findTablesByRestaurantId(lePetitBistro.get().getId());
        assertThat(bistroTables).isNotEmpty();
    }

    @Test
    @DisplayName("Should handle concurrent access with optimistic locking")
    void shouldHandleConcurrentAccessWithOptimisticLocking() {
        // Given - Create and save a restaurant
        Restaurant restaurant = new Restaurant(
            "Concurrency Test Restaurant",
            "Concurrency Street",
            "+33 1 22 22 22 22",
            "concurrency@test.com",
            50
        );
        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        Long originalVersion = savedRestaurant.getVersion();

        // When - Update the restaurant
        savedRestaurant.setCapacity(60);
        Restaurant updatedRestaurant = restaurantRepository.save(savedRestaurant);

        // Then - Version should be incremented
        assertThat(updatedRestaurant.getVersion()).isGreaterThan(originalVersion);
    }

    @Test
    @DisplayName("Should enforce database constraints")
    void shouldEnforceDatabaseConstraints() {
        // Given - Create restaurant with unique email
        Restaurant restaurant1 = new Restaurant(
            "Unique Email Test 1",
            "Address 1",
            "Phone 1",
            "unique@test.com",
            30
        );
        restaurantRepository.save(restaurant1);

        // When & Then - Try to create another restaurant with same email
        Restaurant restaurant2 = new Restaurant(
            "Unique Email Test 2", 
            "Address 2",
            "Phone 2",
            "unique@test.com",
            40
        );
        
        assertThatThrownBy(() -> restaurantRepository.save(restaurant2))
            .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Should handle cascade operations correctly")
    void shouldHandleCascadeOperationsCorrectly() {
        // Given - Create restaurant with tables
        Restaurant restaurant = new Restaurant(
            "Cascade Test Restaurant",
            "Cascade Street",
            "Cascade Phone",
            "cascade@test.com",
            40
        );
        Restaurant savedRestaurant = restaurantRepository.save(restaurant);

        RestaurantTable table = new RestaurantTable(4, TableLocation.INDOOR);
        table.setRestaurantId(savedRestaurant.getId());
        RestaurantTable savedTable = restaurantRepository.saveTable(table);

        // When - Delete restaurant
        restaurantRepository.deleteById(savedRestaurant.getId());

        // Then - Restaurant and its tables should be deleted
        assertThat(restaurantRepository.findById(savedRestaurant.getId())).isEmpty();
        assertThat(restaurantRepository.findTableById(savedTable.getId())).isEmpty();
    }

    @Test
    @DisplayName("Should validate business rules through domain model")
    void shouldValidateBusinessRulesThroughDomainModel() {
        // Given - Create a restaurant
        Restaurant restaurant = new Restaurant(
            "Business Rules Test",
            "Business Street",
            "Business Phone",
            "business@test.com",
            30
        );

        // When - Set business hours
        restaurant.setOpeningTime(LocalTime.of(11, 0));
        restaurant.setClosingTime(LocalTime.of(23, 0));

        // Then - Should be able to save with valid data
        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        assertThat(savedRestaurant.getId()).isNotNull();
        assertThat(savedRestaurant.isActive()).isTrue();
        
        // Verify opening hours
        assertThat(savedRestaurant.getOpeningTime()).isEqualTo(LocalTime.of(11, 0));
        assertThat(savedRestaurant.getClosingTime()).isEqualTo(LocalTime.of(23, 0));
    }

    @Test
    @DisplayName("Should support complex queries and searches")
    void shouldSupportComplexQueriesAndSearches() {
        // Given - Create restaurants in different cities
        Restaurant parisRestaurant = new Restaurant(
            "Paris Gourmet",
            "123 Champs Élysées, Paris",
            "+33 1 33 33 33 33",
            "paris@gourmet.com",
            80
        );
        
        Restaurant lyonRestaurant = new Restaurant(
            "Lyon Bistro",
            "456 Place Bellecour, Lyon",
            "+33 4 44 44 44 44",
            "lyon@bistro.com",
            40
        );

        restaurantRepository.save(parisRestaurant);
        restaurantRepository.save(lyonRestaurant);

        // When - Search by city
        List<Restaurant> parisRestaurants = restaurantRepository.findByCity("Paris");
        List<Restaurant> lyonRestaurants = restaurantRepository.findByCity("Lyon");

        // Then - Should find correct restaurants
        assertThat(parisRestaurants).hasSize(1);
        assertThat(parisRestaurants.get(0).getName()).isEqualTo("Paris Gourmet");
        
        assertThat(lyonRestaurants).hasSize(1);
        assertThat(lyonRestaurants.get(0).getName()).isEqualTo("Lyon Bistro");

        // When - Search by name
        List<Restaurant> bistros = restaurantRepository.searchByName("Bistro");
        List<Restaurant> gourmet = restaurantRepository.searchByName("Gourmet");

        // Then - Should find by partial name match
        assertThat(bistros).hasSize(1);
        assertThat(bistros.get(0).getName()).isEqualTo("Lyon Bistro");
        
        assertThat(gourmet).hasSize(1);
        assertThat(gourmet.get(0).getName()).isEqualTo("Paris Gourmet");
    }
}