package com.restaurant.service.restaurant.domain;

import com.restaurant.service.restaurant.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for Restaurant Domain Model
 * Testing interactions between domain entities
 */
@DisplayName("Restaurant Domain Integration Tests")
class RestaurantDomainIntegrationTest {

    private Restaurant restaurant;
    private RestaurantTable windowTable;
    private RestaurantTable terraceTable;

    @BeforeEach
    void setUp() {
        restaurant = new Restaurant(
                "Le Petit Bistro",
                "123 Rue de la Paix, Paris",
                "+33 1 42 86 87 88",
                "contact@petitbistro.fr",
                50
        );

        windowTable = new RestaurantTable(4, TableLocation.WINDOW);
        terraceTable = new RestaurantTable(6, TableLocation.TERRACE);

        restaurant.addTable(windowTable);
        restaurant.addTable(terraceTable);
    }

    @Test
    @DisplayName("Should create complete restaurant setup with tables and time slots")
    void shouldCreateCompleteRestaurantSetupWithTablesAndTimeSlots() {
        LocalDate date = LocalDate.now().plusDays(1); // Future date
        TimeSlot slot1 = new TimeSlot(date, LocalTime.of(19, 0), LocalTime.of(21, 0), 4);
        TimeSlot slot2 = new TimeSlot(date, LocalTime.of(21, 30), LocalTime.of(23, 0), 6);

        windowTable.addTimeSlot(slot1);
        terraceTable.addTimeSlot(slot2);

        assertThat(restaurant.getTables()).hasSize(2);
        assertThat(restaurant.getTotalAvailableSeats()).isEqualTo(10);

        assertThat(windowTable.getTimeSlots()).hasSize(1);
        assertThat(terraceTable.getTimeSlots()).hasSize(1);

        assertThat(slot1.getTable()).isEqualTo(windowTable);
        assertThat(slot2.getTable()).isEqualTo(terraceTable);
    }

    @Test
    @DisplayName("Should handle reservation workflow from availability to completion")
    void shouldHandleReservationWorkflowFromAvailabilityToCompletion() {
        LocalDate date = LocalDate.now().plusDays(1); // Future date
        LocalTime startTime = LocalTime.of(19, 0);
        LocalTime endTime = LocalTime.of(21, 0);

        boolean isAvailable = windowTable.isAvailableAt(date, startTime, endTime);
        assertThat(isAvailable).isTrue();

        TimeSlot timeSlot = new TimeSlot(date, startTime, endTime, 4);
        windowTable.addTimeSlot(timeSlot);

        boolean isAvailableAfterReservation = windowTable.isAvailableAt(date, startTime, endTime);
        assertThat(isAvailableAfterReservation).isFalse(); // AVAILABLE slots block availability

        timeSlot.confirm();

        boolean isAvailableAfterConfirmation = windowTable.isAvailableAt(date, startTime, endTime);
        assertThat(isAvailableAfterConfirmation).isFalse(); // CONFIRMED slots block availability

        timeSlot.complete();

        assertThat(windowTable.getTimeSlots()).hasSize(1);
        assertThat(timeSlot.getStatus()).isEqualTo(TimeSlotStatus.COMPLETED);
        assertThat(timeSlot.getTable()).isEqualTo(windowTable);

        boolean isAvailableAfterCompletion = windowTable.isAvailableAt(date, startTime, endTime);
        assertThat(isAvailableAfterCompletion).isTrue(); // COMPLETED slots don't block availability
    }

    @Test
    @DisplayName("Should handle cancellation workflow")
    void shouldHandleCancellationWorkflow() {
        LocalDate date = LocalDate.now().plusDays(1); // Future date
        TimeSlot timeSlot = new TimeSlot(date, LocalTime.of(19, 0), LocalTime.of(21, 0), 4);

        windowTable.addTimeSlot(timeSlot);
        timeSlot.confirm();

        timeSlot.cancel();

        assertThat(timeSlot.getStatus()).isEqualTo(TimeSlotStatus.CANCELLED);

        // Table should still have the time slot (for history)
        assertThat(windowTable.getTimeSlots()).contains(timeSlot);
    }

    @Test
    @DisplayName("Should prevent double booking on same table")
    void shouldPreventDoubleBookingOnSameTable() {
        LocalDate date = LocalDate.now().plusDays(1); // Future date
        TimeSlot slot1 = new TimeSlot(date, LocalTime.of(19, 0), LocalTime.of(21, 0), 4);
        TimeSlot slot2 = new TimeSlot(date, LocalTime.of(20, 0), LocalTime.of(22, 0), 2);

        windowTable.addTimeSlot(slot1);

        assertThatThrownBy(() -> windowTable.addTimeSlot(slot2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Time slot overlaps with existing reservation");
    }

    @Test
    @DisplayName("Should allow booking different tables at overlapping times")
    void shouldAllowBookingDifferentTablesAtOverlappingTimes() {
        LocalDate date = LocalDate.now().plusDays(1); // Future date
        TimeSlot windowSlot = new TimeSlot(date, LocalTime.of(19, 0), LocalTime.of(21, 0), 4);
        TimeSlot terraceSlot = new TimeSlot(date, LocalTime.of(19, 30), LocalTime.of(21, 30), 6);

        windowTable.addTimeSlot(windowSlot);
        terraceTable.addTimeSlot(terraceSlot);

        assertThat(windowTable.getTimeSlots()).hasSize(1);
        assertThat(terraceTable.getTimeSlots()).hasSize(1);
        assertThat(windowSlot.overlapsWith(terraceSlot)).isTrue(); // They overlap in time
        // But it's okay because they're on different tables
    }

    @Test
    @DisplayName("Should enforce table capacity limits")
    void shouldEnforceTableCapacityLimits() {
        LocalDate date = LocalDate.now().plusDays(1); // Future date

        assertThatCode(() -> {
            TimeSlot validSlot = new TimeSlot(date, LocalTime.of(19, 0), LocalTime.of(21, 0), 4);
            windowTable.addTimeSlot(validSlot);
        }).doesNotThrowAnyException();

        RestaurantTable smallTable = new RestaurantTable(2, TableLocation.INDOOR);
        restaurant.addTable(smallTable);

        assertThatThrownBy(() -> {
            TimeSlot invalidSlot = new TimeSlot(date, LocalTime.of(21, 30), LocalTime.of(23, 0), 3); // More than 2 seats
            smallTable.addTimeSlot(invalidSlot);
        }).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Reserved seats cannot exceed table capacity");
    }

    @Test
    @DisplayName("Should handle restaurant deactivation affecting reservations")
    void shouldHandleRestaurantDeactivationAffectingReservations() {
        LocalDate date = LocalDate.now().plusDays(1); // Future date
        TimeSlot timeSlot = new TimeSlot(date, LocalTime.of(19, 0), LocalTime.of(21, 0), 4);
        windowTable.addTimeSlot(timeSlot);

        restaurant.deactivate();

        assertThat(restaurant.isActive()).isFalse();
        // Time slot should still exist but table availability should be affected
        assertThat(windowTable.isAvailable()).isTrue(); // Table availability is independent
        assertThat(timeSlot.getStatus()).isEqualTo(TimeSlotStatus.AVAILABLE);
    }

    @Test
    @DisplayName("Should calculate restaurant utilization correctly")
    void shouldCalculateRestaurantUtilizationCorrectly() {
        LocalDate date = LocalDate.now().plusDays(1); // Future date

        // Add time slots to both tables
        TimeSlot slot1 = new TimeSlot(date, LocalTime.of(19, 0), LocalTime.of(21, 0), 4); // Window table fully booked
        TimeSlot slot2 = new TimeSlot(date, LocalTime.of(19, 0), LocalTime.of(21, 0), 3); // Terrace table partially booked

        windowTable.addTimeSlot(slot1);
        terraceTable.addTimeSlot(slot2);

        int totalSeats = restaurant.getTotalAvailableSeats();
        int bookedSeats = slot1.getReservedSeats() + slot2.getReservedSeats();

        assertThat(totalSeats).isEqualTo(10); // 4 + 6 seats
        assertThat(bookedSeats).isEqualTo(7); // 4 + 3 reserved seats

        double utilizationRate = (double) bookedSeats / totalSeats;
        assertThat(utilizationRate).isEqualTo(0.7); // 70% utilization
    }
}