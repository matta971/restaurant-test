package com.restaurant.service.restaurant.infrastructure.adapter.out.persistence;

import com.restaurant.service.restaurant.infrastructure.adapter.out.persistence.entity.RestaurantEntity;
import com.restaurant.service.restaurant.infrastructure.adapter.out.persistence.entity.RestaurantTableEntity;
import com.restaurant.service.restaurant.infrastructure.adapter.out.persistence.entity.RestaurantTableEntity.TableLocationEntity;
import com.restaurant.service.restaurant.infrastructure.adapter.out.persistence.repository.RestaurantJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for RestaurantJpaRepository
 * Tests JPA repository methods with real database
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Restaurant JPA Repository Tests")
class RestaurantJpaRepositoryTest {

    @Autowired
    private RestaurantJpaRepository restaurantRepository;

    @Autowired
    private TestEntityManager entityManager;

    private RestaurantEntity testRestaurant;

    @BeforeEach
    void setUp() {
        testRestaurant = new RestaurantEntity(
            "Test Restaurant",
            "123 Test Street, Paris",
            "+33 1 23 45 67 89",
            "test@restaurant.com",
            50
        );
        testRestaurant.setOpeningTime(LocalTime.of(11, 0));
        testRestaurant.setClosingTime(LocalTime.of(23, 0));
    }

    @Test
    @DisplayName("Should save and find restaurant by ID")
    void shouldSaveAndFindRestaurantById() {
        // When
        RestaurantEntity saved = restaurantRepository.save(testRestaurant);
        entityManager.flush(); // Force persistence
        entityManager.clear(); // Clear persistence context

        // Then
        Optional<RestaurantEntity> found = restaurantRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test Restaurant");
        assertThat(found.get().getEmail()).isEqualTo("test@restaurant.com");
        assertThat(found.get().getActive()).isTrue();
    }

    @Test
    @DisplayName("Should find restaurant by email")
    void shouldFindRestaurantByEmail() {
        // Given
        restaurantRepository.save(testRestaurant);
        entityManager.flush();

        // When
        Optional<RestaurantEntity> found = restaurantRepository.findByEmail("test@restaurant.com");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test Restaurant");
    }

    @Test
    @DisplayName("Should return empty when restaurant email not found")
    void shouldReturnEmptyWhenRestaurantEmailNotFound() {
        // When
        Optional<RestaurantEntity> found = restaurantRepository.findByEmail("nonexistent@restaurant.com");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should find active restaurants only")
    void shouldFindActiveRestaurantsOnly() {
        // Given
        RestaurantEntity activeRestaurant = new RestaurantEntity("Active Restaurant", "Address 1", "Phone 1", "active@test.com", 30);
        RestaurantEntity inactiveRestaurant = new RestaurantEntity("Inactive Restaurant", "Address 2", "Phone 2", "inactive@test.com", 40);
        inactiveRestaurant.setActive(false);

        restaurantRepository.save(activeRestaurant);
        restaurantRepository.save(inactiveRestaurant);
        entityManager.flush();

        // When
        List<RestaurantEntity> activeRestaurants = restaurantRepository.findByActiveTrue();

        // Then
        assertThat(activeRestaurants).hasSize(1);
        assertThat(activeRestaurants.get(0).getName()).isEqualTo("Active Restaurant");
    }

    @Test
    @DisplayName("Should find restaurants by name containing (case insensitive)")
    void shouldFindRestaurantsByNameContaining() {
        // Given
        RestaurantEntity bistro1 = new RestaurantEntity("Italian Bistro", "Address 1", "Phone 1", "italian@test.com", 30);
        RestaurantEntity bistro2 = new RestaurantEntity("french bistro", "Address 2", "Phone 2", "french@test.com", 40);
        RestaurantEntity pizza = new RestaurantEntity("Pizza Palace", "Address 3", "Phone 3", "pizza@test.com", 20);

        restaurantRepository.save(bistro1);
        restaurantRepository.save(bistro2);
        restaurantRepository.save(pizza);
        entityManager.flush();

        // When
        List<RestaurantEntity> bistros = restaurantRepository.findByNameContainingIgnoreCase("BISTRO");

        // Then
        assertThat(bistros).hasSize(2);
        assertThat(bistros).extracting(RestaurantEntity::getName)
            .containsExactlyInAnyOrder("Italian Bistro", "french bistro");
    }

    @Test
    @DisplayName("Should find restaurants by city in address")
    void shouldFindRestaurantsByCity() {
        // Given
        RestaurantEntity parisRestaurant = new RestaurantEntity("Paris Restaurant", "123 Street, Paris", "Phone 1", "paris@test.com", 30);
        RestaurantEntity lyonRestaurant = new RestaurantEntity("Lyon Restaurant", "456 Avenue, Lyon", "Phone 2", "lyon@test.com", 40);
        RestaurantEntity marseilleRestaurant = new RestaurantEntity("Marseille Restaurant", "789 Boulevard, Marseille", "Phone 3", "marseille@test.com", 50);

        restaurantRepository.save(parisRestaurant);
        restaurantRepository.save(lyonRestaurant);
        restaurantRepository.save(marseilleRestaurant);
        entityManager.flush();

        // When
        List<RestaurantEntity> parisRestaurants = restaurantRepository.findByCity("Paris");

        // Then
        assertThat(parisRestaurants).hasSize(1);
        assertThat(parisRestaurants.get(0).getName()).isEqualTo("Paris Restaurant");
    }

    @Test
    @DisplayName("Should find restaurants with minimum capacity")
    void shouldFindRestaurantsWithMinimumCapacity() {
        // Given
        RestaurantEntity smallRestaurant = new RestaurantEntity("Small Restaurant", "Address 1", "Phone 1", "small@test.com", 20);
        RestaurantEntity mediumRestaurant = new RestaurantEntity("Medium Restaurant", "Address 2", "Phone 2", "medium@test.com", 50);
        RestaurantEntity largeRestaurant = new RestaurantEntity("Large Restaurant", "Address 3", "Phone 3", "large@test.com", 100);

        restaurantRepository.save(smallRestaurant);
        restaurantRepository.save(mediumRestaurant);
        restaurantRepository.save(largeRestaurant);
        entityManager.flush();

        // When
        List<RestaurantEntity> largeEnoughRestaurants = restaurantRepository.findByCapacityGreaterThanEqual(50);

        // Then
        assertThat(largeEnoughRestaurants).hasSize(2);
        assertThat(largeEnoughRestaurants).extracting(RestaurantEntity::getName)
            .containsExactlyInAnyOrder("Medium Restaurant", "Large Restaurant");
    }

    @Test
    @DisplayName("Should check if email exists for another restaurant")
    void shouldCheckIfEmailExistsForAnotherRestaurant() {
        // Given
        RestaurantEntity saved = restaurantRepository.save(testRestaurant);
        entityManager.flush();

        // When
        boolean exists = restaurantRepository.existsByEmailAndIdNot("test@restaurant.com", saved.getId());
        boolean existsForAnother = restaurantRepository.existsByEmailAndIdNot("test@restaurant.com", 999L);

        // Then
        assertThat(exists).isFalse(); // Same restaurant
        assertThat(existsForAnother).isTrue(); // Different restaurant
    }

    @Test
    @DisplayName("Should count active restaurants")
    void shouldCountActiveRestaurants() {
        // Given
        RestaurantEntity active1 = new RestaurantEntity("Active 1", "Address 1", "Phone 1", "active1@test.com", 30);
        RestaurantEntity active2 = new RestaurantEntity("Active 2", "Address 2", "Phone 2", "active2@test.com", 40);
        RestaurantEntity inactive = new RestaurantEntity("Inactive", "Address 3", "Phone 3", "inactive@test.com", 50);
        inactive.setActive(false);

        restaurantRepository.save(active1);
        restaurantRepository.save(active2);
        restaurantRepository.save(inactive);
        entityManager.flush();

        // When
        long activeCount = restaurantRepository.countByActiveTrue();

        // Then
        assertThat(activeCount).isEqualTo(2);
    }

    @Test
    @DisplayName("Should find restaurants with available tables")
    void shouldFindRestaurantsWithAvailableTables() {
        // Given
        RestaurantEntity restaurant = restaurantRepository.save(testRestaurant);
        
        RestaurantTableEntity availableTable = new RestaurantTableEntity(4, TableLocationEntity.WINDOW);
        availableTable.setRestaurant(restaurant);
        availableTable.setAvailable(true);
        
        RestaurantTableEntity unavailableTable = new RestaurantTableEntity(6, TableLocationEntity.TERRACE);
        unavailableTable.setRestaurant(restaurant);
        unavailableTable.setAvailable(false);
        
        restaurant.addTable(availableTable);
        restaurant.addTable(unavailableTable);
        
        entityManager.persist(availableTable);
        entityManager.persist(unavailableTable);
        entityManager.flush();

        // When
        List<RestaurantEntity> restaurantsWithAvailableTables = restaurantRepository.findRestaurantsWithAvailableTables();

        // Then
        assertThat(restaurantsWithAvailableTables).hasSize(1);
        assertThat(restaurantsWithAvailableTables.get(0).getId()).isEqualTo(restaurant.getId());
    }

    @Test
    @DisplayName("Should find restaurants by party size")
    void shouldFindRestaurantsByPartySize() {
        // Given
        RestaurantEntity restaurant = restaurantRepository.save(testRestaurant);
        
        RestaurantTableEntity smallTable = new RestaurantTableEntity(2, TableLocationEntity.WINDOW);
        smallTable.setRestaurant(restaurant);
        smallTable.setAvailable(true);
        
        RestaurantTableEntity largeTable = new RestaurantTableEntity(8, TableLocationEntity.TERRACE);
        largeTable.setRestaurant(restaurant);
        largeTable.setAvailable(true);
        
        restaurant.addTable(smallTable);
        restaurant.addTable(largeTable);
        
        entityManager.persist(smallTable);
        entityManager.persist(largeTable);
        entityManager.flush();

        // When
        List<RestaurantEntity> restaurantsForLargeParty = restaurantRepository.findRestaurantsByPartySize(6);

        // Then
        assertThat(restaurantsForLargeParty).hasSize(1);
        assertThat(restaurantsForLargeParty.get(0).getId()).isEqualTo(restaurant.getId());
    }

    @Test
    @DisplayName("Should find restaurant with tables using fetch join")
    void shouldFindRestaurantWithTablesUsingFetchJoin() {
        // Given
        RestaurantEntity restaurant = restaurantRepository.save(testRestaurant);
        
        RestaurantTableEntity table1 = new RestaurantTableEntity(4, TableLocationEntity.WINDOW);
        table1.setRestaurant(restaurant);
        RestaurantTableEntity table2 = new RestaurantTableEntity(6, TableLocationEntity.TERRACE);
        table2.setRestaurant(restaurant);
        
        restaurant.addTable(table1);
        restaurant.addTable(table2);
        
        entityManager.persist(table1);
        entityManager.persist(table2);
        entityManager.flush();
        entityManager.clear(); // Clear to ensure fetch join is tested

        // When
        Optional<RestaurantEntity> found = restaurantRepository.findByIdWithTables(restaurant.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getTables()).hasSize(2);
        // Should not trigger additional queries due to fetch join
    }

    @Test
    @DisplayName("Should handle pagination correctly")
    void shouldHandlePaginationCorrectly() {
        // Given - Create 5 restaurants
        for (int i = 1; i <= 5; i++) {
            RestaurantEntity restaurant = new RestaurantEntity(
                "Restaurant " + i,
                "Address " + i,
                "Phone " + i,
                "email" + i + "@test.com",
                30
            );
            restaurantRepository.save(restaurant);
        }
        entityManager.flush();

        // When
        Page<RestaurantEntity> firstPage = restaurantRepository.findByActiveTrue(PageRequest.of(0, 2));
        Page<RestaurantEntity> secondPage = restaurantRepository.findByActiveTrue(PageRequest.of(1, 2));

        // Then
        assertThat(firstPage.getContent()).hasSize(2);
        assertThat(firstPage.getTotalElements()).isEqualTo(5);
        assertThat(firstPage.getTotalPages()).isEqualTo(3);
        assertThat(firstPage.isFirst()).isTrue();
        assertThat(firstPage.isLast()).isFalse();

        assertThat(secondPage.getContent()).hasSize(2);
        assertThat(secondPage.isFirst()).isFalse();
        assertThat(secondPage.isLast()).isFalse();
    }
}