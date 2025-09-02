package com.restaurant.service.restaurant.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for Restaurant domain entity
 * Testing business rules and invariants following TDD approach
 */
@DisplayName("Restaurant Domain Entity Tests")
class RestaurantTest {

    @Nested
    @DisplayName("Restaurant Creation")
    class RestaurantCreation {

        @Test
        @DisplayName("Should create restaurant with valid data")
        void shouldCreateRestaurantWithValidData() {
            
            String name = "Le Petit Bistro";
            String address = "123 Rue de la Paix, Paris";
            String phoneNumber = "+33 1 42 86 87 88";
            String email = "contact@petitbistro.fr";
            int capacity = 50;

            
            Restaurant restaurant = new Restaurant(name, address, phoneNumber, email, capacity);

            
            assertThat(restaurant.getName()).isEqualTo(name);
            assertThat(restaurant.getAddress()).isEqualTo(address);
            assertThat(restaurant.getPhoneNumber()).isEqualTo(phoneNumber);
            assertThat(restaurant.getEmail()).isEqualTo(email);
            assertThat(restaurant.getCapacity()).isEqualTo(capacity);
            assertThat(restaurant.isActive()).isTrue();
            assertThat(restaurant.getTables()).isNotNull().isEmpty();
            assertThat(restaurant.getId()).isNull(); 
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("Should throw exception when name is null, empty or blank")
        void shouldThrowExceptionWhenNameIsInvalid(String invalidName) {
            
            String address = "123 Rue de la Paix, Paris";
            String phoneNumber = "+33 1 42 86 87 88";
            String email = "contact@petitbistro.fr";
            int capacity = 50;

             
            assertThatThrownBy(() -> new Restaurant(invalidName, address, phoneNumber, email, capacity))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Restaurant name cannot be null or empty");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("Should throw exception when address is null, empty or blank")
        void shouldThrowExceptionWhenAddressIsInvalid(String invalidAddress) {
            
            String name = "Le Petit Bistro";
            String phoneNumber = "+33 1 42 86 87 88";
            String email = "contact@petitbistro.fr";
            int capacity = 50;

             
            assertThatThrownBy(() -> new Restaurant(name, invalidAddress, phoneNumber, email, capacity))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Restaurant address cannot be null or empty");
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, -10})
        @DisplayName("Should throw exception when capacity is zero or negative")
        void shouldThrowExceptionWhenCapacityIsInvalid(int invalidCapacity) {
            
            String name = "Le Petit Bistro";
            String address = "123 Rue de la Paix, Paris";
            String phoneNumber = "+33 1 42 86 87 88";
            String email = "contact@petitbistro.fr";

             
            assertThatThrownBy(() -> new Restaurant(name, address, phoneNumber, email, invalidCapacity))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Restaurant capacity must be positive");
        }

        @ParameterizedTest
        @ValueSource(strings = {"invalid-email", "test@", "@domain.com", "test.domain.com"})
        @DisplayName("Should throw exception when email format is invalid")
        void shouldThrowExceptionWhenEmailIsInvalid(String invalidEmail) {
            
            String name = "Le Petit Bistro";
            String address = "123 Rue de la Paix, Paris";
            String phoneNumber = "+33 1 42 86 87 88";
            int capacity = 50;

             
            assertThatThrownBy(() -> new Restaurant(name, address, phoneNumber, invalidEmail, capacity))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid email format");
        }

        @Test
        @DisplayName("Should allow empty email")
        void shouldAllowEmptyEmail() {
            
            String name = "Le Petit Bistro";
            String address = "123 Rue de la Paix, Paris";
            String phoneNumber = "+33 1 42 86 87 88";
            String emptyEmail = "";
            int capacity = 50;

            assertThatCode(() -> new Restaurant(name, address, phoneNumber, emptyEmail, capacity))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should allow null email")
        void shouldAllowNullEmail() {
            
            String name = "Le Petit Bistro";
            String address = "123 Rue de la Paix, Paris";
            String phoneNumber = "+33 1 42 86 87 88";
            String nullEmail = null;
            int capacity = 50;

            assertThatCode(() -> new Restaurant(name, address, phoneNumber, nullEmail, capacity))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should create restaurant with custom opening hours")
        void shouldCreateRestaurantWithCustomOpeningHours() {
            
            String name = "Le Petit Bistro";
            String address = "123 Rue de la Paix, Paris";
            String phoneNumber = "+33 1 42 86 87 88";
            String email = "contact@petitbistro.fr";
            int capacity = 50;
            LocalTime openingTime = LocalTime.of(10, 0);
            LocalTime closingTime = LocalTime.of(22, 0);

            
            Restaurant restaurant = new Restaurant(name, address, phoneNumber, email, capacity, openingTime, closingTime);

            
            assertThat(restaurant.getOpeningTime()).isEqualTo(openingTime);
            assertThat(restaurant.getClosingTime()).isEqualTo(closingTime);
        }

        @Test
        @DisplayName("Should throw exception when closing time is before opening time")
        void shouldThrowExceptionWhenClosingTimeIsBeforeOpeningTime() {
            
            String name = "Le Petit Bistro";
            String address = "123 Rue de la Paix, Paris";
            String phoneNumber = "+33 1 42 86 87 88";
            String email = "contact@petitbistro.fr";
            int capacity = 50;
            LocalTime openingTime = LocalTime.of(22, 0);
            LocalTime closingTime = LocalTime.of(10, 0); // Before opening time

             
            assertThatThrownBy(() -> new Restaurant(name, address, phoneNumber, email, capacity, openingTime, closingTime))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Closing time must be after opening time");
        }
    }

    @Nested
    @DisplayName("Restaurant Business Operations")
    class RestaurantBusinessOperations {

        @Test
        @DisplayName("Should activate restaurant")
        void shouldActivateRestaurant() {
            
            Restaurant restaurant = createValidRestaurant();
            restaurant.deactivate();

            
            restaurant.activate();

            
            assertThat(restaurant.isActive()).isTrue();
        }

        @Test
        @DisplayName("Should deactivate restaurant")
        void shouldDeactivateRestaurant() {
            
            Restaurant restaurant = createValidRestaurant();

            
            restaurant.deactivate();

            
            assertThat(restaurant.isActive()).isFalse();
        }

        @Test
        @DisplayName("Should add table to restaurant")
        void shouldAddTableToRestaurant() {
            
            Restaurant restaurant = createValidRestaurant();
            RestaurantTable table = new RestaurantTable(4, TableLocation.WINDOW);

            
            restaurant.addTable(table);

            
            assertThat(restaurant.getTables()).hasSize(1);
            assertThat(restaurant.getTables()).contains(table);
            assertThat(table.getRestaurant()).isEqualTo(restaurant);
        }

        @Test
        @DisplayName("Should not add null table")
        void shouldNotAddNullTable() {
            
            Restaurant restaurant = createValidRestaurant();

             
            assertThatThrownBy(() -> restaurant.addTable(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Table cannot be null");
        }

        @Test
        @DisplayName("Should remove table from restaurant")
        void shouldRemoveTableFromRestaurant() {
            
            Restaurant restaurant = createValidRestaurant();
            RestaurantTable table = new RestaurantTable(4, TableLocation.WINDOW);
            restaurant.addTable(table);

            
            restaurant.removeTable(table);

            
            assertThat(restaurant.getTables()).isEmpty();
            assertThat(table.getRestaurant()).isNull();
        }

        @Test
        @DisplayName("Should calculate total available seats")
        void shouldCalculateTotalAvailableSeats() {
            
            Restaurant restaurant = createValidRestaurant();
            restaurant.addTable(new RestaurantTable(4, TableLocation.WINDOW));
            restaurant.addTable(new RestaurantTable(2, TableLocation.TERRACE));
            restaurant.addTable(new RestaurantTable(6, TableLocation.INDOOR));

            
            int totalSeats = restaurant.getTotalAvailableSeats();

            
            assertThat(totalSeats).isEqualTo(12);
        }

        @Test
        @DisplayName("Should return zero seats when no tables")
        void shouldReturnZeroSeatsWhenNoTables() {
            
            Restaurant restaurant = createValidRestaurant();

            
            int totalSeats = restaurant.getTotalAvailableSeats();

            
            assertThat(totalSeats).isZero();
        }
    }

    @Nested
    @DisplayName("Restaurant Equality and Hash Code")
    class RestaurantEqualityAndHashCode {

        @Test
        @DisplayName("Should be equal when same id")
        void shouldBeEqualWhenSameId() {
            
            Restaurant restaurant1 = createValidRestaurant();
            Restaurant restaurant2 = createValidRestaurant();

            // Simulate persistence
            restaurant1.setId(1L);
            restaurant2.setId(1L);

             
            assertThat(restaurant1).isEqualTo(restaurant2);
            assertThat(restaurant1.hashCode()).isEqualTo(restaurant2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when different ids")
        void shouldNotBeEqualWhenDifferentIds() {
            
            Restaurant restaurant1 = createValidRestaurant();
            Restaurant restaurant2 = createValidRestaurant();

            // Simulate persistence
            restaurant1.setId(1L);
            restaurant2.setId(2L);

             
            assertThat(restaurant1).isNotEqualTo(restaurant2);
        }

        @Test
        @DisplayName("Should be equal when both ids are null and same business key")
        void shouldBeEqualWhenBothIdsNullAndSameBusinessKey() {
            
            String name = "Le Petit Bistro";
            String address = "123 Rue de la Paix, Paris";
            String phoneNumber = "+33 1 42 86 87 88";
            String email = "contact@petitbistro.fr";
            int capacity = 50;

            Restaurant restaurant1 = new Restaurant(name, address, phoneNumber, email, capacity);
            Restaurant restaurant2 = new Restaurant(name, address, phoneNumber, email, capacity);

             
            assertThat(restaurant1).isEqualTo(restaurant2);
            assertThat(restaurant1.hashCode()).isEqualTo(restaurant2.hashCode());
        }
    }

    // Helper method
    private Restaurant createValidRestaurant() {
        return new Restaurant(
                "Le Petit Bistro",
                "123 Rue de la Paix, Paris",
                "+33 1 42 86 87 88",
                "contact@petitbistro.fr",
                50
        );
    }
}