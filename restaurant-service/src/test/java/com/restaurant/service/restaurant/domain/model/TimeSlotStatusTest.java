package com.restaurant.service.restaurant.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for TimeSlotStatus enum
 * Testing enum values and business logic
 */
@DisplayName("TimeSlotStatus Enum Tests")
class TimeSlotStatusTest {

    @Test
    @DisplayName("Should have all expected time slot statuses")
    void shouldHaveAllExpectedTimeSlotStatuses() {
        TimeSlotStatus[] statuses = TimeSlotStatus.values();

        assertThat(statuses).hasSize(5);
        assertThat(statuses).contains(
            TimeSlotStatus.AVAILABLE,
            TimeSlotStatus.RESERVED,
            TimeSlotStatus.CONFIRMED,
            TimeSlotStatus.COMPLETED,
            TimeSlotStatus.CANCELLED
        );
    }

    @Test
    @DisplayName("Should provide correct descriptions")
    void shouldProvideCorrectDescriptions() {
        assertThat(TimeSlotStatus.AVAILABLE.getDescription()).isEqualTo("Réservation disponible");
        assertThat(TimeSlotStatus.CONFIRMED.getDescription()).isEqualTo("Réservation confirmée");
        assertThat(TimeSlotStatus.COMPLETED.getDescription()).isEqualTo("Service terminé");
        assertThat(TimeSlotStatus.CANCELLED.getDescription()).isEqualTo("Réservation annulée");
    }

    @Test
    @DisplayName("Should indicate if status is active")
    void shouldIndicateIfStatusIsActive() {
        assertThat(TimeSlotStatus.AVAILABLE.isActive()).isTrue();
        assertThat(TimeSlotStatus.CONFIRMED.isActive()).isTrue();
        assertThat(TimeSlotStatus.COMPLETED.isActive()).isFalse();
        assertThat(TimeSlotStatus.CANCELLED.isActive()).isFalse();
    }

    @Test
    @DisplayName("Should indicate if status allows cancellation")
    void shouldIndicateIfStatusAllowsCancellation() {
        assertThat(TimeSlotStatus.AVAILABLE.allowsCancellation()).isFalse();
        assertThat(TimeSlotStatus.CONFIRMED.allowsCancellation()).isTrue();
        assertThat(TimeSlotStatus.COMPLETED.allowsCancellation()).isFalse();
        assertThat(TimeSlotStatus.CANCELLED.allowsCancellation()).isFalse();
    }

    @Test
    @DisplayName("Should indicate if status allows confirmation")
    void shouldIndicateIfStatusAllowsConfirmation() {
        assertThat(TimeSlotStatus.AVAILABLE.allowsConfirmation()).isTrue();
        assertThat(TimeSlotStatus.CONFIRMED.allowsConfirmation()).isFalse();
        assertThat(TimeSlotStatus.COMPLETED.allowsConfirmation()).isFalse();
        assertThat(TimeSlotStatus.CANCELLED.allowsConfirmation()).isFalse();
    }

    @Test
    @DisplayName("Should indicate if status is final")
    void shouldIndicateIfStatusIsFinal() {
        assertThat(TimeSlotStatus.AVAILABLE.isFinal()).isFalse();
        assertThat(TimeSlotStatus.CONFIRMED.isFinal()).isFalse();
        assertThat(TimeSlotStatus.COMPLETED.isFinal()).isTrue();
        assertThat(TimeSlotStatus.CANCELLED.isFinal()).isTrue();
    }
}