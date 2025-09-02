package com.restaurant.service.restaurant.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for TimeSlot domain entity
 * Testing business rules and invariants following TDD approach
 */
@DisplayName("TimeSlot Domain Entity Tests")
class TimeSlotTest {

    @Nested
    @DisplayName("TimeSlot Creation")
    class TimeSlotCreation {

        @Test
        @DisplayName("Should create time slot with valid data")
        void shouldCreateTimeSlotWithValidData() {
            LocalDate date = LocalDate.now().plusDays(1); // Future date
            LocalTime startTime = LocalTime.of(19, 0);
            LocalTime endTime = LocalTime.of(21, 0);
            int reservedSeats = 4;

            TimeSlot timeSlot = new TimeSlot(date, startTime, endTime, reservedSeats);

            assertThat(timeSlot.getDate()).isEqualTo(date);
            assertThat(timeSlot.getStartTime()).isEqualTo(startTime);
            assertThat(timeSlot.getEndTime()).isEqualTo(endTime);
            assertThat(timeSlot.getReservedSeats()).isEqualTo(reservedSeats);
            assertThat(timeSlot.getStatus()).isEqualTo(TimeSlotStatus.AVAILABLE);
            assertThat(timeSlot.getTable()).isNull();
            assertThat(timeSlot.getId()).isNull(); // Not persisted yet
        }

        @Test
        @DisplayName("Should throw exception when date is null")
        void shouldThrowExceptionWhenDateIsNull() {
            LocalTime startTime = LocalTime.of(19, 0);
            LocalTime endTime = LocalTime.of(21, 0);
            int reservedSeats = 4;

            assertThatThrownBy(() -> new TimeSlot(null, startTime, endTime, reservedSeats))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Date cannot be null");
        }

        @Test
        @DisplayName("Should throw exception when start time is null")
        void shouldThrowExceptionWhenStartTimeIsNull() {
            LocalDate date = LocalDate.now().plusDays(1);
            LocalTime endTime = LocalTime.of(21, 0);
            int reservedSeats = 4;

            assertThatThrownBy(() -> new TimeSlot(date, null, endTime, reservedSeats))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Start time cannot be null");
        }

        @Test
        @DisplayName("Should throw exception when end time is null")
        void shouldThrowExceptionWhenEndTimeIsNull() {
            LocalDate date = LocalDate.now().plusDays(1);
            LocalTime startTime = LocalTime.of(19, 0);
            int reservedSeats = 4;

            assertThatThrownBy(() -> new TimeSlot(date, startTime, null, reservedSeats))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("End time cannot be null");
        }

        @Test
        @DisplayName("Should throw exception when end time is before start time")
        void shouldThrowExceptionWhenEndTimeIsBeforeStartTime() {
            LocalDate date = LocalDate.now().plusDays(1);
            LocalTime startTime = LocalTime.of(21, 0);
            LocalTime endTime = LocalTime.of(19, 0);
            int reservedSeats = 4;

            assertThatThrownBy(() -> new TimeSlot(date, startTime, endTime, reservedSeats))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("End time must be after start time");
        }

        @Test
        @DisplayName("Should throw exception when end time equals start time")
        void shouldThrowExceptionWhenEndTimeEqualsStartTime() {
            LocalDate date = LocalDate.now().plusDays(1);
            LocalTime startTime = LocalTime.of(19, 0);
            LocalTime endTime = LocalTime.of(19, 0); // Same as start time
            int reservedSeats = 4;

            assertThatThrownBy(() -> new TimeSlot(date, startTime, endTime, reservedSeats))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("End time must be after start time");
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, -5})
        @DisplayName("Should throw exception when reserved seats is zero or negative")
        void shouldThrowExceptionWhenReservedSeatsIsInvalid(int invalidSeats) {
            LocalDate date = LocalDate.now().plusDays(1);
            LocalTime startTime = LocalTime.of(19, 0);
            LocalTime endTime = LocalTime.of(21, 0);

            assertThatThrownBy(() -> new TimeSlot(date, startTime, endTime, invalidSeats))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Reserved seats must be positive");
        }

        @Test
        @DisplayName("Should throw exception when date is in the past")
        void shouldThrowExceptionWhenDateIsInPast() {
            LocalDate pastDate = LocalDate.of(2020, 1, 1);
            LocalTime startTime = LocalTime.of(19, 0);
            LocalTime endTime = LocalTime.of(21, 0);
            int reservedSeats = 4;

            assertThatThrownBy(() -> new TimeSlot(pastDate, startTime, endTime, reservedSeats))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot create time slot for past dates");
        }
    }

    @Nested
    @DisplayName("TimeSlot Business Operations")
    class TimeSlotBusinessOperations {

        @Test
        @DisplayName("Should confirm time slot")
        void shouldConfirmTimeSlot() {
            TimeSlot timeSlot = createValidTimeSlot();

            timeSlot.confirm();

            assertThat(timeSlot.getStatus()).isEqualTo(TimeSlotStatus.CONFIRMED);
        }

        @Test
        @DisplayName("Should cancel time slot")
        void shouldCancelTimeSlot() {
            TimeSlot timeSlot = createValidTimeSlot();
            timeSlot.confirm();

            timeSlot.cancel();

            assertThat(timeSlot.getStatus()).isEqualTo(TimeSlotStatus.CANCELLED);
        }

        @Test
        @DisplayName("Should complete time slot")
        void shouldCompleteTimeSlot() {
            TimeSlot timeSlot = createValidTimeSlot();
            timeSlot.confirm();

            timeSlot.complete();

            assertThat(timeSlot.getStatus()).isEqualTo(TimeSlotStatus.COMPLETED);
        }

        @Test
        @DisplayName("Should not confirm already confirmed time slot")
        void shouldNotConfirmAlreadyConfirmedTimeSlot() {
            TimeSlot timeSlot = createValidTimeSlot();
            timeSlot.confirm();

            assertThatThrownBy(timeSlot::confirm)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot confirm time slot that is not available");
        }

        @Test
        @DisplayName("Should not cancel available time slot")
        void shouldNotCancelAvailableTimeSlot() {
            TimeSlot timeSlot = createValidTimeSlot();

            assertThatThrownBy(timeSlot::cancel)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot cancel time slot that is not confirmed");
        }

        @Test
        @DisplayName("Should check if time slot overlaps with another")
        void shouldCheckIfTimeSlotOverlapsWithAnother() {
            LocalDate date = LocalDate.now().plusDays(1);
            TimeSlot timeSlot1 = new TimeSlot(date, LocalTime.of(19, 0), LocalTime.of(21, 0), 4);
            TimeSlot timeSlot2 = new TimeSlot(date, LocalTime.of(20, 0), LocalTime.of(22, 0), 2);
            TimeSlot timeSlot3 = new TimeSlot(date, LocalTime.of(21, 30), LocalTime.of(23, 0), 2);

            assertThat(timeSlot1.overlapsWith(timeSlot2)).isTrue();
            assertThat(timeSlot1.overlapsWith(timeSlot3)).isFalse();
        }

        @Test
        @DisplayName("Should check if time slot overlaps with time range")
        void shouldCheckIfTimeSlotOverlapsWithTimeRange() {
            LocalDate date = LocalDate.now().plusDays(1);
            TimeSlot timeSlot = new TimeSlot(date, LocalTime.of(19, 0), LocalTime.of(21, 0), 4);

            assertThat(timeSlot.overlapsWith(date, LocalTime.of(20, 0), LocalTime.of(22, 0))).isTrue();
            assertThat(timeSlot.overlapsWith(date, LocalTime.of(21, 30), LocalTime.of(23, 0))).isFalse();
            assertThat(timeSlot.overlapsWith(LocalDate.of(2024, 12, 26), LocalTime.of(20, 0), LocalTime.of(22, 0))).isFalse();
        }

        @Test
        @DisplayName("Should calculate duration in minutes")
        void shouldCalculateDurationInMinutes() {
            LocalDate date = LocalDate.now().plusDays(1);
            TimeSlot timeSlot = new TimeSlot(date, LocalTime.of(19, 0), LocalTime.of(21, 30), 4);

            long duration = timeSlot.getDurationInMinutes();

            assertThat(duration).isEqualTo(150); // 2.5 hours = 150 minutes
        }

        @Test
        @DisplayName("Should validate minimum duration")
        void shouldValidateMinimumDuration() {
            LocalDate date = LocalDate.now().plusDays(1);
            LocalTime startTime = LocalTime.of(19, 0);
            LocalTime endTime = LocalTime.of(19, 15); // Only 15 minutes
            int reservedSeats = 4;

            assertThatThrownBy(() -> new TimeSlot(date, startTime, endTime, reservedSeats))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Time slot duration must be at least 30 minutes");
        }
    }

    @Nested
    @DisplayName("TimeSlot Status Transitions")
    class TimeSlotStatusTransitions {

        @Test
        @DisplayName("Should allow valid status transitions")
        void shouldAllowValidStatusTransitions() {
            TimeSlot timeSlot = createValidTimeSlot();

            assertThatCode(timeSlot::confirm).doesNotThrowAnyException();
            assertThat(timeSlot.getStatus()).isEqualTo(TimeSlotStatus.CONFIRMED);

            assertThatCode(timeSlot::complete).doesNotThrowAnyException();
            assertThat(timeSlot.getStatus()).isEqualTo(TimeSlotStatus.COMPLETED);
        }

        @Test
        @DisplayName("Should allow cancellation from confirmed status")
        void shouldAllowCancellationFromConfirmedStatus() {
            TimeSlot timeSlot = createValidTimeSlot();
            timeSlot.confirm();

            assertThatCode(timeSlot::cancel).doesNotThrowAnyException();
            assertThat(timeSlot.getStatus()).isEqualTo(TimeSlotStatus.CANCELLED);
        }

        @Test
        @DisplayName("Should not allow operations on completed time slot")
        void shouldNotAllowOperationsOnCompletedTimeSlot() {
            TimeSlot timeSlot = createValidTimeSlot();
            timeSlot.confirm();
            timeSlot.complete();

            assertThatThrownBy(timeSlot::cancel)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot cancel completed time slot");
        }

        @Test
        @DisplayName("Should not allow operations on cancelled time slot")
        void shouldNotAllowOperationsOnCancelledTimeSlot() {
            TimeSlot timeSlot = createValidTimeSlot();
            timeSlot.confirm();
            timeSlot.cancel();

            assertThatThrownBy(timeSlot::complete)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot complete cancelled time slot");
        }
    }

    @Nested
    @DisplayName("TimeSlot Business Rules")
    class TimeSlotBusinessRules {

        @Test
        @DisplayName("Should enforce restaurant opening hours")
        void shouldEnforceRestaurantOpeningHours() {
            LocalDate date = LocalDate.now().plusDays(1);
            int reservedSeats = 4;

            // Create a restaurant with specific opening hours
            Restaurant restaurant = new Restaurant(
                    "Test Restaurant", "123 Test St", "+1234567890",
                    "test@test.com", 50,
                    LocalTime.of(11, 0), LocalTime.of(23, 0)
            );
            RestaurantTable table = new RestaurantTable(4, TableLocation.WINDOW);
            restaurant.addTable(table);

            LocalTime tooEarlyStart = LocalTime.of(8, 0); // Before opening at 11:00
            LocalTime validEnd = LocalTime.of(10, 0); // Valid duration (2 hours)
            TimeSlot earlyTimeSlot = new TimeSlot(date, tooEarlyStart, validEnd, reservedSeats);

            assertThatThrownBy(() -> table.addTimeSlot(earlyTimeSlot))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Time slot must be within restaurant opening hours");

            LocalTime lateStart = LocalTime.of(22, 0);
            LocalTime tooLateEnd = LocalTime.of(23, 30); 
            TimeSlot lateTimeSlot = new TimeSlot(date, lateStart, tooLateEnd, reservedSeats);

            assertThatThrownBy(() -> table.addTimeSlot(lateTimeSlot))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Time slot must be within restaurant opening hours");
        }

        @Test
        @DisplayName("Should enforce maximum duration")
        void shouldEnforceMaximumDuration() {
            LocalDate date = LocalDate.now().plusDays(1);
            LocalTime startTime = LocalTime.of(19, 0);
            LocalTime endTime = LocalTime.of(23, 30); // 4.5 hours - too long
            int reservedSeats = 4;

            assertThatThrownBy(() -> new TimeSlot(date, startTime, endTime, reservedSeats))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Time slot duration cannot exceed 4 hours");
        }
    }

    @Nested
    @DisplayName("TimeSlot Equality and Hash Code")
    class TimeSlotEqualityAndHashCode {

        @Test
        @DisplayName("Should be equal when same id")
        void shouldBeEqualWhenSameId() {
            TimeSlot timeSlot1 = createValidTimeSlot();
            TimeSlot timeSlot2 = createValidTimeSlot();

            timeSlot1.setId(1L);
            timeSlot2.setId(1L);

            assertThat(timeSlot1).isEqualTo(timeSlot2);
            assertThat(timeSlot1.hashCode()).isEqualTo(timeSlot2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when different ids")
        void shouldNotBeEqualWhenDifferentIds() {
            TimeSlot timeSlot1 = createValidTimeSlot();
            TimeSlot timeSlot2 = createValidTimeSlot();

            timeSlot1.setId(1L);
            timeSlot2.setId(2L);

            assertThat(timeSlot1).isNotEqualTo(timeSlot2);
        }

        @Test
        @DisplayName("Should be equal when same business key")
        void shouldBeEqualWhenSameBusinessKey() {
            LocalDate date = LocalDate.now().plusDays(1);
            LocalTime startTime = LocalTime.of(19, 0);
            LocalTime endTime = LocalTime.of(21, 0);
            int reservedSeats = 4;

            TimeSlot timeSlot1 = new TimeSlot(date, startTime, endTime, reservedSeats);
            TimeSlot timeSlot2 = new TimeSlot(date, startTime, endTime, reservedSeats);

            assertThat(timeSlot1).isEqualTo(timeSlot2);
            assertThat(timeSlot1.hashCode()).isEqualTo(timeSlot2.hashCode());
        }
    }

    // Helper method
    private TimeSlot createValidTimeSlot() {
        LocalDate date = LocalDate.now().plusDays(1);
        LocalTime startTime = LocalTime.of(19, 0);
        LocalTime endTime = LocalTime.of(21, 0);
        return new TimeSlot(date, startTime, endTime, 4);
    }
}