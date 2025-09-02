package com.restaurant.service.restaurant.domain.service;

import com.restaurant.service.restaurant.domain.model.Restaurant;
import com.restaurant.service.restaurant.domain.model.RestaurantTable;
import com.restaurant.service.restaurant.domain.model.TableLocation;
import com.restaurant.service.restaurant.domain.port.in.RestaurantManagementUseCase;
import com.restaurant.service.restaurant.domain.port.in.RestaurantManagementUseCase.*;
import com.restaurant.service.restaurant.domain.port.out.EventPublisherPort;
import com.restaurant.service.restaurant.domain.port.out.RestaurantRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RestaurantManagementUseCase implementation
 * Following TDD approach - testing the business logic before implementation
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Restaurant Management Use Case Tests")
class RestaurantManagementUseCaseTest {

    @Mock
    private RestaurantRepositoryPort restaurantRepository;

    @Mock
    private EventPublisherPort eventPublisher;

    private RestaurantManagementUseCase useCase;

    @BeforeEach
    void setUp() {
        // Will be implemented in step 7
        useCase = new RestaurantManagementUseCaseImpl(restaurantRepository, eventPublisher);
    }

    @Nested
    @DisplayName("Create Restaurant")
    class CreateRestaurant {

        @Test
        @DisplayName("Should create restaurant with valid command")
        void shouldCreateRestaurantWithValidCommand() {
            // Given
            CreateRestaurantCommand command = new CreateRestaurantCommand(
                    "Le Petit Bistro",
                    "123 Rue de la Paix, Paris",
                    "+33 1 42 86 87 88",
                    "contact@petitbistro.fr",
                    50,
                    LocalTime.of(11, 0),
                    LocalTime.of(23, 0)
            );

            Restaurant savedRestaurant = new Restaurant(
                    command.name(),
                    command.address(),
                    command.phoneNumber(),
                    command.email(),
                    command.capacity(),
                    command.openingTime(),
                    command.closingTime()
            );
            savedRestaurant.setId(1L);

            when(restaurantRepository.save(any(Restaurant.class))).thenReturn(savedRestaurant);

            // When
            Restaurant result = useCase.createRestaurant(command);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getName()).isEqualTo(command.name());
            assertThat(result.getAddress()).isEqualTo(command.address());
            assertThat(result.getCapacity()).isEqualTo(command.capacity());

            verify(restaurantRepository).save(any(Restaurant.class));
            verify(eventPublisher).publishEvent(any(EventPublisherPort.RestaurantCreatedEvent.class));
        }

        @Test
        @DisplayName("Should throw exception for invalid command")
        void shouldThrowExceptionForInvalidCommand() {
            // This test is actually working correctly - the exception IS thrown by the constructor
            // We just need to verify it's the right exception and message

            Exception exception = null;
            try {
                new CreateRestaurantCommand(
                        "", // Empty name - should fail validation
                        "123 Rue de la Paix, Paris",
                        "+33 1 42 86 87 88",
                        "contact@petitbistro.fr",
                        50,
                        LocalTime.of(11, 0),
                        LocalTime.of(23, 0)
                );
            } catch (IllegalArgumentException e) {
                exception = e;
            }

            // Then
            assertThat(exception).isNotNull();
            assertThat(exception).isInstanceOf(IllegalArgumentException.class);
            assertThat(exception.getMessage()).contains("Restaurant name cannot be null or empty");

            // No repository interactions expected since validation fails at constructor
            verify(restaurantRepository, never()).save(any());
            verify(eventPublisher, never()).publishEvent(any());
        }
    }

    @Nested
    @DisplayName("Update Restaurant")
    class UpdateRestaurant {

        @Test
        @DisplayName("Should update existing restaurant")
        void shouldUpdateExistingRestaurant() {
            // Given
            Long restaurantId = 1L;
            UpdateRestaurantCommand command = new UpdateRestaurantCommand(
                    restaurantId,
                    "Le Grand Bistro", // Updated name
                    "456 Avenue des Champs, Paris", // Updated address
                    "+33 1 42 86 87 99", // Updated phone
                    "info@grandbistro.fr", // Updated email
                    80, // Updated capacity
                    LocalTime.of(10, 0), // Updated opening time
                    LocalTime.of(23, 30) // FIXED: was 24:00 - now valid hour
            );

            Restaurant existingRestaurant = new Restaurant(
                    "Le Petit Bistro",
                    "123 Rue de la Paix, Paris",
                    "+33 1 42 86 87 88",
                    "contact@petitbistro.fr",
                    50
            );
            existingRestaurant.setId(restaurantId);

            when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(existingRestaurant));
            when(restaurantRepository.save(any(Restaurant.class))).thenReturn(existingRestaurant);

            // When
            Restaurant result = useCase.updateRestaurant(command);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo(command.name());
            assertThat(result.getAddress()).isEqualTo(command.address());
            assertThat(result.getCapacity()).isEqualTo(command.capacity());

            verify(restaurantRepository).findById(restaurantId);
            verify(restaurantRepository).save(existingRestaurant);
        }

        @Test
        @DisplayName("Should throw exception when restaurant not found")
        void shouldThrowExceptionWhenRestaurantNotFound() {
            // Given
            Long restaurantId = 999L;
            UpdateRestaurantCommand command = new UpdateRestaurantCommand(
                    restaurantId,
                    "Le Grand Bistro",
                    "456 Avenue des Champs, Paris",
                    "+33 1 42 86 87 99",
                    "info@grandbistro.fr",
                    80,
                    LocalTime.of(10, 0),
                    LocalTime.of(23, 30) // FIXED: was 24:00 - now valid hour
            );

            when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> useCase.updateRestaurant(command))
                    .isInstanceOf(RestaurantNotFoundException.class)
                    .hasMessageContaining("Restaurant not found with ID: 999");

            verify(restaurantRepository).findById(restaurantId);
            verify(restaurantRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Get Restaurant")
    class GetRestaurant {

        @Test
        @DisplayName("Should return restaurant when found")
        void shouldReturnRestaurantWhenFound() {
            // Given
            Long restaurantId = 1L;
            Restaurant restaurant = new Restaurant(
                    "Le Petit Bistro",
                    "123 Rue de la Paix, Paris",
                    "+33 1 42 86 87 88",
                    "contact@petitbistro.fr",
                    50
            );
            restaurant.setId(restaurantId);

            when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));

            // When
            Restaurant result = useCase.getRestaurant(restaurantId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(restaurantId);
            assertThat(result.getName()).isEqualTo("Le Petit Bistro");

            verify(restaurantRepository).findById(restaurantId);
        }

        @Test
        @DisplayName("Should throw exception when restaurant not found")
        void shouldThrowExceptionWhenRestaurantNotFound() {
            // Given
            Long restaurantId = 999L;

            when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> useCase.getRestaurant(restaurantId))
                    .isInstanceOf(RestaurantNotFoundException.class)
                    .hasMessageContaining("Restaurant not found with ID: 999");

            verify(restaurantRepository).findById(restaurantId);
        }
    }

    @Nested
    @DisplayName("List Restaurants")
    class ListRestaurants {

        @Test
        @DisplayName("Should return all restaurants")
        void shouldReturnAllRestaurants() {
            // Given
            List<Restaurant> restaurants = List.of(
                    createRestaurant(1L, "Bistro 1"),
                    createRestaurant(2L, "Bistro 2")
            );

            when(restaurantRepository.findAll()).thenReturn(restaurants);

            // When
            List<Restaurant> result = useCase.getAllRestaurants();

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getName()).isEqualTo("Bistro 1");
            assertThat(result.get(1).getName()).isEqualTo("Bistro 2");

            verify(restaurantRepository).findAll();
        }

        @Test
        @DisplayName("Should return only active restaurants")
        void shouldReturnOnlyActiveRestaurants() {
            // Given
            List<Restaurant> activeRestaurants = List.of(
                    createRestaurant(1L, "Active Bistro 1"),
                    createRestaurant(2L, "Active Bistro 2")
            );

            when(restaurantRepository.findAllActive()).thenReturn(activeRestaurants);

            // When
            List<Restaurant> result = useCase.getActiveRestaurants();

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).allMatch(Restaurant::isActive);

            verify(restaurantRepository).findAllActive();
        }
    }

    @Nested
    @DisplayName("Restaurant Status Management")
    class RestaurantStatusManagement {

        @Test
        @DisplayName("Should activate restaurant")
        void shouldActivateRestaurant() {
            // Given
            Long restaurantId = 1L;
            Restaurant restaurant = createRestaurant(restaurantId, "Test Bistro");
            restaurant.deactivate();

            when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
            when(restaurantRepository.save(restaurant)).thenReturn(restaurant);

            // When
            Restaurant result = useCase.activateRestaurant(restaurantId);

            // Then
            assertThat(result.isActive()).isTrue();

            verify(restaurantRepository).findById(restaurantId);
            verify(restaurantRepository).save(restaurant);
            verify(eventPublisher).publishEvent(any(EventPublisherPort.RestaurantStatusChangedEvent.class));
        }

        @Test
        @DisplayName("Should deactivate restaurant")
        void shouldDeactivateRestaurant() {
            // Given
            Long restaurantId = 1L;
            Restaurant restaurant = createRestaurant(restaurantId, "Test Bistro");

            when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
            when(restaurantRepository.save(restaurant)).thenReturn(restaurant);

            // When
            Restaurant result = useCase.deactivateRestaurant(restaurantId);

            // Then
            assertThat(result.isActive()).isFalse();

            verify(restaurantRepository).findById(restaurantId);
            verify(restaurantRepository).save(restaurant);
            verify(eventPublisher).publishEvent(any(EventPublisherPort.RestaurantStatusChangedEvent.class));
        }
    }

    @Nested
    @DisplayName("Search Restaurants")
    class SearchRestaurants {

        @Test
        @DisplayName("Should search restaurants by name")
        void shouldSearchRestaurantsByName() {
            // Given
            String searchTerm = "Bistro";
            List<Restaurant> restaurants = List.of(
                    createRestaurant(1L, "Le Petit Bistro"),
                    createRestaurant(2L, "Grand Bistro")
            );

            when(restaurantRepository.findByNameContaining(searchTerm)).thenReturn(restaurants);

            // When
            List<Restaurant> result = useCase.searchRestaurantsByName(searchTerm);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).allMatch(r -> r.getName().contains("Bistro"));

            verify(restaurantRepository).findByNameContaining(searchTerm);
        }

        @Test
        @DisplayName("Should search restaurants by city")
        void shouldSearchRestaurantsByCity() {
            // Given
            String city = "Paris";
            List<Restaurant> restaurants = List.of(
                    createRestaurant(1L, "Bistro Paris 1"),
                    createRestaurant(2L, "Bistro Paris 2")
            );

            when(restaurantRepository.findByCity(city)).thenReturn(restaurants);

            // When
            List<Restaurant> result = useCase.searchRestaurantsByCity(city);

            // Then
            assertThat(result).hasSize(2);

            verify(restaurantRepository).findByCity(city);
        }
    }

    @Nested
    @DisplayName("Restaurant Statistics")
    class RestaurantStatistics {

        @Test
        @DisplayName("Should get restaurant statistics")
        void shouldGetRestaurantStatistics() {
            // Given
            Long restaurantId = 1L;
            Restaurant restaurant = createRestaurant(restaurantId, "Test Bistro");

            // Add some tables
            restaurant.addTable(new RestaurantTable(4, TableLocation.WINDOW));
            restaurant.addTable(new RestaurantTable(6, TableLocation.TERRACE));
            restaurant.addTable(new RestaurantTable(2, TableLocation.INDOOR));

            when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));

            // When
            RestaurantStats result = useCase.getRestaurantStats(restaurantId);

            // Then
            assertThat(result.restaurantId()).isEqualTo(restaurantId);
            assertThat(result.totalTables()).isEqualTo(3);
            assertThat(result.totalSeats()).isEqualTo(12);
            assertThat(result.averageTableSize()).isEqualTo(4.0);
            assertThat(result.active()).isTrue();

            verify(restaurantRepository).findById(restaurantId);
        }
    }

    @Nested
    @DisplayName("Delete Restaurant")
    class DeleteRestaurant {

        @Test
        @DisplayName("Should delete restaurant when exists")
        void shouldDeleteRestaurantWhenExists() {
            // Given
            Long restaurantId = 1L;
            Restaurant restaurant = createRestaurant(restaurantId, "Test Bistro");

            when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));

            // When
            useCase.deleteRestaurant(restaurantId);

            // Then
            verify(restaurantRepository).findById(restaurantId);
            verify(restaurantRepository).deleteById(restaurantId);
        }

        @Test
        @DisplayName("Should throw exception when restaurant not found for deletion")
        void shouldThrowExceptionWhenRestaurantNotFoundForDeletion() {
            // Given
            Long restaurantId = 999L;

            when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> useCase.deleteRestaurant(restaurantId))
                    .isInstanceOf(RestaurantNotFoundException.class);

            verify(restaurantRepository).findById(restaurantId);
            verify(restaurantRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("Should not delete restaurant with active reservations")
        void shouldNotDeleteRestaurantWithActiveReservations() {
            // Given
            Long restaurantId = 1L;
            Restaurant restaurant = createRestaurant(restaurantId, "Test Bistro");

            // Simulate restaurant with tables (which might have reservations)
            restaurant.addTable(new RestaurantTable(4, TableLocation.WINDOW));

            when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));

            // When & Then
            // This business rule will be implemented in the use case
            // For now, we expect it to throw an exception
            assertThatThrownBy(() -> useCase.deleteRestaurant(restaurantId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Cannot delete restaurant with existing tables");

            verify(restaurantRepository).findById(restaurantId);
            verify(restaurantRepository, never()).deleteById(any());
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
}