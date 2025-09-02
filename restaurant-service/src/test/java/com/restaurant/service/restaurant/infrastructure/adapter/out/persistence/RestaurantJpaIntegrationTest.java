package com.restaurant.service.restaurant.infrastructure.adapter.out.persistence;

import com.restaurant.service.restaurant.domain.model.Restaurant;
import com.restaurant.service.restaurant.domain.port.out.RestaurantRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for RestaurantPersistenceAdapter
 * Tests the persistence adapter with an actual database (H2 in-memory)
 */
@DataJpaTest
@Import({RestaurantPersistenceAdapter.class})
@ActiveProfiles("test")
@DisplayName("Restaurant Persistence Adapter Integration Tests")
class RestaurantJpaIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RestaurantRepositoryPort restaurantRepositoryPort;

    @Test
    @DisplayName("Should save and find restaurant by ID")
    void shouldSaveAndFindRestaurantById() {
        // Given
        Restaurant restaurant = new Restaurant(
                "Le Petit Bistro",
                "123 Rue de la Paix, Paris",
                "+33 1 42 86 87 88",
                "contact@petitbistro.fr",
                50
        );

        // When
        Restaurant saved = restaurantRepositoryPort.save(restaurant);
        entityManager.flush();
        entityManager.clear();

        Optional<Restaurant> found = restaurantRepositoryPort.findById(saved.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Le Petit Bistro");
        assertThat(found.get().getAddress()).isEqualTo("123 Rue de la Paix, Paris");
        assertThat(found.get().getCapacity()).isEqualTo(50);
        assertThat(found.get().isActive()).isTrue();
    }

    @Test
    @DisplayName("Should find all active restaurants")
    void shouldFindAllActiveRestaurants() {
        // Given
        Restaurant activeRestaurant = new Restaurant(
                "Active Restaurant", "Address 1", "+33123456789",
                "active@test.com", 30);
        Restaurant inactiveRestaurant = new Restaurant(
                "Inactive Restaurant", "Address 2", "+33987654321",
                "inactive@test.com", 40);
        inactiveRestaurant.deactivate();

        restaurantRepositoryPort.save(activeRestaurant);
        restaurantRepositoryPort.save(inactiveRestaurant);
        entityManager.flush();

        // When
        List<Restaurant> activeRestaurants = restaurantRepositoryPort.findAllActive();

        // Then
        assertThat(activeRestaurants).hasSize(1);
        assertThat(activeRestaurants.get(0).getName()).isEqualTo("Active Restaurant");
        assertThat(activeRestaurants.get(0).isActive()).isTrue();
    }

    @Test
    @DisplayName("Should find restaurants by name containing")
    void shouldFindRestaurantsByNameContaining() {
        // Given
        Restaurant bistro1 = new Restaurant(
                "Le Petit Bistro", "Address 1", "+33123456789",
                "bistro1@test.com", 30);
        Restaurant bistro2 = new Restaurant(
                "Grand Bistro Central", "Address 2", "+33987654321",
                "bistro2@test.com", 50);
        Restaurant restaurant = new Restaurant(
                "Fine Restaurant", "Address 3", "+33555666777",
                "restaurant@test.com", 40);

        restaurantRepositoryPort.save(bistro1);
        restaurantRepositoryPort.save(bistro2);
        restaurantRepositoryPort.save(restaurant);
        entityManager.flush();

        // When
        List<Restaurant> bistros = restaurantRepositoryPort.findByNameContaining("bistro");

        // Then
        assertThat(bistros).hasSize(2);
        assertThat(bistros)
                .extracting(Restaurant::getName)
                .contains("Le Petit Bistro", "Grand Bistro Central");
    }

    @Test
    @DisplayName("Should find restaurants by city")
    void shouldFindRestaurantsByCity() {
        // Given
        Restaurant parisRestaurant = new Restaurant(
                "Paris Bistro", "123 Rue de Rivoli, Paris", "+33123456789",
                "paris@test.com", 30);
        Restaurant lyonRestaurant = new Restaurant(
                "Lyon Bouchon", "456 Rue de la République, Lyon", "+33987654321",
                "lyon@test.com", 25);
        Restaurant marseilleRestaurant = new Restaurant(
                "Marseille Seafood", "789 Vieux Port, Marseille", "+33555666777",
                "marseille@test.com", 40);

        restaurantRepositoryPort.save(parisRestaurant);
        restaurantRepositoryPort.save(lyonRestaurant);
        restaurantRepositoryPort.save(marseilleRestaurant);
        entityManager.flush();

        // When
        List<Restaurant> parisRestaurants = restaurantRepositoryPort.findByCity("Paris");
        List<Restaurant> lyonRestaurants = restaurantRepositoryPort.findByCity("Lyon");

        // Then
        assertThat(parisRestaurants).hasSize(1);
        assertThat(parisRestaurants.get(0).getName()).isEqualTo("Paris Bistro");

        assertThat(lyonRestaurants).hasSize(1);
        assertThat(lyonRestaurants.get(0).getName()).isEqualTo("Lyon Bouchon");
    }

    @Test
    @DisplayName("Should count active restaurants")
    void shouldCountActiveRestaurants() {
        // Given
        Restaurant active1 = new Restaurant(
                "Active 1", "Address 1", "+33123456789", "active1@test.com", 30);
        Restaurant active2 = new Restaurant(
                "Active 2", "Address 2", "+33987654321", "active2@test.com", 40);
        Restaurant inactive = new Restaurant(
                "Inactive", "Address 3", "+33555666777", "inactive@test.com", 50);
        inactive.deactivate();

        restaurantRepositoryPort.save(active1);
        restaurantRepositoryPort.save(active2);
        restaurantRepositoryPort.save(inactive);
        entityManager.flush();

        // When
        long activeCount = restaurantRepositoryPort.countActive();
        long totalCount = restaurantRepositoryPort.count();

        // Then
        assertThat(activeCount).isEqualTo(2);
        assertThat(totalCount).isEqualTo(3);
    }

    @Test
    @DisplayName("Should handle restaurant with custom opening hours")
    void shouldHandleRestaurantWithCustomOpeningHours() {
        // Given
        Restaurant restaurant = new Restaurant(
                "Morning Cafe", "Address", "+33123456789", "cafe@test.com", 20,
                LocalTime.of(7, 0), LocalTime.of(18, 0));

        // When
        Restaurant saved = restaurantRepositoryPort.save(restaurant);
        entityManager.flush();
        entityManager.clear();

        Optional<Restaurant> found = restaurantRepositoryPort.findById(saved.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getOpeningTime()).isEqualTo(LocalTime.of(7, 0));
        assertThat(found.get().getClosingTime()).isEqualTo(LocalTime.of(18, 0));
    }
}