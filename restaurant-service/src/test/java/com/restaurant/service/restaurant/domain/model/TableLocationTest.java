package com.restaurant.service.restaurant.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for TableLocation enum
 * Testing enum values and business logic
 */
@DisplayName("TableLocation Enum Tests")
class TableLocationTest {

    @Test
    @DisplayName("Should have all expected table locations")
    void shouldHaveAllExpectedTableLocations() {
        TableLocation[] locations = TableLocation.values();

        assertThat(locations).hasSize(4);
        assertThat(locations).contains(
            TableLocation.WINDOW,
            TableLocation.TERRACE,
            TableLocation.INDOOR,
            TableLocation.PRIVATE_ROOM
        );
    }

    @Test
    @DisplayName("Should provide correct descriptions")
    void shouldProvideCorrectDescriptions() {
        
        assertThat(TableLocation.WINDOW.getDescription()).isEqualTo("Table fenêtre");
        assertThat(TableLocation.TERRACE.getDescription()).isEqualTo("Table exterieur terasse");
        assertThat(TableLocation.INDOOR.getDescription()).isEqualTo("Table en intérieur");
        assertThat(TableLocation.PRIVATE_ROOM.getDescription()).isEqualTo("Table isolée");
    }

    @Test
    @DisplayName("Should indicate if location is outdoor")
    void shouldIndicateIfLocationIsOutdoor() {
        
        assertThat(TableLocation.WINDOW.isOutdoor()).isFalse();
        assertThat(TableLocation.TERRACE.isOutdoor()).isTrue();
        assertThat(TableLocation.INDOOR.isOutdoor()).isFalse();
        assertThat(TableLocation.PRIVATE_ROOM.isOutdoor()).isFalse();
    }

    @Test
    @DisplayName("Should indicate if location requires reservation")
    void shouldIndicateIfLocationRequiresReservation() {
        
        assertThat(TableLocation.WINDOW.requiresReservation()).isFalse();
        assertThat(TableLocation.TERRACE.requiresReservation()).isFalse();
        assertThat(TableLocation.INDOOR.requiresReservation()).isFalse();
        assertThat(TableLocation.PRIVATE_ROOM.requiresReservation()).isTrue();
    }

    @Test
    @DisplayName("Should provide weather dependency information")
    void shouldProvideWeatherDependencyInformation() {
        
        assertThat(TableLocation.WINDOW.isWeatherDependent()).isFalse();
        assertThat(TableLocation.TERRACE.isWeatherDependent()).isTrue();
        assertThat(TableLocation.INDOOR.isWeatherDependent()).isFalse();
        assertThat(TableLocation.PRIVATE_ROOM.isWeatherDependent()).isFalse();
    }
}